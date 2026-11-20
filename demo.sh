#!/usr/bin/env bash
#
# Zero to running: bring up the database, build the application, start it,
# push a month of GSM traffic and a satellite file through it, show what came
# out, and shut down again.
#
# Everything this does by hand is in db.sh / build.sh / run.sh / feed.sh /
# stop.sh.

set -euo pipefail
cd "$(dirname "$0")"
. ./common.sh

RUN_LOG=$(mktemp -t orseasat)
FRAMEWORK_PID=""

GSM_SAMPLE=LST_01EEN939_01_EXTERNE_FADET_200909
SAT_SAMPLE=SAT_9090791_cdr

cleanup() {
  if [ -n "$FRAMEWORK_PID" ] && kill -0 "$FRAMEWORK_PID" 2>/dev/null; then
    say ""
    step "Shutting the framework down"
    eci 'Framework:shutdown=true' >/dev/null 2>&1 || true
    for _ in $(seq 1 20); do
      framework_running || break
      sleep 1
    done
    kill "$FRAMEWORK_PID" 2>/dev/null || true
    wait "$FRAMEWORK_PID" 2>/dev/null || true
    say "Stopped. The database is still running - ./db.sh stop shuts it down."
  fi
  rm -f "$RUN_LOG"
}
trap cleanup EXIT INT TERM

# Line count of a file, or 0 if it is not there.
lines() { [ -f "$1" ] && wc -l < "$1" | tr -d ' ' || printf '0'; }

# Sum of the rated amount column in a jBilling output file.
rated_total() {
  [ -f "$1" ] || { printf '0.00'; return; }
  awk -F';' '{total += $11} END {printf "%.2f", total}' "$1"
}

check_toolchain
check_openrate_installed
check_db_tooling

if framework_running; then
  fail "Something is already listening on ${ECI_HOST}:${ECI_PORT}.
       Run ./stop.sh first, then try again."
fi

# --------------------------------------------------------------- 1. database
step "1/6  Bringing up MySQL"
./db.sh start | sed 's/^/  /'

# ------------------------------------------------------------------ 2. build
step "2/6  Building against OpenRate $(openrate_version)"
mvn -B -q clean package
say "    Built."

# ------------------------------------------------------------------ 3. clean
step "3/6  Clearing output from any previous run"
clean_output
say "    ${DATA_DIR} holds only the sample archives again."

# ------------------------------------------------------------------ 4. start
step "4/6  Starting the framework"
mvn -B exec:java > "$RUN_LOG" 2>&1 &
FRAMEWORK_PID=$!

for _ in $(seq 1 180); do
  grep -q 'Running\.\.\.' "$RUN_LOG" 2>/dev/null && break
  kill -0 "$FRAMEWORK_PID" 2>/dev/null || { sed 's/^/    /' "$RUN_LOG" | tail -20; fail "The framework exited during startup."; }
  sleep 1
done
grep -q 'Running\.\.\.' "$RUN_LOG" || { sed 's/^/    /' "$RUN_LOG" | tail -20; fail "The framework did not reach 'Running...' in 180s."; }

sed -n '/OpenRate V/,/Running\.\.\./p' "$RUN_LOG" \
  | grep -E "OpenRate V|Copyright|Creating pipeline|Running" | sed 's/^/    /'

# ------------------------------------------------------------------- 5. feed
step "5/6  Feeding a month of GSM traffic and a satellite file"
./feed.sh both | sed -n 's/^    /  /p'

for _ in $(seq 1 180); do
  [ -f "${DATA_DIR}/${GSM_SAMPLE}.done" ] && [ -f "${DATA_DIR}/${SAT_SAMPLE}.done" ] && break
  sleep 2
done
for f in "${GSM_SAMPLE}" "${SAT_SAMPLE}"; do
  [ -f "${DATA_DIR}/${f}.done" ] || fail "${f} was not processed within 6 minutes. Framework log:
$(tail -20 "$RUN_LOG")"
done

# the output adapters flush on transaction close, give them a moment
sleep 5
say "    Processed."

# ----------------------------------------------------------------- 6. result
step "6/6  What the pipelines did"
cat <<'EXPLAIN'

    Each record was matched to its jBilling customer and to the
    products that customer had bought, normalised, zoned by origin and
    destination, priced against the tariff those products imply, and
    written out in the format jBilling loads. The prepaid counters each
    call touched came out alongside, as balance movements.

EXPLAIN

printf '    %-34s %8s %8s %8s\n' "" "IN" "RATED" "SUSPENSE"
printf '    %-34s %8s %8s %8s\n' "GSM   ${GSM_SAMPLE:0:14}..." \
  "$(lines "${DATA_DIR}/${GSM_SAMPLE}.done")" \
  "$(lines "${DATA_DIR}/${GSM_SAMPLE}.Gout")" \
  "$(( $(lines "${DATA_DIR}/${GSM_SAMPLE}.jerr") + $(lines "${DATA_DIR}/${GSM_SAMPLE}.oerr") ))"
printf '    %-34s %8s %8s %8s\n' "SAT   ${SAT_SAMPLE}" \
  "$(lines "${DATA_DIR}/${SAT_SAMPLE}.done")" \
  "$(lines "${DATA_DIR}/${SAT_SAMPLE}.Sout")" \
  "$(( $(lines "${DATA_DIR}/${SAT_SAMPLE}.jerr") + $(lines "${DATA_DIR}/${SAT_SAMPLE}.oerr") ))"

cat <<EXPLAIN

    Rated value   GSM $(rated_total "${DATA_DIR}/${GSM_SAMPLE}.Gout") EUR
    Balance movements written  $(lines "${DATA_DIR}/Balance/BAL_${GSM_SAMPLE#LST_}.out")

    One rated record, trimmed to the fields the pipeline filled in -
    service, call type, price sheet, origin and destination zone,
    tariff, quantity and price:

EXPLAIN
head -1 "${DATA_DIR}/${GSM_SAMPLE}.Gout" 2>/dev/null | cut -d';' -f2-11 | sed 's/^/      /'

cat <<EXPLAIN

    The suspense files hold what could not be rated, with the reason in
    the second field:

EXPLAIN
cat "${DATA_DIR}/${GSM_SAMPLE}".?err "${DATA_DIR}/${SAT_SAMPLE}".?err 2>/dev/null \
  | cut -d';' -f2 | sort | uniq -c | sort -rn | sed 's/^/      /'

cat <<NEXT

    Those are gaps in the archived data rather than faults in the
    pipeline. The README says what each one is.

--------------------------------------------------------------
 Next steps
--------------------------------------------------------------
  ./run.sh              start it and leave it running
  ./feed.sh gsm 200908  feed a different month
  ./feed.sh list        show the samples that are shipped
  ./db.sh shell         poke around the rating tables
  ./stop.sh             shut the framework down
  ./db.sh stop          shut the database down

  telnet ${ECI_HOST} ${ECI_PORT}    admin console, 'Help' for commands

  Every record's full processing trail is in
  ${DATA_DIR}/*.dump - one block per record, one line
  per field, written by the Dump module.
--------------------------------------------------------------
NEXT
