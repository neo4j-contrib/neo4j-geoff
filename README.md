# neo4j-geoff

The neo4j-geoff project provides a core Geoff library plus a Neo4j server
plugin, enabling usage via the RESTful web service interface. see (http://neo4j-contrib.github.io/neo4j-geoff)(http://neo4j-contrib.github.io/neo4j-geoff)
for usage.

For full details of the format and how to use this plugin, please visit
[http://geoff.nigelsmall.net/](http://geoff.nigelsmall.net/).

## Building:

    mvn clean install
    cd geoff-plugin
    mvn -Pneo-docs-build clean install
    cd ..
    mvn site