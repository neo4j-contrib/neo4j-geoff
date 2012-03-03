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
import org.neo4j.geoff.except.SyntaxError;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.IndexHits;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.neo4j.geoff.test.TestDatabase.*;

public class Neo4jGraphMergeTest extends TestBase {

	@Before
	public void setUp(){
		db = new TestDatabase();
	}

	@Test
	public void canCreateNode() throws Exception {
		Subgraph geoff = new Subgraph();
		geoff.add(ALICE);
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		assertAlice((Node) out.get("(A)"));
		db.assertNodeCount(2);
	}

	@Test
	public void canCreateNodeWithBooleanListProperty() throws Exception {
		Subgraph geoff = new Subgraph();
		geoff.add("(a) {\"sequence\": [false, false, true, true, false]}");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
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
		Subgraph geoff = new Subgraph();
		geoff.add("(fib) {\"sequence\": [1,1,2,3,5,8,13,21,35]}");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
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
		Subgraph geoff = new Subgraph();
		geoff.add("(a) {\"sequence\": [1.0, 1.2, 1.4, 1.6, 1.8, 2.0]}");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
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
		Subgraph geoff = new Subgraph();
		geoff.add("(fib) {\"sequence\": [1,1.0,\"two\",3,5,8,13,21,35]}");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertNotNull(out);
	}

	@Test
	public void canUpdateNodeProperties() throws Exception {
		Subgraph geoff = new Subgraph();
		geoff.add("(A) {\"foo\": \"bar\"}");
		geoff.add(ALICE);
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		Node node = (Node) out.get("(A)");
		assertFalse(node.hasProperty("foo"));
		assertAlice(node);
		db.assertNodeCount(2);
	}

	@Test
	public void canUpdateNodeToEraseProperties() throws Exception {
		Subgraph geoff = new Subgraph(ALICE, "(A) {}");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		assertAlice((Node) out.get("(A)"));
		db.assertNodeCount(2);
	}

	@Test
	public void canCreateMultipleNodes() throws Exception {
		Subgraph geoff = new Subgraph(ALICE, BOB, CAROL);
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
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
		Geoff.mergeIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateAnonymousUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[]->()");
		Geoff.mergeIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateAnonymousUntypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[]->()");
		Geoff.mergeIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateAnonymousUntypedRelationshipAB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[]->(B)");
		Geoff.mergeIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateAnonymousUntypedRelationshipB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[]->(B)");
		Geoff.mergeIntoNeo4j(geoff, db, null);
	}

	/////////////////////

