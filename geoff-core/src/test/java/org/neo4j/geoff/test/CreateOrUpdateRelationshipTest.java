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

import org.junit.Test;
import org.neo4j.geoff.Geoff;
import org.neo4j.geoff.Rule;
import org.neo4j.geoff.except.RuleApplicationException;
import org.neo4j.geoff.store.NodeToken;
import org.neo4j.geoff.store.RelationshipToken;
import org.neo4j.geoff.store.Token;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.neo4j.geoff.test.TestDatabase.*;

public class CreateOrUpdateRelationshipTest extends TestBase {

	@Test
	public void canParseShortCreateOrUpdateRelationshipRule() throws Exception {
		String source = "[:KNOWS] {\"since\": 1977}";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("R", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof RelationshipToken);
		RelationshipToken relationshipToken = (RelationshipToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.REL, relationshipToken.getTokenType());
		assertFalse(relationshipToken.hasName());
		assertTrue(relationshipToken.hasType());
		assertEquals("KNOWS", relationshipToken.getType());
		assertTrue(rule.getData().containsKey("since"));
		assertEquals(1977, rule.getData().get("since"));
	}

	@Test
	public void canParseCreateOrUpdateRelationshipRule() throws Exception {
		String source = "(A)-[:KNOWS]->(B) {\"since\": 1977}";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("N-R->N", rule.getDescriptor().getPattern());
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
		assertEquals("KNOWS", relationshipToken.getType());
		NodeToken endToken = (NodeToken) rule.getDescriptor().getToken(5);
		assertEquals(Token.Type.NODE, endToken.getTokenType());
		assertTrue(endToken.hasName());
		assertEquals("B", endToken.getName());
		assertTrue(rule.getData().containsKey("since"));
		assertEquals(1977, rule.getData().get("since"));
	}

	/////////////////////

