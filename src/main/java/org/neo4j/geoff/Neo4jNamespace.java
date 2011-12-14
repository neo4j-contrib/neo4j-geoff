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


import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides context for items to be added to a Neo4j database and retained by name
 * so that they may be referred to from within the same context
 *
 * @author Nigel Small
 */
public class Neo4jNamespace implements Namespace {

	private final GraphDatabaseService graphDB;

	private final HashMap<String, Node> oldNodes = new HashMap<String, Node>();
	private final HashMap<String, Node> newNodes = new HashMap<String, Node>();

	private final HashMap<String, Relationship> oldRelationships = new HashMap<String, Relationship>();
	private final HashMap<String, Relationship> newRelationships = new HashMap<String, Relationship>();

	private final HashMap<String, PropertyContainer> entities = new HashMap<String, PropertyContainer>();

	/**
	 * Set up a new Namespace attached to the supplied GraphDatabaseService
	 *
	 * @param graphDB the database in which to store items
	 * @param hooks   set of pre-existing Nodes and Relationships accessible within this namespace
	 */
	Neo4jNamespace(GraphDatabaseService graphDB, Map<String, ? extends PropertyContainer> hooks) {
		this.graphDB = graphDB;
		if (hooks != null) {
			// separate hooks into nodes and relationships
			for (Map.Entry<String, ? extends PropertyContainer> hook : hooks.entrySet()) {
				this.entities.put("{" + hook.getKey() + "}", hook.getValue());
				if (hook.getValue() instanceof Node) {
					this.oldNodes.put(hook.getKey(), (Node) hook.getValue());
				} else if (hook.getValue() instanceof Relationship) {
					this.oldRelationships.put(hook.getKey(), (Relationship) hook.getValue());
				} else {
					// unexpected hook type! should never happen :-)
					throw new IllegalArgumentException("Unexpected hook " + hook.getClass());
				}
			}
		}
	}

	/**
	 * Update the properties on a pre-existing Node or Relationship
	 *
	 * @param descriptor a pointer to the entity to update
	 * @throws UnknownEntityException when the referenced entity cannot be found
	 */
	@Override
	public void updateEntity(HookDescriptor descriptor)
			throws UnknownEntityException {
		String hookName = descriptor.getHook().getName();
		if (this.oldNodes.containsKey(hookName)) {
			// 'tis a node...
			Node node = this.oldNodes.get(hookName);
			// update properties
			if (descriptor.getData() != null) {
				for (Map.Entry<String, Object> e : descriptor.getData().entrySet()) {
					node.setProperty(e.getKey(), e.getValue());
				}
			}
		} else if (this.oldRelationships.containsKey(hookName)) {
			// 'tis a relationship...
			Relationship rel = this.oldRelationships.get(hookName);
			// update properties
			if (descriptor.getData() != null) {
				for (Map.Entry<String, Object> e : descriptor.getData().entrySet()) {
					rel.setProperty(e.getKey(), e.getValue());
				}
			}
		} else {
			throw new UnknownEntityException(String.format("Hook {%s} not found", descriptor.getHook().getName()));
		}
	}

	@Override
	public void reflectIndexEntry(IndexEntryReflection<Reflective> indexEntryReflection)
			throws UnknownEntityException {
		Reflective entity = indexEntryReflection.getEntity();
		if(entity instanceof NodeRef) {
			Index<Node> index = this.graphDB.index().forNodes(indexEntryReflection.getIndex().getName());
			IndexHits<Node> hits = index.get(indexEntryReflection.getKey(), indexEntryReflection.getValue());
			String name = indexEntryReflection.getEntity().getName();
			Node node = hits.getSingle();
			this.newNodes.put(name, node);
			this.entities.put("(" + name + ")", node);
		} else if(entity instanceof RelationshipRef) {
			Index<Relationship> index = this.graphDB.index().forRelationships(indexEntryReflection.getIndex().getName());
			IndexHits<Relationship> hits = index.get(indexEntryReflection.getKey(), indexEntryReflection.getValue());
			String name = indexEntryReflection.getEntity().getName();
			Relationship rel = hits.getSingle();
			this.newRelationships.put(name, rel);
			this.entities.put("[" + name + "]", rel);
		} else {
			throw new IllegalArgumentException("Unexpected entity type");
		}
	}

	/**
	 * Add a Node to the database and keep a reference to it, indexed by name
	 *
	 * @param descriptor details of the Node to be created
	 * @throws DuplicateNameException when the supplied Node name already exists
	 */
	@Override
	public void createNode(NodeDescriptor descriptor)
			throws DuplicateNameException {
		if (this.newNodes.containsKey(descriptor.getNode().getName())) {
			throw new DuplicateNameException(String.format("Duplicate node name (%s)", descriptor.getNode().getName()));
		}
		// first, create the actual node
		Node node = this.graphDB.createNode();
		// then add any supplied properties
		if (descriptor.getData() != null) {
			for (Map.Entry<String, Object> e : descriptor.getData().entrySet()) {
				node.setProperty(e.getKey(), e.getValue());
			}
		}
		// assuming this Node has a name, hold onto it
		String name = descriptor.getNode().getName();
		if (!name.isEmpty()) {
			this.newNodes.put(name, node);
			this.entities.put("(" + name + ")", node);
		}
	}

