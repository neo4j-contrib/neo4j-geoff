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
import org.neo4j.geoff.NodeDescriptor;
import org.neo4j.geoff.RelationshipDescriptor;


public class RelationshipDescriptorTest {

	@Test
	public void testIfDescriptorFactoryUnderstandsUnnamedRelationshipDescriptors()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("(foo)-[:KNOWS]->(bar)");
		Assert.assertTrue(descriptor instanceof RelationshipDescriptor);
		RelationshipDescriptor relDescriptor = (RelationshipDescriptor)descriptor;
		Assert.assertEquals(relDescriptor.getStartNode().getName(), "foo");
		Assert.assertEquals(relDescriptor.getName(), "");
		Assert.assertEquals(relDescriptor.getType(), "KNOWS");
		Assert.assertEquals(relDescriptor.getEndNode().getName(), "bar");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNamedRelationshipDescriptors()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("(foo)-[bob:KNOWS]->(bar)");
		Assert.assertTrue(descriptor instanceof RelationshipDescriptor);
		RelationshipDescriptor relDescriptor = (RelationshipDescriptor)descriptor;
		Assert.assertEquals(relDescriptor.getStartNode().getName(), "foo");
		Assert.assertEquals(relDescriptor.getName(), "bob");
		Assert.assertEquals(relDescriptor.getType(), "KNOWS");
		Assert.assertEquals(relDescriptor.getEndNode().getName(), "bar");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsRelationshipDescriptorsWithData()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("(foo)-[bob:KNOWS]->(bar) {\"pi\":3.1415}");
		Assert.assertTrue(descriptor instanceof RelationshipDescriptor);
		RelationshipDescriptor relDescriptor = (RelationshipDescriptor)descriptor;
		Assert.assertEquals(relDescriptor.getStartNode().getName(), "foo");
		Assert.assertEquals(relDescriptor.getName(), "bob");
		Assert.assertEquals(relDescriptor.getType(), "KNOWS");
		Assert.assertEquals(relDescriptor.getEndNode().getName(), "bar");
		Assert.assertTrue(relDescriptor.getData().containsKey("pi"));
		Assert.assertEquals(relDescriptor.getData().get("pi"), 3.1415);
	}

}
