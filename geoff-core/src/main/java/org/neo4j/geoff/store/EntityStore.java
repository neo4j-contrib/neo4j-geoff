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
package org.neo4j.geoff.store;

import org.neo4j.geoff.util.SparseArray;

import java.util.*;

public class EntityStore<K extends EntityToken, V> {

	private final TreeMap<String, SparseArray<V>> items = new TreeMap<String, SparseArray<V>>();

	public EntityStore() {
		super();
	}

	public boolean put(K token, V item) {
		if (token.hasName() && item != null) {
			String key = token.getName();
			int index = token.getIndex();
			if (index == 0) {
				if (items.containsKey(key)) {
					return false;
				} else {
					items.put(key, new SparseArray<V>(item));
					return true;
				}
			} else {
				if (!items.containsKey(key)) {
					items.put(key, new SparseArray<V>());
				}
				return items.get(key).put(index, item);
			}
		} else {
			return false;
		}
	}

	public boolean put(K token, Set<V> items) {
		if (token.hasName() && items != null && !items.isEmpty()) {
			String key = token.getName();
			if (this.items.containsKey(key)) {
				return false;
			} else {
				this.items.put(key, new SparseArray<V>(items));
				return true;
			}
		} else {
			return false;
		}
	}

	public Set<V> get(K token) {
		HashSet<V> n = new HashSet<V>();
		String key = token.getName();
		int index = token.getIndex();
		if (index == 0) {
			n.addAll(items.get(key).toList());
		} else {
			n.add(items.get(key).get(index));
		}
		return n;
	}

	public Set<V> remove(K token) {
		HashSet<V> n = new HashSet<V>();
		if (token.hasName()) {
			String key = token.getName();
			int index = token.getIndex();
			if (index == 0) {
				if (items.containsKey(key)) {
					n.addAll(items.get(key).toList());
					items.remove(key);
				}
			} else {
				if (items.containsKey(key) && items.get(key).hasIndex(index)) {
					n.add(items.get(key).remove(index));
				}
			}
		}
		return n;
	}

	public boolean contains(K token) {
		String key = token.getName();
		int index = token.getIndex();
		if (index == 0) {
			return token.hasName() && items.containsKey(key);
		} else {
			return token.hasName() && items.containsKey(key) && items.get(key).hasIndex(index);
		}
	}
	
	public Map<String, V> toMap() {
		Map<String, V> map = new TreeMap<String, V>();
		for (Map.Entry<String, SparseArray<V>> entry : items.entrySet()) {
			String key = entry.getKey();
			Map<Integer, V> values = entry.getValue().toMap();
			boolean single = values.size() == 1 && values.containsKey(1);
			for (Map.Entry<Integer, V> subentry : values.entrySet()) {
				int index = subentry.getKey();
				V value = subentry.getValue();
				if (single) {
					map.put(key, value);
				} else {
					map.put(key + '.' + index, value);
				}
			}
		}
		return map;
	}

}
