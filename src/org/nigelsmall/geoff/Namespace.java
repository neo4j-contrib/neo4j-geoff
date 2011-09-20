package org.nigelsmall.geoff;

import java.util.HashMap;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;

public class Namespace {

	private final GraphDatabaseService graphDB;
	
	private Node firstNode = null;
	private final HashMap<String,Node> nodes = new HashMap<String,Node>();
	private final HashMap<String,Relationship> relationships = new HashMap<String,Relationship>();

	public Namespace(GraphDatabaseService graphDB) {
		this.graphDB = graphDB;
	}

	public Node getFirstNode() {
		return this.firstNode;
	}

	public Node getNode(String name) throws UnknownNodeException {
		if(this.nodes.containsKey(name)) {
			return this.nodes.get(name);
		} else {
			throw new UnknownNodeException();
		}
	}

	public void createNode(NodeDescriptor descriptor) {
		Node node = this.graphDB.createNode();
		if(descriptor.getData() != null) {
			for(String key : descriptor.getData().keySet()) {
				node.setProperty(key, descriptor.getData().get(key));
			}
		}
		if(descriptor.getNodeName().length() > 0) {
			this.nodes.put(descriptor.getNodeName(), node);
		}
		if(this.firstNode == null) {
			this.firstNode = node;
		}
	}

	public void addNodeIndexEntry(NodeIndexEntry entry) throws UnknownNodeException {
		Index<Node> index = this.graphDB.index().forNodes(entry.getIndexName());
		Node node = this.getNode(entry.getNodeName());
		if(entry.getData() != null) {
			for(String key : entry.getData().keySet()) {
				index.add(node, key, entry.getData().get(key));
			}
		}
	}
	
	public Relationship getRelationship(String name) throws UnknownRelationshipException {
		if(this.relationships.containsKey(name)) {
			return this.relationships.get(name);
		} else {
			throw new UnknownRelationshipException();
		}
	}

	public void createRelationship(RelationshipDescriptor descriptor) throws UnknownNodeException {
		Relationship rel = this.getNode(descriptor.getStartNodeName()).createRelationshipTo(
			this.getNode(descriptor.getEndNodeName()),
			DynamicRelationshipType.withName(descriptor.getRelationshipType())
		);
		if(descriptor.getData() != null) {
			for(String key : descriptor.getData().keySet()) {
				rel.setProperty(key, descriptor.getData().get(key));
			}
		}
		if(descriptor.getRelationshipName().length() > 0) {
			this.relationships.put(descriptor.getRelationshipName(), rel);
		}
	}

	public void addRelationshipIndexEntry(RelationshipIndexEntry entry) throws UnknownRelationshipException {
		Index<Relationship> index = this.graphDB.index().forRelationships(entry.getIndexName());
		Relationship rel = this.getRelationship(entry.getRelationshipName());
		if(entry.getData() != null) {
			for(String key : entry.getData().keySet()) {
				index.add(rel, key, entry.getData().get(key));
			}
		}
	}

}
