/**
 * Copyright (c) 2002-2012 "Neo Technology,"
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

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.neo4j.geoff.Geoff;
import org.neo4j.geoff.Rule;
import org.neo4j.geoff.store.NodeToken;
import org.neo4j.geoff.store.RelationshipToken;
import org.neo4j.geoff.store.Token;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

public class DeleteRelationshipsTest extends TestBase{

	@Test
	public void testRelationshipExclusionByNameRule() throws Exception {
		String source = "![R]";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("!R", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(1) instanceof RelationshipToken);
		RelationshipToken token = (RelationshipToken) rule.getDescriptor().getToken(1);
		assertEquals(Token.Type.REL, token.getTokenType());
		assertTrue(token.hasName());
		assertEquals("R", token.getName());
		assertFalse(token.hasType());
	}

	@Test
	public void testRelationshipExclusionByTypeAndBothNodesRule() throws Exception {
		String source = "(A)-[:T]-!(B)";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("N-R-!N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(2) instanceof RelationshipToken);
		assertTrue(rule.getDescriptor().getToken(5) instanceof NodeToken);
		NodeToken startToken = (NodeToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.NODE, startToken.getTokenType());
		assertTrue(startToken.hasName());
		assertEquals("A", startToken.getName());
		RelationshipToken relationshipToken = (RelationshipToken) rule.getDescriptor().getToken(2);
		assertEquals(Token.Type.REL, relationshipToken.getTokenType());
		assertFalse(relationshipToken.hasName());
		assertTrue(relationshipToken.hasType());
		assertEquals("T", relationshipToken.getType());
		NodeToken endToken = (NodeToken) rule.getDescriptor().getToken(5);
		assertEquals(Token.Type.NODE, endToken.getTokenType());
		assertTrue(endToken.hasName());
		assertEquals("B", endToken.getName());
	}

	@Test
	public void testRelationshipExclusionByTypeAndStartNodeRule() throws Exception {
		String source = "(A)-[:T]-!()";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("N-R-!N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(2) instanceof RelationshipToken);
		assertTrue(rule.getDescriptor().getToken(5) instanceof NodeToken);
		NodeToken startToken = (NodeToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.NODE, startToken.getTokenType());
		assertTrue(startToken.hasName());
		assertEquals("A", startToken.getName());
		RelationshipToken relationshipToken = (RelationshipToken) rule.getDescriptor().getToken(2);
		assertEquals(Token.Type.REL, relationshipToken.getTokenType());
		assertFalse(relationshipToken.hasName());
		assertTrue(relationshipToken.hasType());
		assertEquals("T", relationshipToken.getType());
		NodeToken endToken = (NodeToken) rule.getDescriptor().getToken(5);
		assertEquals(Token.Type.NODE, endToken.getTokenType());
		assertFalse(endToken.hasName());
	}

	@Test
	public void testRelationshipExclusionByTypeAndEndNodeRule() throws Exception {
		String source = "()-[:T]-!(B)";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("N-R-!N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(2) instanceof RelationshipToken);
		assertTrue(rule.getDescriptor().getToken(5) instanceof NodeToken);
		NodeToken startToken = (NodeToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.NODE, startToken.getTokenType());
		assertFalse(startToken.hasName());
		RelationshipToken relationshipToken = (RelationshipToken) rule.getDescriptor().getToken(2);
		assertEquals(Token.Type.REL, relationshipToken.getTokenType());
		assertFalse(relationshipToken.hasName());
		assertTrue(relationshipToken.hasType());
		assertEquals("T", relationshipToken.getType());
		NodeToken endToken = (NodeToken) rule.getDescriptor().getToken(5);
		assertEquals(Token.Type.NODE, endToken.getTokenType());
		assertTrue(endToken.hasName());
		assertEquals("B", endToken.getName());
	}

	@Test
	public void testLoadingRelationshipExclusionByNameRule() throws Exception {
		// perform first call to inject relationship
		String source =
				"(A) {\"name\": \"Alice\"}\n" +
				"(B) {\"name\": \"Bob\"}\n" +
				"(A)-[R:KNOWS]->(B) {\"since\": 1977}\n" +
				"";
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		// build params for second call from output of first
		Relationship r = (Relationship) out.get("[R]");
		assertNotNull(r);
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(1);
		params.put("R", r);
		// make second call to remove relationship
		source = "![R]";
		out = Geoff.loadIntoNeo4j(new StringReader(source), db, params);
		// check results
		r = (Relationship) out.get("[R]");
		assertNull(r);
	}

	@Test
	public void testLoadingRelationshipExclusionByTypeAndBothNodesRule() throws Exception {
		// perform first call to inject relationship
		String source =
				"(A) {\"name\": \"Alice\"}\n" +
				"(B) {\"name\": \"Bob\"}\n" +
				"(A)-[:KNOWS]->(B) {\"since\": 1977}\n" +
				"";
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		// build params for second call from output of first
		Node a = (Node) out.get("(A)");
		assertNotNull(a);
		Node b = (Node) out.get("(B)");
		assertNotNull(b);
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(2);
		params.put("A", a);
		params.put("B", b);
		// make second call to remove relationship
		source = "(A)-[:KNOWS]-!(B)";
		out = Geoff.loadIntoNeo4j(new StringReader(source), db, params);
		// check results
		a = (Node) out.get("(A)");
		assertNotNull(a);
		b = (Node) out.get("(B)");
		assertNotNull(b);
		assertNull(a.getSingleRelationship(DynamicRelationshipType.withName("KNOWS"), Direction.OUTGOING));
		assertNull(b.getSingleRelationship(DynamicRelationshipType.withName("KNOWS"), Direction.INCOMING));
	}

	@Test
	public void testLoadingRelationshipExclusionByTypeAndStartNodeRule() throws Exception {
		// perform first call to inject relationship
		String source =
				"(A) {\"name\": \"Alice\"}\n" +
				"(B) {\"name\": \"Bob\"}\n" +
				"(A)-[:KNOWS]->(B) {\"since\": 1977}\n" +
				"";
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		// build params for second call from output of first
		Node a = (Node) out.get("(A)");
		assertNotNull(a);
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(1);
		params.put("A", a);
		// make second call to remove relationship
		source = "(A)-[:KNOWS]-!()";
		out = Geoff.loadIntoNeo4j(new StringReader(source), db, params);
		// check results
		a = (Node) out.get("(A)");
		assertNotNull(a);
		assertNull(a.getSingleRelationship(DynamicRelationshipType.withName("KNOWS"), Direction.OUTGOING));
	}

	@Test
	public void testLoadingRelationshipExclusionByTypeAndEndNodeRule() throws Exception {
		// perform first call to inject relationship
		String source =
				"(A) {\"name\": \"Alice\"}\n" +
				"(B) {\"name\": \"Bob\"}\n" +
				"(A)-[:KNOWS]->(B) {\"since\": 1977}\n" +
				"";
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		// build params for second call from output of first
		Node b = (Node) out.get("(B)");
		assertNotNull(b);
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(1);
		params.put("B", b);
		// make second call to remove relationship
		source = "()-[:KNOWS]-!(B)";
		out = Geoff.loadIntoNeo4j(new StringReader(source), db, params);
		// check results
		b = (Node) out.get("(B)");
		assertNotNull(b);
		assertNull(b.getSingleRelationship(DynamicRelationshipType.withName("KNOWS"), Direction.INCOMING));
	}

}
