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


public class IndexDescriptorTest {

	@Test
	public void testIfDescriptorFactoryUnderstandsHookIndexInclusions()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("{bob}<=|index1| {\"foo\":\"bar\"}");
		Assert.assertTrue(descriptor instanceof IndexRule);
		IndexRule<HookRef> hookIndexRule = (IndexRule<HookRef>) descriptor;
		Assert.assertEquals(hookIndexRule.getEntity().getName(), "bob");
		Assert.assertEquals(hookIndexRule.getIndex().getName(), "index1");
		Assert.assertTrue(hookIndexRule.getData().containsKey("foo"));
		Assert.assertEquals(hookIndexRule.getData().get("foo"), "bar");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNodeIndexInclusions()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("(bob)<=|index1| {\"foo\":\"bar\"}");
		Assert.assertTrue(descriptor instanceof IndexRule);
		IndexRule<NodeRef> nodeIndexRule = (IndexRule<NodeRef>) descriptor;
		Assert.assertEquals(nodeIndexRule.getEntity().getName(), "bob");
		Assert.assertEquals(nodeIndexRule.getIndex().getName(), "index1");
		Assert.assertTrue(nodeIndexRule.getData().containsKey("foo"));
		Assert.assertEquals(nodeIndexRule.getData().get("foo"), "bar");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsRelationshipIndexEntries()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("[bob]<=|index1| {\"foo\":\"bar\"}");
		Assert.assertTrue(descriptor instanceof IndexRule);
		IndexRule relIndexRule = (IndexRule<RelationshipRef>) descriptor;
		Assert.assertEquals(relIndexRule.getEntity().getName(), "bob");
		Assert.assertEquals(relIndexRule.getIndex().getName(), "index1");
		Assert.assertTrue(relIndexRule.getData().containsKey("foo"));
		Assert.assertEquals(relIndexRule.getData().get("foo"), "bar");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNodeIndexEntryReflections()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("(bob):=|index1| {\"foo\":\"bar\"}");
		Assert.assertTrue(descriptor instanceof IndexEntryReflection);
		IndexEntryReflection<NodeRef> nodeIndexEntryReflection = (IndexEntryReflection<NodeRef>) descriptor;
		Assert.assertEquals(nodeIndexEntryReflection.getEntity().getName(), "bob");
		Assert.assertEquals(nodeIndexEntryReflection.getIndex().getName(), "index1");
		Assert.assertEquals(nodeIndexEntryReflection.getKey(), "foo");
		Assert.assertEquals(nodeIndexEntryReflection.getValue(), "bar");
	}

}
