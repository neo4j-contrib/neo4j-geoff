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
import org.neo4j.geoff.GEOFF;
import org.neo4j.geoff.Rule;
import org.neo4j.graphdb.*;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class GraphDescriptionTest {

	private ImpermanentGraphDatabase db;

	@Test
	public void canCreateGraphFromSingleString() throws Exception {
		Reader reader = new StringReader("" +
				"(doc) {\"name\": \"doctor\"}\n" +
				"(dal) {\"name\": \"dalek\"}\n" +
				"(doc)-[:ENEMY_OF]->(dal) {\"since\":\"forever\"}\n" +
				"(doc)<=|People|     {\"name\": \"The Doctor\"}\n" +
				"");
		GEOFF.loadIntoNeo4j(reader, db, null);
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
		assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
	}

	@Test
	public void canCreateGraphWithHookToReferenceNode() throws Exception {
		Reader reader = new StringReader("[" +
				"\"(doc) {\\\"name\\\": \\\"doctor\\\"}\"," +
				"\"(dal) {\\\"name\\\": \\\"dalek\\\"}\"," +
				"\"(doc)-[:ENEMY_OF]->(dal) {\\\"since\\\":\\\"forever\\\"}\"," +
				"\"(doc)<=|People|     {\\\"name\\\": \\\"The Doctor\\\"}\"," +
				"\"(ref)-[:TIMELORD]->(doc)\"" +
				"]");
		HashMap<String,PropertyContainer> hooks = new HashMap<String,PropertyContainer>(1);
		hooks.put("ref", db.getReferenceNode());
		GEOFF.loadIntoNeo4j(reader, db, hooks);
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
		assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
		assertTrue(db.getReferenceNode().hasRelationship(DynamicRelationshipType.withName("TIMELORD")));
	}

	@Test
	public void canCreateGraphWithAllRelationshipTypes() throws Exception {
		/*
		 * Creates a graph with the following shape where (one) and (two) already exist:
		 *
		 *   (one)-[:EAST]->(foo)
		 *     ^              |
		 *     |              |
		 * [:NORTH]        [:SOUTH]
		 *     |              |
		 *     |              v
		 *   (two)<-[:WEST]-(bar)
		 */
		Reader reader = new StringReader(
				"(foo) {\"position\": \"north-east\"}\r\n" +
				"(bar) {\"position\": \"south-east\"}\r\n" +
				"(one) {\"position\": \"north-west\"}\r\n" +
				"(two) {\"position\": \"south-west\"}\r\n" +
				"[\"(foo)-[:SOUTH]->(bar)\", \"(bar)-[:WEST]->(two)\", \"(two)-[:NORTH]->(one)\", \"(one)-[:EAST]->(foo)\"]\r\n"
		);
		Transaction tx = db.beginTx();
		Node nodeOne = db.createNode();
		Node nodeTwo = db.createNode();
		tx.success();
		tx.finish();
		HashMap<String,Node> params = new HashMap<String,Node>(1);
		params.put("one", nodeOne);
		params.put("two", nodeTwo);
		Map<String,PropertyContainer> entities = GEOFF.loadIntoNeo4j(reader, db, params);
		Node nodeFoo = (Node) entities.get("(foo)");
		Node nodeBar = (Node) entities.get("(bar)");
		assertTrue(nodeOne.hasRelationship(DynamicRelationshipType.withName("EAST"), Direction.OUTGOING));
		assertTrue(nodeOne.hasRelationship(DynamicRelationshipType.withName("NORTH"), Direction.INCOMING));
		assertTrue(nodeTwo.hasRelationship(DynamicRelationshipType.withName("NORTH"), Direction.OUTGOING));
		assertTrue(nodeTwo.hasRelationship(DynamicRelationshipType.withName("WEST"), Direction.INCOMING));
		assertTrue(nodeFoo.hasRelationship(DynamicRelationshipType.withName("SOUTH"), Direction.OUTGOING));
		assertTrue(nodeFoo.hasRelationship(DynamicRelationshipType.withName("EAST"), Direction.INCOMING));
		assertTrue(nodeBar.hasRelationship(DynamicRelationshipType.withName("WEST"), Direction.OUTGOING));
		assertTrue(nodeBar.hasRelationship(DynamicRelationshipType.withName("SOUTH"), Direction.INCOMING));
		assertEquals(nodeOne.getProperty("position"), "north-west");
		assertEquals(nodeTwo.getProperty("position"), "south-west");
		assertEquals(nodeFoo.getProperty("position"), "north-east");
		assertEquals(nodeBar.getProperty("position"), "south-east");
	}

	@Test
	public void canCreateGraphWithNodeIndexEntryReflection() throws Exception {
		Reader reader = new StringReader("" +
				"(doc) {\"name\": \"doctor\"}\n" +
				"(dal) {\"name\": \"dalek\"}\n" +
				"(doc)<=|People|     {\"name\": \"The Doctor\"}\n" +
				"(dal)<=|Baddies|    {\"name\": \"Dalek Sec\"}\n" +
				"");
		GEOFF.loadIntoNeo4j(reader, db, null);
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
		assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
		assertTrue(db.index().existsForNodes("Baddies"));
		assertTrue(db.index().forNodes("Baddies").get("name", "Dalek Sec").hasNext());
		assertEquals("dalek", db.index().forNodes("Baddies").get("name", "Dalek Sec").getSingle().getProperty("name"));
		reader = new StringReader("" +
				"(doc)<=|People|     {\"name\": \"The Doctor\"}\n" +
				"(dal)<=|Baddies|    {\"name\": \"Dalek Sec\"}\n" +
				"(doc)-[enemy:ENEMY_OF]->(dal) {\"since\":\"forever\"}\n" +
				"");
		Map<String, PropertyContainer> stuff = GEOFF.loadIntoNeo4j(reader, db, null);
		Relationship enemy = (Relationship) stuff.get("[enemy]");
		assertNotNull(enemy);
		assertEquals("doctor", enemy.getStartNode().getProperty("name"));
		assertEquals("dalek", enemy.getEndNode().getProperty("name"));
	}

	@Test
	public void canCreateGraphWithIndexExclusionRule() throws Exception {
		Reader reader = new StringReader("" +
				"(doc) {\"name\": \"doctor\"}\n" +
				"(doc)<=|People|     {\"name\": \"The Doctor\"}\n" +
				"");
		GEOFF.loadIntoNeo4j(reader, db, null);
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
		assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
		reader = new StringReader("" +
				"(doc)<=|People|     {\"name\": \"The Doctor\"}\n" +
				"(doc)!=|People|     {\"name\": \"The Doctor\"}\n" +
				"");
		GEOFF.loadIntoNeo4j(reader, db, null);
		assertFalse(db.index().forNodes("People").get("name", "The Doctor").hasNext());
	}

	@Test
	public void canLoadRulesCreatedFromValues() throws Exception {
		ArrayList<Rule> rules = new ArrayList<Rule>();
		rules.add(Rule.fromValues("(doc)", "name", "doctor", "age", 991));
		rules.add(Rule.fromValues("(doc)<=|People|", "name", "The Doctor"));
		GEOFF.loadIntoNeo4j(rules, db, null);
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
		assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
	}
	
	@Before
	public void setUp() throws Exception {
		db = new ImpermanentGraphDatabase();
	}

}
