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
import org.neo4j.graphdb.Relationship;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.*;

public class RelationshipReflectionRuleTest {

	private ImpermanentGraphDatabase db;

	@Before
	public void setUp() throws Exception {
		db = new ImpermanentGraphDatabase();
	}

	@Test
	public void testStartNodeFromRelationshipReflectionRule() throws Exception {
		String source = "(A):=(*)-[R]->()";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("N=N-R->N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(2) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(4) instanceof RelToken);
		assertTrue(rule.getDescriptor().getToken(7) instanceof NodeToken);
		NodeToken intoToken = (NodeToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.NODE, intoToken.getTokenType());
		assertTrue(intoToken.hasName());
		assertEquals("A", intoToken.getName());
		NodeToken startToken = (NodeToken) rule.getDescriptor().getToken(2);
		assertEquals(Token.Type.NODE, startToken.getTokenType());
		assertFalse(startToken.hasName());
		assertTrue(startToken.isStarred());
		RelToken relToken = (RelToken) rule.getDescriptor().getToken(4);
		assertEquals(Token.Type.REL, relToken.getTokenType());
		assertTrue(relToken.hasName());
		assertEquals("R", relToken.getName());
		assertFalse(relToken.hasType());
		NodeToken endToken = (NodeToken) rule.getDescriptor().getToken(7);
		assertEquals(Token.Type.NODE, endToken.getTokenType());
		assertFalse(endToken.hasName());
		assertFalse(endToken.isStarred());
	}

	@Test
	public void testEndNodeFromRelationshipReflectionRule() throws Exception {
		String source = "(A):=()-[R]->(*)";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("N=N-R->N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(2) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(4) instanceof RelToken);
		assertTrue(rule.getDescriptor().getToken(7) instanceof NodeToken);
		NodeToken intoToken = (NodeToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.NODE, intoToken.getTokenType());
		assertTrue(intoToken.hasName());
		assertEquals("A", intoToken.getName());
		NodeToken startToken = (NodeToken) rule.getDescriptor().getToken(2);
		assertEquals(Token.Type.NODE, startToken.getTokenType());
		assertFalse(startToken.hasName());
		assertFalse(startToken.isStarred());
		RelToken relToken = (RelToken) rule.getDescriptor().getToken(4);
		assertEquals(Token.Type.REL, relToken.getTokenType());
		assertTrue(relToken.hasName());
		assertEquals("R", relToken.getName());
		assertFalse(relToken.hasType());
		NodeToken endToken = (NodeToken) rule.getDescriptor().getToken(7);
		assertEquals(Token.Type.NODE, endToken.getTokenType());
		assertFalse(endToken.hasName());
		assertTrue(endToken.isStarred());
	}

	@Test
	public void testRelationshipFromBothNodesReflectionRule() throws Exception {
		String source = "[R]:=(A)-[:T]->(B)";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("R=N-R->N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof RelToken);
		assertTrue(rule.getDescriptor().getToken(2) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(4) instanceof RelToken);
		assertTrue(rule.getDescriptor().getToken(7) instanceof NodeToken);
		RelToken intoToken = (RelToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.REL, intoToken.getTokenType());
		assertTrue(intoToken.hasName());
		assertEquals("R", intoToken.getName());
		assertFalse(intoToken.hasType());
		NodeToken startToken = (NodeToken) rule.getDescriptor().getToken(2);
		assertEquals(Token.Type.NODE, startToken.getTokenType());
		assertTrue(startToken.hasName());
		assertEquals("A", startToken.getName());
		RelToken relToken = (RelToken) rule.getDescriptor().getToken(4);
		assertEquals(Token.Type.REL, relToken.getTokenType());
		assertFalse(relToken.hasName());
		assertTrue(relToken.hasType());
		assertEquals("T", relToken.getType());
		NodeToken endToken = (NodeToken) rule.getDescriptor().getToken(7);
		assertEquals(Token.Type.NODE, endToken.getTokenType());
		assertTrue(endToken.hasName());
		assertEquals("B", endToken.getName());
	}

	@Test
	public void testRelationshipFromStartNodeReflectionRule() throws Exception {
		String source = "[R]:=(A)-[:T]->()";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("R=N-R->N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof RelToken);
		assertTrue(rule.getDescriptor().getToken(2) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(4) instanceof RelToken);
		assertTrue(rule.getDescriptor().getToken(7) instanceof NodeToken);
		RelToken intoToken = (RelToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.REL, intoToken.getTokenType());
		assertTrue(intoToken.hasName());
		assertEquals("R", intoToken.getName());
		assertFalse(intoToken.hasType());
		NodeToken startToken = (NodeToken) rule.getDescriptor().getToken(2);
		assertEquals(Token.Type.NODE, startToken.getTokenType());
		assertTrue(startToken.hasName());
		assertEquals("A", startToken.getName());
		RelToken relToken = (RelToken) rule.getDescriptor().getToken(4);
		assertEquals(Token.Type.REL, relToken.getTokenType());
		assertFalse(relToken.hasName());
		assertTrue(relToken.hasType());
		assertEquals("T", relToken.getType());
		NodeToken endToken = (NodeToken) rule.getDescriptor().getToken(7);
		assertEquals(Token.Type.NODE, endToken.getTokenType());
		assertFalse(endToken.hasName());
	}

	@Test
	public void testRelationshipFromEndNodeReflectionRule() throws Exception {
		String source = "[R]:=()-[:T]->(B)";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("R=N-R->N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof RelToken);
		assertTrue(rule.getDescriptor().getToken(2) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(4) instanceof RelToken);
		assertTrue(rule.getDescriptor().getToken(7) instanceof NodeToken);
		RelToken intoToken = (RelToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.REL, intoToken.getTokenType());
		assertTrue(intoToken.hasName());
		assertEquals("R", intoToken.getName());
		assertFalse(intoToken.hasType());
		NodeToken startToken = (NodeToken) rule.getDescriptor().getToken(2);
		assertEquals(Token.Type.NODE, startToken.getTokenType());
		assertFalse(startToken.hasName());
		RelToken relToken = (RelToken) rule.getDescriptor().getToken(4);
		assertEquals(Token.Type.REL, relToken.getTokenType());
		assertFalse(relToken.hasName());
		assertTrue(relToken.hasType());
		assertEquals("T", relToken.getType());
		NodeToken endToken = (NodeToken) rule.getDescriptor().getToken(7);
		assertEquals(Token.Type.NODE, endToken.getTokenType());
		assertTrue(endToken.hasName());
		assertEquals("B", endToken.getName());
	}

	@Test
	public void testLoadingStartNodeFromRelationshipReflectionRule() throws Exception {
		// perform first call to inject relationship
		String source =
				"(A) {\"name\": \"Alice\"}\n" +
				"(B) {\"name\": \"Bob\"}\n" +
				"(A)-[R:KNOWS]->(B) {\"since\": 1977}\n" +
				"";
		Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		// build params for second call from output of first
		Relationship r = (Relationship) out.get("[R]");
		assertNotNull(r);
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(1);
		params.put("R", r);
		// make second call to query index entry
		source = "(A)-[R]->()";
		out = GEOFF.loadIntoNeo4j(new StringReader(source), db, params);
		// check results
		Node a = (Node) out.get("(A)");
		assertNotNull(a);
		assertTrue(a.hasProperty("name"));
		assertEquals("Alice", a.getProperty("name"));
	}

	@Test
	public void testLoadingEndNodeFromRelationshipReflectionRule() throws Exception {
		// perform first call to inject relationship
		String source =
				"(A) {\"name\": \"Alice\"}\n" +
				"(B) {\"name\": \"Bob\"}\n" +
				"(A)-[R:KNOWS]->(B) {\"since\": 1977}\n" +
				"";
		Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		// build params for second call from output of first
		Relationship r = (Relationship) out.get("[R]");
		assertNotNull(r);
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(1);
		params.put("R", r);
		// make second call to query index entry
		source = "()-[R]->(B)";
		out = GEOFF.loadIntoNeo4j(new StringReader(source), db, params);
		// check results
		Node a = (Node) out.get("(B)");
		assertNotNull(a);
		assertTrue(a.hasProperty("name"));
		assertEquals("Bob", a.getProperty("name"));
	}

	@Test
	public void testLoadingRelationshipFromBothNodesReflectionRule() throws Exception {
		// perform first call to inject relationship
		String source =
				"(A) {\"name\": \"Alice\"}\n" +
				"(B) {\"name\": \"Bob\"}\n" +
				"(A)-[:KNOWS]->(B) {\"since\": 1977}\n" +
				"";
		Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		// build params for second call from output of first
		Node a = (Node) out.get("(A)");
		assertNotNull(a);
		Node b = (Node) out.get("(B)");
		assertNotNull(b);
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(2);
		params.put("A", a);
		params.put("B", b);
		// make second call to query index entry
		source = "(A)-[R:KNOWS]->(B)";
		out = GEOFF.loadIntoNeo4j(new StringReader(source), db, params);
		// check results
		Relationship r = (Relationship) out.get("[R]");
		assertNotNull(r);
		assertTrue(r.hasProperty("since"));
		assertEquals(1977, r.getProperty("since"));
	}

	@Test
	public void testLoadingRelationshipFromStartNodeReflectionRule() throws Exception {
		// perform first call to inject relationship
		String source =
				"(A) {\"name\": \"Alice\"}\n" +
				"(B) {\"name\": \"Bob\"}\n" +
				"(A)-[:KNOWS]->(B) {\"since\": 1977}\n" +
				"";
		Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		// build params for second call from output of first
		Node a = (Node) out.get("(A)");
		assertNotNull(a);
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(1);
		params.put("A", a);
		// make second call to query index entry
		source = "(A)-[R:KNOWS]->()";
		out = GEOFF.loadIntoNeo4j(new StringReader(source), db, params);
		// check results
		Relationship r = (Relationship) out.get("[R]");
		assertNotNull(r);
		assertTrue(r.hasProperty("since"));
		assertEquals(1977, r.getProperty("since"));
	}

	@Test
	public void testLoadingRelationshipFromEndNodeReflectionRule() throws Exception {
		// perform first call to inject relationship
		String source =
				"(A) {\"name\": \"Alice\"}\n" +
				"(B) {\"name\": \"Bob\"}\n" +
				"(A)-[:KNOWS]->(B) {\"since\": 1977}\n" +
				"";
		Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		// build params for second call from output of first
		Node b = (Node) out.get("(B)");
		assertNotNull(b);
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(1);
		params.put("B", b);
		// make second call to query index entry
		source = "()-[R:KNOWS]->(B)";
		out = GEOFF.loadIntoNeo4j(new StringReader(source), db, params);
		// check results
		Relationship r = (Relationship) out.get("[R]");
		assertNotNull(r);
		assertTrue(r.hasProperty("since"));
		assertEquals(1977, r.getProperty("since"));
	}

}
