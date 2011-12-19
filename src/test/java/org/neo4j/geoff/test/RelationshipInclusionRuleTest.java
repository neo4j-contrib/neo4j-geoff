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
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.*;

public class RelationshipInclusionRuleTest {

	private ImpermanentGraphDatabase db;

	@Before
	public void setUp() throws Exception {
		db = new ImpermanentGraphDatabase();
	}

	@Test
	public void testRelationshipInclusionRule() throws Exception {
		String source = "(A)-[:KNOWS]->(B) {\"since\": 1977}\n";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("N-R->N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(2) instanceof RelToken);
		assertTrue(rule.getDescriptor().getToken(5) instanceof NodeToken);
		NodeToken startToken = (NodeToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.NODE, startToken.getTokenType());
		assertTrue(startToken.hasName());
		assertEquals("A", startToken.getName());
		RelToken relToken = (RelToken) rule.getDescriptor().getToken(2);
		assertEquals(Token.Type.REL, relToken.getTokenType());
		assertFalse(relToken.hasName());
		assertTrue(relToken.hasType());
		assertEquals("KNOWS", relToken.getType());
		NodeToken endToken = (NodeToken) rule.getDescriptor().getToken(5);
		assertEquals(Token.Type.NODE, endToken.getTokenType());
		assertTrue(endToken.hasName());
		assertEquals("B", endToken.getName());
		assertTrue(rule.getData().containsKey("since"));
		assertEquals(1977, rule.getData().get("since"));
	}


	@Test
	public void testLoadingRelationshipInclusionRule() throws Exception {
		String source =
				"(A) {\"name\": \"Alice\"}\n" +
				"(B) {\"name\": \"Bob\"}\n" +
				"(A)-[friends:KNOWS]->(B) {\"since\": 1977}\n" +
				"";
		Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		Relationship rel = (Relationship) out.get("[friends]");
		assertNotNull(rel);
		assertTrue(rel.hasProperty("since"));
		assertEquals(1977, rel.getProperty("since"));
	}

	@Test
	public void testLoadingRelationshipInclusionRuleWithSelfUpdate() throws Exception {
		String source =
				"(A) {\"name\": \"Alice\"}\n" +
				"(B) {\"name\": \"Bob\"}\n" +
				"(A)-[R:KNOWS]->(B) {\"since\": 1977}\n" +
				"[R] {\"foo\": \"bar\"}\n" +
				"";
		Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		Relationship rel = (Relationship) out.get("[R]");
		assertNotNull(rel);
		assertFalse(rel.hasProperty("since"));
		assertTrue(rel.hasProperty("foo"));
		assertEquals("bar", rel.getProperty("foo"));
	}

	@Test
	public void testLoadingRelationshipInclusionRuleWithLoadParameter() throws Exception {
		String source =
				"(A) {\"name\": \"Alice\"}\n" +
				"(B) {\"name\": \"Bob\"}\n" +
				"(A)-[R:KNOWS]->(B) {\"since\": 1977}\n" +
				"";
		Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		Relationship rel1 = (Relationship) out.get("[R]");
		assertNotNull(rel1);
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(1);
		params.put("R", rel1);
		source = "[R] {\"foo\": \"bar\"}";
		out = GEOFF.loadIntoNeo4j(new StringReader(source), db, params);
		assertNotNull(out);
		Relationship rel2 = (Relationship) out.get("[R]");
		assertNotNull(rel2);
		assertFalse(rel2.hasProperty("since"));
		assertTrue(rel2.hasProperty("foo"));
		assertEquals("bar", rel2.getProperty("foo"));
		assertEquals(rel1.getId(), rel2.getId());
	}

}
