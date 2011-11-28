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

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.geoff.*;


public class RelationshipDescriptorTest {

	@Test
	public void testIfDescriptorFactoryUnderstandsUnnamedRelationshipDescriptors()
			throws BadDescriptorException {
		String[] starts = new String[]{"{foo}", "(foo)"};
		String[] ends = new String[]{"{bar}", "(bar)"};
		for (String start : starts) {
			for (String end : ends) {
				String dstring = start + "-[:KNOWS]->" + end;
				System.out.println("Testing relationship descriptor: " + dstring);
				Descriptor descriptor = Descriptor.from(dstring);
				Assert.assertTrue(descriptor instanceof RelationshipDescriptor);
				RelationshipDescriptor relDescriptor = (RelationshipDescriptor) descriptor;
				if (start.startsWith("{")) {
					Assert.assertTrue(relDescriptor.getStartNode() instanceof HookRef);
				}
				if (start.startsWith("(")) {
					Assert.assertTrue(relDescriptor.getStartNode() instanceof NodeRef);
				}
				if (end.startsWith("{")) {
					Assert.assertTrue(relDescriptor.getEndNode() instanceof HookRef);
				}
				if (end.startsWith("(")) {
					Assert.assertTrue(relDescriptor.getEndNode() instanceof NodeRef);
				}
				Assert.assertEquals(relDescriptor.getStartNode().getName(), "foo");
				Assert.assertEquals(relDescriptor.hasName(), false);
				Assert.assertEquals(relDescriptor.getName(), "");
				Assert.assertEquals(relDescriptor.getType(), "KNOWS");
				Assert.assertEquals(relDescriptor.getEndNode().getName(), "bar");
			}
		}
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNamedRelationshipDescriptors()
			throws BadDescriptorException {
		String[] starts = new String[]{"{foo}", "(foo)"};
		String[] ends = new String[]{"{bar}", "(bar)"};
		for (String start : starts) {
			for (String end : ends) {
				String dstring = start + "-[bob:KNOWS]->" + end;
				System.out.println("Testing relationship descriptor: " + dstring);
				Descriptor descriptor = Descriptor.from(dstring);
				Assert.assertTrue(descriptor instanceof RelationshipDescriptor);
				RelationshipDescriptor relDescriptor = (RelationshipDescriptor) descriptor;
				if (start.startsWith("{")) {
					Assert.assertTrue(relDescriptor.getStartNode() instanceof HookRef);
				}
				if (start.startsWith("(")) {
					Assert.assertTrue(relDescriptor.getStartNode() instanceof NodeRef);
				}
				if (end.startsWith("{")) {
					Assert.assertTrue(relDescriptor.getEndNode() instanceof HookRef);
				}
				if (end.startsWith("(")) {
					Assert.assertTrue(relDescriptor.getEndNode() instanceof NodeRef);
				}
				Assert.assertEquals(relDescriptor.getStartNode().getName(), "foo");
				Assert.assertEquals(relDescriptor.hasName(), true);
				Assert.assertEquals(relDescriptor.getName(), "bob");
				Assert.assertEquals(relDescriptor.getType(), "KNOWS");
				Assert.assertEquals(relDescriptor.getEndNode().getName(), "bar");
			}
		}
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsRelationshipDescriptorsWithData()
			throws BadDescriptorException {
		String[] starts = new String[]{"{foo}", "(foo)"};
		String[] ends = new String[]{"{bar}", "(bar)"};
		for (String start : starts) {
			for (String end : ends) {
				String dstring = start + "-[bob:KNOWS]->" + end + " {\"pi\":3.1415}";
				System.out.println("Testing relationship descriptor: " + dstring);
				Descriptor descriptor = Descriptor.from(dstring);
				Assert.assertTrue(descriptor instanceof RelationshipDescriptor);
				RelationshipDescriptor relDescriptor = (RelationshipDescriptor) descriptor;
				if (start.startsWith("{")) {
					Assert.assertTrue(relDescriptor.getStartNode() instanceof HookRef);
				}
				if (start.startsWith("(")) {
					Assert.assertTrue(relDescriptor.getStartNode() instanceof NodeRef);
				}
				if (end.startsWith("{")) {
					Assert.assertTrue(relDescriptor.getEndNode() instanceof HookRef);
				}
				if (end.startsWith("(")) {
					Assert.assertTrue(relDescriptor.getEndNode() instanceof NodeRef);
				}
				Assert.assertEquals(relDescriptor.getStartNode().getName(), "foo");
				Assert.assertEquals(relDescriptor.hasName(), true);
				Assert.assertEquals(relDescriptor.getName(), "bob");
				Assert.assertEquals(relDescriptor.getType(), "KNOWS");
				Assert.assertEquals(relDescriptor.getEndNode().getName(), "bar");
				Assert.assertTrue(relDescriptor.getData().containsKey("pi"));
				Assert.assertEquals(relDescriptor.getData().get("pi"), 3.1415);
			}
		}
	}

}
