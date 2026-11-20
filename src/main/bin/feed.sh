#!/usr/bin/env bash
#
# Give the running framework some call records to rate.
#
#   bin/feed.sh            one month of GSM records and one satellite file
#   bin/feed.sh gsm        just the GSM month (September 2009 by default)
#   bin/feed.sh gsm 200908 a different month - 200908, 200909 or 200910
#   bin/feed.sh sat        just a satellite file
#   bin/feed.sh list       show what samples are available
#
# The samples are the operator's real traffic, shipped zipped because the
# GSM months are about 6MB each unpacked. Unzipping one into
# Data/SeaSatCom is what triggers rating: the input adapters watch that
# directory for LST_*.csv and SAT_*.csv.

set -euo pipefail

. "$(cd "$(dirname "$0")" && pwd)/common.sh"
cd "$(openrate_home)"

GSM_DEFAULT=200909
SAT_DEFAULT=9090791

gsm_archive() { printf '%s/LST_01EEN939_01_EXTERNE_FADET_%s.zip' "$DATA_DIR" "$1"; }
sat_archive() { printf '%s/SAT_%s_cdr.zip' "$DATA_DIR" "$1"; }

unpack() {
  local archive="$1"
  [ -f "$archive" ] || fail "No such sample: ${archive}
       Available:
$(ls "$DATA_DIR"/*.zip 2>/dev/null | sed 's|^|         |' || echo '         (none)')"
  unzip -o -q "$archive" -d "$DATA_DIR"
  say "    $(basename "${archive%.zip}").csv  ($(wc -l < "${archive%.zip}.csv" | tr -d ' ') records)"
}

case "${1:-both}" in
  list)
    say "Samples in ${DATA_DIR}:"
    ls "$DATA_DIR"/*.zip | sed 's|^|  |'
    exit 0
    ;;
esac

ensure_data_dirs

framework_running || say \
  "Note: nothing is listening on ${ECI_HOST}:${ECI_PORT}, so the framework does
      not look like it is running. The files will be picked up whenever you
      start it with bin/run.sh
"

case "${1:-both}" in
  both)
    step "Unpacking one GSM month and one satellite file into ${DATA_DIR}"
    unpack "$(gsm_archive "$GSM_DEFAULT")"
    unpack "$(sat_archive "$SAT_DEFAULT")"
    ;;
  gsm)
    step "Unpacking the GSM records for ${2:-$GSM_DEFAULT} into ${DATA_DIR}"
    unpack "$(gsm_archive "${2:-$GSM_DEFAULT}")"
    ;;
  sat)
    step "Unpacking satellite file ${2:-$SAT_DEFAULT} into ${DATA_DIR}"
    unpack "$(sat_archive "${2:-$SAT_DEFAULT}")"
    ;;
  *)
    fail "Usage: $0 [both|gsm [month]|sat [id]|list]"
    ;;
esac

say ""
say "The pipelines rename each input to *.done and write the rated records"
say "as *.Gout (GSM) and *.Sout (satellite) in the same directory, with the"
say "balance movements under ${DATA_DIR}/Balance."
