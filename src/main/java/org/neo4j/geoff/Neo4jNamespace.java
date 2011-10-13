/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.geoff;

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
public class Neo4jNamespace implements Namespace<Node,Relationship> {

	private final GraphDatabaseService graphDB;
	private final HashMap<String,Node> nodes = new HashMap<String,Node>();
	private final HashMap<String,Relationship> relationships = new HashMap<String,Relationship>();
	private Node firstNode = null;

	/**
	 * Set up a new Namespace attached to the supplied GraphDatabaseService
	 * 
	 * @param graphDB the database in which to store items
	 */
	public Neo4jNamespace(GraphDatabaseService graphDB) {
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
			throw new UnknownNodeException(name);
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
			throw new UnknownRelationshipException(name);
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
