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

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.util.HashMap;
import java.util.Map;

class Neo4jEntityStore {

	private final HashMap<String, PropertyContainer> entities = new HashMap<String, PropertyContainer>();

	Neo4jEntityStore(Map<String, ? extends PropertyContainer> params) {
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
					define(new NodeToken(key), (Node) param.getValue());
				} else if (param.getValue() instanceof Relationship && (isRelKey || isUntypedKey)) {
					define(new RelToken(key), (Relationship) param.getValue());
				} else {
					throw new IllegalArgumentException(String.format("Illegal parameter '%s':%s ", key, param.getValue().getClass().getName()));
				}
			}
		}
	}

	// will only define if undefined
	void define(NodeToken nodeToken, Node node) {
		if (nodeToken.hasName() && !this.entities.containsKey(nodeToken.getFullName()) && node != null) {
			this.entities.put(nodeToken.getFullName(), node);
		}
	}

	// will only define if undefined
	void define(RelToken relToken, Relationship relationship) {
		if (relToken.hasName() && !this.entities.containsKey(relToken.getFullName()) && relationship != null) {
			this.entities.put(relToken.getFullName(), relationship);
		}
	}

	Node undefine(NodeToken nodeToken) {
		Node node = (Node) this.entities.get(nodeToken.getFullName());
		this.entities.remove(nodeToken.getFullName());
		return node;
	}

	Relationship undefine(RelToken relToken) {
		Relationship relationship = (Relationship) this.entities.get(relToken.getFullName());
		this.entities.remove(relToken.getFullName());
		return relationship;
	}

	Node get(NodeToken nodeToken) {
		return (Node) this.entities.get(nodeToken.getFullName());
	}

	Relationship get(RelToken relToken) {
		return (Relationship) this.entities.get(relToken.getFullName());
	}

	boolean isDefined(NodeToken nodeToken) {
		return nodeToken.hasName() && this.entities.containsKey(nodeToken.getFullName());
	}

	boolean isDefined(RelToken relToken) {
		return relToken.hasName() && this.entities.containsKey(relToken.getFullName());
	}

	NodeState stateOf(NodeToken nodeToken) {
		if (nodeToken.hasName()) {
			if (this.isDefined(nodeToken)) {
				return NodeState.DEFINED;
			} else {
				return NodeState.UNDEFINED;
			}
		} else {
			return NodeState.MISSING;
		}
	}

	RelState stateOf(RelToken relToken) {
		if (relToken.hasName()) {
			if (this.isDefined(relToken)) {
				if (relToken.hasType()) {
					if (this.get(relToken).isType(DynamicRelationshipType.withName(relToken.getType()))) {
						return RelState.DEFINED_AND_CORRECTLY_TYPED;
					} else {
						return RelState.DEFINED_AND_INCORRECTLY_TYPED;
					}
				} else {
					return RelState.DEFINED_AND_UNTYPED;
				}
			} else {
				if (relToken.hasType()) {
					return RelState.UNDEFINED_AND_TYPED;
				} else {
					return RelState.UNDEFINED_AND_UNTYPED;
				}
			}
		} else {
			if (relToken.hasType()) {
				return RelState.MISSING_AND_TYPED;
			} else {
				return RelState.MISSING_AND_UNTYPED;
			}
		}
	}

	Map<String, PropertyContainer> entities() {
		return this.entities;
	}

}