	@Test(expected = RuleApplicationException.class)
	public void cannotCreateShortAnonymousUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("[]");
		Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
	}

	@Test(expected = RuleApplicationException.class)
	public void cannotCreateAnonymousUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("()-[]->()");
		Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
	}

	@Test(expected = RuleApplicationException.class)
	public void cannotCreateAnonymousUntypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("(A)-[]->()");
		Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
	}

	@Test(expected = RuleApplicationException.class)
	public void cannotCreateAnonymousUntypedRelationshipAB() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("(A)-[]->(B)");
		Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
	}

	@Test(expected = RuleApplicationException.class)
	public void cannotCreateAnonymousUntypedRelationshipB() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("()-[]->(B)");
		Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
	}

	/////////////////////

	@Test
	public void canCreateShortAnonymousTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("[:KNOWS]");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertEquals(0, out.size());
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateAnonymousTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("()-[:KNOWS]->()");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertEquals(0, out.size());
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateAnonymousTypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("(A)-[:KNOWS]->()");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertNodesExist(out, "(A)");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateAnonymousTypedRelationshipAB() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("(A)-[:KNOWS]->(B)");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertNodesExist(out, "(A)", "(B)");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateAnonymousTypedRelationshipB() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("()-[:KNOWS]->(B)");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertNodesExist(out, "(B)");
		db.assertCounts(3, 1);
	}

	/////////////////////

	@Test(expected = RuleApplicationException.class)
	public void cannotCreateShortNamedUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("[R]");
		Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
	}

	@Test(expected = RuleApplicationException.class)
	public void cannotCreateNamedUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("()-[R]->()");
		Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
	}

	@Test(expected = RuleApplicationException.class)
	public void cannotCreateNamedUntypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("(A)-[R]->()");
		Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
	}

	@Test(expected = RuleApplicationException.class)
	public void cannotCreateNamedUntypedRelationshipAB() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("(A)-[R]->(B)");
		Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
	}

	@Test(expected = RuleApplicationException.class)
	public void cannotCreateNamedUntypedRelationshipB() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("()-[R]->(B)");
		Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
	}

	/////////////////////

	@Test
	public void canCreateShortNamedTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("[R:KNOWS]");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateNamedTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("()-[R:KNOWS]->()");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateNamedTypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("(A)-[R:KNOWS]->()");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertRelationshipsExist(out, "[R]");
		assertNodesExist(out, "(A)");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateNamedTypedRelationshipAB() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("(A)-[R:KNOWS]->(B)");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertRelationshipsExist(out, "[R]");
		assertNodesExist(out, "(A)", "(B)");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateNamedTypedRelationshipB() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder("()-[R:KNOWS]->(B)");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertRelationshipsExist(out, "[R]");
		assertNodesExist(out, "(B)");
		db.assertCounts(3, 1);
	}

	/////////////////////

	@Test
	public void canUpdateShortNamedUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("[R] {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("()-[R]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedUntypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("(A)-[R]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		assertNodesExist(out, "(A)");
		assertAlice((Node) out.get("(A)"));
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedUntypedRelationshipAB() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("(A)-[R]->(B) {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		assertNodesExist(out, "(A)", "(B)");
		assertAlice((Node) out.get("(A)"));
		assertBob((Node) out.get("(B)"));
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedUntypedRelationshipB() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("()-[R]->(B) {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		assertNodesExist(out, "(B)");
		assertBob((Node) out.get("(B)"));
		db.assertCounts(3, 1);
	}

	/////////////////////

	@Test
	public void canUpdateShortNamedTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("[R:KNOWS] {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("()-[R:KNOWS]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedTypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("(A)-[R:KNOWS]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		assertNodesExist(out, "(A)");
		assertAlice((Node) out.get("(A)"));
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedTypedRelationshipAB() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("(A)-[R:KNOWS]->(B) {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		assertNodesExist(out, "(A)", "(B)");
		assertAlice((Node) out.get("(A)"));
		assertBob((Node) out.get("(B)"));
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedTypedRelationshipB() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("()-[R:KNOWS]->(B) {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		assertNodesExist(out, "(B)");
		assertBob((Node) out.get("(B)"));
		db.assertCounts(3, 1);
	}

	/////////////////////

	@Test
	public void ignoresShortNamedIncorrectlyTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("[R:HATES] {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		Relationship relationship = (Relationship) out.get("[R]");
		assertFalse(relationship.hasProperty("foo"));
		assertTrue(relationship.hasProperty("since"));
		assertEquals(1977, relationship.getProperty("since"));
	}

	@Test
	public void ignoresNamedIncorrectlyTypedRelationship() throws Exception {
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("()-[R:HATES]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		Relationship relationship = (Relationship) out.get("[R]");
		assertFalse(relationship.hasProperty("foo"));
		assertTrue(relationship.hasProperty("since"));
		assertEquals(1977, relationship.getProperty("since"));
	}

	@Test
	public void ignoresNamedIncorrectlyTypedRelationshipA() throws Exception {
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("(A)-[R:HATES]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		Relationship relationship = (Relationship) out.get("[R]");
		assertFalse(relationship.hasProperty("foo"));
		assertTrue(relationship.hasProperty("since"));
		assertEquals(1977, relationship.getProperty("since"));
	}

	@Test
	public void ignoresNamedIncorrectlyTypedRelationshipAB() throws Exception {
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("(A)-[R:HATES]->(B) {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		Relationship relationship = (Relationship) out.get("[R]");
		assertFalse(relationship.hasProperty("foo"));
		assertTrue(relationship.hasProperty("since"));
		assertEquals(1977, relationship.getProperty("since"));
	}

	@Test
	public void ignoresNamedIncorrectlyTypedRelationshipB() throws Exception {
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		TestGeoffBuilder geoff = new TestGeoffBuilder("()-[R:HATES]->(B) {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertRelationshipsExist(out, "[R]");
		Relationship relationship = (Relationship) out.get("[R]");
		assertFalse(relationship.hasProperty("foo"));
		assertTrue(relationship.hasProperty("since"));
		assertEquals(1977, relationship.getProperty("since"));
	}

	/////////////////////

	@Test
	public void canCreateMultipleRelationshipsWithImplicitNodeCreation() throws Exception {
		TestGeoffBuilder geoff = new TestGeoffBuilder();
		geoff.append("(A)-[R_AB:LOVES]->(B)");
		geoff.append("(B)-[R_BC:LOVES]->(C)");
		geoff.append("(A)-[R_AC:HATES]->(C)");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertRelationshipsExist(out, "[R_AB]", "[R_BC]", "[R_AC]");
		assertNodesExist(out, "(A)", "(B)", "(C)");
		db.assertCounts(4, 3);
	}

	@Test
	public void canCreateMultipleRelationshipsWithExplicitNodeCreation() throws Exception {
		TestGeoffBuilder geoff = new TestGeoffBuilder();
		geoff.appendAlice();
		geoff.appendBob();
		geoff.appendCarol();
		geoff.append("(A)-[R_AB:LOVES]->(B)");
		geoff.append("(B)-[R_BC:LOVES]->(C)");
		geoff.append("(A)-[R_AC:HATES]->(C)");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertRelationshipsExist(out, "[R_AB]", "[R_BC]", "[R_AC]");
		assertNodesExist(out, "(A)", "(B)", "(C)");
		db.assertCounts(4, 3);
	}

	@Test
	public void canCreateRelationshipsFromNodeSets() throws Exception {
		TestGeoffBuilder geoff = new TestGeoffBuilder();
		geoff.append("(A.1) {\"name\": \"Alice Allison\"}");
		geoff.append("(A.2) {\"name\": \"Amanda Allison\"}");
		geoff.append("(B.1) {\"name\": \"Bob Robertson\"}");
		geoff.append("(B.2) {\"name\": \"Bert Robertson\"}");
		geoff.append("(B.3) {\"name\": \"Bill Robertson\"}");
		geoff.append("(C)   {\"name\": \"Carol Carlson\"}");
		geoff.append("(A)-[R_AB:KNOWS]->(B) {\"status\": \"friends\"}");
		geoff.append("(B)-[R_BC:KNOWS]->(C) {\"status\": \"colleagues\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertNotNull(out);
		//dumpParams(out);
		db.assertCounts(7, 9);
	}

}
