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

import org.neo4j.graphdb.*;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.util.Map;

import static org.junit.Assert.*;

class TestDatabase extends ImpermanentGraphDatabase {

	TestDatabase() {
		super();
	}

	void assertNodeCount(long count) {
		long n = 0;
		for (Node node : this.getAllNodes()) {
			n++;
		}
		assertEquals(count, n);
	}

	void assertRelationshipCount(long count) {
		long n = 0;
		while (true) {
			try {
				Relationship rel = this.getRelationshipById(n);
				if (rel == null) {
					break;
				}
			} catch (NotFoundException e) {
				break;
			}
			n++;
		}
		assertEquals(count, n);
	}

	void assertCounts(long nodes, long relationships) {
		assertNodeCount(nodes);
		assertRelationshipCount(relationships);
	}

	Node createAlice() {
		Transaction tx = this.beginTx();
		Node node = this.createNode();
		node.setProperty("name", TestName.ALICE);
		tx.success();
		tx.finish();
		return node;
	}

	Node createBob() {
		Transaction tx = this.beginTx();
		Node node = this.createNode();
		node.setProperty("name", TestName.BOB);
		tx.success();
		tx.finish();
		return node;
	}

	Node createCarol() {
		Transaction tx = this.beginTx();
		Node node = this.createNode();
		node.setProperty("name", TestName.CAROL);
		tx.success();
		tx.finish();
		return node;
	}

	Relationship createAliceKnowsBob() {
		Node a = createAlice();
		Node b = createBob();
		Transaction tx = this.beginTx();
		Relationship relationship = a.createRelationshipTo(b, DynamicRelationshipType.withName("KNOWS"));
		relationship.setProperty("since", 1977);
		tx.success();
		tx.finish();
		return relationship;
	}

	static void assertAlice(Node node) {
		assertNotNull(node);
		assertTrue(node.hasProperty("name"));
		assertEquals(TestName.ALICE, node.getProperty("name"));
	}

	static void assertBob(Node node) {
		assertNotNull(node);
		assertTrue(node.hasProperty("name"));
		assertEquals(TestName.BOB, node.getProperty("name"));
	}

	static void assertCarol(Node node) {
		assertNotNull(node);
		assertTrue(node.hasProperty("name"));
		assertEquals(TestName.CAROL, node.getProperty("name"));
	}

	static void assertNodesExist(Map<String, PropertyContainer> params, String... keys) {
		assertNotNull(params);
		for (String key : keys) {
			assertTrue(params.containsKey(key));
			assertTrue(params.get(key) instanceof Node);
		}
	}

	static void assertRelationshipsExist(Map<String, PropertyContainer> params, String... keys) {
		assertNotNull(params);
		for (String key : keys) {
			assertTrue(params.containsKey(key));
			assertTrue(params.get(key) instanceof Relationship);
		}
	}

}
