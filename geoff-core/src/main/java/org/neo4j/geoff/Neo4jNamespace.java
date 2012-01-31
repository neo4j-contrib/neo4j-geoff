/**
 * Copyright (c) 2002-2012 "Neo Technology,"
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
import org.neo4j.geoff.store.EntityStore;
import org.neo4j.geoff.store.IndexToken;
import org.neo4j.geoff.store.NodeToken;
import org.neo4j.geoff.store.RelationshipToken;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import java.lang.reflect.Array;
import java.util.*;

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
	 * @param params  set of pre-existing Nodes and Relationships accessible within this namespace
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
	public void apply(Iterable<Rule> rules)
	throws RuleApplicationException
	{
		if (Geoff.DEBUG) System.out.println("Applying multiple rules");
		for (Rule rule : rules) {
			apply(rule);
		}
	}

	/**
	 * Create or update node
	 *
	 * @param a          node token
	 * @param properties properties to be assigned to the node
	 * @return the Node
	 */
	public Set<Node> createOrUpdateNodes(NodeToken a, Map<String, Object> properties)
	{
		HashSet<Node> nodes = new HashSet<Node>();
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

	// will be called iff r is undefined
	private Set<Relationship> createRelationships(NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties)
	throws RuleApplicationException
	{
		if (!r.hasType()) {
			throw new RuleApplicationException(this.ruleNumber, "Cannot create untyped relationships");
		}
		RelationshipType type = DynamicRelationshipType.withName(r.getType());
		HashSet<Relationship> relationships = new HashSet<Relationship>();
		Set<Node> startNodes = createOrUpdateNodes(a, null);
		Set<Node> endNodes = createOrUpdateNodes(b, null);
		for (Node startNode : startNodes) {
			for (Node endNode : endNodes) {
				relationships.add(startNode.createRelationshipTo(endNode, type));
			}
		}
		setProperties(relationships, properties);
		relationshipStore.put(r, relationships);
		return relationships;
	}

	// will be called iff r is defined
	private Set<Relationship> updateRelationships(NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties)
	{
		Set<Relationship> relationships = relationshipStore.get(r);
		boolean aIsDefined = nodeStore.contains(a);
		boolean bIsDefined = nodeStore.contains(b);
		Set<Node> startNodes = aIsDefined ? nodeStore.get(a) : new HashSet<Node>(relationships.size());
		Set<Node> endNodes = bIsDefined ? nodeStore.get(b) : new HashSet<Node>(relationships.size());
		RelationshipType type = r.hasType() ? DynamicRelationshipType.withName(r.getType()) : null;
		Iterator<Relationship> relationshipIterator = relationships.iterator();
		while (relationshipIterator.hasNext()) {
			Relationship relationship = relationshipIterator.next();
			Node startNode = relationship.getStartNode();
			Node endNode = relationship.getEndNode();
			if (type == null || relationship.isType(type)) {
				if ((aIsDefined && !startNodes.contains(startNode)) || (bIsDefined && !endNodes.contains(endNode))) {
					relationshipIterator.remove();                // start or end node mismatch
				} else {
					if (!aIsDefined) startNodes.add(startNode);   // reflect start node
					if (!bIsDefined) endNodes.add(endNode);       // reflect end node
					setProperties(relationship, properties);
				}
			} else {
				relationshipIterator.remove();                    // type mismatch
			}
		}
		nodeStore.put(a, startNodes);
		nodeStore.put(b, endNodes);
		return relationships;
	}

	// will be called iff r is undefined
	private Set<Relationship> reflectRelationships(NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties)
	{
		Set<Relationship> relationships;
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
	 * @param a          start node token
	 * @param r          relationship token
	 * @param b          end node token
	 * @param properties properties to be assigned to the relationships
	 * @return the Relationships
	 * @throws RuleApplicationException if an attempt is made to create an untyped relationship
	 */
	public Set<Relationship> createOrUpdateRelationships(NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties)
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
	 * @param r          relationship token
	 * @param properties properties to be assigned to the relationships
	 * @return the Relationships
	 * @throws RuleApplicationException if an attempt is made to create an untyped relationship
	 */
	public Set<Relationship> createOrUpdateRelationships(RelationshipToken r, Map<String, Object> properties)
	throws RuleApplicationException
	{
		return createOrUpdateRelationships(NodeToken.anon(), r, NodeToken.anon(), properties);
	}

	/**
	 * Reflect or update relationships
	 *
	 * @param a          start node token
	 * @param r          relationship token
	 * @param b          end node token
	 * @param properties properties to be assigned to the relationships
	 * @return the Relationships
	 */
	public Set<Relationship> reflectOrUpdateRelationships(NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties)
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
	 * @param a          node token
	 * @param properties
	 */
	public void deleteNodes(NodeToken a, Map<String, Object> properties)
	{
		for (Node node : nodeStore.remove(a)) {
			node.delete();
		}
	}

	/**
	 * Delete one or more relationships
	 *
	 * @param a          start node token
	 * @param r          relationship token
	 * @param b          end node token
	 * @param properties
	 */
	public void deleteRelationships(NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties) {
		Set<Relationship> relationships;
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
	 * @param r          relationship token
	 * @param properties
	 */
	public void deleteRelationships(RelationshipToken r, Map<String, Object> properties)
	{
		deleteRelationships(NodeToken.anon(), r, NodeToken.anon(), properties);
	}

	private void assertIndexHasName(IndexToken token)
	throws RuleApplicationException
	{
		if (!token.hasName()) {
			throw new RuleApplicationException(this.ruleNumber, "Index must be named");
		}
	}

	/**
	 * Ensure entry is included within node index
	 *
	 * @param a             node token
	 * @param i             index token
	 * @param keyValuePairs the key:value pairs against which to create index entries
	 * @throws RuleApplicationException if index is not named
	 */
	public void includeIndexEntries(NodeToken a, IndexToken i, Map<String, Object> keyValuePairs)
	throws RuleApplicationException {
		assertIndexHasName(i);
		Index<Node> index = this.graphDB.index().forNodes(i.getName());
		boolean aIsDefined = nodeStore.contains(a);
		HashSet<Node> nodes = aIsDefined ? new HashSet<Node>(nodeStore.get(a)) : new HashSet<Node>(keyValuePairs.size());
		for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (aIsDefined) {
				for (Node node : nodes) {
					index.putIfAbsent(node, key, value);
				}
			} else {
				IndexHits<Node> hits = index.get(key, value);
				if (hits.size() == 0) {
					Node node = this.graphDB.createNode();
					index.putIfAbsent(node, key, value);
					nodes.add(node);
				} else {
					for (Node node : hits) {
						nodes.add(node);
					}
				}
			}
		}
		nodeStore.put(a, nodes);
	}

	/**
	 * Ensure entry is included within relationship index
	 *
	 * @param r             relationship token
	 * @param i             index token
	 * @param keyValuePairs
	 * @throws RuleApplicationException
	 */
	public void includeIndexEntries(RelationshipToken r, IndexToken i, Map<String, Object> keyValuePairs)
	throws RuleApplicationException
	{
		assertIndexHasName(i);
		Index<Relationship> index = this.graphDB.index().forRelationships(i.getName());
		RelationshipType type = r.hasType() ? DynamicRelationshipType.withName(r.getType()) : null;
		boolean rIsDefined = relationshipStore.contains(r);
		HashSet<Relationship> relationships = rIsDefined ? new HashSet<Relationship>(relationshipStore.get(r)) : new HashSet<Relationship>(keyValuePairs.size());
		for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (rIsDefined) {
				for (Relationship relationship : relationships) {
					if (type == null || relationship.isType(type)) {
						index.putIfAbsent(relationship, key, value);
					}
				}
			} else if (r.hasType()) {
				IndexHits<Relationship> hits = index.get(key, value);
				if (hits.size() == 0) {
					for (Relationship relationship : createRelationships(NodeToken.anon(), RelationshipToken.anon(r.getType()), NodeToken.anon(), null)) {
						index.putIfAbsent(relationship, key, value);
						relationships.add(relationship);
					}
				} else {
					for (Relationship relationship : hits) {
						if (relationship.isType(type)) {
							relationships.add(relationship);
						}
					}
				}
			} else {
				IndexHits<Relationship> hits = index.get(key, value);
				for (Relationship relationship : hits) {
					relationships.add(relationship);
				}
			}
		}
		relationshipStore.put(r, relationships);
	}

	/**
	 * Exclude Node Index Entry
	 * ========================
	 *
	 * (A)!=|I| {...}
	 *
	 * @param a             node token
	 * @param i             index token
	 * @param keyValuePairs
	 * @throws RuleApplicationException
	 */
	public void excludeIndexEntries(NodeToken a, IndexToken i, Map<String, Object> keyValuePairs)
	throws RuleApplicationException
	{
		assertIndexHasName(i);
		Index<Node> index = this.graphDB.index().forNodes(i.getName());
		boolean aIsDefined = nodeStore.contains(a);
		HashSet<Node> nodes = aIsDefined ? new HashSet<Node>(nodeStore.get(a)) : new HashSet<Node>(keyValuePairs.size());
		if (aIsDefined) {
			for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
				for(Node node : nodes) {
					index.remove(node, entry.getKey(), entry.getValue());
				}
			}
		} else {
			for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				IndexHits<Node> hits = index.get(key, value);
				for (Node node : hits) {
					nodes.add(node);
					index.remove(node, key, value);
				}
			}
		}
		nodeStore.put(a, nodes);
	}

	/**
	 * Exclude Relationship Index Entry
	 * ================================
	 *
	 * [R]!=|I|   {...}
	 * [R:T]!=|I| {...}
	 *
	 * @param r             relationship token
	 * @param i             index token
	 * @param keyValuePairs
	 * @throws RuleApplicationException
	 */
	public void excludeIndexEntries(RelationshipToken r, IndexToken i, Map<String, Object> keyValuePairs)
	throws RuleApplicationException
	{
		assertIndexHasName(i);
		Index<Relationship> index = this.graphDB.index().forRelationships(i.getName());
		RelationshipType type = r.hasType() ? DynamicRelationshipType.withName(r.getType()) : null;
		boolean rIsDefined = relationshipStore.contains(r);
		HashSet<Relationship> relationships = rIsDefined ? new HashSet<Relationship>(relationshipStore.get(r)) : new HashSet<Relationship>(keyValuePairs.size());
		if (rIsDefined) {
			for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
				for (Relationship relationship : relationships) {
					if (type == null || relationship.isType(type)) {
						index.remove(relationship, entry.getKey(), entry.getValue());
					}
				}
			}
		} else if (r.hasType()) {
			for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				IndexHits<Relationship> hits = index.get(key, value);
				for (Relationship relationship : hits) {
					if (relationship.isType(type)) {
						relationships.add(relationship);
						index.remove(relationship, key, value);
					}
				}
			}
		} else {
			for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				IndexHits<Relationship> hits = index.get(key, value);
				for (Relationship relationship : hits) {
					relationships.add(relationship);
					index.remove(relationship, key, value);
				}
			}
		}
		relationshipStore.put(r, relationships);
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
	private Set<Relationship> match(NodeToken a, NodeToken b) {
		final HashSet<Relationship> matches = new HashSet<Relationship>();
		if (nodeStore.contains(a)) {
			Set<Node> startNodes = nodeStore.get(a);
			if (nodeStore.contains(b)) {
				Set<Node> endNodes = nodeStore.get(b);
				for (Node startNode : startNodes) {
					for (Relationship candidate : startNode.getRelationships(Direction.OUTGOING)) {
						if (endNodes.contains(candidate.getEndNode())) {
							matches.add(candidate);
						}
					}
				}
			} else {
				for (Node startNode : startNodes) {
					for (Relationship candidate : startNode.getRelationships(Direction.OUTGOING)) {
						matches.add(candidate);
					}
				}
			}
		} else if (nodeStore.contains(b)) {
			Set<Node> endNodes = nodeStore.get(b);
			for (Node endNode : endNodes) {
				for (Relationship candidate : endNode.getRelationships(Direction.INCOMING)) {
					matches.add(candidate);
				}
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
	 * @param a    start node token
	 * @param b    end node token
	 * @param type type of matching relationships
	 * @return list of matching relationships
	 */
	private Set<Relationship> match(NodeToken a, NodeToken b, RelationshipType type) {
		final HashSet<Relationship> matches = new HashSet<Relationship>();
		if (nodeStore.contains(a)) {
			Set<Node> startNodes = nodeStore.get(a);
			if (nodeStore.contains(b)) {
				Set<Node> endNodes = nodeStore.get(b);
				for (Node startNode : startNodes) {
					for (Relationship candidate : startNode.getRelationships(Direction.OUTGOING, type)) {
						if (endNodes.contains(candidate.getEndNode())) {
							matches.add(candidate);
						}
					}
				}
			} else {
				for (Node startNode : startNodes) {
					for (Relationship candidate : startNode.getRelationships(Direction.OUTGOING, type)) {
						matches.add(candidate);
					}
				}
			}
		} else if (nodeStore.contains(b)) {
			Set<Node> endNodes = nodeStore.get(b);
			for (Node endNode : endNodes) {
				for (Relationship candidate : endNode.getRelationships(Direction.INCOMING, type)) {
					matches.add(candidate);
				}
			}
		}
		return matches;
	}

	private void setProperties(Set<? extends PropertyContainer> entities, Map<String, Object> properties) {
		if (properties != null) {
			for (PropertyContainer entity : entities) {
				setProperties(entity, properties);
			}
		}
	}

	private void setProperties(PropertyContainer entity, Map<String, Object> properties) {
		if (properties != null) {
			int count = 0;
			for (String key : entity.getPropertyKeys()) {
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
			for (Map.Entry<String, Object> entry : properties.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value == null) {
					continue;
				}
				if (value instanceof List) {
					try {
						List listValue = (List) value;
						if (listValue.isEmpty()) {
							continue;
						} else if (listValue.get(0) instanceof Boolean) {
							boolean[] values = new boolean[listValue.size()];
							for (int i = 0; i < values.length; i++) {
								values[i] = (Boolean) listValue.get(i);
							}
							value = values;
						} else if (listValue.get(0) instanceof Integer) {
							int[] values = new int[listValue.size()];
							for (int i = 0; i < values.length; i++) {
								values[i] = (Integer) listValue.get(i);
							}
							value = values;
						} else if (listValue.get(0) instanceof Double) {
							double[] values = new double[listValue.size()];
							for (int i = 0; i < values.length; i++) {
								values[i] = (Double) listValue.get(i);
							}
							value = values;
						} else if (listValue.get(0) instanceof String) {
							String[] values = new String[listValue.size()];
							for (int i = 0; i < values.length; i++) {
								values[i] = listValue.get(i).toString();
							}
							value = values;
						} else {
							throw new IllegalArgumentException("Illegal property type: " + value.getClass().getName());
						}
					} catch (ClassCastException ex) {
						throw new IllegalArgumentException("Illegal combination of list item types");
					}
				}
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
