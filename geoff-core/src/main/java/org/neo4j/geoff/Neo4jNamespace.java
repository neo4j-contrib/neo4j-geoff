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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides context for items to be added to a Neo4j database and retained by name
 * so that they may be referred to from within the same context
 *
 * @author Nigel Small
 */
public class Neo4jNamespace implements Namespace {

	private final GraphDatabaseService graphDB;
	private final Neo4jEntityStore store;

	private int ruleNumber = 0;

	/**
	 * Set up a new Namespace attached to the supplied GraphDatabaseService
	 *
	 * @param graphDB the database in which to store items
	 * @param params set of pre-existing Nodes and Relationships accessible within this namespace
	 */
	Neo4jNamespace(GraphDatabaseService graphDB, Map<String, ? extends PropertyContainer> params) {
		this.graphDB = graphDB;
		this.store = new Neo4jEntityStore(params);
	}

	public int getRuleNumber() {
		return this.ruleNumber;
	}

	public Map<String, PropertyContainer> getEntities() {
		return this.store.entities();
	}

	@Override
	public void apply(Rule rule) throws RuleApplicationException {
		this.ruleNumber++;
		if (Geoff.DEBUG) System.out.println(String.format("Applying rule #%d: %s", this.ruleNumber, rule));
		String pattern = rule.getDescriptor().getPattern();
		if ("N".equals(pattern)) {
			createOrUpdateNode(
					(NodeToken) rule.getDescriptor().getToken(0),
					rule.getData()
			);
		} else if ("N-R->N".equals(pattern)) {
			createOrUpdateRelationship(
					(NodeToken) rule.getDescriptor().getToken(0),
					(RelToken) rule.getDescriptor().getToken(2),
					(NodeToken) rule.getDescriptor().getToken(5),
					rule.getData()
			);
		} else if ("R".equals(pattern)) {
			createOrUpdateRelationship(
					(RelToken) rule.getDescriptor().getToken(0),
					rule.getData()
			);
		} else if ("!N".equals(pattern)) {
			deleteNode(
					(NodeToken) rule.getDescriptor().getToken(1),
					rule.getData()
			);
		} else if ("N-R-!N".equals(pattern)) {
			deleteRelationships(
					(NodeToken) rule.getDescriptor().getToken(0),
					(RelToken) rule.getDescriptor().getToken(2),
					(NodeToken) rule.getDescriptor().getToken(5),
					rule.getData()
			);
		} else if ("!R".equals(pattern)) {
			deleteRelationship(
					(RelToken) rule.getDescriptor().getToken(1),
					rule.getData()
			);
		} else if ("N^I".equals(pattern)) {
			includeIndexEntry(
					(NodeToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("R^I".equals(pattern)) {
			includeIndexEntry(
					(RelToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("N'I".equals(pattern)) {
			excludeIndexEntry(
					(NodeToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("R'I".equals(pattern)) {
			excludeIndexEntry(
					(RelToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else {
			throw new RuleApplicationException(this.ruleNumber, "Unknown rule: " + rule.toString());
		}
	}

	@Override
	public void apply(Iterable<Rule> rules) throws RuleApplicationException {
		if (Geoff.DEBUG) System.out.println("Applying multiple rules");
		for (Rule rule : rules) {
			apply(rule);
		}
	}

	/**
	 * Create or update node
	 *
	 * @param a node token
	 * @param properties properties to be assigned to the node
	 * @return the Node
	 */
	private Node createOrUpdateNode(NodeToken a, Map<String, Object> properties) {
		Node node;
		if (store.isDefined(a)) {
			node = store.get(a);
		} else {
			node = this.graphDB.createNode();
			store.define(a, node);
		}
		setProperties(node, properties);
		return node;
	}

	/**
	 * Create or update relationship
	 *
	 * @param a start node token
	 * @param r relationship token
	 * @param b end node token
	 * @param properties properties to be assigned to the relationship
	 * @return the Relationship
	 * @throws RuleApplicationException if start node, end node or type are invalid
	 */
	private Relationship createOrUpdateRelationship(NodeToken a, RelToken r, NodeToken b, Map<String, Object> properties)
			throws RuleApplicationException {
		failIfIncorrectlyTyped(r);
		Relationship relationship;
		if (store.isDefined(r)) {
			relationship = store.get(r);
			if (store.isDefined(a)) {
				failIfNotEqual(store.get(a), relationship.getStartNode(), "Start node mismatch");
			}
			if (store.isDefined(b)) {
				failIfNotEqual(store.get(b), relationship.getEndNode(), "End node mismatch");
			}
			store.define(a, relationship.getStartNode());
			store.define(b, relationship.getEndNode());
			setProperties(relationship, properties);
		} else if (r.hasType()) {
			Node startNode = createOrUpdateNode(a, null);
			Node endNode = createOrUpdateNode(b, null);
			relationship = startNode.createRelationshipTo(endNode, DynamicRelationshipType.withName(r.getType()));
			setProperties(relationship, properties);
		} else {
			throw new RuleApplicationException(this.ruleNumber, "Cannot create untyped relationship");
		}
		store.define(r, relationship);
		return relationship;
	}

	/**
	 * Create or update relationship
	 *
	 * @param r relationship token
	 * @param properties properties to be assigned to the relationship
	 * @return the Relationship
	 * @throws RuleApplicationException if type is invalid
	 */
	private Relationship createOrUpdateRelationship(RelToken r, Map<String, Object> properties)
			throws RuleApplicationException {
		return createOrUpdateRelationship(new NodeToken(""), r, new NodeToken(""), properties);
	}

	/**
	 * Delete specific node
	 *
	 * @param a node token
	 * @param properties
	 * @throws RuleApplicationException if node is undefined
	 */
	private void deleteNode(NodeToken a, Map<String, Object> properties)
			throws RuleApplicationException {
		failIfNotDefined(a, "Cannot exclude undefined node");
		store.undefine(a).delete();
	}

	/**
	 * Delete one or more relationships
	 * 
	 * @param a start node token
	 * @param r relationship token
	 * @param b end node token
	 * @param properties
	 * @throws RuleApplicationException if start node, end node or type are invalid
	 */
	private void deleteRelationships(NodeToken a, RelToken r, NodeToken b, Map<String, Object> properties)
			throws RuleApplicationException {
		failIfIncorrectlyTyped(r);
		if (store.isDefined(r)) {
			Relationship relationship = store.undefine(r);
			if (store.isDefined(a)) {
				failIfNotEqual(store.get(a), relationship.getStartNode(), "Start node mismatch");
			}
			if (store.isDefined(b)) {
				failIfNotEqual(store.get(b), relationship.getEndNode(), "End node mismatch");
			}
			store.define(a, relationship.getStartNode());
			store.define(b, relationship.getEndNode());
			relationship.delete();
		} else if (r.hasType()) {
			if (store.isDefined(a) || store.isDefined(b)) {
				RelationshipType type = DynamicRelationshipType.withName(r.getType());
				List<Relationship> matches = match(a, b, type);
				for (Relationship match : matches) {
					match.delete();
				}
			} else {
				throw new RuleApplicationException(this.ruleNumber, "Not enough information to identify relationships for deletion");
			}
		} else {
			if (store.isDefined(a) || store.isDefined(b)) {
				List<Relationship> matches = match(a, b);
				for (Relationship match : matches) {
					match.delete();
				}
			} else {
				throw new RuleApplicationException(this.ruleNumber, "Not enough information to identify relationships for deletion");
			}
		}
	}

	/**
	 * Delete specific relationship
	 * 
	 * @param r relationship token
	 * @param properties
	 * @throws RuleApplicationException if type is invalid
	 */
	private void deleteRelationship(RelToken r, Map<String, Object> properties)
			throws RuleApplicationException {
		deleteRelationships(new NodeToken(""), r, new NodeToken(""), properties);
	}

	/**
	 * Ensure entry is included within node index
	 *
	 * @param a node token
	 * @param i index token
	 * @param keyValuePairs the key:value pairs against which to create index entries
	 * @throws RuleApplicationException if index is not named
	 */
	private void includeIndexEntry(NodeToken a, IndexToken i, Map<String, Object> keyValuePairs)
			throws RuleApplicationException {
		failIfNotNamed(i, "Index must be named");
		Index<Node> index = this.graphDB.index().forNodes(i.getName());
		for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			IndexHits<Node> hits = index.get(key, value);
			if (store.isDefined(a)) {
				Node node = store.get(a);
				boolean indexed = false;
				for (Node hit : hits) {
					indexed = indexed || (node.getId() == hit.getId());
				}
				if (!indexed) {
					index.add(node, key, value);
				}
			} else {
				Node node;
				if (hits.size() == 0) {
					node = this.graphDB.createNode();
					index.add(node, key, value);
				} else {
					node = hits.getSingle();
				}
				store.define(a, node);
			}
		}
	}

	/**
	 * Ensure entry is included within relationship index
	 *
	 * @param r relationship token
	 * @param i index token
	 * @param keyValuePairs
	 * @throws RuleApplicationException
	 */
	private void includeIndexEntry(RelToken r, IndexToken i, Map<String, Object> keyValuePairs)
			throws RuleApplicationException {
		failIfNotNamed(i, "Index must be named");
		failIfIncorrectlyTyped(r);
		Index<Relationship> index = this.graphDB.index().forRelationships(i.getName());
		for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			IndexHits<Relationship> hits = index.get(key, value);
			if (store.isDefined(r)) {
				Relationship relationship = store.get(r);
				boolean indexed = false;
				for (Relationship hit : hits) {
					indexed = indexed || (relationship.getId() == hit.getId());
				}
				if (!indexed) {
					index.add(relationship, key, value);
				}
			} else if (r.hasType()) {
				RelationshipType type = DynamicRelationshipType.withName(r.getType());
				Relationship relationship = null;
				for (Relationship hit : hits) {
					if (hit.isType(type)) {
						relationship = hit;
						break;
					}
				}
				failIfNull(relationship, String.format("No index entries found of type %s", r.getType()));
				store.define(r, relationship);
			} else {
				Relationship relationship = null;
				for (Relationship hit : hits) {
					relationship = hit;
					break;
				}
				failIfNull(relationship, "No index entries found");
				store.define(r, relationship);
			}
		}
	}

	/**
	 * Exclude Node Index Entry
	 * ========================
	 *
	 * (A)!=|I| {...}
	 *
	 * @param a node token
	 * @param i index token
	 * @param keyValuePairs
	 * @throws RuleFormatException
	 * @throws RuleApplicationException
	 */
	private void excludeIndexEntry(NodeToken a, IndexToken i, Map<String, Object> keyValuePairs)
			throws RuleApplicationException {
		failIfNotNamed(i, "Index must be named");
		Index<Node> index = this.graphDB.index().forNodes(i.getName());
		if (store.isDefined(a)) {
			Node node = store.get(a);
			for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
				index.remove(node, entry.getKey(), entry.getValue());
			}
		} else {
			for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				IndexHits<Node> hits = index.get(key, value);
				for (Node hit : hits) {
					store.define(a, hit);
					index.remove(hit, key, value);
				}
			}
		}
	}

	/**
	 * Exclude Relationship Index Entry
	 * ================================
	 *
	 * [R]!=|I|   {...}
	 * [R:T]!=|I| {...}
	 *
	 * @param r relationship token
	 * @param i index token
	 * @param keyValuePairs
	 * @throws RuleFormatException
	 * @throws RuleApplicationException
	 */
	private void excludeIndexEntry(RelToken r, IndexToken i, Map<String, Object> keyValuePairs)
			throws RuleApplicationException {
		failIfNotNamed(i, "Index must be named");
		failIfIncorrectlyTyped(r);
		Index<Relationship> index = this.graphDB.index().forRelationships(i.getName());
		if (store.isDefined(r)) {
			Relationship relationship = store.get(r);
			for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
				index.remove(relationship, entry.getKey(), entry.getValue());
			}
		} else if (r.hasType()) {
			RelationshipType type = DynamicRelationshipType.withName(r.getType());
			for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				IndexHits<Relationship> hits = index.get(key, value);
				for (Relationship hit : hits) {
					if (hit.isType(type)) {
						store.define(r, hit);
						index.remove(hit, key, value);
					}
				}
			}
		} else {
			for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				IndexHits<Relationship> hits = index.get(key, value);
				for (Relationship hit : hits) {
					store.define(r, hit);
					index.remove(hit, key, value);
				}
			}
		}
	}

	private void failIfNotNamed(NameableToken token, String message) throws RuleApplicationException {
		if (!token.hasName()) {
			throw new RuleApplicationException(this.ruleNumber, message);
		}
	}

	private void failIfNotDefined(NodeToken node, String message) throws RuleApplicationException {
		if (!store.isDefined(node)) {
			throw new RuleApplicationException(this.ruleNumber, message);
		}
	}

	private void failIfIncorrectlyTyped(RelToken rel) throws RuleApplicationException {
		if (store.isIncorrectlyTyped(rel)) {
			throw new RuleApplicationException(this.ruleNumber, "Relationship is incorrectly typed: " + rel.toString());
		}
	}

	private void failIfNotEqual(Node node1, Node node2, String message) throws RuleApplicationException {
		if (node1.getId() != node2.getId()) {
			throw new RuleApplicationException(this.ruleNumber, message);
		}
	}

	private void failIfNull(Object object, String message) throws RuleApplicationException {
		if (object == null) {
			throw new RuleApplicationException(this.ruleNumber, message);
		}
	}

	/**
	 * If A and B are both defined, match all relationships between A and B
	 * If only A is defined, match all outgoing relationships from A
	 * If only B is defined, match all incoming relationships into B
	 * If neither are defined, match none
	 *
	 * @param a start node token
	 * @param b end node token
	 * @return list of matching relationships
	 */
	private List<Relationship> match(NodeToken a, NodeToken b) {
		final ArrayList<Relationship> matches = new ArrayList<Relationship>();
		if (store.isDefined(a)) {
			Node start = store.get(a);
			if (store.isDefined(b)) {
				Node end = store.get(b);
				for (Relationship candidate : start.getRelationships(Direction.OUTGOING)) {
					if (candidate.getEndNode().getId() == end.getId()) {
						matches.add(candidate);
					}
				}
			} else {
				for (Relationship candidate : start.getRelationships(Direction.OUTGOING)) {
					matches.add(candidate);
				}
			}
		} else if (store.isDefined(b)) {
			Node end = store.get(b);
			for (Relationship candidate : end.getRelationships(Direction.INCOMING)) {
				matches.add(candidate);
			}
		}
		return matches;
	}

	/**
	 * If A and B are both defined, match all relationships between A and B of given type
	 * If only A is defined, match all outgoing relationships from A of given type
	 * If only B is defined, match all incoming relationships into B of given type
	 * If neither are defined, match none
	 *
	 * @param a start node token
	 * @param b end node token
	 * @param type type of matching relationships
	 * @return list of matching relationships
	 */
	private List<Relationship> match(NodeToken a, NodeToken b, RelationshipType type) {
		final ArrayList<Relationship> matches = new ArrayList<Relationship>();
		if (store.isDefined(a)) {
			Node start = store.get(a);
			if (store.isDefined(b)) {
				Node end = store.get(b);
				for (Relationship candidate : start.getRelationships(Direction.OUTGOING, type)) {
					if (candidate.getEndNode().getId() == end.getId()) {
						matches.add(candidate);
					}
				}
			} else {
				for (Relationship candidate : start.getRelationships(Direction.OUTGOING, type)) {
					matches.add(candidate);
				}
			}
		} else if (store.isDefined(b)) {
			Node end = store.get(b);
			for (Relationship candidate : end.getRelationships(Direction.INCOMING, type)) {
				matches.add(candidate);
			}
		}
		return matches;
	}

	private void setProperties(PropertyContainer entity, Map<String, Object> data) {
		if (data != null) {
			for(String key : entity.getPropertyKeys()) {
				entity.removeProperty(key);
			}
			for(Map.Entry<String, Object> entry : data.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value == null) {
					continue;
				}
	//			if (value instanceof List) {
	//				List listValue = (List) value;
	//				if (listValue.isEmpty()) {
	//					continue;
	//				} else {
	//					Array a  = listValue.toArray();
	//				}
	//			}
				entity.setProperty(key, value);
			}
		}
	}

}
