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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.geoff.Geoff;
import org.neo4j.geoff.Subgraph;
import org.neo4j.geoff.except.SubgraphError;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.IndexHits;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.neo4j.geoff.test.TestDatabase.*;

public class Neo4jGraphInsertTest extends TestBase {

	@Before
	public void setUp(){
		db = new TestDatabase();
	}

	@Test
	public void canCreateNode() throws Exception {
		Subgraph geoff = new Subgraph(ALICE);
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		assertAlice((Node) out.get("(A)"));
		db.assertNodeCount(2);
	}

	@Test
	public void canCreateNodeWithBooleanListProperty() throws Exception {
		Subgraph geoff = new Subgraph("(a) {\"sequence\": [false, false, true, true, false]}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		Node node = (Node) out.get("(a)");
		boolean[] seq = (boolean[]) node.getProperty("sequence");
		assertEquals(5, seq.length);
		assertEquals(false, seq[0]);
		assertEquals(false, seq[1]);
		assertEquals(true, seq[2]);
		assertEquals(true, seq[3]);
		assertEquals(false, seq[4]);
		db.assertNodeCount(2);
	}

	@Test
	public void canCreateNodeWithIntegerListProperty() throws Exception {
		Subgraph geoff = new Subgraph("(fib) {\"sequence\": [1,1,2,3,5,8,13,21,35]}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		Node node = (Node) out.get("(fib)");
		int[] seq = (int[]) node.getProperty("sequence");
		assertEquals(9, seq.length);
		assertEquals(1, seq[0]);
		assertEquals(1, seq[1]);
		assertEquals(2, seq[2]);
		assertEquals(3, seq[3]);
		assertEquals(5, seq[4]);
		assertEquals(8, seq[5]);
		assertEquals(13, seq[6]);
		assertEquals(21, seq[7]);
		assertEquals(35, seq[8]);
		db.assertNodeCount(2);
	}

	@Test
	public void canCreateNodeWithDoubleListProperty() throws Exception {
		Subgraph geoff = new Subgraph("(a) {\"sequence\": [1.0, 1.2, 1.4, 1.6, 1.8, 2.0]}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		Node node = (Node) out.get("(a)");
		double[] seq = (double[]) node.getProperty("sequence");
		assertEquals(6, seq.length);
		assertEquals(1.0, seq[0]);
		assertEquals(1.2, seq[1]);
		assertEquals(1.4, seq[2]);
		assertEquals(1.6, seq[3]);
		assertEquals(1.8, seq[4]);
		assertEquals(2.0, seq[5]);
		db.assertNodeCount(2);
	}

	@Test
	public void canCreateNodeWithStringListProperty() throws Exception {
		Subgraph geoff = new Subgraph();
		geoff.add("(fib) {\"sequence\": [\"one\",\"one\",\"two\",\"three\",\"five\",\"eight\",\"thirteen\"]}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		Node node = (Node) out.get("(fib)");
		String[] seq = (String[]) node.getProperty("sequence");
		assertEquals(7, seq.length);
		assertEquals("one", seq[0]);
		assertEquals("one", seq[1]);
		assertEquals("two", seq[2]);
		assertEquals("three", seq[3]);
		assertEquals("five", seq[4]);
		assertEquals("eight", seq[5]);
		assertEquals("thirteen", seq[6]);
		db.assertNodeCount(2);
	}

	@Test
	public void canCreateNodeWithCastStringListProperty() throws Exception {
		Subgraph geoff = new Subgraph();
		geoff.add("(fib) {\"sequence\": [\"one\",\"one\",\"two\",\"three\",\"five\",\"eight\",\"thirteen\",21,35]}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		Node node = (Node) out.get("(fib)");
		String[] seq = (String[]) node.getProperty("sequence");
		assertEquals(9, seq.length);
		assertEquals("one", seq[0]);
		assertEquals("one", seq[1]);
		assertEquals("two", seq[2]);
		assertEquals("three", seq[3]);
		assertEquals("five", seq[4]);
		assertEquals("eight", seq[5]);
		assertEquals("thirteen", seq[6]);
		assertEquals("21", seq[7]);
		assertEquals("35", seq[8]);
		db.assertNodeCount(2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void failsOnIllegalListProperty() throws Exception {
		Subgraph geoff = new Subgraph("(fib) {\"sequence\": [1,1.0,\"two\",3,5,8,13,21,35]}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNotNull(out);
	}

	@Test
	public void canUpdateNodeProperties() throws Exception {
		Subgraph geoff = new Subgraph("(A) {\"foo\": \"bar\"}", ALICE);
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		Node node = (Node) out.get("(A)");
		assertFalse(node.hasProperty("foo"));
		assertAlice(node);
		db.assertNodeCount(2);
	}

	@Test
	public void canUpdateNodeToEraseProperties() throws Exception {
		Subgraph geoff = new Subgraph(ALICE, "(A) {}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		Node node = (Node) out.get("(A)");
		assertNotNull(node);
		assertFalse(node.hasProperty("name"));
		int propertyCount = 0;
		for (String key : node.getPropertyKeys()) {
			propertyCount++;
		}
		assertEquals(0, propertyCount);
		db.assertNodeCount(2);
	}

	@Test
	public void canUpdateNodeWithoutErasingProperties() throws Exception {
		Subgraph geoff = new Subgraph(ALICE, "(A)");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		assertAlice((Node) out.get("(A)"));
		db.assertNodeCount(2);
	}

	@Test
	public void canCreateMultipleNodes() throws Exception {
		Subgraph geoff = new Subgraph(ALICE, BOB, CAROL);
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		assertAlice((Node) out.get("(A)"));
		assertBob((Node) out.get("(B)"));
		assertCarol((Node) out.get("(C)"));
		db.assertNodeCount(4);
	}

	@Test
	public void canCreateMultipleAnonymousNodes() throws Exception {
		Subgraph geoff = new Subgraph();
		geoff.add("() {\"name\": \"Alice Allison\"}");
		geoff.add("() {\"name\": \"Bob Robertson\"}");
		geoff.add("() {\"name\": \"Carol Carlson\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		db.assertNodeCount(4);
	}

	@Test
	public void canCreateAndUpdateNodeSet() throws Exception {
		Subgraph geoff = new Subgraph();
		geoff.add("(N.1) {\"name\": \"Alice Allison\"}");
		geoff.add("(N.2) {\"name\": \"Bob Robertson\"}");
		geoff.add("(N.3) {\"name\": \"Carol Carlson\"}");
		geoff.add("(N)   {\"name\": \"Bob Robertson\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		//dumpParams(out);
		assertBob((Node) out.get("(N.1)"));
		assertBob((Node) out.get("(N.2)"));
		assertBob((Node) out.get("(N.3)"));
		db.assertNodeCount(4);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateShortAnonymousUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("[]");
		Geoff.insertIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateAnonymousUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[]->()");
		Geoff.insertIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateAnonymousUntypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[]->()");
		Geoff.insertIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateAnonymousUntypedRelationshipAB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[]->(B)");
		Geoff.insertIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateAnonymousUntypedRelationshipB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[]->(B)");
		Geoff.insertIntoNeo4j(geoff, db, null);
	}

	/////////////////////

	@Test
	public void canCreateShortAnonymousTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("[:KNOWS]");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertEquals(0, out.size());
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateAnonymousTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[:KNOWS]->()");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertEquals(0, out.size());
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateAnonymousTypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[:KNOWS]->()");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNodesExist(out, "(A)");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateAnonymousTypedRelationshipAB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[:KNOWS]->(B)");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNodesExist(out, "(A)", "(B)");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateAnonymousTypedRelationshipB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[:KNOWS]->(B)");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNodesExist(out, "(B)");
		db.assertCounts(3, 1);
	}

	/////////////////////

	@Test(expected = SubgraphError.class)
	public void cannotCreateShortNamedUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("[R]");
		Geoff.insertIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateNamedUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[R]->()");
		Geoff.insertIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateNamedUntypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[R]->()");
		Geoff.insertIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateNamedUntypedRelationshipAB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[R]->(B)");
		Geoff.insertIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateNamedUntypedRelationshipB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[R]->(B)");
		Geoff.insertIntoNeo4j(geoff, db, null);
	}

	/////////////////////

	@Test
	public void canCreateShortNamedTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("[R:KNOWS]");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateNamedTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[R:KNOWS]->()");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateNamedTypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[R:KNOWS]->()");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertRelationshipsExist(out, "[R]");
		assertNodesExist(out, "(A)");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateNamedTypedRelationshipAB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[R:KNOWS]->(B)");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertRelationshipsExist(out, "[R]");
		assertNodesExist(out, "(A)", "(B)");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateNamedTypedRelationshipB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[R:KNOWS]->(B)");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
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
		Subgraph geoff = new Subgraph("[R] {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		Subgraph geoff = new Subgraph("()-[R]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedUntypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		Subgraph geoff = new Subgraph("(A)-[R]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
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
		Subgraph geoff = new Subgraph("(A)-[R]->(B) {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
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
		Subgraph geoff = new Subgraph("()-[R]->(B) {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
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
		Subgraph geoff = new Subgraph("[R:KNOWS] {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		Subgraph geoff = new Subgraph("()-[R:KNOWS]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedTypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		Subgraph geoff = new Subgraph("(A)-[R:KNOWS]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
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
		Subgraph geoff = new Subgraph("(A)-[R:KNOWS]->(B) {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
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
		Subgraph geoff = new Subgraph("()-[R:KNOWS]->(B) {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
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
		Subgraph geoff = new Subgraph("[R:HATES] {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
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
		Subgraph geoff = new Subgraph("()-[R:HATES]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
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
		Subgraph geoff = new Subgraph("(A)-[R:HATES]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
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
		Subgraph geoff = new Subgraph("(A)-[R:HATES]->(B) {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
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
		Subgraph geoff = new Subgraph("()-[R:HATES]->(B) {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, params);
		assertRelationshipsExist(out, "[R]");
		Relationship relationship = (Relationship) out.get("[R]");
		assertFalse(relationship.hasProperty("foo"));
		assertTrue(relationship.hasProperty("since"));
		assertEquals(1977, relationship.getProperty("since"));
	}

	/////////////////////

	@Test
	public void canCreateMultipleRelationshipsWithImplicitNodeCreation() throws Exception {
		Subgraph geoff = new Subgraph();
		geoff.add("(A)-[R_AB:LOVES]->(B)");
		geoff.add("(B)-[R_BC:LOVES]->(C)");
		geoff.add("(A)-[R_AC:HATES]->(C)");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertRelationshipsExist(out, "[R_AB]", "[R_BC]", "[R_AC]");
		assertNodesExist(out, "(A)", "(B)", "(C)");
		db.assertCounts(4, 3);
	}

	@Test
	public void canCreateMultipleRelationshipsWithExplicitNodeCreation() throws Exception {
		Subgraph geoff = new Subgraph();
		geoff.add(ALICE);
		geoff.add(BOB);
		geoff.add(CAROL);
		geoff.add("(A)-[R_AB:LOVES]->(B)");
		geoff.add("(B)-[R_BC:LOVES]->(C)");
		geoff.add("(A)-[R_AC:HATES]->(C)");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertRelationshipsExist(out, "[R_AB]", "[R_BC]", "[R_AC]");
		assertNodesExist(out, "(A)", "(B)", "(C)");
		db.assertCounts(4, 3);
	}

	@Test
	public void canCreateMultipleRelationshipsWithExplicitNodeCreationInReverse() throws Exception {
		Subgraph geoff = new Subgraph();
		geoff.add(ALICE);
		geoff.add(BOB);
		geoff.add(CAROL);
		geoff.add("(B)<-[AB:LOVES]-(A)");
		geoff.add("(C)<-[BC:LOVES]-(B)");
		geoff.add("(C)<-[AC:HATES]-(A)");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertRelationshipsExist(out, "[AB]", "[BC]", "[AC]");
		assertNodesExist(out, "(A)", "(B)", "(C)");
		db.assertCounts(4, 3);
	}

	@Test
	public void canCreateTwoWayRelationships() throws Exception {
		Subgraph geoff = new Subgraph();
		geoff.add(ALICE);
		geoff.add(BOB);
		geoff.add(CAROL);
		geoff.add("(A)<-[AB:KNOWS]->(B)");
		geoff.add("(A)<-[BC:KNOWS]->(C)");
		geoff.add("(B)<-[AC:KNOWS]->(C)");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		for (String name : out.keySet()) {
			System.out.println(name);
		}
		db.assertCounts(4, 6);
		assertRelationshipsExist(out, "[AB.1]", "[AC.1]", "[BC.1]", "[AB.2]", "[AC.2]", "[BC.2]");
		assertNodesExist(out, "(A)", "(B)", "(C)");
		Assert.assertEquals(
			((Relationship) out.get("[AB.1]")).getStartNode(),
			((Relationship) out.get("[AB.2]")).getEndNode()
		);
		Assert.assertEquals(
			((Relationship) out.get("[AB.1]")).getEndNode(),
			((Relationship) out.get("[AB.2]")).getStartNode()
		);
		Assert.assertEquals(
			((Relationship) out.get("[AB.1]")).getType(),
			((Relationship) out.get("[AB.2]")).getType()
		);
	}

	@Test
	public void canCreateRelationshipsFromNodeSets() throws Exception {
		Subgraph geoff = new Subgraph();
		geoff.add("(A.1) {\"name\": \"Alice Allison\"}");
		geoff.add("(A.2) {\"name\": \"Amanda Allison\"}");
		geoff.add("(B.1) {\"name\": \"Bob Robertson\"}");
		geoff.add("(B.2) {\"name\": \"Bert Robertson\"}");
		geoff.add("(B.3) {\"name\": \"Bill Robertson\"}");
		geoff.add("(C)   {\"name\": \"Carol Carlson\"}");
		geoff.add("(A)-[R_AB:KNOWS]->(B) {\"status\": \"friends\"}");
		geoff.add("(B)-[R_BC:KNOWS]->(C) {\"status\": \"colleagues\"}");
		Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		//dumpParams(out);
		db.assertCounts(7, 9);
	}

	@Test
	public void canInsertIndexEntryForExistingNode() throws Exception {
		Subgraph subgraph = new Subgraph(
			"(A) {\"name\": \"Alice Allison\"}\n" +
			"(A)<=|People| {\"name\": \"Allison, Alice\"}\n"
		);
		Geoff.insertIntoNeo4j(subgraph, db, null);
		assertTrue(db.index().existsForNodes("People"));
		IndexHits<Node> hits = db.index().forNodes("People").get("name", "Allison, Alice");
		assertEquals(1, hits.size());
		Node node1 = hits.next();
		assertEquals("Alice Allison", node1.getProperty("name"));
		db.assertNodeCount(2);
	}

	@Test
	public void canInsertIndexEntryForNonExistingNode() throws Exception {
		Subgraph subgraph = new Subgraph("(A)<=|People| {\"name\": \"Allison, Alice\"}");
		Geoff.insertIntoNeo4j(subgraph, db, null);
		assertTrue(db.index().existsForNodes("People"));
		IndexHits<Node> hits = db.index().forNodes("People").get("name", "Allison, Alice");
		assertEquals(1, hits.size());
		db.assertNodeCount(2);
	}

	@Test
	public void canInsertMultipleIndexEntriesForExistingNode() throws Exception {
		Subgraph subgraph = new Subgraph("(A) {\"name\": \"Alice Allison\"}");
		Map<String, PropertyContainer> params = Geoff.insertIntoNeo4j(subgraph, db, null);
		subgraph = new Subgraph("(A)<=|People| {\"name\": \"Allison, Alice\"}");
		Geoff.insertIntoNeo4j(subgraph, db, params);
		Geoff.insertIntoNeo4j(subgraph, db, params);
		assertTrue(db.index().existsForNodes("People"));
		IndexHits<Node> hits = db.index().forNodes("People").get("name", "Allison, Alice");
		assertEquals(1, hits.size());
		Node node1 = hits.next();
		assertEquals("Alice Allison", node1.getProperty("name"));
		db.assertNodeCount(2);
	}

	@Test
	public void canInsertMultipleIndexEntriesForNonExistingNode() throws Exception {
		Subgraph subgraph = new Subgraph("(A)<=|People| {\"name\": \"Allison, Alice\"}");
		Geoff.insertIntoNeo4j(subgraph, db, null);
		Geoff.insertIntoNeo4j(subgraph, db, null);
		assertTrue(db.index().existsForNodes("People"));
		IndexHits<Node> hits = db.index().forNodes("People").get("name", "Allison, Alice");
		assertEquals(2, hits.size());
		Node node1 = hits.next();
		Node node2 = hits.next();
		assertFalse(node1.getId() == node2.getId());
		db.assertNodeCount(3);
	}

	@Test
	public void canInsertIndexEntryForExistingRelationship() throws Exception {
		Subgraph subgraph = new Subgraph(ALICE, BOB, ALICE_KNOWS_BOB,
			"[AB]<=|Friendships| {\"name\": \"Alice & Bob\"}"
		);
		Geoff.insertIntoNeo4j(subgraph, db, null);
		assertTrue(db.index().existsForRelationships("Friendships"));
		IndexHits<Relationship> hits = db.index().forRelationships("Friendships").get("name", "Alice & Bob");
		assertEquals(1, hits.size());
		Relationship rel1 = hits.next();
		assertEquals(1977, rel1.getProperty("since"));
		db.assertCounts(3, 1);
	}

	@Test
	public void canInsertIndexEntryForNonExistingRelationship() throws Exception {
		Subgraph subgraph = new Subgraph("[AB:KNOWS]<=|Friendships| {\"name\": \"Alice & Bob\"}");
		Geoff.insertIntoNeo4j(subgraph, db, null);
		assertTrue(db.index().existsForRelationships("Friendships"));
		IndexHits<Relationship> hits = db.index().forRelationships("Friendships").get("name", "Alice & Bob");
		assertEquals(1, hits.size());
		db.assertCounts(3, 1);
	}

	@Test
	public void canInsertMultipleIndexEntriesForExistingRelationship() throws Exception {
		Subgraph subgraph = new Subgraph(ALICE, BOB, ALICE_KNOWS_BOB);
		Map<String, PropertyContainer> params = Geoff.insertIntoNeo4j(subgraph, db, null);
		subgraph = new Subgraph("[AB]<=|Friendships| {\"name\": \"Alice & Bob\"}");
		Geoff.insertIntoNeo4j(subgraph, db, params);
		Geoff.insertIntoNeo4j(subgraph, db, params);
		assertTrue(db.index().existsForRelationships("Friendships"));
		IndexHits<Relationship> hits = db.index().forRelationships("Friendships").get("name", "Alice & Bob");
		assertEquals(1, hits.size());
		Relationship rel1 = hits.next();
		assertEquals(1977, rel1.getProperty("since"));
		db.assertCounts(3, 1);
	}

	@Test
	public void canInsertMultipleIndexEntriesForNonExistingRelationship() throws Exception {
		Subgraph subgraph = new Subgraph("[AB:KNOWS]<=|Friendships| {\"name\": \"Alice & Bob\"}");
		Geoff.insertIntoNeo4j(subgraph, db, null);
		Geoff.insertIntoNeo4j(subgraph, db, null);
		assertTrue(db.index().existsForRelationships("Friendships"));
		IndexHits<Relationship> hits = db.index().forRelationships("Friendships").get("name", "Alice & Bob");
		assertEquals(2, hits.size());
		Relationship rel1 = hits.next();
		Relationship rel2 = hits.next();
		assertFalse(rel1.getId() == rel2.getId());
		db.assertCounts(5, 2);
	}

	@Test
	public void canInsertManySubgraphs() throws Exception {
		for (int i = 0; i < 1000; i++) {
			TestTransaction txn = new TestTransaction(1000001 + i);
			Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(txn, db, null);
			assertNotNull(out);
		}
		db.assertCounts(2001, 1000);
	}

	@Test
	public void canInsertGraphWithInputParams() throws Exception {
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
		Subgraph subgraph = new Subgraph(
			"(foo) {\"position\": \"north-east\"}",
			"(bar) {\"position\": \"south-east\"}",
			"(one) {\"position\": \"north-west\"}",
			"(two) {\"position\": \"south-west\"}",
			"[\"(foo)-[:SOUTH]->(bar)\", \"(bar)-[:WEST]->(two)\", \"(two)-[:NORTH]->(one)\", \"(one)-[:EAST]->(foo)\"]"
		);
		org.neo4j.graphdb.Transaction tx = db.beginTx();
		Node nodeOne = db.createNode();
		Node nodeTwo = db.createNode();
		tx.success();
		tx.finish();
		HashMap<String,Node> params = new HashMap<String,Node>(1);
		params.put("one", nodeOne);
		params.put("two", nodeTwo);
		Map<String,PropertyContainer> entities = Geoff.insertIntoNeo4j(subgraph, db, params);
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
		Assert.assertEquals(nodeOne.getProperty("position"), "north-west");
		Assert.assertEquals(nodeTwo.getProperty("position"), "south-west");
		Assert.assertEquals(nodeFoo.getProperty("position"), "north-east");
		Assert.assertEquals(nodeBar.getProperty("position"), "south-east");
	}

}
