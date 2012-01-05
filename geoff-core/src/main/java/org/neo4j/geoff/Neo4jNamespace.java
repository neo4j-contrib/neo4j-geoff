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
	 * @param params   set of pre-existing Nodes and Relationships accessible within this namespace
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
	public void apply(Rule rule) throws RuleFormatException, RuleApplicationException {
		this.ruleNumber++;
		if (GEOFF.DEBUG) System.out.println(String.format("Applying rule #%d: %s", this.ruleNumber, rule));
		String pattern = rule.getDescriptor().getPattern();
		if ("N".equals(pattern)) {
			applyNodeInclusionRule(
					(NodeToken) rule.getDescriptor().getToken(0),
					rule.getData()
			);
		} else if ("R".equals(pattern)) {
			applySimpleRelationshipInclusionRule(
					(RelToken) rule.getDescriptor().getToken(0),
					rule.getData()
			);
		} else if ("N-R->N".equals(pattern)) {
			applyFullRelationshipInclusionRule(
					(NodeToken) rule.getDescriptor().getToken(0),
					(RelToken) rule.getDescriptor().getToken(2),
					(NodeToken) rule.getDescriptor().getToken(5),
					rule.getData(),
					false
			);
		} else if ("N-R->>N".equals(pattern)) {
			applyFullRelationshipInclusionRule(
					(NodeToken) rule.getDescriptor().getToken(0),
					(RelToken) rule.getDescriptor().getToken(2),
					(NodeToken) rule.getDescriptor().getToken(6),
					rule.getData(),
					true
			);
		} else if ("N^I".equals(pattern)) {
			applyNodeIndexInclusionRule(
					(NodeToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("R^I".equals(pattern)) {
			applyRelationshipIndexInclusionRule(
					(RelToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("!N".equals(pattern)) {
			applyNodeExclusionRule(
					(NodeToken) rule.getDescriptor().getToken(1),
					rule.getData()
			);
		} else if ("!R".equals(pattern)) {
			applySimpleRelationshipExclusionRule(
					(RelToken) rule.getDescriptor().getToken(1),
					rule.getData()
			);
		} else if ("N-R-!N".equals(pattern)) {
			applyFullRelationshipExclusionRule(
					(NodeToken) rule.getDescriptor().getToken(0),
					(RelToken) rule.getDescriptor().getToken(2),
					(NodeToken) rule.getDescriptor().getToken(5),
					rule.getData()
			);
		} else if ("N'I".equals(pattern)) {
			applyNodeIndexExclusionRule(
					(NodeToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("R'I".equals(pattern)) {
			applyRelationshipIndexExclusionRule(
					(RelToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else {
			throw new RuleFormatException(this.ruleNumber, "Rule cannot be identified: " + rule.toString());
		}
	}

	@Override
	public void apply(Iterable<Rule> rules) throws RuleFormatException, RuleApplicationException {
		if (GEOFF.DEBUG) System.out.println("Applying multiple rules");
		for (Rule rule : rules) {
			apply(rule);
		}
	}

	/**
	 * Node Inclusion Rule
	 * ===================
	 *
	 * (A) {...}
	 *
	 * @param a node token
	 * @param data
	 * @return
	 * @throws RuleFormatException
	 * @throws RuleApplicationException
	 */
	private Node applyNodeInclusionRule(NodeToken a, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		Node node;
		if (store.isDefined(a)) {
			node = store.get(a);
		} else {
			node = this.graphDB.createNode();
			store.define(a, node);
		}
		setProperties(node, data);
		return node;
	}

	/**
	 * Simple Relationship Inclusion Rule
	 * ==================================
	 *
	 * [R]   {...}
	 * [R:T] {...}
	 *
	 * @param r relationship token
	 * @param data
	 * @throws RuleFormatException
	 * @throws RuleApplicationException
	 */
	private void applySimpleRelationshipInclusionRule(RelToken r, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfIncorrectlyTyped(r);
		if (store.isDefined(r)) {
			setProperties(store.get(r), data);
		} else {
			fail("Illegal rule format");
		}
	}

	/**
	 * Full Relationship Inclusion Rule
	 * ================================
	 *
	 * (A)-[R]->(B)   {...}
	 * (A)-[:T]->(B)  {...}
	 * (A)-[:T]->>(B) {...}
	 * (A)-[R:T]->(B) {...}
	 *
	 * @param a start node token
	 * @param r relationship token
	 * @param b end node token
	 * @param data
	 * @param append
	 * @throws RuleFormatException
	 * @throws RuleApplicationException
	 */
	private void applyFullRelationshipInclusionRule(NodeToken a, RelToken r, NodeToken b, Map<String, Object> data, boolean append)
			throws RuleFormatException, RuleApplicationException {
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
			setProperties(relationship, data);
		} else if (r.hasType()) {
			RelationshipType type = DynamicRelationshipType.withName(r.getType());
			List<Relationship> relationships = match(a, b, type);
			if (relationships.isEmpty() || append) {
				Node startNode = applyNodeInclusionRule(a, null);
				Node endNode = applyNodeInclusionRule(b, null);
				relationship = startNode.createRelationshipTo(endNode, type);
				setProperties(relationship, data);
			} else {
				setProperties(relationships, data);
				relationship = relationships.get(0);
			}
		} else {
			List<Relationship> relationships = match(a, b);
			if (relationships.isEmpty() || append) {
				fail("Cannot create untyped relationship");
			}
			setProperties(relationships, data);
			relationship = relationships.get(0);
		}
		store.define(r, relationship);
		store.define(a, relationship.getStartNode());
		store.define(b, relationship.getEndNode());
	}

	/**
	 * Node Index Inclusion Rule
	 * =========================
	 *
	 * (A)<=|I| {...}
	 *
	 * @param a node token
	 * @param i index token
	 * @param data
	 * @throws RuleFormatException
	 * @throws RuleApplicationException
	 */
	private void applyNodeIndexInclusionRule(NodeToken a, IndexToken i, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotAllNamed(i);
		Index<Node> index = this.graphDB.index().forNodes(i.getName());
		for (Map.Entry<String, Object> entry : data.entrySet()) {
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
	 * Relationship Index Inclusion Rule
	 * =================================
	 *
	 * [R]<=|I|   {...}
	 * [R:T]<=|I| {...}
	 *
	 * @param r relationship token
	 * @param i index token
	 * @param data
	 * @throws RuleFormatException
	 * @throws RuleApplicationException
	 */
	private void applyRelationshipIndexInclusionRule(RelToken r, IndexToken i, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotAllNamed(i);
		failIfIncorrectlyTyped(r);
		Index<Relationship> index = this.graphDB.index().forRelationships(i.getName());
		for (Map.Entry<String, Object> entry : data.entrySet()) {
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
				for (Relationship hit : hits) {
					store.define(r, hit);
				}
				failIfNotDefined(r, "No index entries found");
			}
		}
	}

	/**
	 * Node Exclusion Rule
	 * ===================
	 *
	 * !(A)
	 *
	 * @param a node token
	 * @param data
	 * @throws RuleFormatException
	 * @throws RuleApplicationException
	 */
	private void applyNodeExclusionRule(NodeToken a, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotDefined(a, "Cannot exclude undefined node");
		store.undefine(a).delete();
	}

	/**
	 * Simple Relationship Exclusion Rule
	 * ==================================
	 *
	 * ![R]
	 * ![R:T]
	 *
	 * @param r
	 * @param data
	 * @throws RuleFormatException
	 * @throws RuleApplicationException
	 */
	private void applySimpleRelationshipExclusionRule(RelToken r, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotDefined(r, "Cannot exclude undefined relationship");
		failIfIncorrectlyTyped(r);
		store.undefine(r).delete();
	}

	// N-R-!N
	// TODO: optimisation
	private void applyFullRelationshipExclusionRule(NodeToken a, RelToken r, NodeToken b, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotAtLeastOneNamed(a, b);
		failIfNamed(r);
		failIfNotTyped(r);
		failIfNotEmpty(data);
		if (a.hasName()) {
			failIfNotDefined(a, "");
		}
		if (b.hasName()) {
			failIfNotDefined(b, "");
		}
		failIfIncorrectlyTyped(r);
		DynamicRelationshipType t = DynamicRelationshipType.withName(r.getType());
		if (a.hasName() && b.hasName()) {
			// (A)-[:T]-!(B)
			Node startNode = store.get(a);
			Node endNode = store.get(b);
			for (Relationship relationship : startNode.getRelationships(Direction.OUTGOING, t)) {
				if (relationship.getEndNode().getId() == endNode.getId()) {
					relationship.delete();
				}
			}
		} else if (a.hasName()) {
			// (A)-[:T]-!()
			Node s = store.get(a);
			for (Relationship relationship : s.getRelationships(Direction.OUTGOING, t)) {
				relationship.delete();
			}
		} else {
			// ()-[:T]-!(B)
			Node e = store.get(b);
			for (Relationship relationship : e.getRelationships(Direction.INCOMING, t)) {
				relationship.delete();
			}
		}
	}

	/**
	 * Node Index Exclusion Rule
	 * =========================
	 *
	 * (A)!=|I| {...}
	 *
	 * @param a node token
	 * @param i index token
	 * @param data
	 * @throws RuleFormatException
	 * @throws RuleApplicationException
	 */
	private void applyNodeIndexExclusionRule(NodeToken a, IndexToken i, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotAllNamed(i);
		Index<Node> index = this.graphDB.index().forNodes(i.getName());
		if (store.isDefined(a)) {
			Node node = store.get(a);
			for (Map.Entry<String, Object> entry : data.entrySet()) {
				index.remove(node, entry.getKey(), entry.getValue());
			}
		} else {
			for (Map.Entry<String, Object> entry : data.entrySet()) {
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

	// R'I

	/**
	 * Relationship Index Exclusion Rule
	 * =================================
	 *
	 * [R]!=|I|   {...}
	 * [R:T]!=|I| {...}
	 *
	 * @param r relationship token
	 * @param i index token
	 * @param data
	 * @throws RuleFormatException
	 * @throws RuleApplicationException
	 */
	private void applyRelationshipIndexExclusionRule(RelToken r, IndexToken i, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotAllNamed(i);
		failIfIncorrectlyTyped(r);
		Index<Relationship> index = this.graphDB.index().forRelationships(i.getName());
		if (store.isDefined(r)) {
			Relationship relationship = store.get(r);
			for (Map.Entry<String, Object> entry : data.entrySet()) {
				index.remove(relationship, entry.getKey(), entry.getValue());
			}
		} else if (r.hasType()) {
			RelationshipType type = DynamicRelationshipType.withName(r.getType());
			for (Map.Entry<String, Object> entry : data.entrySet()) {
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
			for (Map.Entry<String, Object> entry : data.entrySet()) {
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


	/* START OF RULE FORMAT VALIDATORS */

	private void failIfNotAllNamed(NameableToken... tokens) throws RuleFormatException {
		for (NameableToken token : tokens) {
			if (!token.hasName()) {
				throw new RuleFormatException(this.ruleNumber, "All entities must have a name");
			}
		}
	}

	private void failIfNamed(NameableToken... tokens) throws RuleFormatException {
		for (NameableToken token : tokens) {
			if (token.hasName()) {
				throw new RuleFormatException(this.ruleNumber, "Entities cannot have a name");
			}
		}
	}

	private void failIfNotAtLeastOneNamed(NameableToken... tokens) throws RuleFormatException {
		for (NameableToken nameable : tokens) {
			if (nameable.hasName()) {
				return;
			}
		}
		throw new RuleFormatException(this.ruleNumber, "At least one entity must have a name");
	}

	private void failIfNotTyped(RelToken relToken) throws RuleFormatException {
		if (!relToken.hasType()) {
			throw new RuleFormatException(this.ruleNumber, "Relationship must have a type: " + relToken.toString());
		}
	}

	private void failIfTyped(RelToken relToken) throws RuleFormatException {
		if (relToken.hasType()) {
			throw new RuleFormatException(this.ruleNumber, "Relationship cannot have a type: " + relToken.toString());
		}
	}

	private void failIfNotEmpty(Map map) throws RuleFormatException {
		if (map != null && !map.isEmpty()) {
			throw new RuleFormatException(this.ruleNumber, "Data cannot be supplied with this rule");
		}
	}

	/* END OF RULE FORMAT VALIDATORS */


	/* START OF RULE APPLICATION VALIDATORS */

	private void failIfNotDefined(NodeToken node, String message) throws RuleApplicationException {
		if (!store.isDefined(node)) {
			throw new RuleApplicationException(this.ruleNumber, message);
		}
	}

	private void failIfNotDefined(RelToken rel, String message) throws RuleApplicationException {
		if (!store.isDefined(rel)) {
			throw new RuleApplicationException(this.ruleNumber, message);
		}
	}

	private void failIfIncorrectlyTyped(RelToken rel) throws RuleApplicationException {
		if (store.stateOf(rel) == RelState.DEFINED_AND_INCORRECTLY_TYPED) {
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
	
	private void fail(String message) throws RuleApplicationException {
		throw new RuleApplicationException(this.ruleNumber, message);
	}
	
	/* END OF DEPENDENCY VALIDATORS */

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

	private void setProperties(Iterable<? extends PropertyContainer> entities, Map<String, Object> data) {
		if (data != null) {
			for(PropertyContainer entity : entities) {
				setProperties(entity, data);
			}
		}
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