	@Test
	public void canCreateShortAnonymousTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("[:KNOWS]");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertEquals(0, out.size());
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateAnonymousTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[:KNOWS]->()");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertEquals(0, out.size());
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateAnonymousTypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[:KNOWS]->()");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertNodesExist(out, "(A)");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateAnonymousTypedRelationshipAB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[:KNOWS]->(B)");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertNodesExist(out, "(A)", "(B)");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateAnonymousTypedRelationshipB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[:KNOWS]->(B)");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertNodesExist(out, "(B)");
		db.assertCounts(3, 1);
	}

	/////////////////////

	@Test(expected = SubgraphError.class)
	public void cannotCreateShortNamedUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("[R]");
		Geoff.mergeIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateNamedUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[R]->()");
		Geoff.mergeIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateNamedUntypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[R]->()");
		Geoff.mergeIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateNamedUntypedRelationshipAB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[R]->(B)");
		Geoff.mergeIntoNeo4j(geoff, db, null);
	}

	@Test(expected = SubgraphError.class)
	public void cannotCreateNamedUntypedRelationshipB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[R]->(B)");
		Geoff.mergeIntoNeo4j(geoff, db, null);
	}

	/////////////////////

	@Test
	public void canCreateShortNamedTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("[R:KNOWS]");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateNamedTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[R:KNOWS]->()");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateNamedTypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[R:KNOWS]->()");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertRelationshipsExist(out, "[R]");
		assertNodesExist(out, "(A)");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateNamedTypedRelationshipAB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("(A)-[R:KNOWS]->(B)");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertRelationshipsExist(out, "[R]");
		assertNodesExist(out, "(A)", "(B)");
		db.assertCounts(3, 1);
	}

	@Test
	public void canCreateNamedTypedRelationshipB() throws Exception {
		TestDatabase db = new TestDatabase();
		Subgraph geoff = new Subgraph("()-[R:KNOWS]->(B)");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedUntypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		Subgraph geoff = new Subgraph("()-[R]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedUntypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		Subgraph geoff = new Subgraph("(A)-[R]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedTypedRelationship() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		Subgraph geoff = new Subgraph("()-[R:KNOWS]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
		assertRelationshipsExist(out, "[R]");
		db.assertCounts(3, 1);
	}

	@Test
	public void canUpdateNamedTypedRelationshipA() throws Exception {
		TestDatabase db = new TestDatabase();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("[R]", db.createAliceKnowsBob());
		Subgraph geoff = new Subgraph("(A)-[R:KNOWS]->() {\"foo\": \"bar\"}");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, params);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertRelationshipsExist(out, "[R_AB]", "[R_BC]", "[R_AC]");
		assertNodesExist(out, "(A)", "(B)", "(C)");
		db.assertCounts(4, 3);
	}

	@Test
	public void canCreateMultipleRelationshipsWithExplicitNodeCreation() throws Exception {
		Subgraph geoff = new Subgraph(ALICE, BOB, CAROL);
		geoff.add("(A)-[R_AB:LOVES]->(B)");
		geoff.add("(B)-[R_BC:LOVES]->(C)");
		geoff.add("(A)-[R_AC:HATES]->(C)");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertRelationshipsExist(out, "[R_AB]", "[R_BC]", "[R_AC]");
		assertNodesExist(out, "(A)", "(B)", "(C)");
		db.assertCounts(4, 3);
	}

	@Test
	public void canMergeTwoWayRelationships() throws Exception {
		Subgraph geoff = new Subgraph();
		geoff.add(ALICE);
		geoff.add(BOB);
		geoff.add(CAROL);
		Map<String, PropertyContainer> in = Geoff.mergeIntoNeo4j(geoff, db, null);
		geoff.add("(A)<-[AB:KNOWS]->(B)");
		geoff.add("(A)<-[BC:KNOWS]->(C)");
		geoff.add("(B)<-[AC:KNOWS]->(C)");
		for (int i = 0; i < 10; i++) {
			Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, in);
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
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		//dumpParams(out);
		db.assertCounts(7, 9);
	}

	@Test
	public void canReflectRelationshipSet() throws Exception {
		Subgraph geoff = new Subgraph();
		geoff.add("(A) {\"name\": \"Alice Allison\"}");
		geoff.add("(B) {\"name\": \"Bob Robertson\"}");
		geoff.add("(C)   {\"name\": \"Carol Carlson\"}");
		geoff.add("(A)-[:KNOWS]->(B) {\"status\": \"friends\"}");
		geoff.add("(C)-[:KNOWS]->(B) {\"status\": \"colleagues\"}");
		geoff.add("()-[R:KNOWS]->(B)");
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(geoff, db, null);
		assertNotNull(out);
		//dumpParams(out);
		assertTrue(out.containsKey("[R.1]"));
		assertTrue(out.containsKey("[R.2]"));
		db.assertCounts(4, 2);
	}

	@Test
	public void canMergeIndexEntryForExistingNode() throws Exception {
		Subgraph subgraph = new Subgraph(
			"(A) {\"name\": \"Alice Allison\"}\n" +
				"(A)<=|People| {\"name\": \"Allison, Alice\"}\n"
		);
		Geoff.mergeIntoNeo4j(subgraph, db, null);
		assertTrue(db.index().existsForNodes("People"));
		IndexHits<Node> hits = db.index().forNodes("People").get("name", "Allison, Alice");
		assertEquals(1, hits.size());
		Node node1 = hits.next();
		assertEquals("Alice Allison", node1.getProperty("name"));
		db.assertNodeCount(2);
	}

	@Test
	public void canMergeIndexEntryForNonExistingNode() throws Exception {
		Subgraph subgraph = new Subgraph("(A)<=|People| {\"name\": \"Allison, Alice\"}");
		Geoff.mergeIntoNeo4j(subgraph, db, null);
		assertTrue(db.index().existsForNodes("People"));
		IndexHits<Node> hits = db.index().forNodes("People").get("name", "Allison, Alice");
		assertEquals(1, hits.size());
		db.assertNodeCount(2);
	}

	@Test
	public void canMergeMultipleIndexEntriesForExistingNode() throws Exception {
		Subgraph subgraph = new Subgraph("(A) {\"name\": \"Alice Allison\"}");
		Map<String, PropertyContainer> params = Geoff.mergeIntoNeo4j(subgraph, db, null);
		subgraph = new Subgraph("(A)<=|People| {\"name\": \"Allison, Alice\"}");
		Geoff.mergeIntoNeo4j(subgraph, db, params);
		Geoff.mergeIntoNeo4j(subgraph, db, params);
		assertTrue(db.index().existsForNodes("People"));
		IndexHits<Node> hits = db.index().forNodes("People").get("name", "Allison, Alice");
		assertEquals(1, hits.size());
		Node node1 = hits.next();
		assertEquals("Alice Allison", node1.getProperty("name"));
		db.assertNodeCount(2);
	}

	@Test
	public void canMergeMultipleIndexEntriesForNonExistingNode() throws Exception {
		Subgraph subgraph = new Subgraph("(A)<=|People| {\"name\": \"Allison, Alice\"}");
		Geoff.mergeIntoNeo4j(subgraph, db, null);
		Geoff.mergeIntoNeo4j(subgraph, db, null);
		assertTrue(db.index().existsForNodes("People"));
		IndexHits<Node> hits = db.index().forNodes("People").get("name", "Allison, Alice");
		assertEquals(1, hits.size());
		db.assertNodeCount(2);
	}

	@Test
	public void canMergeIndexEntryForExistingRelationship() throws Exception {
		Subgraph subgraph = new Subgraph(ALICE, BOB, ALICE_KNOWS_BOB,
			"[AB]<=|Friendships| {\"name\": \"Alice & Bob\"}"
		);
		Geoff.mergeIntoNeo4j(subgraph, db, null);
		assertTrue(db.index().existsForRelationships("Friendships"));
		IndexHits<Relationship> hits = db.index().forRelationships("Friendships").get("name", "Alice & Bob");
		assertEquals(1, hits.size());
		Relationship rel1 = hits.next();
		assertEquals(1977, rel1.getProperty("since"));
		db.assertCounts(3, 1);
	}

	@Test
	public void canMergeIndexEntryForNonExistingRelationship() throws Exception {
		Subgraph subgraph = new Subgraph("[AB:KNOWS]<=|Friendships| {\"name\": \"Alice & Bob\"}");
		Geoff.mergeIntoNeo4j(subgraph, db, null);
		assertTrue(db.index().existsForRelationships("Friendships"));
		IndexHits<Relationship> hits = db.index().forRelationships("Friendships").get("name", "Alice & Bob");
		assertEquals(1, hits.size());
		db.assertCounts(3, 1);
	}

	@Test
	public void canMergeMultipleIndexEntriesForExistingRelationship() throws Exception {
		Subgraph subgraph = new Subgraph(ALICE, BOB, ALICE_KNOWS_BOB);
		Map<String, PropertyContainer> params = Geoff.mergeIntoNeo4j(subgraph, db, null);
		subgraph = new Subgraph("[AB]<=|Friendships| {\"name\": \"Alice & Bob\"}");
		Geoff.mergeIntoNeo4j(subgraph, db, params);
		Geoff.mergeIntoNeo4j(subgraph, db, params);
		assertTrue(db.index().existsForRelationships("Friendships"));
		IndexHits<Relationship> hits = db.index().forRelationships("Friendships").get("name", "Alice & Bob");
		assertEquals(1, hits.size());
		Relationship rel1 = hits.next();
		assertEquals(1977, rel1.getProperty("since"));
		db.assertCounts(3, 1);
	}

	@Test
	public void canMergeMultipleIndexEntriesForNonExistingRelationship() throws Exception {
		Subgraph subgraph = new Subgraph("[AB:KNOWS]<=|Friendships| {\"name\": \"Alice & Bob\"}");
		Geoff.mergeIntoNeo4j(subgraph, db, null);
		Geoff.mergeIntoNeo4j(subgraph, db, null);
		assertTrue(db.index().existsForRelationships("Friendships"));
		IndexHits<Relationship> hits = db.index().forRelationships("Friendships").get("name", "Alice & Bob");
		assertEquals(1, hits.size());
		db.assertCounts(3, 1);
	}

	private static Subgraph readerForResource(String name) throws IOException, SyntaxError {
		return new Subgraph(new InputStreamReader(ClassLoader.getSystemResourceAsStream(name)));
	}
	
	/*
	 * Should be able to load multiple files with overlapping entities
	 * without causing duplication
	 */
	@Test
	public void canLoadOverlappingFiles() throws SubgraphError, IOException, SyntaxError {
		Geoff.mergeIntoNeo4j(readerForResource("music/David Bowie - Space Oddity.geoff"), db, null);
		Geoff.mergeIntoNeo4j(readerForResource("music/David Bowie - Space Oddity v2.geoff"), db, null);
		Geoff.mergeIntoNeo4j(readerForResource("music/David Bowie - Life On Mars.geoff"), db, null);
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(readerForResource("music/David Bowie.geoff"), db, null);
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

	@Test
	public void canLoadFilesMultipleTimes() throws SubgraphError, IOException, SyntaxError {
		Geoff.mergeIntoNeo4j(readerForResource("music/David Bowie - Space Oddity.geoff"), db, null);
		Geoff.mergeIntoNeo4j(readerForResource("music/David Bowie - Space Oddity.geoff"), db, null);
		Geoff.mergeIntoNeo4j(readerForResource("music/David Bowie - Space Oddity v2.geoff"), db, null);
		Geoff.mergeIntoNeo4j(readerForResource("music/David Bowie - Life On Mars.geoff"), db, null);
		Geoff.mergeIntoNeo4j(readerForResource("music/David Bowie - Life On Mars.geoff"), db, null);
		Geoff.mergeIntoNeo4j(readerForResource("music/David Bowie - Life On Mars.geoff"), db, null);
		Geoff.mergeIntoNeo4j(readerForResource("music/David Bowie.geoff"), db, null);
		Geoff.mergeIntoNeo4j(readerForResource("music/David Bowie.geoff"), db, null);
		Map<String, PropertyContainer> out = Geoff.mergeIntoNeo4j(readerForResource("music/David Bowie.geoff"), db, null);
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

	@Test
	public void canCreateGraphWithNodeIndexEntryReflection() throws Exception {
		Subgraph subgraph = new Subgraph("" +
			"(doc) {\"name\": \"doctor\"}\n" +
			"(dal) {\"name\": \"dalek\"}\n" +
			"(doc)<=|People|     {\"name\": \"The Doctor\"}\n" +
			"(dal)<=|Baddies|    {\"name\": \"Dalek Sec\"}\n" +
			"");
		Geoff.mergeIntoNeo4j(subgraph, db, null);
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
		Assert.assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
		assertTrue(db.index().existsForNodes("Baddies"));
		assertTrue(db.index().forNodes("Baddies").get("name", "Dalek Sec").hasNext());
		Assert.assertEquals("dalek", db.index().forNodes("Baddies").get("name", "Dalek Sec").getSingle().getProperty("name"));
		subgraph = new Subgraph("" +
			"(doc)<=|People|     {\"name\": \"The Doctor\"}\n" +
			"(dal)<=|Baddies|    {\"name\": \"Dalek Sec\"}\n" +
			"(doc)-[enemy:ENEMY_OF]->(dal) {\"since\":\"forever\"}\n" +
			"");
		Map<String, PropertyContainer> stuff = Geoff.mergeIntoNeo4j(subgraph, db, null);
		Relationship enemy = (Relationship) stuff.get("[enemy]");
		assertNotNull(enemy);
		Assert.assertEquals("doctor", enemy.getStartNode().getProperty("name"));
		Assert.assertEquals("dalek", enemy.getEndNode().getProperty("name"));
	}

}
