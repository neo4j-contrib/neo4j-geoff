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

import org.neo4j.geoff.except.SubgraphError;
import org.neo4j.geoff.store.EntityStore;
import org.neo4j.geoff.store.IndexToken;
import org.neo4j.geoff.store.NodeToken;
import org.neo4j.geoff.store.RelationshipToken;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import java.util.*;

/**
 * Implementation of {@link GraphProxy} for use with Neo4j.
 *
 * @author Nigel Small
 */
public class Neo4jGraphProxy implements GraphProxy<PropertyContainer> {

	/**
	 * Used for ordering relationships by ID within a TreeSet
	 */
	private static class RelationshipComparator implements Comparator<Relationship> {

		@Override
		public int compare(Relationship rel1, Relationship rel2) {
			long id1 = rel1.getId();
			long id2 = rel2.getId();
			return id1 < id2 ? -1 : id1 > id2 ? 1 : 0;
		}

	}

	private final GraphDatabaseService graphDB;
	private final EntityStore<NodeToken, Node> nodeStore;
	private final EntityStore<RelationshipToken, Relationship> relationshipStore;

	private int ruleNumber = 0;

	/**
	 * Set up a new proxy for the supplied GraphDatabaseService
	 *
	 * @param graphDB the database in which to store items
	 */
	public Neo4jGraphProxy(GraphDatabaseService graphDB) {
		this.graphDB = graphDB;
		this.nodeStore = new EntityStore<NodeToken, Node>();
		this.relationshipStore = new EntityStore<RelationshipToken, Relationship>();
	}

