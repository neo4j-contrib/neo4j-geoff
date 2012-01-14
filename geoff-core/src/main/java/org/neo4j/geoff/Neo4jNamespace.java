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


import org.neo4j.geoff.except.RuleApplicationException;
import org.neo4j.geoff.tokens.*;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import java.util.ArrayList;
import java.util.HashMap;
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
	private final EntityStore<NodeToken, Node> nodeStore;
	private final EntityStore<RelationshipToken, Relationship> relationshipStore;

	private int ruleNumber = 0;
	private final ArrayList<String> info = new ArrayList<String>(1);

	/**
	 * Set up a new Namespace attached to the supplied GraphDatabaseService
	 *
	 * @param graphDB the database in which to store items
	 * @param params set of pre-existing Nodes and Relationships accessible within this namespace
	 */
	public Neo4jNamespace(GraphDatabaseService graphDB, Map<String, ? extends PropertyContainer> params) {
		this.graphDB = graphDB;
		this.nodeStore = new EntityStore<NodeToken, Node>();
		this.relationshipStore = new EntityStore<RelationshipToken, Relationship>();
		if (params != null) {
			// separate params into nodes and relationships
			for (Map.Entry<String, ? extends PropertyContainer> param : params.entrySet()) {
				String key = param.getKey();
				boolean isNodeKey = key.startsWith("(") && key.endsWith(")");
				boolean isRelKey = key.startsWith("[") && key.endsWith("]");
				boolean isUntypedKey = !(isNodeKey || isRelKey);
				if (isNodeKey || isRelKey) {
					key = key.substring(1, key.length() - 1);
				}
				if (param.getValue() instanceof Node && (isNodeKey || isUntypedKey)) {
					nodeStore.put(new NodeToken(key), (Node) param.getValue());
				} else if (param.getValue() instanceof Relationship && (isRelKey || isUntypedKey)) {
					relationshipStore.put(new RelationshipToken(key), (Relationship) param.getValue());
				} else {
					throw new IllegalArgumentException(String.format("Illegal parameter '%s':%s ", key, param.getValue().getClass().getName()));
				}
			}
		}
	}

	public List<String> getInfo() {
		return this.info;
	}
	
	public Map<String, PropertyContainer> getEntities() {
		Map<String, PropertyContainer> entities = new HashMap<String, PropertyContainer>();
		for (Map.Entry<String, Node> entry : nodeStore.toMap().entrySet()) {
			entities.put('(' + entry.getKey() + ')', entry.getValue());
		}
		for (Map.Entry<String, Relationship> entry : relationshipStore.toMap().entrySet()) {
			entities.put('[' + entry.getKey() + ']', entry.getValue());
		}
		return entities;
	}

	@Override
	public void apply(Rule rule) throws RuleApplicationException {
		this.ruleNumber++;
		this.info.clear();
		if (Geoff.DEBUG) System.out.println(String.format("Applying rule #%d: %s", this.ruleNumber, rule));
		String pattern = rule.getDescriptor().getPattern();
		if ("N".equals(pattern)) {
			createOrUpdateNodes(
					(NodeToken) rule.getDescriptor().getToken(0),
					rule.getData()
			);
		} else if ("N-R->N".equals(pattern)) {
			createOrUpdateRelationships(
					(NodeToken) rule.getDescriptor().getToken(0),
					(RelationshipToken) rule.getDescriptor().getToken(2),
					(NodeToken) rule.getDescriptor().getToken(5),
					rule.getData()
			);
		} else if ("R".equals(pattern)) {
			createOrUpdateRelationships(
					(RelationshipToken) rule.getDescriptor().getToken(0),
					rule.getData()
			);
		} else if ("N=R=>N".equals(pattern)) {
			reflectOrUpdateRelationships(
					(NodeToken) rule.getDescriptor().getToken(0),
					(RelationshipToken) rule.getDescriptor().getToken(2),
					(NodeToken) rule.getDescriptor().getToken(5),
					rule.getData()
			);
		} else if ("!N".equals(pattern)) {
			deleteNodes(
					(NodeToken) rule.getDescriptor().getToken(1),
					rule.getData()
			);
		} else if ("N-R-!N".equals(pattern)) {
			deleteRelationships(
					(NodeToken) rule.getDescriptor().getToken(0),
					(RelationshipToken) rule.getDescriptor().getToken(2),
					(NodeToken) rule.getDescriptor().getToken(5),
					rule.getData()
			);
		} else if ("!R".equals(pattern)) {
			deleteRelationships(
					(RelationshipToken) rule.getDescriptor().getToken(1),
					rule.getData()
			);
		} else if ("N^I".equals(pattern)) {
			includeIndexEntries(
					(NodeToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("R^I".equals(pattern)) {
			includeIndexEntries(
					(RelationshipToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("N'I".equals(pattern)) {
			excludeIndexEntries(
					(NodeToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("R'I".equals(pattern)) {
			excludeIndexEntries(
					(RelationshipToken) rule.getDescriptor().getToken(0),
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
	public List<Node> createOrUpdateNodes(NodeToken a, Map<String, Object> properties) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		if (nodeStore.contains(a)) {
			nodes.addAll(nodeStore.get(a));
		} else {
			Node node = this.graphDB.createNode();
			nodeStore.put(a, node);
			nodes.add(node);
		}
		setProperties(nodes, properties);
		return nodes;
	}

	private List<Relationship> createRelationships(
			NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties
	)
			throws RuleApplicationException
	{
		if (!r.hasType()) {
			throw new RuleApplicationException(this.ruleNumber, "Cannot create untyped relationships");
		}
		Node startNode = createOrUpdateNodes(a, null).get(0);
		Node endNode = createOrUpdateNodes(b, null).get(0);
		Relationship relationship = startNode.createRelationshipTo(endNode, DynamicRelationshipType.withName(r.getType()));
		setProperties(relationship, properties);
		relationshipStore.put(r, relationship);
		ArrayList<Relationship> relationships = new ArrayList<Relationship>();
		relationships.add(relationship);
		return relationships;
	}

	private List<Relationship> updateRelationships(
			NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties
	)
	{
		ArrayList<Relationship> relationships = new ArrayList<Relationship>();
		if (isIncorrectlyTyped(r, 0)) {
			return new ArrayList<Relationship>();
		}
		Relationship relationship = relationshipStore.get(r).get(0);
		if (nodeStore.contains(a)) {
			if (!areEqual(nodeStore.get(a).get(0), relationship.getStartNode())) {
				// start node mismatch
				return new ArrayList<Relationship>();
			}
		}
		if (nodeStore.contains(b)) {
			if (!areEqual(nodeStore.get(b).get(0), relationship.getEndNode())) {
				// end node mismatch
				return new ArrayList<Relationship>();
			}
		}
		nodeStore.put(a, relationship.getStartNode());
		nodeStore.put(b, relationship.getEndNode());
		setProperties(relationship, properties);
		relationshipStore.put(r, relationship);
		relationships.add(relationship);
		return relationships;
	}

	private List<Relationship> reflectRelationships(
			NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties
	)
	{
		List<Relationship> relationships;
		if (r.hasType()) {
			relationships = match(a, b, DynamicRelationshipType.withName(r.getType()));
		} else {
			relationships = match(a, b);
		}
		setProperties(relationships, properties);
		relationshipStore.put(r, relationships);
		return relationships;
	}

	/**
	 * Create or update relationships
	 *
	 * @param a start node token
	 * @param r relationship token
	 * @param b end node token
	 * @param properties properties to be assigned to the relationships
	 * @return the Relationships
	 * @throws RuleApplicationException if an attempt is made to create an untyped relationship
	 */
	public List<Relationship> createOrUpdateRelationships(
			NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties
	)
			throws RuleApplicationException
	{
		if (relationshipStore.contains(r)) {
			return updateRelationships(a, r, b, properties);
		} else {
			return createRelationships(a, r, b, properties);
		}
	}

	/**
	 * Create or update relationships
	 *
	 * @param r relationship token
	 * @param properties properties to be assigned to the relationships
	 * @return the Relationships
	 * @throws RuleApplicationException if an attempt is made to create an untyped relationship
	 */
	public List<Relationship> createOrUpdateRelationships(
			RelationshipToken r, Map<String, Object> properties
	)
			throws RuleApplicationException
	{
		return createOrUpdateRelationships(new NodeToken(""), r, new NodeToken(""), properties);
	}

	/**
	 * Reflect or update relationships
	 *
	 * @param a start node token
	 * @param r relationship token
	 * @param b end node token
	 * @param properties properties to be assigned to the relationships
	 * @return the Relationships
	 */
	public List<Relationship> reflectOrUpdateRelationships(
			NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties
	)
	{
		if (relationshipStore.contains(r)) {
			return updateRelationships(a, r, b, properties);
		} else {
			return reflectRelationships(a, r, b, properties);
		}
	}

	/**
	 * Delete specific node
	 *
	 * @param a node token
	 * @param properties
	 * @throws RuleApplicationException if node is undefined
	 */
	public void deleteNodes(NodeToken a, Map<String, Object> properties)
			throws RuleApplicationException
	{
		failIfNotDefined(a, "Cannot exclude undefined node");
		for (Node node : nodeStore.remove(a)) {
			node.delete();
		}
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
	public void deleteRelationships(NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties)
			throws RuleApplicationException {
		List<Relationship> relationships;
		if (relationshipStore.contains(r)) {
			relationships = relationshipStore.remove(r);
		} else if (r.hasType()) {
			relationships = match(a, b, DynamicRelationshipType.withName(r.getType()));
		} else {
			relationships = match(a, b);
		}
		for (Relationship relationship : relationships) {
			relationship.delete();
		}
	}

	/**
	 * Delete specific relationship
	 * 
	 * @param r relationship token
	 * @param properties
	 * @throws RuleApplicationException if type is invalid
	 */
	public void deleteRelationships(RelationshipToken r, Map<String, Object> properties)
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
	public void includeIndexEntries(NodeToken a, IndexToken i, Map<String, Object> keyValuePairs)
			throws RuleApplicationException {
		failIfNotNamed(i, "Index must be named");
		Index<Node> index = this.graphDB.index().forNodes(i.getName());
		for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (nodeStore.contains(a)) {
				Node node = nodeStore.get(a).get(0);
				index.putIfAbsent(node, key, value);
			} else {
				Node node;
				IndexHits<Node> hits = index.get(key, value);
				if (hits.size() == 0) {
					node = this.graphDB.createNode();
					index.putIfAbsent(node, key, value);
				} else {
					node = hits.getSingle();
				}
				nodeStore.put(a, node);
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
	public void includeIndexEntries(RelationshipToken r, IndexToken i, Map<String, Object> keyValuePairs)
			throws RuleApplicationException {
		failIfNotNamed(i, "Index must be named");
		if (isIncorrectlyTyped(r, 0)) {
			return;
		}
		Index<Relationship> index = this.graphDB.index().forRelationships(i.getName());
		for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (relationshipStore.contains(r)) {
				Relationship relationship = relationshipStore.get(r).get(0);
				index.putIfAbsent(relationship, key, value);
			} else if (r.hasType()) {
				RelationshipType type = DynamicRelationshipType.withName(r.getType());
				Relationship relationship = null;
				IndexHits<Relationship> hits = index.get(key, value);
				for (Relationship hit : hits) {
					if (hit.isType(type)) {
						relationship = hit;
						break;
					}
				}
				failIfNull(relationship, String.format("No index entries found of type %s", r.getType()));
				relationshipStore.put(r, relationship);
			} else {
				Relationship relationship = null;
				IndexHits<Relationship> hits = index.get(key, value);
				for (Relationship hit : hits) {
					relationship = hit;
					break;
				}
				failIfNull(relationship, "No index entries found");
				relationshipStore.put(r, relationship);
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
	 * @throws RuleApplicationException
	 */
	public void excludeIndexEntries(NodeToken a, IndexToken i, Map<String, Object> keyValuePairs)
			throws RuleApplicationException {
		failIfNotNamed(i, "Index must be named");
		Index<Node> index = this.graphDB.index().forNodes(i.getName());
		if (nodeStore.contains(a)) {
			Node node = nodeStore.get(a).get(0);
			for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
				index.remove(node, entry.getKey(), entry.getValue());
			}
		} else {
			for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				IndexHits<Node> hits = index.get(key, value);
				for (Node hit : hits) {
					nodeStore.put(a, hit);
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
	 * @throws RuleApplicationException
	 */
	public void excludeIndexEntries(RelationshipToken r, IndexToken i, Map<String, Object> keyValuePairs)
			throws RuleApplicationException {
		failIfNotNamed(i, "Index must be named");
		if (isIncorrectlyTyped(r, 0)) {
			return;
		}
		Index<Relationship> index = this.graphDB.index().forRelationships(i.getName());
		if (relationshipStore.contains(r)) {
			Relationship relationship = relationshipStore.get(r).get(0);
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
						relationshipStore.put(r, hit);
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
					relationshipStore.put(r, hit);
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
		if (!nodeStore.contains(node)) {
			throw new RuleApplicationException(this.ruleNumber, message);
		}
	}

	private boolean areEqual(Node node1, Node node2) {
		return node1.getId() == node2.getId();
	}

	private boolean isIncorrectlyTyped(RelationshipToken relationshipToken, int index) {
		return
				relationshipStore.contains(relationshipToken) &&
				relationshipToken.hasType() &&
				!relationshipStore.get(relationshipToken).get(index).isType(DynamicRelationshipType.withName(relationshipToken.getType()));
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
		if (nodeStore.contains(a)) {
			Node start = nodeStore.get(a).get(0);
			if (nodeStore.contains(b)) {
				Node end = nodeStore.get(b).get(0);
				for (Relationship candidate : start.getRelationships(Direction.OUTGOING)) {
					if (areEqual(candidate.getEndNode(), end)) {
						matches.add(candidate);
					}
				}
			} else {
				for (Relationship candidate : start.getRelationships(Direction.OUTGOING)) {
					matches.add(candidate);
				}
			}
		} else if (nodeStore.contains(b)) {
			Node end = nodeStore.get(b).get(0);
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
		if (nodeStore.contains(a)) {
			Node start = nodeStore.get(a).get(0);
			if (nodeStore.contains(b)) {
				Node end = nodeStore.get(b).get(0);
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
		} else if (nodeStore.contains(b)) {
			Node end = nodeStore.get(b).get(0);
			for (Relationship candidate : end.getRelationships(Direction.INCOMING, type)) {
				matches.add(candidate);
			}
		}
		return matches;
	}

	private void setProperties(List<? extends PropertyContainer> entities, Map<String, Object> properties) {
		if (properties != null) {
			for (PropertyContainer entity : entities) {
				setProperties(entity, properties);
			}
		}
	}
	
	private void setProperties(PropertyContainer entity, Map<String, Object> properties) {
		if (properties != null) {
			int count = 0;
			for(String key : entity.getPropertyKeys()) {
				entity.removeProperty(key);
				count++;
			}
			if (count > 0) {
				if (entity instanceof Node) {
					info.add(String.format("%d properties removed from node %d", count, ((Node) entity).getId()));
				} else if (entity instanceof Relationship) {
					info.add(String.format("%d properties removed from relationship %d", count, ((Relationship) entity).getId()));
				}
			}
			for(Map.Entry<String, Object> entry : properties.entrySet()) {
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
			if (entity instanceof Node) {
				info.add(String.format("%d properties set on node %d", properties.size(), ((Node) entity).getId()));
			} else if (entity instanceof Relationship) {
				info.add(String.format("%d properties set on relationship %d", properties.size(), ((Relationship) entity).getId()));
			}

		}
	}

}
