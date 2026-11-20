ORSeaSatCom
===========

Retail rating for a maritime communications provider, built on
[OpenRate](https://github.com/isparkes/OpenRate). It rates two feeds of call
records — GSM traffic from a mobile operator, and satellite traffic from
Inmarsat terminals — against the tariffs each customer has bought in
jBilling, and writes the result in the format jBilling loads.

This is the packaged distribution: the application, the OpenRate framework,
every dependency, the configuration, both database dumps and three months of
the operator's real traffic. No Maven and no source checkout needed — a JDK
17 and Docker are enough.

Quick start
-----------

```bash
bin/db.sh        # start MySQL and load both databases
bin/run.sh       # start the framework - runs until stopped
```

`run.sh` occupies the terminal. From a second one, in the same directory:

```bash
bin/feed.sh      # rate a month of GSM traffic and a satellite file
bin/stop.sh      # shut down cleanly
```

The first `bin/db.sh` takes a few minutes, mostly loading the 69MB jBilling
dump.

What comes out
--------------

Rated records go to `*.Gout` (GSM) and `*.Sout` (satellite) in
`Data/SeaSatCom`, in jBilling's load format. The prepaid counters each call
touched come out separately as balance movements under
`Data/SeaSatCom/Balance`.

| File | Contents |
|---|---|
| `*.done` | The input file, renamed once it has been consumed |
| `*.Gout` / `*.Sout` | Rated records, ready for jBilling |
| `*.jerr` | Records whose customer is not in the jBilling database |
| `*.oerr` | Records that could not be rated for some other reason |
| `*.dump` | The full processing trail, one block per record |
| `Balance/BAL_*.out` | Balance movements for the prepaid counters |

A month of GSM traffic is about 19900 records, of which some 17600 rate. The
rest are subscribers who had already left by the time the customer database
was dumped, and a handful of zone and tariff combinations with no price. The
project README explains each one.

The databases
-------------

| Schema | Holds |
|---|---|
| `orseasat113` | The rating reference data |
| `jbseasat113` | The jBilling customer database |

`bin/db.sh` runs MySQL in a Docker container on port **3308** and loads both
from the dumps in `db/`.

| Command | Does |
|---|---|
| `bin/db.sh` | Start MySQL and load both schemas |
| `bin/db.sh status` | Say whether it is up, and show the reference data |
| `bin/db.sh shell` | Open a mysql prompt on the rating database |
| `bin/db.sh stop` | Stop the database, keeping its data |
| `bin/db.sh reset` | Throw it away and start over |

To use a MySQL you already run instead:

```bash
export OPENRATE_DB_MODE=local
export OPENRATE_DB_USER=root OPENRATE_DB_PASS=yourpassword
bin/db.sh load
```

The connection details are in `properties/SeaSatCom.properties.xml`, so a
server on another host or port needs that file changed to match.

Feeding it
----------

```bash
bin/feed.sh             # a month of GSM traffic and a satellite file
bin/feed.sh gsm 200908  # a different month - 200908, 200909 or 200910
bin/feed.sh sat         # just a satellite file
bin/feed.sh list        # what is shipped
```

The samples are zipped; unzipping one into `Data/SeaSatCom` is what triggers
rating, because the input adapters watch that directory for `LST_*.csv` and
`SAT_*.csv`.

Layout
------

| Directory | Contents |
|---|---|
| `bin/` | The scripts above |
| `properties/` | `SeaSatCom.properties.xml` and `logSeaSatCom.xml` |
| `dist/` | This application |
| `ORlib/` | The OpenRate framework |
| `lib/` | Third party dependencies |
| `db/` | The database dumps and stored procedures |
| `Data/SeaSatCom/` | Sample traffic, and where the output lands |
| `ConfigData/SeaSatCom/` | The balance handling configuration |
| `log/` | Framework, pipeline and error logs |

Environment
-----------

| Variable | Default | Does |
|---|---|---|
| `JAVA_HOME` | first `java` on the PATH | Which JDK to run (17 or later) |
| `JAVA_OPTS` | — | Extra JVM options, e.g. `-Xmx2g` |
| `OPENRATE_ECI_PORT` | `8086` | Admin console port |
| `OPENRATE_DB_MODE` | `docker` | `docker` or `local` |
| `OPENRATE_DB_PORT` | `3308` | MySQL port |
| `OPENRATE_DB_USER` | `root` | Administrative MySQL user |
| `OPENRATE_DB_PASS` | `cpr` | Its password |

The admin console
-----------------

```bash
telnet localhost 8086
```

`Help` lists the commands. `CacheFactory:Reload=true` reloads the reference
data and the customer cache without restarting.

> The console is unauthenticated. It is fine on a development machine; do not
> expose the port on a shared or public network.

License
-------

Licensed under the Apache License, Version 2.0. See `LICENSE`.

Copyright 2006-2026 Ian Sparkes.
