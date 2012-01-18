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

import org.junit.Before;
import org.junit.Test;
import org.neo4j.geoff.Geoff;
import org.neo4j.geoff.Rule;
import org.neo4j.geoff.store.NodeToken;
import org.neo4j.geoff.store.Token;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DeleteNodeTest {

	private ImpermanentGraphDatabase db;

	@Before
	public void setUp() throws Exception {
		db = new ImpermanentGraphDatabase();
	}

	@Test
	public void testNodeExclusionRule() throws Exception {
		String source = "!(A)";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("!N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(1) instanceof NodeToken);
		NodeToken token = (NodeToken) rule.getDescriptor().getToken(1);
		assertEquals(Token.Type.NODE, token.getTokenType());
		assertTrue(token.hasName());
		assertEquals("A", token.getName());
	}

	@Test
	public void testLoadingNodeExclusionRule() throws Exception {
		// perform call to add and remove node
		String source =
				"(A) {\"name\": \"Alice\"}\n" +
				"!(A)\n" +
				"";
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(new StringReader(source), db, null);
		// check results
		assertNotNull(out);
		assertEquals(0, out.size());
	}

	@Test
	public void testLoadingNodeInclusionRuleWithLoadParameter() throws Exception {
		// perform first call to inject node
		String source = "(A) {\"name\": \"Alice\"}";
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		// build params for second call from output of first
		Node node = (Node) out.get("(A)");
		assertNotNull(node);
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(1);
		params.put("A", node);
		// make second call to remove node
		source = "!(A)";
		out = Geoff.loadIntoNeo4j(new StringReader(source), db, params);
		// check results
		assertNotNull(out);
		assertEquals(0, out.size());
	}

}
