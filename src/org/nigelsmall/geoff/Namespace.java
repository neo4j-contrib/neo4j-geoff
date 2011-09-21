package org.nigelsmall.geoff;

import java.util.HashMap;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;

/**
 * Provides context for items to be added to a database and retained by name
 * so that they may be referred to from within the same context 
 * 
 * @author Nigel Small
 *
 */
public class Namespace {

	private final GraphDatabaseService graphDB;
	private final HashMap<String,Node> nodes = new HashMap<String,Node>();
	private final HashMap<String,Relationship> relationships = new HashMap<String,Relationship>();
	private Node firstNode = null;

	/**
	 * Set up a new Namespace attached to the supplied GraphDatabaseService
	 * 
	 * @param graphDB the database in which to store items
	 */
	public Namespace(GraphDatabaseService graphDB) {
		this.graphDB = graphDB;
	}

	/**
	 * Return the first Node added to this Namespace
	 * 
	 * @return the Node object
	 */
	public Node getFirstNode() {
		return this.firstNode;
	}

	/**
	 * Look up a previously created Node by name
	 * 
	 * @param name the name of the Node to find
	 * @return the Node object
	 * @throws UnknownNodeException if no Node exists with that name
	 */
	public Node getNode(String name) throws UnknownNodeException {
		if(this.nodes.containsKey(name)) {
			return this.nodes.get(name);
		} else {
			throw new UnknownNodeException();
		}
	}

	/**
	 * Add a Node to the database and keep a reference to it, indexed by name
	 * 
	 * @param descriptor details of the Node to be created
	 */
	public void createNode(NodeDescriptor descriptor) {
		// first, create the actual node 
		Node node = this.graphDB.createNode();
		// then add any supplied properties
		if(descriptor.getData() != null) {
			for(String key : descriptor.getData().keySet()) {
				node.setProperty(key, descriptor.getData().get(key));
			}
		}
		// assuming this Node has a name, hold onto it
		if(descriptor.getNodeName().length() > 0) {
			this.nodes.put(descriptor.getNodeName(), node);
		}
		// and if this is the first Node to be added, make this the handle
		if(this.firstNode == null) {
			this.firstNode = node;
		}
	}

	/**
	 * Add an entry to an Index of Nodes
	 * 
	 * @param entry details of the Index entry to be added
	 * @throws UnknownNodeException if no Node exists with the name specified
	 */
	public void addNodeIndexEntry(NodeIndexEntry entry) throws UnknownNodeException {
		// locate the required Index
		Index<Node> index = this.graphDB.index().forNodes(entry.getIndexName());
		// look up the Node we need to index
		Node node = this.getNode(entry.getNodeName());
		// add entries under all the supplied key:value pairs
		if(entry.getData() != null) {
			for(String key : entry.getData().keySet()) {
				index.add(node, key, entry.getData().get(key));
			}
		}
	}
	
	/**
	 * Look up a previously created Relationship by name
	 * 
	 * @param name the name of the Relationship to find
	 * @return the Relationship object
	 * @throws UnknownRelationshipException if no Relationship exists with that name
	 */
	public Relationship getRelationship(String name) throws UnknownRelationshipException {
		if(this.relationships.containsKey(name)) {
			return this.relationships.get(name);
		} else {
			throw new UnknownRelationshipException();
		}
	}

	/**
	 * Add a Relationship to the database and keep a reference to it, indexed
	 * by name, if it has a name
	 * 
	 * @param descriptor details of the Relationship to be created
	 */
	public void createRelationship(RelationshipDescriptor descriptor) throws UnknownNodeException {
		// create the Relationship using the supplied criteria
		Relationship rel = this.getNode(descriptor.getStartNodeName()).createRelationshipTo(
			this.getNode(descriptor.getEndNodeName()),
			DynamicRelationshipType.withName(descriptor.getRelationshipType())
		);
		// then add any supplied properties
		if(descriptor.getData() != null) {
			for(String key : descriptor.getData().keySet()) {
				rel.setProperty(key, descriptor.getData().get(key));
			}
		}
		// assuming this Relationship has a name, hold onto it
		if(descriptor.getRelationshipName().length() > 0) {
			this.relationships.put(descriptor.getRelationshipName(), rel);
		}
	}

	/**
	 * Add an entry to an Index of Relationships
	 * 
	 * @param entry details of the Index entry to be added
	 * @throws UnknownRelationshipException if no Relationship exists with the name specified
	 */
	public void addRelationshipIndexEntry(RelationshipIndexEntry entry) throws UnknownRelationshipException {
		// locate the required Index
		Index<Relationship> index = this.graphDB.index().forRelationships(entry.getIndexName());
		// look up the Relationship we need to index
		Relationship rel = this.getRelationship(entry.getRelationshipName());
		// add entries under all the supplied key:value pairs
		if(entry.getData() != null) {
			for(String key : entry.getData().keySet()) {
				index.add(rel, key, entry.getData().get(key));
			}
		}
	}

}
