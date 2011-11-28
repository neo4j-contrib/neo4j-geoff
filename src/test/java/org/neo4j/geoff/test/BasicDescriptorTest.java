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
import org.neo4j.geoff.BadDescriptorException;
import org.neo4j.geoff.Descriptor;
import org.neo4j.geoff.HookDescriptor;
import org.neo4j.geoff.NodeDescriptor;


public class BasicDescriptorTest {

	@Test
	public void testIfDescriptorFactoryUnderstandsBlankLines()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("");
		Assert.assertNull(descriptor);
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsLinesOfWhitespace()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("\t    ");
		Assert.assertNull(descriptor);
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsComments()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("# this is a comment");
		Assert.assertNull(descriptor);
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsHookDescriptors()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("{foo}");
		Assert.assertTrue(descriptor instanceof HookDescriptor);
		HookDescriptor hookDescriptor = (HookDescriptor) descriptor;
		Assert.assertEquals(hookDescriptor.getHook().getName(), "foo");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsHookDescriptorsWithData()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("{foo} {\"pi\":3.1415}");
		Assert.assertTrue(descriptor instanceof HookDescriptor);
		HookDescriptor hookDescriptor = (HookDescriptor) descriptor;
		Assert.assertEquals(hookDescriptor.getHook().getName(), "foo");
		Assert.assertTrue(hookDescriptor.getData().containsKey("pi"));
		Assert.assertEquals(hookDescriptor.getData().get("pi"), 3.1415);
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNodeDescriptors()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("(foo)");
		Assert.assertTrue(descriptor instanceof NodeDescriptor);
		NodeDescriptor nodeDescriptor = (NodeDescriptor) descriptor;
		Assert.assertEquals(nodeDescriptor.getNode().getName(), "foo");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNodeDescriptorsWithData()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("(foo) {\"pi\":3.1415}");
		Assert.assertTrue(descriptor instanceof NodeDescriptor);
		NodeDescriptor nodeDescriptor = (NodeDescriptor) descriptor;
		Assert.assertEquals(nodeDescriptor.getNode().getName(), "foo");
		Assert.assertTrue(nodeDescriptor.getData().containsKey("pi"));
		Assert.assertEquals(nodeDescriptor.getData().get("pi"), 3.1415);
	}

	@Test(expected = BadDescriptorException.class)
	public void testIfDescriptorFactoryFailsOnUnrecognisableDescriptor()
			throws BadDescriptorException {
		Descriptor descriptor = Descriptor.from("@!$#");
	}

}
