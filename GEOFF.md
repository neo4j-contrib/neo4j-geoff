# GEOFF

GEOFF is a declarative notation for representing graph data within concise, human-readable text. It can be used to store
snapshots within a flat file or to transmit data changes over a network stream. When used to represent changes, it
can be seen as akin to the Unix tool *diff*.

A GEOFF data set or file consists of a sequence of *rules*. Each rule comprises a *descriptor* and an optional set of
data held as key:value pairs. The descriptor is a sequence of characters, somewhat similar to the notation used in the
[Cypher](http://docs.neo4j.org/chunked/stable/cypher-query-lang.html) query language and forms the basis of an
*inclusion rule*, an *exclusion rule* or a *reflection rule*.

## Inclusion Rules

Inclusion rules are the simplest rules within the GEOFF format and, at a basic level, can be seen to represent a simple
serialisation of graph content. These rules are the building blocks of the format itself and hold information
representing *nodes*, *relationships* and *index entries*.

### Node Inclusion Rules

A node is the simplest entity which can be represented within GEOFF. A node inclusion descriptor consists of a name
surrounded by parentheses. Any attached data represents a set of properties applicable to that node. The name itself
is used only as a local reference to that node within the same context and holds no relevance to the underlying graph
database. If the name held within a node inclusion descriptor is new, a new node will be created. If the name is
recognised from an earlier rule or from a *load parameter*, the node in question will be updated with the supplied
properties.

The following example describes a typical node:

```
(bert) {"name": "Albert Einstein", "date_of_birth": "1879-03-14"}
```

The two parts of the rule - the descriptor and the property data - are separated by linear white space which may consist
of any number of `TAB` or `SPACE` characters. While the GEOFF format makes no restrictions on the types of the data
provided as properties, the underlying database may do so.

### Relationship Inclusion Rules

A relationship is a typed connection between two nodes and is represented by a name and/or a type name in square
brackets, between two node tokens, all connected by an ASCII art arrow. The example below shows the definition of two
nodes plus an unnamed relationship:

```
(bert)                         {"name": "Albert Einstein", "date_of_birth": "1879-03-14"}
(genrel)                       {"name": "General Theory of Relativity"}
(bert)-[:PUBLISHED]->(genrel)  {"year_of_publication": 1916}
```

This describes two nodes `(bert)` and `(genrel)` as well as an unnamed relationship between them, of type
`PUBLISHED`, also with attached properties. The values within square brackets can vary depending on the context but
exhibit one of the forms `[<name>]`, `[:<type>]` or `[<name>:<type>]` depending on the context. For example, the
relationship rule above may also be given a name, thus:

```
(bert)-[pub1:PUBLISHED]->(genrel)  {"year_of_publication": 1916}
```

Once a relationship name has been defined, either via a definition such as the one above or by passing a *load
parameter*, it may later be updated using a simpler version of the rule containing only the name:

```
[pub1]  {"year_of_publication": 1916, "complicated": true}
```

### Index Inclusion Rules

It is further possible to specify inclusions in database indexes from within a GEOFF file; this can apply to nodes or
relationships and both use a similar syntax. The index token itself it contained between pipe `|` symbols and is drawn
connected to the relevant node or relationship token with a heavy, left-pointing arrow `<=`:

```
# Include the node "bert" within the "Scientists" index where name="Einstein"
(bert)<=|Scientists|    {"name": "Einstein"}
# Include the relationship "pub1" within the "Publications" index where year=1916
[pub1]<=|Publications|  {"year": 1916}
```

The data supplied to these descriptors provide the key:value pairs under which the entities are indexed and may be
restricted by the underlying database software.

## Exclusion Rules

*(coming soon)*

### Node Exclusion Rules

*(coming soon)*

### Relationship Exclusion Rules

*(coming soon)*

### Index Exclusion Rules

*(coming soon)*

## Reflection Rules

Reflection rules allow node and relationship references to be extracted from an existing database instance from within
GEOFF source. These extractions may traverse known nodes and relationships or may look up values in an index.

### Relationship Reflection Rules

*(coming soon)*

### Index Reflection Rules

An index reflection rule looks up a value within a database index and assigns the entity discovered to a name within
the current GEOFF namespace. The following example extracts the node `(bert)` from the `|Scientists|` index where the
entry key and value equal "name" and "Einstein" respectively:

```
(bert):=|Scientists|    {"name": "Einstein"}
```

Such a rule must have *exactly one* key:value pair specified.

## Inputs and Outputs

A GEOFF parser for loading data can allow further inputs and outputs, beside the rules themselves. For input,
parameters may be supplied which reference existing graph nodes or relationships; for output, an iterator for all
entity references is returned.

### Load Parameters

The rules described above all deal with entities which are defined within an earlier rule. Sometimes it is desirable to
refer to pre-existing nodes and relationships however, and this can be achieved through use of *load parameters*. Such a
parameter takes the form of a named entity passed into a GEOFF parser which may be referenced within the file by name.

Consider the example from above:

```
(bert)                         {"name": "Albert Einstein", "date_of_birth": "1879-03-14"}
(genrel)                       {"name": "General Theory of Relativity"}
(bert)-[:PUBLISHED]->(genrel)  {"year_of_publication": 1916}
```

Here, the node `(bert)` may already exist within the underlying graph and could be specified as part of the load call
thereby allowing the first line to be omitted. The following excerpt shows possible usage from within the Java
implementation:

```java
HashMap<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(1);
params.put("bert", albertEinsteinNode);
GEOFF.loadIntoNeo4j(sourceReader, graphDB, params);
```

### Return Values

*(coming soon)*

## Other Syntax

*(coming soon)*

### Rule Sets

*(coming soon)*

### Comments and Spacing

*(coming soon)*

## Format Specification

*(coming soon)*

## Example Code

*(coming soon)*
