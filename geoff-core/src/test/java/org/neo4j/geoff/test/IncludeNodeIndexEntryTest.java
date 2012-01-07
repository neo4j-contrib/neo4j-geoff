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
import org.neo4j.geoff.tokens.IndexToken;
import org.neo4j.geoff.tokens.NodeToken;
import org.neo4j.geoff.tokens.Token;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.neo4j.geoff.test.TestDatabase.assertNodesExist;

public class IncludeNodeIndexEntryTest {

	@Test
	public void canParseIncludeNodeIndexEntryRule() throws Exception {
		String source = "(A)<=|People| {\"name\": \"Alice\"}";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("N^I", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(2) instanceof IndexToken);
		NodeToken nodeToken = (NodeToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.NODE, nodeToken.getTokenType());
		assertTrue(nodeToken.hasName());
		assertEquals("A", nodeToken.getName());
		IndexToken indexToken = (IndexToken) rule.getDescriptor().getToken(2);
		assertEquals(Token.Type.INDEX, indexToken.getTokenType());
		assertTrue(indexToken.hasName());
		assertEquals("People", indexToken.getName());
		assertTrue(rule.getData().containsKey("name"));
		assertEquals("Alice", rule.getData().get("name"));
	}

	@Test
	public void willCreateNodeAndAddIndexEntryWhenNeitherExist() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder();
		geoff.append("(A)<=|People| {\"name\": \"Allison, Alice\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertNotNull(out);
		assertNodesExist(out, "(A)");
		Node createdNode = (Node) out.get("(A)");
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "Allison, Alice").hasNext());
		Node indexedNode = db.index().forNodes("People").get("name", "Allison, Alice").getSingle();
		assertEquals(indexedNode.getId(), createdNode.getId());
		db.assertNodeCount(2);
	}

	@Test
	public void willAddIndexEntryWhenOnlyNodeExists() throws Exception {
		TestDatabase db = new TestDatabase();
		TestGeoffBuilder geoff = new TestGeoffBuilder();
		geoff.appendAlice();
		geoff.append("(A)<=|People| {\"name\": \"Allison, Alice\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertNotNull(out);
		assertNodesExist(out, "(A)");
		Node createdNode = (Node) out.get("(A)");
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "Allison, Alice").hasNext());
		Node indexedNode = db.index().forNodes("People").get("name", "Allison, Alice").getSingle();
		assertEquals(indexedNode.getId(), createdNode.getId());
		db.assertNodeCount(2);
	}

	@Test
	public void willReflectNodeWhenOnlyEntryExists() throws Exception {
		TestDatabase db = new TestDatabase();
		Node alice = db.createAlice();
		Transaction tx = db.beginTx();
		db.index().forNodes("People").add(alice, "name", "Allison, Alice");
		tx.success();
		tx.finish();
		TestGeoffBuilder geoff = new TestGeoffBuilder();
		geoff.append("(A)<=|People| {\"name\": \"Allison, Alice\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertNotNull(out);
		assertNodesExist(out, "(A)");
		Node createdNode = (Node) out.get("(A)");
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "Allison, Alice").hasNext());
		Node indexedNode = db.index().forNodes("People").get("name", "Allison, Alice").getSingle();
		assertEquals(indexedNode.getId(), createdNode.getId());
		db.assertNodeCount(2);
	}

	@Test
	public void willDoNothingWhenBothExist() throws Exception {
		TestDatabase db = new TestDatabase();
		Node alice = db.createAlice();
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>();
		params.put("(A)", alice);
		Transaction tx = db.beginTx();
		db.index().forNodes("People").add(alice, "name", "Allison, Alice");
		tx.success();
		tx.finish();
		TestGeoffBuilder geoff = new TestGeoffBuilder();
		geoff.append("(A)<=|People| {\"name\": \"Allison, Alice\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, params);
		assertNotNull(out);
		assertNodesExist(out, "(A)");
		Node createdNode = (Node) out.get("(A)");
		assertTrue(db.index().existsForNodes("People"));
		assertTrue(db.index().forNodes("People").get("name", "Allison, Alice").hasNext());
		Node indexedNode = db.index().forNodes("People").get("name", "Allison, Alice").getSingle();
		assertEquals(indexedNode.getId(), createdNode.getId());
		db.assertNodeCount(2);
	}

}
