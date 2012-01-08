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

# Create a nameless, data-less node!
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

Representation of relationships in GEOFF is similar to that of nodes except
that square brackets are used instead of parentheses. Additionally,
relationships may optionally have an attached type (which is required when
attempting to create a new relationship).

```
# Create an anonymous relationship of type "KNOWS"
[:KNOWS] {"how_many_years": 4}

# Create a similar relationship, named "REL1"
[REL1:KNOWS] {"how_many_years": 4}
```

The examples above all define simple relationships where the start and end
nodes have not been explicitly specified. In these cases, new anonymous nodes
will be implicitly created before connecting them with the new relationship.

As with nodes, it is possible to update realtionships which have previously
been defined using a similar notation:

```
# Update the relationship "REL1" with new properties
[REL1] {"how_many_years": 5}

# Update the relationship "REL1" with new properties (if of type "KNOWS")
[REL1:KNOWS] {"how_many_years": 6}

# Update the relationship "REL1" with new properties (if of type "LOVES")
# In this example, this will be filtered out and ignored as an incorrect
# relationship type has been specified
[REL1:LOVES] {"how_many_years": 7}
```

Defining new relationships would not be much use if it were not possible to
refer to the nodes which they connect. The notation above is actually a
shorthand form of the longer ASCII art style notation `()-[R:TYPE]->()`.
Those familiar with Cypher will of course recognise the pattern from this
language.

The full relationship notation of course allows more complex combinations of
rules to be defined, including using named nodes. The following two examples
illustrate how to pre-define and post-define the nodes of a new realtionship
respectively; both produce identical results:

```
# Example 1: create nodes, then join with relationship
(A)                {"name": "Alice"}
(B)                {"name": "Bob"}
(A)-[R:KNOWS]->(B) {"since": 1977}

# Example 2: create relationship, then update node properties
(A)-[R:KNOWS]->(B) {"since": 1977}
(A)                {"name": "Alice"}
(B)                {"name": "Bob"}
```

## Index Entries

## Parameters

## Installation and Usage

### Java (REST server plugin)

To install, copy the [geoff-core.jar](http://py2neo.org/geoff/geoff-core.jar)
file into `$NEO4J_HOME/system/lib/` and the
[geoff-plugin.jar](http://py2neo.org/geoff/geoff-core.jar) file into
`$NEO4J_HOME/plugins/`. Be sure to restart the server to enable the plugin.

### Python (py2neo)

A `geoff.py` module is provided with [py2neo](http://py2neo.org/) which allows
dumping and loading of data through functions familiar to those who have used
the Python [pickle](http://docs.python.org/library/pickle.html) and
[marshal](http://docs.python.org/library/marshal.html) modules.

#### Dumping

The dump (export) operations require a list of `neo4j.Path` objects and are
defined as follows:

`geoff.dump(paths, file)` - output the set of all component nodes and
relationships within the specified paths to a file

`geoff.dumps(paths)` - return the set of all component nodes and relationships
within the specified paths as a string

Example code:

```python
from py2neo import neo4j, geoff
gdb = neo4j.GraphDatabaseService("http://localhost:7474/db/data/")
ref_node = gdb.get_reference_node()
traverser = ref_node.traverse(order="depth_first", max_depth=2)
print geoff.dumps(traverser.paths)
```

#### Loading

The load (import) operations require both serialised data and a
`neo4j.GraphDatabaseService` into which the load will be carried out. If the
REST server plugin has been installed (see above) then py2neo will detect and
use this plugin automatically for loading Geoff data. If this plugin is not
available, a client-side version will be used instead. **Please note that the
client-side version is now deprecated in favour of the server plugin.**

The load functions are defined as follows:

`geoff.load(file, gdb)` - load all nodes, relationships and index entries
from a file into the specified database

`geoff.loads(str, gdb)` - load all nodes, relationships and index entries
from a string into the specified database

Example code:

```python
from py2neo import neo4j, geoff
gdb = neo4j.GraphDatabaseService("http://localhost:7474/db/data/")
ref_node = gdb.get_reference_node()
ref_node.create_relationship_to(geoff.load(file("foo.geoff"), gdb), "FOO")
```

## Summary of Notation

```
# create anonymous node with no properties
()
() {}

# create anonymous node with properties
() {"property": "value"}

# create node A with no properties
(A)

# create node A with no properties or
# update node A to remove all properties
(A) {}

# create node A with properties or
# update node A with replacement properties
(A) {"property": "value"}

# create anonymous relationship of type TYPE between two new anonymous nodes
[:TYPE]        {"property": "value"}
()-[:TYPE]->() {"property": "value"}

# create relationship R of type TYPE between two new anonymous nodes or
# update relationship R (if of type TYPE) with replacement properties
[R:TYPE]        {"property": "value"}
()-[R:TYPE]->() {"property": "value"}

# update relationship R with replacement properties
[R]        {"property": "value"}
()-[R]->() {"property": "value"}

# create relationship R of type TYPE between one or more named nodes or
# update relationship R (if of type TYPE) with replacement properties
# - if A and/or B are undefined, look up start and end nodes respectively
(A)-[R:TYPE]->()  {"property": "value"}
(A)-[R:TYPE]->(B) {"property": "value"}
()-[R:TYPE]->(B)  {"property": "value"}

# update relationship R with replacement properties
# - if A and/or B are undefined, look up start and end nodes respectively
(A)-[R]->()  {"property": "value"}
(A)-[R]->(B) {"property": "value"}
()-[R]->(B)  {"property": "value"}

# create relationship of type TYPE between one or more named nodes 
(A)-[:TYPE]->()  {"property": "value"}
(A)-[:TYPE]->(B) {"property": "value"}
()-[:TYPE]->(B)  {"property": "value"}

# ensure inclusion of node in index under key:value pair
# - if node defined and index entry doesn't exist, add index entry
# - if node undefined and index entry exists, look up index entry
# - if node undefined and entry doesn't exist, create node and add entry
(A)<=|Index| {"key": "value"}

# ensure inclusion of relationship in index under key:value pair
# - if rel defined and index entry doesn't exist, add index entry
# - if rel undefined and index entry exists, look up index entry
[R]<=|Index| {"key": "value"}

# ensure inclusion of relationship in index under key:value pair
# - if rel of type TYPE defined and index entry doesn't exist, add index entry
# - if rel undefined and index entry of type TYPE exists, look up index entry
# - if rel undefined and entry doesn't exist, create rel and add entry
[R:TYPE]<=|Index| {"key": "value"}

```
