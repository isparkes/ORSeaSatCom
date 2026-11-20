#!/usr/bin/env bash
#
# Bring up the MySQL server this application rates against, and load its two
# schemas: orseasat113 (the rating reference data) and jbseasat113 (the
# jBilling customer database).
#
#   ./db.sh            start MySQL if needed, then load the schemas (= start)
#   ./db.sh start      same
#   ./db.sh load       (re)load both schemas from the dumps in db-backups/
#   ./db.sh status     say whether the database is up and loaded
#   ./db.sh shell      open a mysql prompt on the rating database
#   ./db.sh stop       stop the database
#   ./db.sh reset      throw the database away and start over
#
# By default MySQL runs in a Docker container on port 3308, so there is
# nothing to install and it does not collide with a MySQL of your own, or
# with the other OpenRate examples. To use a MySQL you already run instead:
#
#   export OPENRATE_DB_MODE=local
#   export OPENRATE_DB_USER=root OPENRATE_DB_PASS=yourpassword
#
# The connection details are hard coded in SeaSatCom.properties.xml, so a
# server on another host or port needs that file changed to match.

set -euo pipefail
cd "$(dirname "$0")"
. ./common.sh

RATE_DUMP_ZIP=db-backups/OR-DB-Backup-2009-12-30.zip
JB_DUMP_ZIP=db-backups/JB-DB-Backup-2009-12-30.zip
PROCEDURES=ConfigData/SeaSatCom/SP.sql

# The dumps are zipped because the jBilling one is 69MB unpacked. Unpack
# them next to the archives, once. Only the path goes to stdout, because the
# caller captures it.
unpack_dump() {
  local zip="$1" sql="${1%.zip}.sql"
  [ -f "$zip" ] || fail "Missing ${zip}"
  if [ ! -s "$sql" ]; then
    say "    Unpacking $(basename "$zip")." >&2
    unzip -o -q "$zip" -d "$(dirname "$zip")"
  fi
  printf '%s' "$sql"
}

db_start() {
  if [ "$DB_MODE" = local ]; then
    db_answering || fail \
      "No MySQL answering on ${DB_HOST}:${DB_PORT} as user '${DB_USER}'.
       Start your own server, or let the scripts run one:
           export OPENRATE_DB_MODE=docker"
    say "    MySQL is answering on ${DB_HOST}:${DB_PORT}."
    return
  fi

  if db_container_running; then
    say "    Container '${DB_CONTAINER}' is already running."
  elif docker inspect "$DB_CONTAINER" >/dev/null 2>&1; then
    say "    Starting the existing container '${DB_CONTAINER}'."
    docker start "$DB_CONTAINER" >/dev/null
  else
    if port_open "$DB_HOST" "$DB_PORT"; then
      fail "Something is already listening on ${DB_HOST}:${DB_PORT}.
       That is probably a MySQL of your own. Either stop it, or use it:
           export OPENRATE_DB_MODE=local"
    fi
    say "    Creating container '${DB_CONTAINER}' from ${DB_IMAGE}."
    docker run --name "$DB_CONTAINER" \
      -e MYSQL_ROOT_PASSWORD="$DB_PASS" \
      -p "${DB_PORT}:3306" \
      -d "$DB_IMAGE" >/dev/null
  fi

  # The image initialises itself on first run, which takes a while and
  # involves a restart, so wait for the server to answer and stay answering.
  printf '    Waiting for MySQL to accept connections'
  local i settled=0
  for i in $(seq 1 90); do
    if port_open "$DB_HOST" "$DB_PORT" && db_answering; then
      settled=$((settled + 1))
      if [ "$settled" -ge 3 ]; then
        printf ' ok\n'
        return
      fi
    else
      settled=0
    fi
    db_container_running || { printf '\n'; docker logs --tail 20 "$DB_CONTAINER"; fail "The container stopped during startup."; }
    printf '.'
    sleep 1
  done
  printf '\n'
  fail "MySQL did not come up within 90s. Try:  docker logs ${DB_CONTAINER}"
}

