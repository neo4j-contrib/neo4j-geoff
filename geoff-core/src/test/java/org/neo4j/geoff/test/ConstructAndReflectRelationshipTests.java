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
import org.neo4j.geoff.GEOFF;
import org.neo4j.geoff.GEOFFLoadException;
import org.neo4j.graphdb.*;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.*;

public class ConstructAndReflectRelationshipTests {

	private static final RelationshipType KNOWS = DynamicRelationshipType.withName("KNOWS");
	
	private ImpermanentGraphDatabase graphDB;
	private Map<String, PropertyContainer> params;
	
	private void createDB(
			boolean createAlice, boolean storeAlice,
			boolean createBob, boolean storeBob,
			boolean createAliceKnowsBob, boolean storeAliceKnowsBob
	) {
		this.graphDB = new ImpermanentGraphDatabase();
		this.params = new HashMap<String, PropertyContainer>(3);
		Transaction tx = this.graphDB.beginTx();
		Node nodeA = null, nodeB = null;
		if (createAlice) {
			nodeA = graphDB.createNode();
			if (storeAlice) {
				this.params.put("(A)", nodeA);
			}
		}
		if (createBob) {
			nodeB = graphDB.createNode();
			if (storeBob) {
				this.params.put("(B)", nodeB);
			}
		}
		if (createAlice && createBob && createAliceKnowsBob) {
			Relationship rel = nodeA.createRelationshipTo(nodeB, KNOWS);
			if (storeAliceKnowsBob) {
				this.params.put("[R]", rel);
			}
		}
		tx.success();
		tx.finish();
	}
	
	private Node getAlice() {
		assertTrue(this.params.containsKey("(A)"));
		assertTrue(this.params.get("(A)") instanceof Node);
		return (Node) this.params.get("(A)");
	}

	private Node getBob() {
		assertTrue(this.params.containsKey("(B)"));
		assertTrue(this.params.get("(B)") instanceof Node);
		return (Node) this.params.get("(B)");
	}

	private Relationship getR() {
		assertTrue(this.params.containsKey("[R]"));
		assertTrue(this.params.get("[R]") instanceof Relationship);
		return (Relationship) this.params.get("[R]");
	}

	private Relationship getKnows(Node alice, Node bob) {
		assertNotNull(alice);
		assertNotNull(bob);
		Iterable<Relationship> rels = alice.getRelationships(Direction.OUTGOING);
		Relationship knows = null;
		for(Relationship rel : rels) {
			if (rel.isType(KNOWS) && rel.getEndNode().getId() == bob.getId()) {
				assertNull(knows);
				knows = rel;
			}
		}
		assertNotNull(knows);
		return knows;
	}

	@Test
	public void test_ALICE_knows_BOB() throws GEOFFLoadException, IOException {
		createDB(true, true, true, true, false, false);
		String source = "(A)-[:KNOWS]->(B)";
		this.params = GEOFF.loadIntoNeo4j(new StringReader(source), this.graphDB, this.params);
		assertNotNull(this.params);
		Node alice = getAlice();
		Node bob = getBob();
		Relationship aliceKnowsBob = getKnows(alice, bob);
	}

	@Test
	public void test_alice_knows_BOB() throws GEOFFLoadException, IOException {
		createDB(false, false, true, true, false, false);
		String source = "(A)-[:KNOWS]->(B)";
		this.params = GEOFF.loadIntoNeo4j(new StringReader(source), this.graphDB, this.params);
		assertNotNull(this.params);
		Node alice = getAlice();
		Node bob = getBob();
		Relationship aliceKnowsBob = getKnows(alice, bob);
	}

	@Test
	public void test_ALICE_knows_bob() throws GEOFFLoadException, IOException {
		createDB(true, true, false, false, false, false);
		String source = "(A)-[:KNOWS]->(B)";
		this.params = GEOFF.loadIntoNeo4j(new StringReader(source), this.graphDB, this.params);
		assertNotNull(this.params);
		Node alice = getAlice();
		Node bob = getBob();
		Relationship aliceKnowsBob = getKnows(alice, bob);
	}

	@Test
	public void test_alice_knows_bob() throws GEOFFLoadException, IOException {
		createDB(false, false, false, false, false, false);
		String source = "(A)-[:KNOWS]->(B)";
		this.params = GEOFF.loadIntoNeo4j(new StringReader(source), this.graphDB, this.params);
		assertNotNull(this.params);
		Node alice = getAlice();
		Node bob = getBob();
		Relationship aliceKnowsBob = getKnows(alice, bob);
	}

