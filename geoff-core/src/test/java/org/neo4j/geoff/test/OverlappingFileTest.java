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
package org.neo4j.geoff.test;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.geoff.Geoff;
import org.neo4j.geoff.GeoffLoadException;
import org.neo4j.graphdb.*;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import static junit.framework.Assert.*;

public class OverlappingFileTest {

	private ImpermanentGraphDatabase db;

	@Before
	public void setUp() throws Exception {
		db = new ImpermanentGraphDatabase();
	}

	private static Reader readerForResource(String name) {
		return new InputStreamReader(ClassLoader.getSystemResourceAsStream(name));
	}
	
	/*
	 * Should be able to load multiple files with overlapping entities
	 * without causing duplication
	 */
	@Test
	public void canLoadOverlappingFiles() throws GeoffLoadException, IOException {
		Geoff.loadIntoNeo4j(readerForResource("music/David Bowie - Space Oddity.geoff"), db, null);
		Geoff.loadIntoNeo4j(readerForResource("music/David Bowie - Life On Mars.geoff"), db, null);
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(readerForResource("music/David Bowie.geoff"), db, null);
		assertNotNull(out);
		assertTrue(out.containsKey("(bowie)"));
		assertTrue(out.get("(bowie)") instanceof Node);
		Node bowie = (Node) out.get("(bowie)");
		assertEquals("David Robert Jones", bowie.getProperty("real_name"));
		assertTrue(bowie.hasRelationship(Direction.OUTGOING, DynamicRelationshipType.withName("RELEASED")));
		int rels = 0;
		for(Relationship rel : bowie.getRelationships(Direction.OUTGOING, DynamicRelationshipType.withName("RELEASED"))) {
			rels++;
			Node artist = rel.getStartNode();
			Node track = rel.getEndNode();
			System.out.println(String.format("%s released %s on %s",
					artist.getProperty("name"),
					track.getProperty("name"),
					rel.getProperty("release_date")
			));
		}
		assertEquals(3, rels);
	}

}
