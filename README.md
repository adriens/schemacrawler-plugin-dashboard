[![Build Status](https://travis-ci.org/adriens/schemacrawler-csv-plugin.svg?branch=master)](https://travis-ci.org/adriens/schemacrawler-csv-plugin) [![Dependency Status](https://beta.gemnasium.com/badges/github.com/adriens/schemacrawler-plugin-dashboard.svg)](https://beta.gemnasium.com/projects/github.com/adriens/schemacrawler-plugin-dashboard) [![](https://jitpack.io/v/adriens/schemacrawler-plugin-dashboard.svg)](https://jitpack.io/#adriens/schemacrawler-plugin-dashboard)


# schemacrawler-csv-plugin

[Schemacrawler](http://sualeh.github.io/SchemaCrawler/) [additional command](http://sualeh.github.io/SchemaCrawler/how-to.html) (ie. plugin) that exports lint reports as csv : you run the command and get the lints as a simple csv file...that you can further parse or make stats from R, python or any other statistics tools.

# Install

Drop the jar in ```$SCHEMACRAWLER_HOME/lib``` and you're done.


# Usage

Assuming you have a [properly setup](http://sualeh.github.io/SchemaCrawler/how-to.html) schemacrawler environment within ```schemacrawler.config.properties```

```
schemacrawler-csv
```

or

```
schemacrawler -c=csv -filename=toto.csv
```
