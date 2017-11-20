[![Build Status](https://travis-ci.org/adriens/schemacrawler-plugin-dashboard.svg?branch=master)](https://travis-ci.org/adriens/schemacrawler-plugin-dashboard)

[![Dependency Status](https://beta.gemnasium.com/badges/github.com/adriens/schemacrawler-plugin-dashboard.svg)](https://beta.gemnasium.com/projects/github.com/adriens/schemacrawler-plugin-dashboard) [![](https://jitpack.io/v/adriens/schemacrawler-plugin-dashboard.svg)](https://jitpack.io/#adriens/schemacrawler-plugin-dashboard)


# schemacrawler-plugin-dashboard 

[Schemacrawler](http://sualeh.github.io/SchemaCrawler/) [additional command](http://sualeh.github.io/SchemaCrawler/how-to.html) (ie. plugin) that
exports lint reports as a set of precompiled and aggregated csv, then provides a ready to use Html dashboard compiled from a R, thanks to a
[flexdashbaord](http://rmarkdown.rstudio.com/flexdashboard/).

Just run the ```dashboard``` command, provide an output directory and take a look the newly created index.html.

# Prerequisites

Proprer schemacrawler install
R runtime installed
the ```schemacrawler-plugin-dashboard ``` jar in schemacrawler ```lib``` dir.

# Install

Drop the jar in ```$SCHEMACRAWLER_HOME/lib``` and you're done.


# Usage

Assuming you have a [properly setup](http://sualeh.github.io/SchemaCrawler/how-to.html) schemacrawler environment within ```schemacrawler.config.properties```

```
schemacrawler -c=dashboard
```

or

```
schemacrawler -c=dashboard -outputdir=my_output_dir
```

# Output samples