db_load() {
  local rate_sql jb_sql
  rate_sql=$(unpack_dump "$RATE_DUMP_ZIP")
  jb_sql=$(unpack_dump "$JB_DUMP_ZIP")
  [ -f "$PROCEDURES" ] || fail "Missing ${PROCEDURES}"

  say "    Creating ${RATE_DB} and ${JB_DB}, and their users."
  db_mysql <<SQL
CREATE DATABASE IF NOT EXISTS ${RATE_DB};
CREATE DATABASE IF NOT EXISTS ${JB_DB};
CREATE USER IF NOT EXISTS 'openrate'@'%' IDENTIFIED BY 'openrate';
ALTER USER 'openrate'@'%' IDENTIFIED BY 'openrate';
CREATE USER IF NOT EXISTS 'jbilling'@'%' IDENTIFIED BY 'jbilling';
ALTER USER 'jbilling'@'%' IDENTIFIED BY 'jbilling';
GRANT ALL PRIVILEGES ON ${RATE_DB}.* TO 'openrate'@'%';
GRANT ALL PRIVILEGES ON ${JB_DB}.* TO 'jbilling'@'%';
GRANT EXECUTE ON *.* TO 'openrate'@'%';
FLUSH PRIVILEGES;
SQL

  say "    Loading the rating reference data into ${RATE_DB}."
  db_mysql "$RATE_DB" < "$rate_sql"
  db_mysql "$RATE_DB" < "$PROCEDURES"

  say "    Loading the jBilling customer database into ${JB_DB} (69MB, takes a minute)."
  db_mysql "$JB_DB" < "$jb_sql"

  say "    Loaded: $(db_scalar "$RATE_DB" 'select count(*) from PRICE_MODEL;') price model rows, \
$(db_scalar "$RATE_DB" 'select count(*) from GSM_DESTINATION_MAP;') GSM destinations, \
$(db_scalar "$JB_DB" 'select count(*) from purchase_order;') jBilling orders."
}

case "${1:-start}" in
  start)
    check_db_tooling
    step "Starting MySQL"
    db_start
    if db_loaded; then
      say "    ${RATE_DB} and ${JB_DB} are already loaded - nothing to do."
    else
      step "Loading ${RATE_DB} and ${JB_DB}"
      db_load
    fi
    say ""
    say "Database ready. Next:  ./build.sh  then  ./run.sh"
    ;;

  load)
    check_db_tooling
    db_answering || fail "No MySQL answering on ${DB_HOST}:${DB_PORT}. Run ./db.sh start first."
    step "Loading ${RATE_DB} and ${JB_DB}"
    db_load
    ;;

  status)
    check_db_tooling
    if [ "$DB_MODE" = docker ] && ! db_container_running; then
      say "Container '${DB_CONTAINER}': not running"
    elif ! db_answering; then
      say "MySQL on ${DB_HOST}:${DB_PORT}: not answering"
    elif db_loaded; then
      say "MySQL on ${DB_HOST}:${DB_PORT}: up, ${RATE_DB} and ${JB_DB} loaded"
      printf '%s\n' "
select 'price models'     as REFERENCE_DATA, count(*) as ROWS_ from PRICE_MODEL
union all select 'GSM destinations', count(*) from GSM_DESTINATION_MAP
union all select 'GSM prices',       count(*) from GSM_PRICE_MAP
union all select 'satellite destinations', count(*) from SAT_DESTINATION_MAP
union all select 'satellite prices', count(*) from SAT_PRICE_MAP
union all select 'origins',          count(*) from ORIGIN_MAP
union all select 'discounts',        count(*) from DISCOUNT_MAP
union all select 'friends and family', count(*) from FRIENDS_FAMILY;
" | db_mysql --table "$RATE_DB"
    else
      say "MySQL on ${DB_HOST}:${DB_PORT}: up, but the schemas are not loaded. Run ./db.sh load"
    fi
    ;;

  shell)
    check_db_tooling
    if [ "$DB_MODE" = docker ]; then
      exec docker exec -it "$DB_CONTAINER" \
        mysql --user="$DB_USER" --password="$DB_PASS" "$RATE_DB"
    else
      exec mysql --host="$DB_HOST" --port="$DB_PORT" \
                 --user="$DB_USER" --password="$DB_PASS" "$RATE_DB"
    fi
    ;;

  stop)
    check_db_tooling
    if [ "$DB_MODE" = local ]; then
      say "OPENRATE_DB_MODE is 'local' - this script does not stop your own server."
      exit 0
    fi
    if db_container_running; then
      step "Stopping '${DB_CONTAINER}'"
      docker stop "$DB_CONTAINER" >/dev/null
      say "Stopped. The data is still there - ./db.sh start brings it back."
    else
      say "Container '${DB_CONTAINER}' is not running."
    fi
    ;;

  reset)
    check_db_tooling
    if [ "$DB_MODE" = local ]; then
      step "Dropping and reloading ${RATE_DB} and ${JB_DB}"
      printf 'DROP DATABASE IF EXISTS %s;\nDROP DATABASE IF EXISTS %s;\n' \
        "$RATE_DB" "$JB_DB" | db_mysql
      db_load
    else
      step "Removing '${DB_CONTAINER}' and starting over"
      docker rm -f "$DB_CONTAINER" >/dev/null 2>&1 || true
      db_start
      db_load
    fi
    say ""
    say "Database reset."
    ;;

  *)
    fail "Usage: $0 [start|load|status|shell|stop|reset]"
    ;;
esac
