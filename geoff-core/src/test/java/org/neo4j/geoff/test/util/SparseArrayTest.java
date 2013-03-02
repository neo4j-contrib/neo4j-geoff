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
package org.neo4j.geoff.test.util;

import org.junit.Test;
import org.neo4j.geoff.util.SparseArray;

import java.util.List;

import static org.junit.Assert.*;

public class SparseArrayTest {
	
	@Test
	public void canCreateArray() {
		SparseArray<String> array = new SparseArray<String>();
		array.put(10, "foo");
		array.put(6, "bar");
		assertTrue(array.hasIndex(6));
		assertTrue(array.hasIndex(10));
		assertFalse(array.hasIndex(9));
		assertEquals("foo", array.get(10));
		array.remove(10);
		assertTrue(array.hasIndex(6));
		assertFalse(array.hasIndex(10));
		List<String> list = array.toList();
		assertNotNull(list);
		assertEquals(1, list.size());
		assertEquals("bar", list.get(0));
	}
	
}
