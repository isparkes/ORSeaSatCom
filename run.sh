#!/usr/bin/env bash
#
# Start the SeaSatCom rating framework in the foreground.
#
# It runs as a daemon: it starts, watches Data/SeaSatCom for input files, and
# keeps running until told to stop. Use ./feed.sh and ./stop.sh from another
# shell.

set -euo pipefail
cd "$(dirname "$0")"
. ./common.sh

check_toolchain
check_openrate_installed
check_database
ensure_data_dirs

if framework_running; then
  fail "Something is already listening on ${ECI_HOST}:${ECI_PORT}.
       The framework may already be running - try ./stop.sh first."
fi

cat <<BANNER

--------------------------------------------------------------
 Starting ORSeaSatCom
--------------------------------------------------------------
 Databases ${RATE_DB} (rates) and ${JB_DB} (customers)
           on ${DB_HOST}:${DB_PORT}
 Pipelines GSMPipe  rates the GSM call records  (LST_*.csv)
           SATPipe  rates the satellite records (SAT_*.csv)

 Both write records in the format jBilling loads, plus the
 balance movements for the prepaid counters.

 The framework runs until you stop it. Once you see "Running...",
 open a second terminal in this directory and:

     ./feed.sh      give it a month of GSM and satellite records
     ./stop.sh      shut it down cleanly

 Admin console:  telnet ${ECI_HOST} ${ECI_PORT}   ('Help' for commands)
--------------------------------------------------------------

BANNER

# Relative paths in SeaSatCom.properties.xml resolve against the project
# root, so this must run from here.
exec mvn -B exec:java
