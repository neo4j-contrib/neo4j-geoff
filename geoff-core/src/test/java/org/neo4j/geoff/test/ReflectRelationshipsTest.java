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

import org.junit.Test;
import org.neo4j.geoff.Geoff;
import org.neo4j.geoff.Rule;
import org.neo4j.geoff.store.NodeToken;
import org.neo4j.geoff.store.RelationshipToken;
import org.neo4j.geoff.store.Token;
import org.neo4j.graphdb.PropertyContainer;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ReflectRelationshipsTest extends TestBase {

	@Test
	public void canParseReflectRelationshipsRule() throws Exception {
		String source = "(A)=[:KNOWS]=>(B) {\"since\": 1977}";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("N=R=>N", rule.getDescriptor().getPattern());
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

	@Test
	public void canReflectRelationshipSet() throws Exception {
		TestGeoffBuilder geoff = new TestGeoffBuilder();
		geoff.append("(A) {\"name\": \"Alice Allison\"}");
		geoff.append("(B) {\"name\": \"Bob Robertson\"}");
		geoff.append("(C)   {\"name\": \"Carol Carlson\"}");
		geoff.append("(A)-[:KNOWS]->(B) {\"status\": \"friends\"}");
		geoff.append("(C)-[:KNOWS]->(B) {\"status\": \"colleagues\"}");
		geoff.append("()=[R:KNOWS]=>(B)");
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(geoff.getReader(), db, null);
		assertNotNull(out);
		//dumpParams(out);
		assertTrue(out.containsKey("[R.1]"));
		assertTrue(out.containsKey("[R.2]"));
		db.assertCounts(4, 2);
	}

}
