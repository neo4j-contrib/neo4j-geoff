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
import org.neo4j.geoff.*;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class InclusionRuleTest {

	private ImpermanentGraphDatabase db;

	@Before
	public void setUp() throws Exception {
		db = new ImpermanentGraphDatabase();
	}

	public synchronized void testRule(String text) throws Exception {
		System.out.println("Text: " + text);
		Rule rule = Rule.from(text);
		System.out.println("Descriptor:");
		System.out.print("\tTokens:");
		for(Token token : rule.getDescriptor().getTokens()) {
			System.out.print(" " + token.getTokenType().name());
		}
		System.out.println();
		System.out.println("\tPattern: \"" + rule.getDescriptor().getPattern() + "\"");
		System.out.println("Data:");
		for(Map.Entry<String, Object> entry : rule.getData().entrySet()) {
			System.out.println("\t" + entry.getKey() + "=" + entry.getValue());
		}
		System.out.println();
	}

	@Test
	public void testIncludeNode() throws Exception {
		Rule rule = Rule.from("(A)");
		assertNotNull(rule);
		assertEquals("N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof NodeToken);
		NodeToken token = (NodeToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.NODE, token.getTokenType());
		assertEquals(true, token.hasName());
		assertEquals("A", token.getName());
	}

	@Test
	public void testIncludeRelationshipByName() throws Exception {
		Rule rule = Rule.from("[R]");
		assertNotNull(rule);
		assertEquals("R", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof RelToken);
		RelToken token = (RelToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.REL, token.getTokenType());
		assertEquals(true, token.hasName());
		assertEquals("R", token.getName());
		assertEquals(false, token.hasType());
	}

	@Test
	public void testIncludeRelationshipByType() throws Exception {
		Rule rule = Rule.from("(A)-[:T]->(B)");
		assertNotNull(rule);
		assertEquals("N-R->N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(2) instanceof RelToken);
		assertTrue(rule.getDescriptor().getToken(5) instanceof NodeToken);
		NodeToken start = (NodeToken) rule.getDescriptor().getToken(0);
		RelToken rel = (RelToken) rule.getDescriptor().getToken(2);
		NodeToken end = (NodeToken) rule.getDescriptor().getToken(5);
		assertEquals(Token.Type.NODE, start.getTokenType());
		assertEquals(Token.Type.REL, rel.getTokenType());
		assertEquals(Token.Type.NODE, end.getTokenType());
		assertEquals(true, start.hasName());
		assertEquals("A", start.getName());
		assertEquals(false, rel.hasName());
		assertEquals(true, rel.hasType());
		assertEquals("T", rel.getType());
		assertEquals(true, end.hasName());
		assertEquals("B", end.getName());
	}

	@Test
	public void testIncludeRelationshipByTypeWithName() throws Exception {
		Rule rule = Rule.from("(A)-[R:T]->(B)");
		assertNotNull(rule);
		assertEquals("N-R->N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(2) instanceof RelToken);
		assertTrue(rule.getDescriptor().getToken(5) instanceof NodeToken);
		NodeToken start = (NodeToken) rule.getDescriptor().getToken(0);
		RelToken rel = (RelToken) rule.getDescriptor().getToken(2);
		NodeToken end = (NodeToken) rule.getDescriptor().getToken(5);
		assertEquals(Token.Type.NODE, start.getTokenType());
		assertEquals(Token.Type.REL, rel.getTokenType());
		assertEquals(Token.Type.NODE, end.getTokenType());
		assertEquals(true, start.hasName());
		assertEquals("A", start.getName());
		assertEquals(true, rel.hasName());
		assertEquals("R", rel.getName());
		assertEquals(true, rel.hasType());
		assertEquals("T", rel.getType());
		assertEquals(true, end.hasName());
		assertEquals("B", end.getName());
	}

	@Test
	public void testIncludeNodeIndexEntry() throws Exception {
		testRule("(A)<=|I| {\"foo\":\"bar\"}");
	}

	@Test
	public void testIncludeRelationshipIndexEntry() throws Exception {
		testRule("[R]<=|I| {\"foo\":\"bar\"}");
	}

	@Test
	public void canCreateGraphWithSingleNode() throws Exception {
		Reader reader = new StringReader("" +
				"(doc) {\"name\": \"Doctor Who\"}\n" +
				"(doc) {\"name\": \"The Doctor\"}\n" +
				"");
		Map<String, PropertyContainer> entities = GEOFF.loadIntoNeo4j(reader, db, null);
		assertEquals("The Doctor", entities.get("(doc)").getProperty("name"));
	}

	@Test
	public void canCreateGraphWithSingleNodeParam() throws Exception {
		Reader reader = new StringReader("" +
				"(doc) {\"name\": \"Doctor Who\"}\n" +
				"(doc) {\"name\": \"The Doctor\"}\n" +
				"");
		HashMap<String, Node> params = new HashMap<String, Node>();
		params.put("doc", db.getReferenceNode());
		Map<String, PropertyContainer> entities = GEOFF.loadIntoNeo4j(reader, db, params);
		assertEquals("The Doctor", entities.get("(doc)").getProperty("name"));
		assertEquals(0, ((Node) entities.get("(doc)")).getId());
	}

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

}
