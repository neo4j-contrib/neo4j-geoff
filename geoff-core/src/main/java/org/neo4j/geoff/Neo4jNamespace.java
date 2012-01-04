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
			handleNodeInclusionRule(
					(NodeToken) rule.getDescriptor().getToken(0),
					rule.getData()
			);
		} else if ("R".equals(pattern)) {
			handleSimpleRelationshipInclusionRule(
					(RelToken) rule.getDescriptor().getToken(0),
					rule.getData()
			);
		} else if ("N-R->N".equals(pattern)) {
			handleFullRelationshipInclusionRule(
					(NodeToken) rule.getDescriptor().getToken(0),
					(RelToken) rule.getDescriptor().getToken(2),
					(NodeToken) rule.getDescriptor().getToken(5),
					rule.getData(),
					false
			);
		} else if ("N-R->>N".equals(pattern)) {
			handleFullRelationshipInclusionRule(
					(NodeToken) rule.getDescriptor().getToken(0),
					(RelToken) rule.getDescriptor().getToken(2),
					(NodeToken) rule.getDescriptor().getToken(6),
					rule.getData(),
					true
			);
		} else if ("N^I".equals(pattern)) {
			handleNodeIndexInclusionRule(
					(NodeToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("R^I".equals(pattern)) {
			handleRelationshipIndexInclusionRule(
					(RelToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("!N".equals(pattern)) {
			handleNodeExclusionRule(
					(NodeToken) rule.getDescriptor().getToken(1),
					rule.getData()
			);
		} else if ("!R".equals(pattern)) {
			handleSimpleRelationshipExclusionRule(
					(RelToken) rule.getDescriptor().getToken(1),
					rule.getData()
			);
		} else if ("N-R-!N".equals(pattern)) {
			handleFullRelationshipExclusionRule(
					(NodeToken) rule.getDescriptor().getToken(0),
					(RelToken) rule.getDescriptor().getToken(2),
					(NodeToken) rule.getDescriptor().getToken(5),
					rule.getData()
			);
		} else if ("N'I".equals(pattern)) {
			handleNodeIndexExclusionRule(
					(NodeToken) rule.getDescriptor().getToken(0),
					(IndexToken) rule.getDescriptor().getToken(2),
					rule.getData()
			);
		} else if ("R'I".equals(pattern)) {
			handleRelationshipIndexExclusionRule(
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


	/* START OF INCLUSION RULE HANDLERS */

	// N
	private void handleNodeInclusionRule(NodeToken node, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotAllNamed(node);
		if (store.contains(node)) {
			Node n = store.get(node);
			setProperties(n, data);
		} else {
			Node n = this.graphDB.createNode();
			store.add(node, n);
			setProperties(n, data);
		}
	}

	// R
	private void handleSimpleRelationshipInclusionRule(RelToken rel, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotAllNamed(rel);
		failIfNotRegistered(rel);
		Relationship r = store.get(rel);
		if (!rel.hasType() || (rel.hasType() && r.isType(DynamicRelationshipType.withName(rel.getType())))) {
			setProperties(r, data);
		}
	}

	// N-R->N
	// N-R->>N
	private void handleFullRelationshipInclusionRule(NodeToken startNode, RelToken rel, NodeToken endNode, Map<String, Object> data, boolean append)
			throws RuleFormatException, RuleApplicationException {
		try {
			Relationship r;
			// match
			List<Relationship> matches = match(startNode, rel, endNode);
			if (matches.isEmpty() || append) {
				// create
				if (rel.hasType()) {
					Node n1 = getOrCreate(startNode);
					Node n2 = getOrCreate(endNode);
					RelationshipType type = DynamicRelationshipType.withName(rel.getType());
					r = n1.createRelationshipTo(n2, type);
				} else {
					throw new Mismatch("Cannot create typeless relationship");
				}
			} else {
				// select
				r = matches.get(0);
			}
			// update
			if (!data.isEmpty()) {
				setProperties(r, data);
			}
			// reflect
			if (startNode.hasName() && !store.contains(startNode)) {
				store.add(startNode, r.getStartNode());
			}
			if (endNode.hasName() && !store.contains(endNode)) {
				store.add(endNode, r.getEndNode());
			}
			if (rel.hasName() && !store.contains(rel)) {
				store.add(rel, r);
			}
		} catch (Mismatch e) {
			throw new RuleFormatException(this.ruleNumber, "Rule processing mismatch", e);
		}
	}

	// N^I
	private void handleNodeIndexInclusionRule(NodeToken node, IndexToken index, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotAllNamed(node, index);
		boolean nodeExists = store.contains(node);
		Index<Node> i = this.graphDB.index().forNodes(index.getName());
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			IndexHits<Node> hits = i.get(key, value);
			if (nodeExists) {
				Node n = store.get(node);
				boolean nodeIndexed = false;
				for (Node hit : hits) {
					nodeIndexed = nodeIndexed || (n.getId() == hit.getId());
				}
				if (!nodeIndexed) {
					i.add(n, key, value);
				}
			} else {
				Node n;
				if (hits.size() == 0) {
					n = this.graphDB.createNode();
					i.add(n, key, value);
				} else {
					n = hits.getSingle();
				}
				store.add(node, n);
			}
		}
	}

	// R^I
	private void handleRelationshipIndexInclusionRule(RelToken rel, IndexToken index, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotAllNamed(rel, index);
		try {
			RelationshipType type = null;
			if (rel.hasType()) {
				type = DynamicRelationshipType.withName(rel.getType());
			}
			boolean relExists = store.contains(rel);
			if (relExists && type != null && !store.get(rel).isType(type)) {
				throw new Mismatch(String.format("Relationship [%s] is not of type %s", rel.getName(), rel.getType()));
			}
			Index<Relationship> i = this.graphDB.index().forRelationships(index.getName());
			for (Map.Entry<String, Object> entry : data.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				IndexHits<Relationship> hits = i.get(key, value);
				if (relExists) {
					Relationship r = store.get(rel);
					boolean relIndexed = false;
					for (Relationship hit : hits) {
						relIndexed = relIndexed || (r.getId() == hit.getId());
					}
					if (!relIndexed) {
						i.add(r, key, value);
					}
				} else {
					if (hits.size() == 0) {
						throw new Mismatch("No index entries found");
					}
					Relationship r = null;
					for (Relationship hit : hits) {
						if (type == null || hit.isType(type)) {
							r = hit;
							break;
						}
					}
					if (r == null) {
						throw new Mismatch(String.format("No index entries found of type %s", rel.getType()));
					} else {
						store.add(rel, r);
					}
				}
			}
		} catch (Mismatch e) {
			throw new RuleApplicationException(this.ruleNumber, "Rule processing mismatch", e);
		}
	}

	/* END OF INCLUSION RULE HANDLERS */


	/* START OF EXCLUSION RULE HANDLERS */

	// !N
	private void handleNodeExclusionRule(NodeToken node, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotAllNamed(node);
		failIfNotEmpty(data);
		failIfNotRegistered(node);
		store.remove(node).delete();
	}

	// !R
	private void handleSimpleRelationshipExclusionRule(RelToken rel, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotAllNamed(rel);
		failIfTyped(rel);
		failIfNotEmpty(data);
		failIfNotRegistered(rel);
		store.remove(rel).delete();
	}

	// N-R-!N
	private void handleFullRelationshipExclusionRule(NodeToken startNode, RelToken rel, NodeToken endNode, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotAtLeastOneNamed(startNode, endNode);
		failIfNamed(rel);
		failIfNotTyped(rel);
		failIfNotEmpty(data);
		if (startNode.hasName()) {
			failIfNotRegistered(startNode);
		}
		if (endNode.hasName()) {
			failIfNotRegistered(endNode);
		}
		DynamicRelationshipType t = DynamicRelationshipType.withName(rel.getType());
		if (startNode.hasName() && endNode.hasName()) {
			// (A)-[:T]-!(B)
			Node s = store.get(startNode);
			Node e = store.get(endNode);
			for (Relationship r : s.getRelationships(Direction.OUTGOING, t)) {
				if (r.getEndNode().getId() == e.getId()) {
					r.delete();
				}
			}
		} else if (startNode.hasName()) {
			// (A)-[:T]-!()
			Node s = store.get(startNode);
			for (Relationship r : s.getRelationships(Direction.OUTGOING, t)) {
				r.delete();
			}
		} else {
			// ()-[:T]-!(B)
			Node e = store.get(endNode);
			for (Relationship r : e.getRelationships(Direction.INCOMING, t)) {
				r.delete();
			}
		}
	}

	// N'I
	private void handleNodeIndexExclusionRule(NodeToken node, IndexToken index, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotAllNamed(node, index);
		failIfNotRegistered(node);
		Node n = store.get(node);
		Index<Node> i = this.graphDB.index().forNodes(index.getName());
		for(Map.Entry<String, Object> entry : data.entrySet()) {
			i.remove(n, entry.getKey(), entry.getValue());
		}
	}

	// R'I
	private void handleRelationshipIndexExclusionRule(RelToken rel, IndexToken index, Map<String, Object> data)
			throws RuleFormatException, RuleApplicationException {
		failIfNotAllNamed(rel, index);
		failIfTyped(rel);
		failIfNotRegistered(rel);
		Relationship r = store.get(rel);
		Index<Relationship> i = this.graphDB.index().forRelationships(index.getName());
		for(Map.Entry<String, Object> entry : data.entrySet()) {
			i.remove(r, entry.getKey(), entry.getValue());
		}
	}

	/* END OF EXCLUSION RULE HANDLERS */


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
		if (!map.isEmpty()) {
			throw new RuleFormatException(this.ruleNumber, "Data cannot be supplied with this rule");
		}
	}

	/* END OF RULE FORMAT VALIDATORS */


	/* START OF RULE APPLICATION VALIDATORS */

	private void failIfNotRegistered(NodeToken... nodes)
			throws RuleApplicationException {
		for (NodeToken node : nodes) {
			if (!store.contains(node)) {
				throw new RuleApplicationException(this.ruleNumber, "Node not found: " + node.toString());
			}
		}
	}

	private void failIfNotRegistered(RelToken rel)
			throws RuleApplicationException {
		if (!store.contains(rel)) {
			throw new RuleApplicationException(this.ruleNumber, "Relationship not found: " + rel.toString());
		}
	}

	/* END OF DEPENDENCY VALIDATORS */


	private Node getOrCreate(NodeToken node) {
		Node n;
		if (store.contains(node)) {
			n = store.get(node);
		} else {
			n = this.graphDB.createNode();
			store.add(node, n);
		}
		return n;
	}

	/**
	 * Attempt to identify relationships from info in tokens
	 * 
	 * @param startNode
	 * @param rel
	 * @param endNode
	 * @return
	 * @throws Mismatch if details are contradictory
	 */
	private List<Relationship> match(NodeToken startNode, RelToken rel, NodeToken endNode)
			throws Mismatch {
		final ArrayList<Relationship> MATCH_LIST = new ArrayList<Relationship>();
		if (rel.hasName() && store.contains(rel)) {
			// registered relationship
			Relationship candidate = store.get(rel);
			// confirm start node
			if (startNode.hasName() && store.contains(startNode)) {
				Node required = store.get(startNode);
				if (candidate.getStartNode().getId() != required.getId()) {
					throw new Mismatch(String.format("Relationship [%s] does not start with node %s", rel.getName(), startNode));
				}
			}
			// confirm end node
			if (endNode.hasName() && store.contains(endNode)) {
				Node required = store.get(endNode);
				if (candidate.getEndNode().getId() != required.getId()) {
					throw new Mismatch(String.format("Relationship [%s] does not end with node %s", rel.getName(), endNode));
				}
			}
			// confirm type
			if (rel.hasType()) {
				if (!candidate.isType(DynamicRelationshipType.withName(rel.getType()))) {
					throw new Mismatch(String.format("Relationship [%s] is not of type %s", rel.getName(), rel.getType()));
				}
			}
			// confirmed
			MATCH_LIST.add(candidate);
			return MATCH_LIST;
		} else {
			Iterable<Relationship> candidates;
			if (startNode.hasName() && store.contains(startNode)) {
				// try all nodes outgoing from start node
				Node start = store.get(startNode);
				if (rel.hasType()) {
					candidates = start.getRelationships(Direction.OUTGOING, DynamicRelationshipType.withName(rel.getType()));
				} else {
					candidates = start.getRelationships(Direction.OUTGOING);
				}
				if (endNode.hasName() && store.contains(endNode)) {
					Node end = store.get(endNode);
					for (Relationship candidate : candidates) {
						if (candidate.getEndNode().getId() == end.getId())
							MATCH_LIST.add(candidate);
					}
				} else {
					for (Relationship candidate : candidates) {
						MATCH_LIST.add(candidate);
					}
				}
				return MATCH_LIST;
			} else if (endNode.hasName() && store.contains(endNode)) {
				// try all nodes incoming to end node
				Node end = store.get(endNode);
				if (rel.hasType()) {
					candidates = end.getRelationships(Direction.INCOMING, DynamicRelationshipType.withName(rel.getType()));
				} else {
					candidates = end.getRelationships(Direction.INCOMING);
				}
				for (Relationship candidate : candidates) {
					MATCH_LIST.add(candidate);
				}
				return MATCH_LIST;
			} else {
				return MATCH_LIST;
			}
		}
	}

	private void setProperties(PropertyContainer entity, Map<String, Object> data) {
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
