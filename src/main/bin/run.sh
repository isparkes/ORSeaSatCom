#!/usr/bin/env bash
#
# Start ORSeaSatCom from an unpacked distribution.
#
#   bin/run.sh [properties-file]
#
# The properties file defaults to SeaSatCom.properties.xml and is resolved
# from the classpath, which includes the properties/ directory.
#
# Environment:
#   JAVA_HOME           JDK to use (must be 17 or later)
#   JAVA_OPTS           extra JVM options, e.g. -Xmx2g
#   OPENRATE_ECI_PORT   admin console port (default 8086)

set -euo pipefail

. "$(cd "$(dirname "$0")" && pwd)/common.sh"

HOME_DIR="$(openrate_home)"
cd "$HOME_DIR"

JAVA_BIN="$(resolve_java)"
PROPERTIES="${1:-SeaSatCom.properties.xml}"

[ -f "properties/${PROPERTIES}" ] || fail \
  "No properties/${PROPERTIES} in ${HOME_DIR}.
       Available:
$(ls properties/*.properties.xml 2>/dev/null | sed 's|^|         |' || echo '         (none)')"

check_database
ensure_data_dirs

if framework_running; then
  fail "Something is already listening on ${ECI_HOST}:${ECI_PORT}.
       OpenRate may already be running - try bin/stop.sh first."
fi

cat <<BANNER
--------------------------------------------------------------
 ORSeaSatCom
--------------------------------------------------------------
 Home           ${HOME_DIR}
 Configuration  properties/${PROPERTIES}
 Databases      ${RATE_DB} (rates) and ${JB_DB} (customers)
                on ${DB_HOST}:${DB_PORT}
 Java           $("$JAVA_BIN" -version 2>&1 | head -1)

 Two pipelines: GSMPipe rates the GSM records (LST_*.csv) and
 SATPipe the satellite ones (SAT_*.csv), both writing output in
 the format jBilling loads.

 Runs until stopped. Once you see "Running...", from another
 terminal in this directory:

     bin/feed.sh      rate a month of GSM and a satellite file
     bin/stop.sh      shut down cleanly

 Admin console:  telnet ${ECI_HOST} ${ECI_PORT}
--------------------------------------------------------------

BANNER

# Relative paths in the configuration resolve against the installation root,
# so this must run from there.
exec "$JAVA_BIN" ${JAVA_OPTS:-} -cp "$(openrate_classpath)" \
     OpenRate.OpenRate -p "$PROPERTIES"
