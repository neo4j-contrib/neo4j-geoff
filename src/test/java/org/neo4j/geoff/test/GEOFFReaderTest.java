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
import org.neo4j.geoff.Rule;
import org.neo4j.geoff.Token;

import java.util.Map;

public class GEOFFReaderTest {

	private synchronized Rule testRule(String text) throws Exception {
		System.out.println("Text: " + text);
		Rule rule = Rule.from(text);
		System.out.println("Descriptor:");
		System.out.print("\tTokens:");
		for(Token token : rule.getDescriptor().getTokens()) {
			System.out.print(" " + token);
		}
		System.out.println();
		System.out.println("\tPattern: \"" + rule.getDescriptor().getPattern() + "\"");
		System.out.println("Data:");
		for(Map.Entry<String, Object> entry : rule.getData().entrySet()) {
			System.out.println("\t" + entry.getKey() + "=" + entry.getValue());
		}
		System.out.println();
		return rule;
	}

	@Test
	public void testNodeNoDef() throws Exception {
		testRule("!(A)");
	}

	@Test
	public void testRelNoDef() throws Exception {
		testRule("![R]");
	}

	@Test
	public void testTypedRel() throws Exception {
		testRule("(A)-[:T]->(B)");
	}

	@Test
	public void testNamedTypedRel() throws Exception {
		testRule("(A)-[R:T]->(B)");
	}

	@Test
	public void testTypedNoRel() throws Exception {
		testRule("(A)-[:T]-!(B)");
	}

	@Test
	public void testNamedTypedNoRel() throws Exception {
		testRule("(A)-[R:T]-!(B)");
	}

	@Test
	public void testRelFromAToBReflection() throws Exception {
		testRule("[R]:=(A)-[:T]->(B)");
	}

	@Test
	public void testRelFromAReflection() throws Exception {
		testRule("[R]:=(A)-[:T]->()");
	}

	@Test
	public void testRelToBReflection() throws Exception {
		testRule("[R]:=()-[:T]->(B)");
	}

	@Test
	public void testStartNodeReflection() throws Exception {
		testRule("(A):=(*)-[R]->()");
	}

	@Test
	public void testEndNodeReflection() throws Exception {
		testRule("(B):=()-[R]->(*)");
	}

	@Test
	public void testNodeIncludedInIndex() throws Exception {
		testRule("(A)<=|I| {\"foo\":\"bar\"}");
	}

	@Test
	public void testRelIncludedInIndex() throws Exception {
		testRule("[R]<=|I| {\"foo\":\"bar\"}");
	}

	@Test
	public void testNodeExcludedFromIndex() throws Exception {
		testRule("(A)!=|I|");
	}

	@Test
	public void testRelExcludedFromIndex() throws Exception {
		testRule("[R]!=|I|");
	}

	@Test
	public void testNodeReflectsIndexEntry() throws Exception {
		testRule("(A):=|I|");
	}

	@Test
	public void testRelReflectsIndexEntry() throws Exception {
		testRule("[R]:=|I|");
	}

}
