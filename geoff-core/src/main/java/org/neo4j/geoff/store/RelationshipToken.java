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
package org.neo4j.geoff.store;

import java.util.HashMap;

public class RelationshipToken extends EntityToken {

	private static final HashMap<String, RelationshipToken> ANONYMOUS = new HashMap<String, RelationshipToken>();

	public static RelationshipToken anon(String type) {
		if (!ANONYMOUS.containsKey(type)) {
			ANONYMOUS.put(type, new RelationshipToken("", type));
		}
		return ANONYMOUS.get(type);
	}

	protected final String type;

	public RelationshipToken(String name, String type) {
		super(Type.REL, name);
		this.type = type;
	}

	public RelationshipToken(String name) {
		super(Type.REL, name);
		this.type = null;
	}

	public boolean hasType() {
		return !(this.type == null || this.type.isEmpty() || "*".equals(this.type));
	}

	public String getType() {
		return this.type;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("[");
		str.append(this.name);
		if (index > 0) {
			str.append('.');
			str.append(index);
		}
		if(this.type != null && !this.type.isEmpty()) {
			str.append(':');
			str.append(this.type);
		}
		str.append(']');
		return str.toString();
	}

}
