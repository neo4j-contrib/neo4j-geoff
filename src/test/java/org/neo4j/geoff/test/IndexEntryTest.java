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


public class IndexEntryTest {

	@Test
	public void testIfDescriptorFactoryUnderstandsNodeIndexEntries()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("|index1|->(bob) {\"foo\":\"bar\"}");
		Assert.assertTrue(descriptor instanceof IndexEntry);
		IndexEntry<NodeRef> nodeIndexEntry = (IndexEntry<NodeRef>)descriptor;
		Assert.assertEquals(nodeIndexEntry.getIndex().getName(), "index1");
		Assert.assertEquals(nodeIndexEntry.getEntity().getName(), "bob");
		Assert.assertTrue(nodeIndexEntry.getData().containsKey("foo"));
		Assert.assertEquals(nodeIndexEntry.getData().get("foo"), "bar");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsRelationshipIndexEntries()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("|index1|->[bob] {\"foo\":\"bar\"}");
		Assert.assertTrue(descriptor instanceof IndexEntry);
		IndexEntry relIndexEntry = (IndexEntry<RelationshipRef>)descriptor;
		Assert.assertEquals(relIndexEntry.getIndex().getName(), "index1");
		Assert.assertEquals(relIndexEntry.getEntity().getName(), "bob");
		Assert.assertTrue(relIndexEntry.getData().containsKey("foo"));
		Assert.assertEquals(relIndexEntry.getData().get("foo"), "bar");
	}

}
