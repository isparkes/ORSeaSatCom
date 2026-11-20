ORSeaSatCom
===========

Retail rating for a maritime communications provider, built on
[OpenRate](https://github.com/isparkes/OpenRate). It rates two feeds of call
records — GSM traffic from a mobile operator, and satellite traffic from
Inmarsat terminals — against the tariffs each customer has bought, and writes
the result in the format [jBilling](https://www.jbilling.com) loads.

This is a decommissioned live implementation for SeaSatCom, which sold
connectivity to ships and fell out of use when its service provider started
offering retail rating itself. It ships with the operator's real reference
data, its real jBilling customer database, and three months of its real
traffic.

What makes it different from the other examples: the customer data is not in
the rating database. It comes out of jBilling, and what a record costs depends
on which products that customer had bought on the day of the call.

Requires OpenRate 1.5.2.6 or later, JDK 17, and Docker (for the database, see
below).

Quick start
-----------

```bash
./demo.sh
```

That brings up MySQL, loads both databases, builds the application, starts the
framework, rates a month of GSM traffic and a satellite file, shows what came
out, and shuts down again. The first run takes a few minutes, mostly loading
the 69MB jBilling dump.

If OpenRate itself is not installed yet, `demo.sh` stops and tells you how.
OpenRate is not published to Maven Central, so the framework has to be built
locally first — once, before any application will resolve it:

```bash
git clone https://github.com/isparkes/OpenRate.git
cd OpenRate
./bootstrap.sh
```

What comes out
--------------

```
                                         IN    RATED SUSPENSE
GSM   LST_01EEN939_0...               19907    17615     2303
SAT   SAT_9090791_cdr                   255      180       74

Rated value   GSM 44580.35 EUR
Balance movements written  5563
```

Rated records go to `*.Gout` (GSM) and `*.Sout` (satellite) next to the input,
in jBilling's load format. The prepaid counters each call touched come out
separately, as balance movements under `Data/SeaSatCom/Balance`.

| File | Contents |
|---|---|
| `*.done` | The input file, renamed once it has been consumed |
| `*.Gout` / `*.Sout` | Rated records, ready for jBilling |
| `*.jerr` | Records whose customer is not in the jBilling database |
| `*.oerr` | Records that could not be rated for some other reason |
| `*.recycle` | Records held back for a configuration problem |
| `*.rej` | Records rejected outright |
| `*.dump` | The full processing trail, one block per record |
| `Balance/BAL_*.out` | Balance movements for the prepaid counters |

What does not rate, and why
---------------------------

Three kinds of record end up in suspense. All three are gaps in the archived
data rather than faults in the pipeline, and they are worth understanding
because they are what a rating system's suspense handling is for.

**`ERR_JB_CUST_NOT_FOUND` (2235 GSM records).** The MSISDN is not in the
jBilling extract. The customer database was dumped in December 2009 and the
traffic is from August to October, so subscribers who had left by then are no
longer in it.

**`ERR_PRICE_MODEL_NOT_FOUND` (68 GSM records).** The combination of origin
zone, destination zone and tariff has no row in `GSM_PRICE_MAP`.

**`ERR_TIME_NOT_FOUND` (74 satellite records).** These are the satellite
broadband records. The tariff is spelled `FLEETBB` in the CDRs and in
`SAT_PRICE_MAP`, but `FLEET BB` in `TIME_MODEL_MAP`, so the time model lookup
that the price selection depends on finds nothing. Correcting the spelling:

```sql
update TIME_MODEL_MAP set PRODUCT_NAME_IN = 'FLEETBB'
 where PRODUCT_NAME_IN = 'FLEET BB';
```

does make them rate — and then prices them at roughly a thousand times what
the source system charged, because `PMV_13_0000` has a charge base of 0.001
against a quantity the input adapter converts to megabytes. In other words the
tariff was never actually run against this traffic. The dumps are shipped as
they were taken rather than patched, so this is left as it is.

The databases
-------------

Two schemas on one MySQL server:

| Schema | Holds |
|---|---|
| `orseasat113` | The rating reference data — normalisation, origins, destinations, time models, prices, discounts |
| `jbseasat113` | The jBilling customer database — accounts, orders and the products on them |

`db.sh` runs MySQL in a Docker container on port **3308** and loads both from
the dumps in `db-backups/`, so there is nothing to install beyond Docker. Port
3308 rather than 3306 so it does not collide with a MySQL of your own, or with
the other OpenRate examples.

| Command | Does |
|---|---|
| `./db.sh` | Start MySQL and load both schemas |
| `./db.sh status` | Say whether it is up, and show the reference data |
| `./db.sh shell` | Open a mysql prompt on the rating database |
| `./db.sh stop` | Stop the database, keeping its data |
| `./db.sh reset` | Throw it away and start over |

To use a MySQL you already run instead:

```bash
export OPENRATE_DB_MODE=local
export OPENRATE_DB_USER=root OPENRATE_DB_PASS=yourpassword
./db.sh load
```

The connection details are in `src/main/resources/SeaSatCom.properties.xml`,
so a server on another host or port needs that file changed to match. The
application connects as `openrate`/`openrate` and `jbilling`/`jbilling`, which
`db.sh load` creates.

Running it yourself
-------------------

`demo.sh` just chains the other scripts together. To drive it by hand:

| Script | Does |
|---|---|
| `./build.sh` | Build the application |
| `./run.sh` | Start the framework in the foreground; runs until stopped |
| `./feed.sh` | Rate a month of GSM traffic and a satellite file |
| `./feed.sh gsm 200908` | A different month — 200908, 200909 or 200910 |
| `./feed.sh sat` | Just a satellite file |
| `./feed.sh list` | Show the samples that are shipped |
| `./stop.sh` | Shut it down cleanly |

OpenRate runs here as a **daemon**: it starts, watches `Data/SeaSatCom` for
`LST_*.csv` and `SAT_*.csv`, and keeps running until told to stop. So `run.sh`
occupies a terminal, and you drive it from a second one:

```bash
./db.sh                  # once
./run.sh                 # terminal 1 - wait for "Running..."
./feed.sh                # terminal 2
./stop.sh                # terminal 2
```

The samples are shipped zipped, because the GSM months are about 6MB each
unpacked. Unzipping one into `Data/SeaSatCom` is what triggers rating.

How it works
------------

Two pipelines, both defined in
`src/main/resources/SeaSatCom.properties.xml`.

**GSMPipe** rates the mobile traffic:

| Module | Role |
|---|---|
| `GSMInputAdapter` | Reads the operator's CSV and maps it into a `SeaSatRecord` |
| `Normalisation` | Normalises the dialled number (regex match) |
| `OriginLookup` → `GSMDestinationLookup` | Zones the call by where it started and where it went |
| `JBCustomerTariffLookup` | Finds the jBilling account for the MSISDN, and the products valid on the day of the call. The base product is the tariff; the options modify it |
| `DiscountLookup` | Applies the discount the customer's options entitle them to |
| `FriendsFamily` → `PromoCalcPreRating` | Friends and family numbers, and whatever allowance the call should come out of |
| `GSMPriceModelLookup` | Picks the price model for the zone, tariff and price sheet |
| `RateOverride` → `RateLookup` | Rate plan overrides, then RUM based rating |
| `Rollup` | Rolls the charge packets up into one amount and rounds it |
| `OutputPreparation` | Decides which output stream the record belongs in |
| `JBillingOutput` / `SuspenseOutput` / `BalanceOutputAdapter` | Writes the rated record, the suspended ones, and the balance movements |

**SATPipe** does the same for satellite traffic, with `SATDestinationLookup`
and `SATPriceModelLookup` in place of the GSM ones, and a `TimeLookup` between
them: satellite tariffs are time split (peak and off peak), mobile ones are
not.

All the reference data is in `orseasat113`, loaded into in-memory caches at
startup and reloadable from the admin console while the framework runs. The
customer data is cached from jBilling the same way.

The admin console
-----------------

OpenRate exposes a control interface on port 8086:

```bash
telnet localhost 8086
```

`Help` lists the available commands. `stop.sh` uses this to issue:

```
Framework:shutdown=true
```

`CacheFactory:Reload=true` reloads the reference data and the customer cache
without restarting.

> The console is unauthenticated. It is fine on a development machine; do not
> expose the port on a shared or public network.

Building a distribution
-----------------------

```bash
mvn clean package
```

also produces `target/ORSeaSatCom-1.0.0-bin.zip`: a self-contained
distribution that runs anywhere with a JDK 17 and Docker, with no Maven and no
source checkout.

```
unzip ORSeaSatCom-1.0.0-bin.zip -d seasat
cd seasat
bin/db.sh && bin/run.sh
```

It carries its own `README.md`, the framework and dependency jars, the
configuration, both database dumps and all the sample traffic, and
`bin/db.sh`, `bin/run.sh`, `bin/feed.sh`, `bin/stop.sh`. Those distribution
scripts launch Java directly; the scripts in the project root drive Maven, so
each set only works in its own context. The distribution sources live in
`src/main/bin` and `src/main/dist`.

Documents
---------

`Documents/` has the original design material: the pipeline high level design,
the interface specification, and the field maps that the input adapters
implement.

Other examples: [OpenRateSimple](https://github.com/isparkes/OpenRateSimple)
(the simplest one, no database),
[OpenRateWeblinksInterconnect](https://github.com/isparkes/OpenRateWeblinksInterconnect)
(interconnect rating and billing),
[ORPixip](https://github.com/isparkes/ORPixip) (revenue assurance across five
data sources), [ORTyfon](https://github.com/isparkes/ORTyfon) (fixed line and
VoIP rating with fraud detection).

License
-------

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE).

Copyright 2006-2026 Ian Sparkes.