	private Node getNode(Connectable connectable) throws UnknownEntityException {
		String name = connectable.getName();
		if (connectable instanceof HookRef) {
			if (this.oldNodes.containsKey(name)) {
				return this.oldNodes.get(name);
			} else {
				throw new UnknownEntityException(String.format("Node hook {%s} not found", name));
			}
		} else if (connectable instanceof NodeRef) {
			if (this.newNodes.containsKey(name)) {
				return this.newNodes.get(name);
			} else {
				throw new UnknownEntityException(String.format("Node (%s) not found", name));
			}
		} else {
			// should only happen if someone has made something else Connectable!
			throw new IllegalArgumentException("Unexpected connector type");
		}
	}

	/**
	 * Add a Relationship to the database and keep a reference to it, indexed
	 * by name, if it has a name
	 *
	 * @param descriptor details of the Relationship to be created
	 * @throws DuplicateNameException when the supplied Relationship name already exists
	 * @throws UnknownEntityException when an end point Node cannot be identified
	 */
	@Override
	public void createRelationship(RelationshipDescriptor<Connectable, Connectable> descriptor)
			throws DuplicateNameException, UnknownEntityException {
		if (descriptor.hasName() && this.newRelationships.containsKey(descriptor.getName())) {
			throw new DuplicateNameException(String.format("Duplicate relationship name [%s]", descriptor.getName()));
		}
		// create the Relationship using the supplied criteria
		Relationship rel = this.getNode(descriptor.getStartNode()).createRelationshipTo(
				this.getNode(descriptor.getEndNode()),
				DynamicRelationshipType.withName(descriptor.getType())
		);
		// then add any supplied properties
		if (descriptor.getData() != null) {
			for (Map.Entry<String, Object> e : descriptor.getData().entrySet()) {
				rel.setProperty(e.getKey(), e.getValue());
			}
		}
		// assuming this Relationship has a name, hold onto it
		String name = descriptor.getName();
		if (!name.isEmpty()) {
			this.newRelationships.put(name, rel);
			this.entities.put("[" + name + "]", rel);
		}
	}

	/**
	 * Include a reference to an entity within an Index
	 *
	 * @param indexRule details of the inclusion within the Index
	 * @throws UnknownEntityException when no Node exists with the name specified
	 */
	@Override
	public void updateIndex(IndexRule<Indexable> indexRule)
			throws UnknownEntityException {
		Indexable entity = indexRule.getEntity();
		String entityName = entity.getName();
		boolean forOldNode = entity instanceof HookRef && this.oldNodes.containsKey(entityName);
		boolean forOldRel = entity instanceof HookRef && this.oldRelationships.containsKey(entityName);
		boolean forNewNode = entity instanceof NodeRef && this.newNodes.containsKey(entityName);
		boolean forNewRel = entity instanceof RelationshipRef && this.oldRelationships.containsKey(entityName);
		if (forOldNode || forNewNode) {
			// locate the required Index
			Index<Node> index = this.graphDB.index().forNodes(indexRule.getIndex().getName());
			// look up the Node we need to work with
			Node node = forOldNode ? this.oldNodes.get(entityName) : this.newNodes.get(entityName);
			// update entries under all the supplied key:value pairs
			if (indexRule.getData() != null) {
				for (Map.Entry<String, Object> entry : indexRule.getData().entrySet()) {
					index.remove(node, entry.getKey(), entry.getValue());
					if(indexRule instanceof IndexInclusionRule) {
						index.add(node, entry.getKey(), entry.getValue());
					}
				}
			}
		} else if (forOldRel || forNewRel) {
			// locate the required Index
			Index<Relationship> index = this.graphDB.index().forRelationships(indexRule.getIndex().getName());
			// look up the Relationship we need to work with
			Relationship rel = forOldRel ? this.oldRelationships.get(entityName) : this.newRelationships.get(entityName);
			// update entries under all the supplied key:value pairs
			if (indexRule.getData() != null) {
				for (Map.Entry<String, Object> entry : indexRule.getData().entrySet()) {
					index.remove(rel, entry.getKey(), entry.getValue());
					if(indexRule instanceof IndexInclusionRule) {
						index.add(rel, entry.getKey(), entry.getValue());
					}
				}
			}
		} else {
			throw new UnknownEntityException(String.format("Unresolvable indexable entity"));
		}
	}

	public Map<String, PropertyContainer> getEntities() {
		return this.entities;
	}

}
