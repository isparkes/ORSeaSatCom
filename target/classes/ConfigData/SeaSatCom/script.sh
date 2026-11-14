#!/bin/bash
FILE_PATH=/data/Data/ProjectWork/OpenRateProjects/SeaSatCom/Software/ORSeaSatCom/ConfigData/SeaSatCom/LST_*.csv

FIRST_FILE=0

for i in `ls $FILE_PATH`
do
  if [ $FIRST_FILE -ne "1" ]; then
    echo ":: clearing table ::"
    mysql --user=root seasat < clear.sql
    FIRST_FILE=1
  fi

  echo ":: loading file :$i:"
  sed "s|<FILENAME>|${i}|" load.sql.template > load.sql
  mysql --user=root seasat < load.sql

  LOAD_EXIT_CODE=$?
  if [ $LOAD_EXIT_CODE -ne "0" ]; then
    echo "exit code = $LOAD_EXIT_CODE"
  else
    #loaded OK, move the file
    mv $i $i.loaded
  fi
done
