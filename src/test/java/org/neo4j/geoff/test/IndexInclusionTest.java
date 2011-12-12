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


public class IndexInclusionTest {

	@Test
	public void testIfDescriptorFactoryUnderstandsHookIndexEntries()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("{bob}<=|index1| {\"foo\":\"bar\"}");
		Assert.assertTrue(descriptor instanceof IndexInclusion);
		IndexInclusion<HookRef> hookIndexInclusion = (IndexInclusion<HookRef>) descriptor;
		Assert.assertEquals(hookIndexInclusion.getEntity().getName(), "bob");
		Assert.assertEquals(hookIndexInclusion.getIndex().getName(), "index1");
		Assert.assertTrue(hookIndexInclusion.getData().containsKey("foo"));
		Assert.assertEquals(hookIndexInclusion.getData().get("foo"), "bar");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNodeIndexEntries()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("(bob)<=|index1| {\"foo\":\"bar\"}");
		Assert.assertTrue(descriptor instanceof IndexInclusion);
		IndexInclusion<NodeRef> nodeIndexInclusion = (IndexInclusion<NodeRef>) descriptor;
		Assert.assertEquals(nodeIndexInclusion.getEntity().getName(), "bob");
		Assert.assertEquals(nodeIndexInclusion.getIndex().getName(), "index1");
		Assert.assertTrue(nodeIndexInclusion.getData().containsKey("foo"));
		Assert.assertEquals(nodeIndexInclusion.getData().get("foo"), "bar");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsRelationshipIndexEntries()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("[bob]<=|index1| {\"foo\":\"bar\"}");
		Assert.assertTrue(descriptor instanceof IndexInclusion);
		IndexInclusion relIndexInclusion = (IndexInclusion<RelationshipRef>) descriptor;
		Assert.assertEquals(relIndexInclusion.getEntity().getName(), "bob");
		Assert.assertEquals(relIndexInclusion.getIndex().getName(), "index1");
		Assert.assertTrue(relIndexInclusion.getData().containsKey("foo"));
		Assert.assertEquals(relIndexInclusion.getData().get("foo"), "bar");
	}

}
