#!/bin/bash
if [ ! -e OR-DB-Backup-2009-12-30.sql ]; then
  unzip OR-DB-Backup-2009-12-30.zip
fi

if [ ! -e JB-DB-Backup-2009-12-30.sql ]; then
  unzip JB-DB-Backup-2009-12-30.zip
fi

# drop and recreate the databases
mysqladmin --user=root --password=cpr drop -f orseasat113
mysqladmin --user=root --password=cpr drop -f jbseasat113
mysqladmin --user=root --password=cpr create orseasat113
mysqladmin --user=root --password=cpr create jbseasat113

# load the OpenRate configuration
mysql --user=root --password=cpr orseasat113 < OR-DB-Backup-2009-12-30.sql

# load the JBilling configuration
mysql --user=root --password=cpr jbseasat113 < JB-DB-Backup-2009-12-30.sql

# load the stored procedure
mysql --user=root --password=cpr orseasat113 < ../ConfigData/SeaSatCom/SP.sql

