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
	private final HashMap<String, Node> nodes = new HashMap<String, Node>();
	private final HashMap<String, Relationship> relationships = new HashMap<String, Relationship>();
	private final HashMap<String, PropertyContainer> entities = new HashMap<String, PropertyContainer>();

	private int ruleNumber = 0;

	/**
	 * Set up a new Namespace attached to the supplied GraphDatabaseService
	 *
	 * @param graphDB the database in which to store items
	 * @param params   set of pre-existing Nodes and Relationships accessible within this namespace
	 */
	Neo4jNamespace(GraphDatabaseService graphDB, Map<String, ? extends PropertyContainer> params) {
		this.graphDB = graphDB;
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
					register(key, (Node) param.getValue());
				} else if (param.getValue() instanceof Relationship && (isRelKey || isUntypedKey)) {
					register(key, (Relationship) param.getValue());
				} else {
					throw new IllegalArgumentException(String.format("Illegal parameter '%s':%s ", key, param.getValue().getClass().getName()));
				}
			}
		}
	}

	public int getRuleNumber() {
		return this.ruleNumber;
	}

	public Map<String, PropertyContainer> getEntities() {
		return this.entities;
	}

	@Override
	public void apply(Rule rule) throws DependencyException, IllegalRuleException, VampiricException {
		this.ruleNumber++;
		if (GEOFF.DEBUG) System.out.println(String.format("Applying rule #%d: %s", this.ruleNumber, rule));
		String pattern = rule.getDescriptor().getPattern();
		if ("N".equals(pattern)) {
			includeNode(
					(NodeToken) rule.getDescriptor().getToken(0),
					rule.getData()
			);
		} else if ("R".equals(pattern)) {
			includeRelationshipByName(
					(RelToken) rule.getDescriptor().getToken(0),
					rule.getData()
			);
		} else if ("N-R->N".equals(pattern)) {
			includeRelationshipByType(
					(NodeToken) rule.getDescriptor().getToken(0),
					(RelToken) rule.getDescriptor().getToken(2),
					(NodeToken) rule.getDescriptor().getToken(5),
					rule.getData(),
					false
			);
		} else if ("N-R->>N".equals(pattern)) {
			includeRelationshipByType(
					(NodeToken) rule.getDescriptor().getToken(0),
					(RelToken) rule.getDescriptor().getToken(2),
					(NodeToken) rule.getDescriptor().getToken(6),
					rule.getData(),
					true
			);
		} else if ("N^I".equals(pattern)) {
			includeNodeIndexEntry(
					(NodeToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("R^I".equals(pattern)) {
			includeRelationshipIndexEntry(
					(RelToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("!N".equals(pattern)) {
			excludeNode(
					(NodeToken) rule.getDescriptor().getToken(1),
					rule.getData()
			);
		} else if ("!R".equals(pattern)) {
			excludeRelationshipByName(
					(RelToken) rule.getDescriptor().getToken(1),
					rule.getData()
			);
		} else if ("N-R-!N".equals(pattern)) {
			excludeRelationshipByType(
					(NodeToken) rule.getDescriptor().getToken(0),
					(RelToken) rule.getDescriptor().getToken(2),
					(NodeToken) rule.getDescriptor().getToken(5),
					rule.getData()
			);
		} else if ("N'I".equals(pattern)) {
			excludeNodeIndexEntry(
					(NodeToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("R'I".equals(pattern)) {
			excludeRelationshipIndexEntry(
					(RelToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("N=N-R->N".equals(pattern)) {
			reflectNodeFromRelationship(
					(NodeToken) rule.getDescriptor().getToken(0),
					(NodeToken) rule.getDescriptor().getToken(2),
					(RelToken) rule.getDescriptor().getToken(4),
					(NodeToken) rule.getDescriptor().getToken(7),
					rule.getData()
			);
		} else if ("R=N-R->N".equals(pattern)) {
			reflectRelationshipFromRelationship(
					(RelToken) rule.getDescriptor().getToken(0),
					(NodeToken) rule.getDescriptor().getToken(2),
					(RelToken) rule.getDescriptor().getToken(4),
					(NodeToken) rule.getDescriptor().getToken(7),
					rule.getData()
			);
		} else if ("N=I".equals(pattern)) {
			reflectNodeFromIndexEntry(
					(NodeToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("R=I".equals(pattern)) {
			reflectRelationshipFromIndexEntry(
					(RelToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else {
			throw new IllegalRuleException(this.ruleNumber, "Rule cannot be identified: " + rule.toString());
		}
	}

	@Override
	public void apply(Iterable<Rule> rules) throws DependencyException, IllegalRuleException, VampiricException {
		if (GEOFF.DEBUG) System.out.println("Applying multiple rules");
		for (Rule rule : rules) {
			apply(rule);
		}
	}


	/* START OF INCLUSION RULE HANDLERS */

	// N
	private void includeNode(NodeToken node, Map<String, Object> data)
			throws IllegalRuleException {
		failIfNotAllNamed(node);
		if (this.nodes.containsKey(node.getName())) {
			Node n = this.nodes.get(node.getName());
			removeProperties(n);
			addProperties(n, data);
		} else {
			Node n = this.graphDB.createNode();
			register(node.getName(), n);
			addProperties(n, data);
		}
	}

	// R
	private void includeRelationshipByName(RelToken rel, Map<String, Object> data) throws DependencyException, IllegalRuleException {
		failIfNotAllNamed(rel);
		failIfTyped(rel);
		failIfNotRegistered(rel);
		Relationship r = this.relationships.get(rel.getName());
		removeProperties(r);
		addProperties(r, data);
	}

	// N-R->N
	// N-R->>N
	// behaviour varies depending on what exists
	private void includeRelationshipByType(NodeToken startNode, RelToken rel, NodeToken endNode, Map<String, Object> data, boolean append)
			throws DependencyException, IllegalRuleException {
		failIfNotAllNamed(startNode, endNode);
		if (rel.hasName()) {
			failIfRegistered(rel);
		}
		failIfNotTyped(rel);
		Node n1 = getOrCreate(startNode);
		Node n2 = getOrCreate(endNode);
		RelationshipType type = DynamicRelationshipType.withName(rel.getType());
		List<Relationship> rels = getRelationships(n1, type, n2);
		Relationship r;
		if (rels.isEmpty() || append) {
			r = n1.createRelationshipTo(n2, type);
		} else {
			r = rels.get(0);
		}
		addProperties(r, data);
		if (rel.hasName()) {
			register(rel.getName(), r);
		}
	}

	// N^I
	// behaviour depends on what already exists
	private void includeNodeIndexEntry(NodeToken node, IndexToken index, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		failIfNotAllNamed(node, index);
		boolean nodeExists = this.nodes.containsKey(node.getName());
		Index<Node> i = this.graphDB.index().forNodes(index.getName());
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			IndexHits<Node> hits = i.get(key, value);
			if (nodeExists && hits.size() == 0) {
				// node exists, index entry doesn't - add entry to index
				Node n1 = this.nodes.get(node.getName());
				i.add(n1, key, value);
			} else if (nodeExists) {
				// both exist - test if they match
				Node n1 = this.nodes.get(node.getName());
				Node n2 = hits.getSingle();
				if (n1.getId() == n2.getId()) {
					// same - no need to do anything
				} else {
					// different - add new index entry anyway
					i.add(n1, key, value);
				}
			} else if (hits.size() == 0) {
				// neither exist - create node and add to index
				Node n1 = this.graphDB.createNode();
				register(node.getName(), n1);
				i.add(n1, key, value);
			} else {
				// index entry exists, node doesn't - reflect entry into node
				register(node.getName(), hits.getSingle());
			}
		}
	}

	// R^I
	private void includeRelationshipIndexEntry(RelToken rel, IndexToken index, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		failIfNotAllNamed(rel, index);
		failIfTyped(rel);
		failIfNotRegistered(rel);
		Relationship r = this.relationships.get(rel.getName());
		Index<Relationship> i = this.graphDB.index().forRelationships(index.getName());
		for(Map.Entry<String, Object> entry : data.entrySet()) {
			i.add(r, entry.getKey(), entry.getValue());
		}
	}

	/* END OF INCLUSION RULE HANDLERS */


	/* START OF EXCLUSION RULE HANDLERS */

	// !N
	private void excludeNode(NodeToken node, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		failIfNotAllNamed(node);
		failIfNotRegistered(node);
		failIfNotEmpty(data);
		unregisterNode(node.getName()).delete();
	}

	// !R
	private void excludeRelationshipByName(RelToken rel, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		failIfNotAllNamed(rel);
		failIfTyped(rel);
		failIfNotRegistered(rel);
		failIfNotEmpty(data);
		unregisterRelationship(rel.getName()).delete();
	}

	// N-R-!N
	private void excludeRelationshipByType(NodeToken startNode, RelToken rel, NodeToken endNode, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		failIfNotAtLeastOneNamed(startNode, endNode);
		if (startNode.hasName()) {
			failIfNotRegistered(startNode);
		}
		if (endNode.hasName()) {
			failIfNotRegistered(endNode);
		}
		failIfNamed(rel);
		failIfNotTyped(rel);
		failIfNotEmpty(data);
		DynamicRelationshipType t = DynamicRelationshipType.withName(rel.getType());
		if (startNode.hasName() && endNode.hasName()) {
			// (A)-[:T]-!(B)
			Node s = this.nodes.get(startNode.getName());
			Node e = this.nodes.get(endNode.getName());
			for (Relationship r : s.getRelationships(Direction.OUTGOING, t)) {
				if (r.getEndNode().getId() == e.getId()) {
					r.delete();
				}
			}
		} else if (startNode.hasName()) {
			// (A)-[:T]-!()
			Node s = this.nodes.get(startNode.getName());
			for (Relationship r : s.getRelationships(Direction.OUTGOING, t)) {
				r.delete();
			}
		} else {
			// ()-[:T]-!(B)
			Node e = this.nodes.get(endNode.getName());
			for (Relationship r : e.getRelationships(Direction.INCOMING, t)) {
				r.delete();
			}
		}
	}

	// N'I
	private void excludeNodeIndexEntry(NodeToken node, IndexToken index, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		failIfNotAllNamed(node, index);
		failIfNotRegistered(node);
		Node n = this.nodes.get(node.getName());
		Index<Node> i = this.graphDB.index().forNodes(index.getName());
		for(Map.Entry<String, Object> entry : data.entrySet()) {
			i.remove(n, entry.getKey(), entry.getValue());
		}
	}

	// R'I
	private void excludeRelationshipIndexEntry(RelToken rel, IndexToken index, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		failIfNotAllNamed(rel, index);
		failIfTyped(rel);
		failIfNotRegistered(rel);
		Relationship r = this.relationships.get(rel.getName());
		Index<Relationship> i = this.graphDB.index().forRelationships(index.getName());
		for(Map.Entry<String, Object> entry : data.entrySet()) {
			i.remove(r, entry.getKey(), entry.getValue());
		}
	}

	/* END OF EXCLUSION RULE HANDLERS */


	/* START OF REFLECTION RULE HANDLERS */

	/*
	 * N=N-R->N
	 * ========
	 * # A reflects start node of rel R
	 * (A):=(*)-[R]->()
	 * # B reflects end node of rel R
	 * (B):=()-[R]->(*)
	 *
	 */
	private void reflectNodeFromRelationship(NodeToken intoNode, NodeToken startNode, RelToken rel, NodeToken endNode, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		failIfNotAllNamed(intoNode, rel);
		failIfNotExactlyOneStarred(startNode, endNode);
		failIfRegistered(intoNode);
		failIfNamed(startNode, endNode);
		failIfNotRegistered(rel);
		failIfNotEmpty(data);
		Relationship r = this.relationships.get(rel.getName());
		if (startNode.isStarred()) {
			register(intoNode.getName(), r.getStartNode());
		} else {
			register(intoNode.getName(), r.getEndNode());
		}
	}

	/*
	 * R=N-R->N
	 * ========
	 * # R reflects rel of type T between A and B (redundant? - could use (A)-[R:T]->(B) where R does not exist)
	 * [R]:=(A)-[:T]->(B)
	 * # R reflects rel of type T starting at node A
	 * [R]:=(A)-[:T]->()
	 * # R reflects rel of type T ending at node B
	 * [R]:=()-[:T]->(B)
	 *
	 */
	private void reflectRelationshipFromRelationship(RelToken intoRel, NodeToken startNode, RelToken rel, NodeToken endNode, Map<String, Object> data)
			throws DependencyException, IllegalRuleException, VampiricException {
		failIfNotAllNamed(intoRel);
		failIfRegistered(intoRel);
		failIfNotAtLeastOneNamed(startNode, endNode);
		if (startNode.hasName()) {
			failIfNotRegistered(startNode);
		}
		if (endNode.hasName()) {
			failIfNotRegistered(endNode);
		}
		failIfNamed(rel);
		failIfNotTyped(rel);
		failIfNotEmpty(data);
		DynamicRelationshipType t = DynamicRelationshipType.withName(rel.getType());
		if (startNode.hasName() && endNode.hasName()) {
			// [R]:=(A)-[:T]->(B)
			Node s = this.nodes.get(startNode.getName());
			Node e = this.nodes.get(endNode.getName());
			List<Relationship> rels = getRelationships(s, t, e);
			if (rels.size() == 0) {
				throw new VampiricException(this.ruleNumber, "No relationship to reflect");
			} else {
				register(intoRel.getName(), rels.get(0));
			}
		} else if (startNode.hasName()) {
			// [R]:=(A)-[:T]->()
			Node s = this.nodes.get(startNode.getName());
			Relationship r = s.getSingleRelationship(t, Direction.OUTGOING);
			if (r == null) {
				throw new VampiricException(this.ruleNumber, "No relationship to reflect");
			} else {
				register(intoRel.getName(), r);
			}
		} else {
			// [R]:=()-[:T]->(B)
			Node e = this.nodes.get(endNode.getName());
			Relationship r = e.getSingleRelationship(t, Direction.INCOMING);
			if (r == null) {
				throw new VampiricException(this.ruleNumber, "No relationship to reflect");
			} else {
				register(intoRel.getName(), r);
			}
		}
	}

	/*
	 * N=I
	 * ===
	 * # node A reflects entry in index I (redundant? could use (A)<=|I| where (A) does not exist and index entry does)
	 * (A):=|I|
	 *
	 */
	private void reflectNodeFromIndexEntry(NodeToken node, IndexToken index, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		failIfNotAllNamed(node, index);
		failIfRegistered(node);
		failIfNotExactlyOneEntry(data);
		Index<Node> i = this.graphDB.index().forNodes(index.getName());
		IndexHits<Node> hits = null;
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			hits = i.get(entry.getKey(), entry.getValue());
		}
		if (hits == null || hits.size() == 0) {
			// no index entry found so create node and add to index
			Node n = this.graphDB.createNode();
			register(node.getName(), n);
			for(Map.Entry<String, Object> entry : data.entrySet()) {
				i.add(n, entry.getKey(), entry.getValue());
			}
		} else {
			register(node.getName(), hits.getSingle());
		}
	}

	/*
	 * R=I
	 * ===
	 * # rel R reflects entry in index I
	 * [R]:=|I|
	 *
	 */
	private void reflectRelationshipFromIndexEntry(RelToken rel, IndexToken index, Map<String, Object> data)
			throws DependencyException, IllegalRuleException, VampiricException {
		failIfNotAllNamed(rel, index);
		failIfTyped(rel);
		failIfRegistered(rel);
		failIfNotExactlyOneEntry(data);
		Index<Relationship> i = this.graphDB.index().forRelationships(index.getName());
		IndexHits<Relationship> hits = null;
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			hits = i.get(entry.getKey(), entry.getValue());
		}
		if (hits == null || hits.size() == 0) {
			throw new VampiricException(this.ruleNumber, "No index entry to reflect");
		} else {
			register(rel.getName(), hits.getSingle());
		}
	}

	/* END OF REFLECTION RULE HANDLERS */


	/* START OF RULE FORMAT VALIDATORS */

	private void failIfNotAllNamed(NameableToken... tokens) throws IllegalRuleException {
		for (NameableToken token : tokens) {
			if (!token.hasName()) {
				throw new IllegalRuleException(this.ruleNumber, "All entities must have a name");
			}
		}
	}

	private void failIfNamed(NameableToken... tokens) throws IllegalRuleException {
		for (NameableToken token : tokens) {
			if (token.hasName()) {
				throw new IllegalRuleException(this.ruleNumber, "Entities cannot have a name");
			}
		}
	}

	private void failIfNotAtLeastOneNamed(NameableToken... tokens) throws IllegalRuleException {
		for (NameableToken nameable : tokens) {
			if (nameable.hasName()) {
				return;
			}
		}
		throw new IllegalRuleException(this.ruleNumber, "At least one entity must have a name");
	}

	private void failIfNotExactlyOneStarred(NodeToken... tokens) throws IllegalRuleException {
		int starCount = 0;
		for (NameableToken nameable : tokens) {
			if (nameable.isStarred()) {
				starCount++;
			}
		}
		if (starCount != 1) {
			throw new IllegalRuleException(this.ruleNumber, "Exactly one node must be starred");
		}
	}

	private void failIfNotTyped(RelToken relToken) throws IllegalRuleException {
		if (!relToken.hasType()) {
			throw new IllegalRuleException(this.ruleNumber, "Relationship must have a type: " + relToken.toString());
		}
	}

	private void failIfTyped(RelToken relToken) throws IllegalRuleException {
		if (relToken.hasType()) {
			throw new IllegalRuleException(this.ruleNumber, "Relationship cannot have a type: " + relToken.toString());
		}
	}

	private void failIfNotEmpty(Map map) throws IllegalRuleException {
		if (!map.isEmpty()) {
			throw new IllegalRuleException(this.ruleNumber, "Data cannot be supplied with this rule");
		}
	}

	private void failIfNotExactlyOneEntry(Map map) throws IllegalRuleException {
		if (map.size() != 1) {
			throw new IllegalRuleException(this.ruleNumber, "Map must contain exactly one key:value pair");
		}
	}

	/* END OF RULE FORMAT VALIDATORS */


	/* START OF DEPENDENCY VALIDATORS */

	private void failIfNotRegistered(NodeToken... nodes) throws DependencyException {
		for (NodeToken node : nodes) {
			if (!this.nodes.containsKey(node.getName())) {
				throw new DependencyException(this.ruleNumber, "Node not found: " + node.toString());
			}
		}
	}

	private void failIfRegistered(NodeToken node) throws DependencyException {
		if (this.nodes.containsKey(node.getName())) {
			throw new DependencyException(this.ruleNumber, "Node already exists: " + node.toString());
		}
	}

	private void failIfNotRegistered(RelToken rel) throws DependencyException {
		if (!this.relationships.containsKey(rel.getName())) {
			throw new DependencyException(this.ruleNumber, "Relationship not found: " + rel.toString());
		}
	}

	private void failIfRegistered(RelToken rel) throws DependencyException {
		if (this.relationships.containsKey(rel.getName())) {
			throw new DependencyException(this.ruleNumber, "Relationship already exists: " + rel.toString());
		}
	}

	/* END OF DEPENDENCY VALIDATORS */

	
	private Node getOrCreate(NodeToken node) {
		Node n;
		if (this.nodes.containsKey(node.getName())) {
			n = this.nodes.get(node.getName());
		} else {
			n = this.graphDB.createNode();
			register(node.getName(), n);
		}
		return n;
	}

	private List<Relationship> getRelationships(Node startNode, RelationshipType type, Node endNode) {
		ArrayList<Relationship> rels = new ArrayList<Relationship>();
		for (Relationship rel : startNode.getRelationships(Direction.OUTGOING, type)) {
			if (rel.getEndNode().getId() == endNode.getId()) {
				rels.add(rel);
			}
		}
		return rels;
	}
	
	private void register(String name, Node node) {
		if (GEOFF.DEBUG) {
			System.out.println("Registering node " + node.getId() + " as (" + name + ")");
		}
		this.nodes.put(name, node);
		this.entities.put("(" + name + ")", node);
	}

	private Node unregisterNode(String name) {
		Node node = this.nodes.get(name);
		this.nodes.remove(name);
		this.entities.remove("(" + name + ")");
		return node;
	}

	private void register(String name, Relationship relationship) {
		if (GEOFF.DEBUG) {
			System.out.println("Registering relationship " + relationship.getId() + " as [" + name + "]");
		}
		this.relationships.put(name, relationship);
		this.entities.put("[" + name + "]", relationship);
	}

	private Relationship unregisterRelationship(String name) {
		Relationship rel = this.relationships.get(name);
		this.relationships.remove(name);
		this.entities.remove("[" + name + "]");
		return rel;
	}

	private void addProperties(PropertyContainer entity, Map<String, Object> data) {
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

	private void removeProperties(PropertyContainer entity) {
		for(String key : entity.getPropertyKeys()) {
			entity.removeProperty(key);
		}
	}

}
