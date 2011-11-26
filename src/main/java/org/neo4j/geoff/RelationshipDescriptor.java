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

import java.util.Map;

public class RelationshipDescriptor<A extends Connectable, B extends Connectable> extends Descriptor {

	protected final A startNode;
	protected final String name;
	protected final String type;
	protected final B endNode;
	protected final Map<String, Object> data;

	protected RelationshipDescriptor(A startNode, String name, String type, B endNode, Map<String, Object> data) {
		super();
		this.startNode = startNode;
		this.name = name.intern();
		this.type = type.intern();
		this.endNode = endNode;
		this.data = data;
	}

	public A getStartNode() {
		return this.startNode;
	}

	public boolean hasName() {
		return !(this.name == null || this.name.isEmpty());
	}

	/**
	 * Return the name of the Relationship described
	 *
	 * @return the Relationship name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Return the type of the Relationship described
	 *
	 * @return the Relationship type
	 */
	public String getType() {
		return this.type;
	}

	public B getEndNode() {
		return this.endNode;
	}

	/**
	 * Return the key:value pairs attached to this Descriptor
	 *
	 * @return Map of key:value pairs
	 */
	public Map<String, Object> getData() {
		return this.data;
	}

}
