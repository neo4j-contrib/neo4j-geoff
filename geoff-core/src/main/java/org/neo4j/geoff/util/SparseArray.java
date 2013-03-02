/**
 * Copyright (c) 2002-2013 "Neo Technology,"
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
package org.neo4j.geoff.util;

import java.util.*;

public class SparseArray<T> {
	
	private final TreeMap<Integer, T> items;
	
	public SparseArray() {
		this.items = new TreeMap<Integer, T>();
	}

	public SparseArray(T item) {
		this();
		this.items.put(1, item);
	}

	public SparseArray(Set<T> items) {
		this();
		int i = 1;
		for (T item : items) {
			this.items.put(i++, item);
		}
	}

	public T get(int index) {
		if (items.containsKey(index)) {
			return items.get(index);
		} else {
			return null;
		}
	}
	
	public boolean put(int index, T item) {
		if (items.containsKey(index)) {
			return false;
		} else {
			items.put(index, item);
			return true;
		}
	}
	
	public T remove(int index) {
		if (items.containsKey(index)) {
			T item = items.get(index);
			items.remove(index);
			return item;
		} else {
			return null;
		}
	}

	public boolean hasIndex(int index) {
		return items.containsKey(index);
	}
	
	public List<T> toList() {
		return new ArrayList<T>(items.values());
	}
	
	public Map<Integer, T> toMap() {
		return items;
	}
	
}
