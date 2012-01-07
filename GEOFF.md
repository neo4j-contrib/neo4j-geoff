# Geoff

Geoff is a declarative notation for representing graph data within concise
human-readable text. It can be used to store snapshots within a flat file or
to transmit data changes over a network stream. When used to represent
changes, it can be seen as akin to the Unix tool *diff*.

A Geoff data set or file consists of a sequence of *rules*. Each rule
comprises a *descriptor* and an optional set of data held as key:value pairs.
The descriptor is a sequence of tokens, somewhat similar to the notation used
in the [Cypher](http://docs.neo4j.org/chunked/stable/cypher-query-lang.html)
query language and can designate additive or subtractive requirements for
nodes and relationships as well as manipulations to index entries.

## Nodes

Nodes within Geoff are simply represented as an identifying name surrounded by
parentheses, such as `(A)` or `(foo)`. Any alphabetic or numeric characters may
be used within such names as well as underscores. These names are used purely
as local references within the Geoff data set - plus its input and output
parameters - and they therefore hold no permanent relevance to the nodes within
the underlying database.

The following example describes a typical node, locally referred to as
`(bert)`:

```
(bert) {"name": "Albert Einstein", "date_of_birth": "1879-03-14"}
```

The two parts of the rule - the descriptor and the property data - are
separated by linear white space which may consist of any number of `TAB` or
`SPACE` characters. While the Geoff format makes no restrictions on the types
of the data provided as properties, the underlying database may do so.

Certain parts of such rules are optional. Here, it is not necessary to provide
either a name or a set of data, therefore allowing variations such as:

```
# Create an anonymous node
() {"name": "Albert Einstein", "date_of_birth": "1879-03-14"}

# Create a node with no data
(bert)

# Create a nameless, dataless node!
()
```

If the name provided to a node token already exists within that context, these
rules can be used to update the properties on existing nodes. Properties
provided to rules which update nodes will *completely replace* any existing
properties on that node:

```
# Create a new node
(bert) {"name": "Albert Einstein", "date_of_birth": "1879-03-14"}

# Update it (replace its properties)
(bert) {"full_name": "Bertrand Arthur William Russell"}

# Providing no data will preserve existing properties (but isn't very useful!)
(bert)

# However, an empty data set will remove all properties
(bert) {}
```

## Relationships

## Index Entries

## Parameters
