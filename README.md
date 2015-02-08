ORSeaSatCom
===========

<br><br>You might find this file easier to read in a text editor!!!<br><br>

Dismissed SeaSatCom rating pipeline. We did this rating pipeline for SeaSatCom 
(www.seasatcom.com) but it fell out of use when the service provider started
to offer retail rating as part of their service.

We rate two different feeds of CDRs in this system in two different pipes:
 - GSM records
 - Satellite records

The records are written in a format which is suitable for loading into
jBilling.


Pre-requesites:
  MySQL Installed and running
  Dump files have been uncompressed
  Maven is installed


To set up this project:

Configuration Database:
=======================

The system uses two databases: 

 - A configuration database "orseasat113" which holds the static reference data 
for rating.
 - A jBilling database for holding the customer data "jbseasat113"

mysqladmin --user=root --password=cpr create orseasat113
mysqladmin --user=root --password=cpr create jbseasat113

mysql --user=root --password=<root_password>

mysql> create user 'openrate'@'localhost' identified by 'openrate';

mysql> grant all privileges on orseasat113.* to 'openrate'@'localhost';

mysql> grant execute on *.* to 'openrate'@'localhost';

mysql> create user 'jbilling'@'localhost' identified by 'jbilling';

mysql> grant all privileges on jbseasat113.* to 'jbilling'@'localhost';

mysql> exit

Then load the data

mysql --user=openrate --password=openrate orseasat113 < OR-DB-Backup-2009-12-30.sql

mysql --user=jbilling --password=jbilling jbseasat113 < JB-DB-Backup-2009-12-30.sql


Compile OpenRate Core:
======================

Download the OpenRate project from GitHub. Clean and build. Ensure that it is
loaded into your Maven repository.

cd <your-choice-of-directory>
git clone git@github.com:isparkes/OpenRate.git
mvn install


Compile SeaSat Project:
=======================

Download the Pixip project from GitHub (you already have if you are reading 
this).

cd <your-choice-of-directory>
git clone git@github.com:isparkes/ORSeaSatCom.git
mvn clean install


Running:
=======

The easiest way to run through Maven. From the command line, type

   mvn exec:java


Running for debug:
==================

You should be able to run the Pixip project from any IDE. We use Netbeans. Load
up the project in your IDE and you should be able to run, build and debug as
with any other Maven project.


Running in real life:
=====================

The startup script launches the OpenRate application. There is a startup
script (bin/startup.sh) and a shutdown script (bin/shutdown.sh) in the 
distribution directory.