	@Test
	public void test_ALICE_KNOWS_BOB() throws GEOFFLoadException, IOException {
		createDB(true, true, true, true, true, false);
		String source = "(A)-[:KNOWS]->(B)";
		this.params = GEOFF.loadIntoNeo4j(new StringReader(source), this.graphDB, this.params);
		assertNotNull(this.params);
		Node alice = getAlice();
		Node bob = getBob();
		Relationship aliceKnowsBob = getKnows(alice, bob);
	}

	@Test
	public void test_alice_KNOWS_BOB() throws GEOFFLoadException, IOException {
		createDB(true, false, true, true, true, false);
		String source = "(A)-[:KNOWS]->(B)";
		this.params = GEOFF.loadIntoNeo4j(new StringReader(source), this.graphDB, this.params);
		assertNotNull(this.params);
		Node alice = getAlice();
		Node bob = getBob();
		Relationship aliceKnowsBob = getKnows(alice, bob);
	}

	@Test
	public void test_ALICE_KNOWS_bob() throws GEOFFLoadException, IOException {
		createDB(true, true, true, false, true, false);
		String source = "(A)-[:KNOWS]->(B)";
		this.params = GEOFF.loadIntoNeo4j(new StringReader(source), this.graphDB, this.params);
		assertNotNull(this.params);
		Node alice = getAlice();
		Node bob = getBob();
		Relationship aliceKnowsBob = getKnows(alice, bob);
	}

	@Test
	public void test_alice_KNOWS_bob() throws GEOFFLoadException, IOException {
		createDB(true, false, true, false, true, false);
		String source = "(A)-[:KNOWS]->(B)";
		this.params = GEOFF.loadIntoNeo4j(new StringReader(source), this.graphDB, this.params);
		assertNotNull(this.params);
		Node alice = getAlice();
		Node bob = getBob();
		Relationship aliceKnowsBob = getKnows(alice, bob);
	}

	@Test
	public void test_ALICE_R_BOB() throws GEOFFLoadException, IOException {
		createDB(true, true, true, true, true, true);
		String source = "(A)-[R]->(B)";
		this.params = GEOFF.loadIntoNeo4j(new StringReader(source), this.graphDB, this.params);
		assertNotNull(this.params);
		Node alice = getAlice();
		Node bob = getBob();
		Relationship aliceKnowsBob = getKnows(alice, bob);
		Relationship r = getR();
		assertEquals(r.getId(), aliceKnowsBob.getId());
	}

	@Test
	public void test_alice_R_BOB() throws GEOFFLoadException, IOException {
		createDB(true, false, true, true, true, true);
		String source = "(A)-[R]->(B)";
		this.params = GEOFF.loadIntoNeo4j(new StringReader(source), this.graphDB, this.params);
		assertNotNull(this.params);
		Node alice = getAlice();
		Node bob = getBob();
		Relationship aliceKnowsBob = getKnows(alice, bob);
		Relationship r = getR();
		assertEquals(r.getId(), aliceKnowsBob.getId());
	}

	@Test
	public void test_ALICE_R_bob() throws GEOFFLoadException, IOException {
		createDB(true, true, true, false, true, true);
		String source = "(A)-[R]->(B)";
		this.params = GEOFF.loadIntoNeo4j(new StringReader(source), this.graphDB, this.params);
		assertNotNull(this.params);
		Node alice = getAlice();
		Node bob = getBob();
		Relationship aliceKnowsBob = getKnows(alice, bob);
		Relationship r = getR();
		assertEquals(r.getId(), aliceKnowsBob.getId());
	}

	@Test
	public void test_alice_R_bob() throws GEOFFLoadException, IOException {
		createDB(true, false, true, false, true, true);
		String source = "(A)-[R]->(B)";
		this.params = GEOFF.loadIntoNeo4j(new StringReader(source), this.graphDB, this.params);
		assertNotNull(this.params);
		Node alice = getAlice();
		Node bob = getBob();
		Relationship aliceKnowsBob = getKnows(alice, bob);
		Relationship r = getR();
		assertEquals(r.getId(), aliceKnowsBob.getId());
	}

}
