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
					add(new NodeToken(key), (Node) param.getValue());
				} else if (param.getValue() instanceof Relationship && (isRelKey || isUntypedKey)) {
					add(new RelToken(key), (Relationship) param.getValue());
				} else {
					throw new IllegalArgumentException(String.format("Illegal parameter '%s':%s ", key, param.getValue().getClass().getName()));
				}
			}
		}
	}

	void add(NodeToken nodeToken, Node node) {
		this.entities.put(nodeToken.getFullName(), node);
	}

	void add(RelToken relToken, Relationship relationship) {
		this.entities.put(relToken.getFullName(), relationship);
	}

	Node remove(NodeToken nodeToken) {
		Node node = (Node) this.entities.get(nodeToken.getFullName());
		this.entities.remove(nodeToken.getFullName());
		return node;
	}

	Relationship remove(RelToken relToken) {
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

	boolean contains(NodeToken nodeToken) {
		return this.entities.containsKey(nodeToken.getFullName());
	}

	boolean contains(RelToken relToken) {
		return this.entities.containsKey(relToken.getFullName());
	}

	public Map<String, PropertyContainer> entities() {
		return this.entities;
	}

}
