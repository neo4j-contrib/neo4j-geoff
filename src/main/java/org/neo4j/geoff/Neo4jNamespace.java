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

import java.util.*;

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
				if (param.getValue() instanceof Node) {
					register(param.getKey(), (Node) param.getValue());
				} else if (param.getValue() instanceof Relationship) {
					register(param.getKey(), (Relationship) param.getValue());
				} else {
					// unexpected param type! should never happen :-)
					throw new IllegalArgumentException("Unexpected parameter type: " + param.getClass());
				}
			}
		}
	}

	public Map<String, PropertyContainer> getEntities() {
		return this.entities;
	}

	@Override
	public void apply(Rule rule) throws DependencyException, IllegalRuleException {
		if (GEOFF.DEBUG) {
			System.out.println("Applying rule: " + rule.toString());
		}
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
					rule.getData()
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
			throw new IllegalRuleException("Rule cannot be identified: " + rule.toString());
		}
	}

	public void apply(RuleSet rules) throws DependencyException, IllegalRuleException {
		if (GEOFF.DEBUG) {
			System.out.println("Applying set of " + rules.length() + " rules");
		}
		ArrayList<Rule> rulesToApply = new ArrayList<Rule>(
				Arrays.asList(rules.getRules().toArray(new Rule[rules.length()]))
		);
		int numberOfRulesToApply = rulesToApply.size();
		// cycle through remaining rules until all are applied (if possible)
		while(numberOfRulesToApply > 0) {
			Iterator<Rule> iterator = rulesToApply.iterator();
			while(iterator.hasNext()) {
				try {
					apply(iterator.next());
					iterator.remove();
				} catch(DependencyException e) {
					// continue, leaving this rule for next cycle
				}
			}
			if (numberOfRulesToApply == rulesToApply.size()) {
				// admit defeat: dependencies cannot be satisfied
				throw new DependencyException("Unresolvable dependencies in rule set");
			} else {
				numberOfRulesToApply = rulesToApply.size();
			}
		}
	}


	/* START OF INCLUSION RULE HANDLERS */

	// N
	private void includeNode(NodeToken node, Map<String, Object> data)
			throws IllegalRuleException {
		assertNamed(node);
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
		assertNamed(rel);
		assertNotTyped(rel);
		assertRegistered(rel);
		Relationship r = this.relationships.get(rel.getName());
		removeProperties(r);
		addProperties(r, data);
	}

	// N-R->N
	private void includeRelationshipByType(NodeToken startNode, RelToken rel, NodeToken endNode, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		if (GEOFF.DEBUG) {
			System.out.println("Including relationship by type \"N-R->N\"");
		}
		assertNamed(startNode, endNode);
		assertRegistered(startNode, endNode);
		if (rel.hasName()) {
			assertNotRegistered(rel);
		}
		assertTyped(rel);
		Relationship r = this.nodes.get(startNode.getName()).createRelationshipTo(
				this.nodes.get(endNode.getName()),
				DynamicRelationshipType.withName(rel.getType())
		);
		if (rel.hasName()) {
			register(rel.getName(), r);
		}
		addProperties(r, data);
	}

	// N^I
	private void includeNodeIndexEntry(NodeToken node, IndexToken index, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		assertNamed(node, index);
		assertRegistered(node);
		Node n = this.nodes.get(node.getName());
		Index<Node> i = this.graphDB.index().forNodes(index.getName());
		for(Map.Entry<String, Object> entry : data.entrySet()) {
			i.add(n, entry.getKey(), entry.getValue());
		}
	}

	// R^I
	private void includeRelationshipIndexEntry(RelToken rel, IndexToken index, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		assertNamed(rel, index);
		assertNotTyped(rel);
		assertRegistered(rel);
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
		assertNamed(node);
		assertRegistered(node);
		assertIsEmpty(data);
		unregisterNode(node.getName()).delete();
	}

	// !R
	private void excludeRelationshipByName(RelToken rel, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		assertNamed(rel);
		assertNotTyped(rel);
		assertRegistered(rel);
		assertIsEmpty(data);
		unregisterRelationship(rel.getName()).delete();
	}

	// N-R-!N
	private void excludeRelationshipByType(NodeToken startNode, RelToken rel, NodeToken endNode, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		assertAtLeastOneNamed(startNode, endNode);
		if (startNode.hasName()) {
			assertRegistered(startNode);
		}
		if (endNode.hasName()) {
			assertRegistered(endNode);
		}
		assertNotNamed(rel);
		assertTyped(rel);
		assertIsEmpty(data);
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
		assertNamed(node, index);
		assertRegistered(node);
		Node n = this.nodes.get(node.getName());
		Index<Node> i = this.graphDB.index().forNodes(index.getName());
		for(Map.Entry<String, Object> entry : data.entrySet()) {
			i.remove(n, entry.getKey(), entry.getValue());
		}
	}

	// R'I
	private void excludeRelationshipIndexEntry(RelToken rel, IndexToken index, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		assertNamed(rel, index);
		assertNotTyped(rel);
		assertRegistered(rel);
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
		assertNamed(intoNode, rel);
		assertExactlyOneStarred(startNode, endNode);
		assertNotRegistered(intoNode);
		assertNotNamed(startNode, endNode);
		assertRegistered(rel);
		assertIsEmpty(data);
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
	 * # R reflects rel of type T between A and B
	 * [R]:=(A)-[:T]->(B)
	 * # R reflects rel of type T starting at node A
	 * [R]:=(A)-[:T]->()
	 * # R reflects rel of type T ending at node B
	 * [R]:=()-[:T]->(B)
	 *
	 */
	private void reflectRelationshipFromRelationship(RelToken intoRel, NodeToken startNode, RelToken rel, NodeToken endNode, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		assertNamed(intoRel);
		assertNotRegistered(intoRel);
		assertAtLeastOneNamed(startNode, endNode);
		if (startNode.hasName()) {
			assertRegistered(startNode);
		}
		if (endNode.hasName()) {
			assertRegistered(endNode);
		}
		assertNotNamed(rel);
		assertTyped(rel);
		assertIsEmpty(data);
		DynamicRelationshipType t = DynamicRelationshipType.withName(rel.getType());
		if (startNode.hasName() && endNode.hasName()) {
			// [R]:=(A)-[:T]->(B)
			Node s = this.nodes.get(startNode.getName());
			Node e = this.nodes.get(endNode.getName());
			for (Relationship r : s.getRelationships(Direction.OUTGOING, t)) {
				if (r.getEndNode().getId() == e.getId()) {
					register(intoRel.getName(), r);
					return;
				}
			}
			throw new DependencyException("No relationship to reflect");
		} else if (startNode.hasName()) {
			// [R]:=(A)-[:T]->()
			Node s = this.nodes.get(startNode.getName());
			Relationship r = s.getSingleRelationship(t, Direction.OUTGOING);
			if (r == null) {
				throw new DependencyException("No relationship to reflect");
			} else {
				register(intoRel.getName(), r);
			}
		} else {
			// [R]:=()-[:T]->(B)
			Node e = this.nodes.get(endNode.getName());
			Relationship r = e.getSingleRelationship(t, Direction.INCOMING);
			if (r == null) {
				throw new DependencyException("No relationship to reflect");
			} else {
				register(intoRel.getName(), r);
			}
		}
	}

	/*
	 * N=I
	 * ===
	 * # node A reflects entry in index I
	 * (A):=|I|
	 *
	 */
	private void reflectNodeFromIndexEntry(NodeToken node, IndexToken index, Map<String, Object> data)
			throws DependencyException, IllegalRuleException {
		assertNamed(node, index);
		assertNotRegistered(node);
		assertHasExactlyOneEntry(data);
		Index<Node> i = this.graphDB.index().forNodes(index.getName());
		IndexHits<Node> hits = null;
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			hits = i.get(entry.getKey(), entry.getValue());
		}
		if (hits == null || hits.size() == 0) {
			throw new DependencyException("No index entry to reflect");
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
			throws DependencyException, IllegalRuleException {
		assertNamed(rel, index);
		assertNotTyped(rel);
		assertNotRegistered(rel);
		assertHasExactlyOneEntry(data);
		Index<Relationship> i = this.graphDB.index().forRelationships(index.getName());
		IndexHits<Relationship> hits = null;
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			hits = i.get(entry.getKey(), entry.getValue());
		}
		if (hits == null || hits.size() == 0) {
			throw new DependencyException("No index entry to reflect");
		} else {
			register(rel.getName(), hits.getSingle());
		}
	}

	/* END OF REFLECTION RULE HANDLERS */


	/* START OF RULE FORMAT VALIDATORS */

	private void assertNamed(NameableToken... tokens) throws IllegalRuleException {
		for (NameableToken token : tokens) {
			if (!token.hasName()) {
				throw new IllegalRuleException("All entities must have a name");
			}
		}
	}

	private void assertNotNamed(NameableToken... tokens) throws IllegalRuleException {
		for (NameableToken token : tokens) {
			if (token.hasName()) {
				throw new IllegalRuleException("Entities cannot have a name");
			}
		}
	}

	private void assertAtLeastOneNamed(NameableToken... tokens) throws IllegalRuleException {
		for (NameableToken nameable : tokens) {
			if (nameable.hasName()) {
				return;
			}
		}
		throw new IllegalRuleException("At least one entity must have a name");
	}

	private void assertExactlyOneStarred(NodeToken... tokens) throws IllegalRuleException {
		int starCount = 0;
		for (NameableToken nameable : tokens) {
			if (nameable.isStarred()) {
				starCount++;
			}
		}
		if (starCount != 1) {
			throw new IllegalRuleException("Exactly one node must be starred");
		}
	}

	private void assertTyped(RelToken relToken) throws IllegalRuleException {
		if (!relToken.hasType()) {
			throw new IllegalRuleException("Relationship must have a type: " + relToken.toString());
		}
	}

	private void assertNotTyped(RelToken relToken) throws IllegalRuleException {
		if (relToken.hasType()) {
			throw new IllegalRuleException("Relationship cannot have a type: " + relToken.toString());
		}
	}

	private void assertIsEmpty(Map map) throws IllegalRuleException {
		if (!map.isEmpty()) {
			throw new IllegalRuleException("Data cannot be supplied with this rule");
		}
	}

	private void assertHasExactlyOneEntry(Map map) throws IllegalRuleException {
		if (map.size() != 1) {
			throw new IllegalRuleException("Map must contain exactly one key:value pair");
		}
	}

	/* END OF RULE FORMAT VALIDATORS */


	/* START OF DEPENDENCY VALIDATORS */

	private void assertRegistered(NodeToken... nodes) throws DependencyException {
		for (NodeToken node : nodes) {
			if (!this.nodes.containsKey(node.getName())) {
				throw new DependencyException("Node not found: " + node.toString());
			}
		}
	}

	private void assertNotRegistered(NodeToken node) throws DependencyException {
		if (this.nodes.containsKey(node.getName())) {
			throw new DependencyException("Node already exists: " + node.toString());
		}
	}

	private void assertRegistered(RelToken rel) throws DependencyException {
		if (!this.relationships.containsKey(rel.getName())) {
			throw new DependencyException("Relationship not found: " + rel.toString());
		}
	}

	private void assertNotRegistered(RelToken rel) throws DependencyException {
		if (this.relationships.containsKey(rel.getName())) {
			throw new DependencyException("Relationship already exists: " + rel.toString());
		}
	}

	/* END OF DEPENDENCY VALIDATORS */


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
			entity.setProperty(entry.getKey(), entry.getValue());
		}
	}

	private void removeProperties(PropertyContainer entity) {
		for(String key : entity.getPropertyKeys()) {
			entity.removeProperty(key);
		}
	}

}