	@Override
	public void inputParams(Map<String, PropertyContainer> params) {
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
					throw new IllegalArgumentException(String.format(
						"Illegal parameter '%s':%s ", key, param.getValue().getClass().getName()
					));
				}
			}
		}
	}

	@Override
	public Map<String, PropertyContainer> outputParams() {
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
	public void merge(Subgraph subgraph) throws SubgraphError {
		Transaction tx = graphDB.beginTx();
		try {
			for (Rule rule : subgraph) {
				this.ruleNumber++;
				String pattern = rule.getDescriptor().getPattern();
				if ("N".equals(pattern)) {
					createOrUpdateNodes(
						(NodeToken) rule.getDescriptor().getToken(0),
						rule.getData()
					);
				} else if ("R".equals(pattern)) {
					mergeRelationships(
						NodeToken.anon(),
						(RelationshipToken) rule.getDescriptor().getToken(0),
						NodeToken.anon(),
						rule.getData(),
						false
					);
				} else if ("N-R->N".equals(pattern)) {
					mergeRelationships(
						(NodeToken) rule.getDescriptor().getToken(0),
						(RelationshipToken) rule.getDescriptor().getToken(2),
						(NodeToken) rule.getDescriptor().getToken(5),
						rule.getData(),
						false
					);
				} else if ("N<-R-N".equals(pattern)) {
					mergeRelationships(
						(NodeToken) rule.getDescriptor().getToken(5),
						(RelationshipToken) rule.getDescriptor().getToken(3),
						(NodeToken) rule.getDescriptor().getToken(0),
						rule.getData(),
						false
					);
				} else if ("N<-R->N".equals(pattern)) {
					mergeRelationships(
						(NodeToken) rule.getDescriptor().getToken(0),
						(RelationshipToken) rule.getDescriptor().getToken(3),
						(NodeToken) rule.getDescriptor().getToken(6),
						rule.getData(),
						true
					);
				} else if ("N^I".equals(pattern)) {
					mergeIndexEntries(
						(NodeToken) rule.getDescriptor().getToken(0),
						(IndexToken) rule.getDescriptor().getToken(2),
						rule.getData()
					);
				} else if ("R^I".equals(pattern)) {
					mergeIndexEntries(
						(RelationshipToken) rule.getDescriptor().getToken(0),
						(IndexToken) rule.getDescriptor().getToken(2),
						rule.getData()
					);
				} else {
					throw new SubgraphError(this.ruleNumber, "Unknown rule: " + rule.toString());
				}
			}
			tx.success();
		} finally {
			tx.finish();
		}
	}

	@Override
	public void insert(Subgraph subgraph) throws SubgraphError {
		Transaction tx = graphDB.beginTx();
		try {
			for (Rule rule : subgraph) {
				this.ruleNumber++;
				String pattern = rule.getDescriptor().getPattern();
				if ("N".equals(pattern)) {
					createOrUpdateNodes(
						(NodeToken) rule.getDescriptor().getToken(0),
						rule.getData()
					);
				} else if ("R".equals(pattern)) {
					insertRelationships(
						NodeToken.anon(),
						(RelationshipToken) rule.getDescriptor().getToken(0),
						NodeToken.anon(),
						rule.getData(),
						false
					);
				} else if ("N-R->N".equals(pattern)) {
					insertRelationships(
						(NodeToken) rule.getDescriptor().getToken(0),
						(RelationshipToken) rule.getDescriptor().getToken(2),
						(NodeToken) rule.getDescriptor().getToken(5),
						rule.getData(),
						false
					);
				} else if ("N<-R-N".equals(pattern)) {
					insertRelationships(
						(NodeToken) rule.getDescriptor().getToken(5),
						(RelationshipToken) rule.getDescriptor().getToken(3),
						(NodeToken) rule.getDescriptor().getToken(0),
						rule.getData(),
						false
					);
				} else if ("N<-R->N".equals(pattern)) {
					insertRelationships(
						(NodeToken) rule.getDescriptor().getToken(0),
						(RelationshipToken) rule.getDescriptor().getToken(3),
						(NodeToken) rule.getDescriptor().getToken(6),
						rule.getData(),
						true
					);
				} else if ("N^I".equals(pattern)) {
					insertIndexEntries(
						(NodeToken) rule.getDescriptor().getToken(0),
						(IndexToken) rule.getDescriptor().getToken(2),
						rule.getData()
					);
				} else if ("R^I".equals(pattern)) {
					insertIndexEntries(
						(RelationshipToken) rule.getDescriptor().getToken(0),
						(IndexToken) rule.getDescriptor().getToken(2),
						rule.getData()
					);
				} else {
					throw new SubgraphError(this.ruleNumber, "Unknown rule: " + rule.toString());
				}
			}
			tx.success();
		} finally {
			tx.finish();
		}
	}

	@Override
	public void delete(Subgraph subgraph) throws SubgraphError {
		Transaction tx = graphDB.beginTx();
		try {
			for (Rule rule : subgraph.reverse()) {
				this.ruleNumber++;
				String pattern = rule.getDescriptor().getPattern();
				if ("N".equals(pattern)) {
					deleteNodes(
						(NodeToken) rule.getDescriptor().getToken(0),
						rule.getData()
					);
				} else if ("R".equals(pattern)) {
					deleteRelationships(
						NodeToken.anon(),
						(RelationshipToken) rule.getDescriptor().getToken(0),
						NodeToken.anon(),
						rule.getData(),
						false
					);
				} else if ("N-R->N".equals(pattern)) {
					deleteRelationships(
						(NodeToken) rule.getDescriptor().getToken(0),
						(RelationshipToken) rule.getDescriptor().getToken(2),
						(NodeToken) rule.getDescriptor().getToken(5),
						rule.getData(),
						false
					);
				} else if ("N<-R-N".equals(pattern)) {
					deleteRelationships(
						(NodeToken) rule.getDescriptor().getToken(5),
						(RelationshipToken) rule.getDescriptor().getToken(3),
						(NodeToken) rule.getDescriptor().getToken(0),
						rule.getData(),
						false
					);
				} else if ("N<-R->N".equals(pattern)) {
					deleteRelationships(
						(NodeToken) rule.getDescriptor().getToken(0),
						(RelationshipToken) rule.getDescriptor().getToken(3),
						(NodeToken) rule.getDescriptor().getToken(6),
						rule.getData(),
						true
					);
				} else if ("N^I".equals(pattern)) {
					deleteIndexEntries(
						(NodeToken) rule.getDescriptor().getToken(0),
						(IndexToken) rule.getDescriptor().getToken(2),
						rule.getData()
					);
				} else if ("R^I".equals(pattern)) {
					deleteIndexEntries(
						(RelationshipToken) rule.getDescriptor().getToken(0),
						(IndexToken) rule.getDescriptor().getToken(2),
						rule.getData()
					);
				} else {
					throw new SubgraphError(this.ruleNumber, "Unknown rule: " + rule.toString());
				}
			}
			tx.success();
		} finally {
			tx.finish();
		}
	}

	private Set<Node> createOrUpdateNodes(NodeToken a, Map<String, Object> properties)
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

	/**
	 * Create relationships described by the supplied tokens. Should only be
	 * called if the RelationshipToken <code>r</code> does not describe a
	 * pre-existing relationship.
	 *
	 * @param a start nodes
	 * @param r relationships
	 * @param b end nodes
	 * @param properties relationship properties
	 * @param bothWays bi-directional relationship
	 * @return
	 * @throws SubgraphError
	 */
	private Set<Relationship> createRelationships(NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties, boolean bothWays)
		throws SubgraphError
	{
		assert !relationshipStore.contains(r);
		if (!r.hasType()) {
			throw new SubgraphError(this.ruleNumber, "Cannot create untyped relationships");
		}
		RelationshipType type = DynamicRelationshipType.withName(r.getType());
		HashSet<Relationship> relationships = new HashSet<Relationship>();
		Set<Node> startNodes = createOrUpdateNodes(a, null);
		Set<Node> endNodes = createOrUpdateNodes(b, null);
		for (Node startNode : startNodes) {
			for (Node endNode : endNodes) {
				relationships.add(startNode.createRelationshipTo(endNode, type));
				if (bothWays) {
					relationships.add(endNode.createRelationshipTo(startNode, type));
				}
			}
		}
		setProperties(relationships, properties);
		relationshipStore.put(r, relationships);
		return relationships;
	}

	private Set<Relationship> updateRelationships(NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties, boolean bothWays)
	{
		assert relationshipStore.contains(r);
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
				// TODO: adapt mismatch checking for two-way relationships
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

	private void mergeRelationships(NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties, boolean bothWays)
		throws SubgraphError
	{
		if (relationshipStore.contains(r)) {
			updateRelationships(a, r, b, properties, bothWays);
		} else {
			TreeSet<Relationship> relationships;
			if (r.hasType()) {
				relationships = match(a, b, DynamicRelationshipType.withName(r.getType()));
				if (bothWays) {
					relationships.addAll(match(b, a, DynamicRelationshipType.withName(r.getType())));
				}
			} else {
				relationships = match(a, b);
				if (bothWays) {
					relationships.addAll(match(b, a));
				}
			}
			int index = r.getIndex();
			int currentIndex = 0;
			boolean found = false;
			HashSet<Node> startNodes = new HashSet<Node>();
			HashSet<Node> endNodes = new HashSet<Node>();
			for (Relationship relationship : relationships) {
				currentIndex++;
				if (index == 0 || index == currentIndex) {
					found = true;
					startNodes.add(relationship.getStartNode());
					endNodes.add(relationship.getEndNode());
					setProperties(relationship, properties);
				}
			}
			if (found) {
				this.nodeStore.put(a, startNodes);
				this.nodeStore.put(b, endNodes);
			} else {
				relationships.addAll(createRelationships(a, r, b, properties, bothWays));
			}
			relationshipStore.put(r, relationships);
		}
	}

	private void mergeIndexEntries(NodeToken a, IndexToken i, Map<String, Object> keyValuePairs)
	throws SubgraphError
	{
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

	private void mergeIndexEntries(RelationshipToken r, IndexToken i, Map<String, Object> keyValuePairs)
		throws SubgraphError
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
					for (Relationship relationship : createRelationships(NodeToken.anon(), RelationshipToken.anon(r.getType()), NodeToken.anon(), null, false)) {
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

	private Set<Relationship> insertRelationships(NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties, boolean bothWays)
		throws SubgraphError
	{
		if (relationshipStore.contains(r)) {
			return updateRelationships(a, r, b, properties, bothWays);
		} else {
			return createRelationships(a, r, b, properties, bothWays);
		}
	}

	private void insertIndexEntries(NodeToken a, IndexToken i, Map<String, Object> keyValuePairs)
		throws SubgraphError
	{
		assertIndexHasName(i);
		Index<Node> index = this.graphDB.index().forNodes(i.getName());
		boolean aIsDefined = nodeStore.contains(a);
		HashSet<Node> nodes = aIsDefined ? new HashSet<Node>(nodeStore.get(a)) : new HashSet<Node>(keyValuePairs.size());
		for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (aIsDefined) {
				for (Node node : nodes) {
					index.add(node, key, value);
				}
			} else {
				Node node = this.graphDB.createNode();
				index.add(node, key, value);
				nodes.add(node);
			}
		}
		nodeStore.put(a, nodes);
	}

	private void insertIndexEntries(RelationshipToken r, IndexToken i, Map<String, Object> keyValuePairs)
		throws SubgraphError
	{
		assertIndexHasName(i);
		Index<Relationship> index = this.graphDB.index().forRelationships(i.getName());
		Set<Relationship> relationships;
		if (relationshipStore.contains(r)) {
			relationships = relationshipStore.get(r);
		} else {
			relationships = createRelationships(NodeToken.anon(), r, NodeToken.anon(), null, false);
		}
		for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			for (Relationship relationship : relationships) {
				index.add(relationship, key, value);
			}
		}
		relationshipStore.put(r, relationships);
	}

	private void deleteNodes(NodeToken a, Map<String, Object> properties)
	{
		if (nodeStore.contains(a)) {
			for (Node node : nodeStore.remove(a)) {
				node.delete();
			}
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
	private void deleteRelationships(NodeToken a, RelationshipToken r, NodeToken b, Map<String, Object> properties, boolean bothWays) {
		Set<Relationship> relationships;
		if (relationshipStore.contains(r)) {
			relationships = relationshipStore.remove(r);
		} else if (r.hasType()) {
			relationships = match(a, b, DynamicRelationshipType.withName(r.getType()));
			if (bothWays) {
				relationships.addAll(match(b, a, DynamicRelationshipType.withName(r.getType())));
			}
		} else {
			relationships = match(a, b);
			if (bothWays) {
				relationships.addAll(match(b, a));
			}
		}
		TreeSet<Node> startNodes = new TreeSet<Node>();
		TreeSet<Node> endNodes = new TreeSet<Node>();
		for (Relationship relationship : relationships) {
			startNodes.add(relationship.getStartNode());
			endNodes.add(relationship.getEndNode());
			relationship.delete();
		}
		this.nodeStore.put(a, startNodes);
		this.nodeStore.put(b, endNodes);
	}

	private void deleteIndexEntries(NodeToken a, IndexToken i, Map<String, Object> keyValuePairs)
		throws SubgraphError
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

	private void deleteIndexEntries(RelationshipToken r, IndexToken i, Map<String, Object> keyValuePairs)
		throws SubgraphError
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

	private void assertIndexHasName(IndexToken token)
	throws SubgraphError
	{
		if (!token.hasName()) {
			throw new SubgraphError(this.ruleNumber, "Index must be named");
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
	private TreeSet<Relationship> match(NodeToken a, NodeToken b) {
		final TreeSet<Relationship> matches = new TreeSet<Relationship>(new RelationshipComparator());
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
	private TreeSet<Relationship> match(NodeToken a, NodeToken b, RelationshipType type) {
		final TreeSet<Relationship> matches = new TreeSet<Relationship>(new RelationshipComparator());
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

	/**
	 * Replace all entity properties on a collection of entities.
	 *
	 * @param entities the entities on which to replaces properties
	 * @param properties the new properties
	 */
	private void setProperties(Set<? extends PropertyContainer> entities, Map<String, Object> properties) {
		if (properties != null) {
			for (PropertyContainer entity : entities) {
				setProperties(entity, properties);
			}
		}
	}

	/**
	 * Replace all entity properties with a new set, as supplied.
	 *
	 * @param entity the entity on which to replace properties
	 * @param properties the new properties
	 */
	private void setProperties(PropertyContainer entity, Map<String, Object> properties) {
		if (properties != null) {
			for (String key : entity.getPropertyKeys()) {
				entity.removeProperty(key);
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
						throw new IllegalArgumentException("Illegal combination of list item types", ex);
					}
				}
				entity.setProperty(key, value);
			}
		}
	}

}
