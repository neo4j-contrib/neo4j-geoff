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
import org.neo4j.geoff.GEOFF;
import org.neo4j.geoff.NodeToken;
import org.neo4j.geoff.Rule;
import org.neo4j.geoff.Token;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.*;

public class NodeInclusionRuleTest {

	private ImpermanentGraphDatabase db;

	@Before
	public void setUp() throws Exception {
		db = new ImpermanentGraphDatabase();
	}

	@Test
	public void testNodeInclusionRule() throws Exception {
		String source = "(A) {\"foo\": \"bar\"}";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof NodeToken);
		NodeToken token = (NodeToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.NODE, token.getTokenType());
		assertEquals(true, token.hasName());
		assertEquals("A", token.getName());
		Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		Node node = (Node) out.get("(A)");
		assertNotNull(node);
		assertTrue(node.hasProperty("foo"));
		assertEquals("bar", node.getProperty("foo"));
	}

	@Test
	public void testNodeInclusionRuleWithSelfUpdate() throws Exception {
		String source =
				"(A)\n" +
				"(A) {\"foo\": \"bar\"}\n" +
				"(A) {\"wim\": \"wom\"}\n" +
				"";
		Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		Node node = (Node) out.get("(A)");
		assertNotNull(node);
		assertFalse(node.hasProperty("foo"));
		assertTrue(node.hasProperty("wim"));
		assertEquals("wom", node.getProperty("wim"));
	}

	@Test
	public void testNodeInclusionRuleWithLoadParameter() throws Exception {
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(1);
		params.put("A", db.getReferenceNode());
		String source = "(A) {\"foo\": \"bar\"}";
		Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(new StringReader(source), db, params);
		assertNotNull(out);
		Node node = (Node) out.get("(A)");
		assertNotNull(node);
		assertTrue(node.hasProperty("foo"));
		assertEquals("bar", node.getProperty("foo"));
		assertEquals(db.getReferenceNode().getId(), node.getId());
	}

}
