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
import org.neo4j.geoff.Geoff;
import org.neo4j.geoff.Rule;
import org.neo4j.geoff.store.NodeToken;
import org.neo4j.geoff.store.Token;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;

import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.*;
import static org.neo4j.geoff.test.TestDatabase.*;

public class CreateOrUpdateNodeTest extends TestBase{

    @Before
    public void setUp(){
        db = new TestDatabase();
        
    }
	@Test
	public void canParseCreateOrUpdateNodeRule() throws Exception {
		String source = "(A) {\"name\": \"Alice\"}";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof NodeToken);
		NodeToken token = (NodeToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.NODE, token.getTokenType());
		assertTrue(token.hasName());
		assertEquals("A", token.getName());
		assertTrue(rule.getData().containsKey("name"));
		assertEquals("Alice", rule.getData().get("name"));
	}

	@Test
	public void canCreateNode() throws Exception {
		TestGeoffBuilder geoff = new TestGeoffBuilder();
		geoff.appendAlice();
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertNotNull(out);
		assertAlice((Node) out.get("(A)"));
		db.assertNodeCount(2);
	}

	@Test
	public void canUpdateNodeProperties() throws Exception {
		TestGeoffBuilder geoff = new TestGeoffBuilder();
		geoff.append("(A) {\"foo\": \"bar\"}");
		geoff.appendAlice();
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertNotNull(out);
		Node node = (Node) out.get("(A)");
		assertFalse(node.hasProperty("foo"));
		assertAlice(node);
		db.assertNodeCount(2);
	}

	@Test
	public void canUpdateNodeToEraseProperties() throws Exception {
		TestGeoffBuilder geoff = new TestGeoffBuilder();
		geoff.appendAlice();
		geoff.append("(A) {}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
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
		TestGeoffBuilder geoff = new TestGeoffBuilder();
		geoff.appendAlice();
		geoff.append("(A)");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertNotNull(out);
		assertAlice((Node) out.get("(A)"));
		db.assertNodeCount(2);
	}

	@Test
	public void canCreateMultipleNodes() throws Exception {
		TestGeoffBuilder geoff = new TestGeoffBuilder();
		geoff.appendAlice();
		geoff.appendBob();
		geoff.appendCarol();
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertNotNull(out);
		assertAlice((Node) out.get("(A)"));
		assertBob((Node) out.get("(B)"));
		assertCarol((Node) out.get("(C)"));
		db.assertNodeCount(4);
	}

	@Test
	public void canCreateMultipleAnonymousNodes() throws Exception {
		TestGeoffBuilder geoff = new TestGeoffBuilder();
		geoff.append("() {\"name\": \"Alice Allison\"}");
		geoff.append("() {\"name\": \"Bob Robertson\"}");
		geoff.append("() {\"name\": \"Carol Carlson\"}");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertNotNull(out);
		db.assertNodeCount(4);
	}

}
