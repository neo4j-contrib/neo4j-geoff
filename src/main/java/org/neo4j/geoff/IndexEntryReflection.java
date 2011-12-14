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

public class IndexEntryReflection<T extends Reflective> extends Descriptor {

	protected final T entity;
	protected final IndexRef index;
	protected String key;
	protected Object value;

	protected IndexEntryReflection(T entity, IndexRef index, Map<String, Object> data) {
		super();
		this.index = index;
		this.entity = entity;
		if(data.size() == 1) {
			for(Map.Entry<String, Object> entry : data.entrySet()) {
				this.key = entry.getKey();
				this.value = entry.getValue();
				break;
			}
		} else {
			throw new IllegalArgumentException("Exactly one key:value pair expected");
		}
	}

	public T getEntity() {
		return this.entity;
	}

	/**
	 * Return the name of the Index described
	 *
	 * @return the Index name
	 */
	public IndexRef getIndex() {
		return this.index;
	}

	public String getKey() {
		return this.key;
	}

	public Object getValue() {
		return this.value;
	}

}
