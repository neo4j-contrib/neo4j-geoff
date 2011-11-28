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
import org.neo4j.geoff.GEOFFLoader;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GraphDescriptionTest {
	private ImpermanentGraphDatabase db;


	@Test
	public void canCreateGraphFromSingleString() throws Exception {
		Reader reader = new StringReader("" +
				"(doc) {\"name\": \"doctor\"}\n" +
				"(dal) {\"name\": \"dalek\"}\n" +
				"(doc)-[:ENEMY_OF]->(dal) {\"since\":\"forever\"}\n" +
				"|People|->(doc)     {\"name\": \"The Doctor\"}\n" +
				"");
		GEOFFLoader.loadIntoNeo4j(reader, db, null);
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
		assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
	}

	@Test
	public void canCreateGraphFromSingleCompositeDescriptor() throws Exception {
		Reader reader = new StringReader("{" +
				"\"(doc)\": {\"name\": \"doctor\"}," +
				"\"(dal)\": {\"name\": \"dalek\"}," +
				"\"(doc)-[:ENEMY_OF]->(dal)\": {\"since\":\"forever\"}," +
				"\"|People|->(doc)\":     {\"name\": \"The Doctor\"}" +
				"}");
		GEOFFLoader.loadIntoNeo4j(reader, db, null);
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
		assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
	}

	@Test
	public void canCreateGraphFromCompositeDescriptorInUnexpectedOrder() throws Exception {
		Reader reader = new StringReader("{" +
				"\"(doc)-[:ENEMY_OF]->(dal)\": {\"since\":\"forever\"}," +
				"\"|People|->(doc)\":     {\"name\": \"The Doctor\"}," +
				"\"(doc)\": {\"name\": \"doctor\"}," +
				"\"(dal)\": {\"name\": \"dalek\"}" +
				"}");
		GEOFFLoader.loadIntoNeo4j(reader, db, null);
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
		assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
	}

	@Test
	public void canCreateGraphFromOrderedMap() throws Exception {
		TreeMap<String, Object> props;
		TreeMap<String, Map<String, Object>> map = new TreeMap<String, Map<String, Object>>();
		props = new TreeMap<String, Object>();
		props.put("name", "doctor");
		map.put("(doc)", props);
		props = new TreeMap<String, Object>();
		props.put("name", "dalek");
		map.put("(dal)", props);
		props = new TreeMap<String, Object>();
		props.put("since", "forever");
		map.put("(doc)-[:ENEMY_OF]->(dal)", props);
		props = new TreeMap<String, Object>();
		props.put("name", "The Doctor");
		map.put("|People|->(doc)", props);
		GEOFFLoader.loadIntoNeo4j(map, db, null);
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
		assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
	}

	@Test
	public void canCreateGraphFromUnorderedMap() throws Exception {
		HashMap<String, Object> props;
		HashMap<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
		props = new HashMap<String, Object>();
		props.put("since", "forever");
		map.put("(doc)-[:ENEMY_OF]->(dal)", props);
		props = new HashMap<String, Object>();
		props.put("name", "The Doctor");
		map.put("|People|->(doc)", props);
		props = new HashMap<String, Object>();
		props.put("name", "doctor");
		map.put("(doc)", props);
		props = new HashMap<String, Object>();
		props.put("name", "dalek");
		map.put("(dal)", props);
		GEOFFLoader.loadIntoNeo4j(map, db, null);
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
		assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
	}

	@Test(expected = ClassCastException.class)
	public void shouldFailOnInappropriateMap() throws Exception {
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		map.put(12, "twelve");
		map.put(7, "seven");
		map.put(3, "three");
		map.put(25, "twenty-five");
		GEOFFLoader.loadIntoNeo4j(map, db, null);
	}

	@Test
	public void canCreateGraphWithHookToReferenceNode() throws Exception {
		Reader reader = new StringReader("{" +
				"\"(doc)\": {\"name\": \"doctor\"}," +
				"\"(dal)\": {\"name\": \"dalek\"}," +
				"\"(doc)-[:ENEMY_OF]->(dal)\": {\"since\":\"forever\"}," +
				"\"|People|->(doc)\":     {\"name\": \"The Doctor\"}," +
				"\"{ref}-[:TIMELORD]->(doc)\":     null" +
				"}");
		HashMap<String,PropertyContainer> hooks = new HashMap<String,PropertyContainer>(1);
		hooks.put("ref", db.getReferenceNode());
		GEOFFLoader.loadIntoNeo4j(reader, db, hooks);
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
		assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
		assertTrue(db.getReferenceNode().hasRelationship(DynamicRelationshipType.withName("TIMELORD")));
	}

	@Before
	public void setUp() throws Exception {
		db = new ImpermanentGraphDatabase();
	}

}
