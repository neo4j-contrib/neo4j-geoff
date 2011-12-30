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
import org.neo4j.geoff.*;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.*;

public class AssertionRuleTest {

	private ImpermanentGraphDatabase db;

	@Before
	public void setUp() throws Exception {
		db = new ImpermanentGraphDatabase();
	}

	@Test
	public void testNodeDefinedAssertionRule() throws Exception {
		String source = "?(A)";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("?N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(1) instanceof NodeToken);
		NodeToken token = (NodeToken) rule.getDescriptor().getToken(1);
		assertEquals(Token.Type.NODE, token.getTokenType());
		assertTrue(token.hasName());
		assertEquals("A", token.getName());
	}

	@Test
	public void testRelationshipDefinedAssertionRule() throws Exception {
		String source = "?[R]";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("?R", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(1) instanceof RelToken);
		RelToken token = (RelToken) rule.getDescriptor().getToken(1);
		assertEquals(Token.Type.REL, token.getTokenType());
		assertTrue(token.hasName());
		assertEquals("R", token.getName());
		assertFalse(token.hasType());
	}

	@Test
	public void testNodeNotDefinedAssertionRule() throws Exception {
		String source = "?!(A)";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("?!N", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(2) instanceof NodeToken);
		NodeToken token = (NodeToken) rule.getDescriptor().getToken(2);
		assertEquals(Token.Type.NODE, token.getTokenType());
		assertTrue(token.hasName());
		assertEquals("A", token.getName());
	}

	@Test
	public void testRelationshipNotDefinedAssertionRule() throws Exception {
		String source = "?![R]";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("?!R", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(2) instanceof RelToken);
		RelToken token = (RelToken) rule.getDescriptor().getToken(2);
		assertEquals(Token.Type.REL, token.getTokenType());
		assertTrue(token.hasName());
		assertEquals("R", token.getName());
		assertFalse(token.hasType());
	}

	@Test
	public void testTrueAssertionRule() throws Exception {
		String source = "?";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("?", rule.getDescriptor().getPattern());
	}

	@Test
	public void testLoadingNodeNotDefinedAssertionRule() throws Exception {
		String source =
				"# Artist: David Bowie (version 1)\n" +
				"(bowie):=|Artists| {\"name\": \"David Bowie\"}\n" +
				"?!(bowie)\n" +
				"(bowie)\n" +
				"(bowie)<=|Artists| {\"name\": \"David Bowie\"}\n" +
				"?\n" +
				"(bowie)            {\"name\": \"David Bowie\", \"real_name\": \"David Jones\"}\n" +
				"";
		GEOFF.loadIntoNeo4j(new StringReader(source), db, null);
		source =
				"# Artist: David Bowie (version 2)\n" +
				"(bowie):=|Artists| {\"name\": \"David Bowie\"}\n" +
				"?!(bowie)\n" +
				"(bowie)\n" +
				"(bowie)<=|Artists| {\"name\": \"David Bowie\"}\n" +
				"?\n" +
				"(bowie)            {\"name\": \"David Bowie\", \"real_name\": \"David Robert Jones\"}\n" +
				"";
		GEOFF.loadIntoNeo4j(new StringReader(source), db, null);
		assertTrue(db.index().existsForNodes("Artists"));
		assertTrue(db.index().forNodes("Artists").get("name", "David Bowie").hasNext());
		assertEquals(1, db.index().forNodes("Artists").get("name", "David Bowie").size());
		assertEquals("David Robert Jones", db.index().forNodes("Artists").get("name", "David Bowie").getSingle().getProperty("real_name"));
	}

}
