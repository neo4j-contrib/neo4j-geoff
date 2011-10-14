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


public class DescriptorTest {

	@Test
	public void testIfDescriptorFactoryUnderstandsBlankLines()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("");
		Assert.assertNull(descriptor);
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsLinesOfWhitespace()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("\t    ");
		Assert.assertNull(descriptor);
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsComments()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("# this is a comment");
		Assert.assertNull(descriptor);
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNodeDescriptors()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("(foo)");
		Assert.assertTrue(descriptor instanceof NodeDescriptor);
		NodeDescriptor nodeDescriptor = (NodeDescriptor)descriptor;
		Assert.assertEquals(nodeDescriptor.getNodeName(), "foo");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNodeDescriptorsWithData()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("(foo) {\"pi\":3.1415}");
		Assert.assertTrue(descriptor instanceof NodeDescriptor);
		NodeDescriptor nodeDescriptor = (NodeDescriptor)descriptor;
		Assert.assertEquals(nodeDescriptor.getNodeName(), "foo");
		Assert.assertTrue(nodeDescriptor.getData().containsKey("pi"));
		Assert.assertEquals(nodeDescriptor.getData().get("pi"), 3.1415);
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNodeIndexEntries()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("{index1}->(bob) {\"foo\":\"bar\"}");
		Assert.assertTrue(descriptor instanceof NodeIndexEntry);
		NodeIndexEntry nodeIndexEntry = (NodeIndexEntry)descriptor;
		Assert.assertEquals(nodeIndexEntry.getIndexName(), "index1");
		Assert.assertEquals(nodeIndexEntry.getNodeName(), "bob");
		Assert.assertTrue(nodeIndexEntry.getData().containsKey("foo"));
		Assert.assertEquals(nodeIndexEntry.getData().get("foo"), "bar");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsUnnamedRelationshipDescriptors()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("(foo)-[:KNOWS]->(bar)");
		Assert.assertTrue(descriptor instanceof RelationshipDescriptor);
		RelationshipDescriptor relDescriptor = (RelationshipDescriptor)descriptor;
		Assert.assertEquals(relDescriptor.getStartNodeName(), "foo");
		Assert.assertEquals(relDescriptor.getRelationshipName(), "");
		Assert.assertEquals(relDescriptor.getRelationshipType(), "KNOWS");
		Assert.assertEquals(relDescriptor.getEndNodeName(), "bar");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNamedRelationshipDescriptors()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("(foo)-[bob:KNOWS]->(bar)");
		Assert.assertTrue(descriptor instanceof RelationshipDescriptor);
		RelationshipDescriptor relDescriptor = (RelationshipDescriptor)descriptor;
		Assert.assertEquals(relDescriptor.getStartNodeName(), "foo");
		Assert.assertEquals(relDescriptor.getRelationshipName(), "bob");
		Assert.assertEquals(relDescriptor.getRelationshipType(), "KNOWS");
		Assert.assertEquals(relDescriptor.getEndNodeName(), "bar");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsRelationshipDescriptorsWithData()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("(foo)-[bob:KNOWS]->(bar) {\"pi\":3.1415}");
		Assert.assertTrue(descriptor instanceof RelationshipDescriptor);
		RelationshipDescriptor relDescriptor = (RelationshipDescriptor)descriptor;
		Assert.assertEquals(relDescriptor.getStartNodeName(), "foo");
		Assert.assertEquals(relDescriptor.getRelationshipName(), "bob");
		Assert.assertEquals(relDescriptor.getRelationshipType(), "KNOWS");
		Assert.assertEquals(relDescriptor.getEndNodeName(), "bar");
		Assert.assertTrue(relDescriptor.getData().containsKey("pi"));
		Assert.assertEquals(relDescriptor.getData().get("pi"), 3.1415);
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsRelationshipIndexEntries()
	throws BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from("{index1}->[bob] {\"foo\":\"bar\"}");
		Assert.assertTrue(descriptor instanceof RelationshipIndexEntry);
		RelationshipIndexEntry relIndexEntry = (RelationshipIndexEntry)descriptor;
		Assert.assertEquals(relIndexEntry.getIndexName(), "index1");
		Assert.assertEquals(relIndexEntry.getRelationshipName(), "bob");
		Assert.assertTrue(relIndexEntry.getData().containsKey("foo"));
		Assert.assertEquals(relIndexEntry.getData().get("foo"), "bar");
	}

    @Test
    public void testIfDescriptorFactoryUnderstandsCompositeDescriptors()
    throws BadDescriptorException
    {
        String descString = "{\"(doc)\": {\"name\": \"doctor\"}, \"(dal)\": {\"name\": \"dalek\"}," +
            "\"(doc)-[:ENEMY_OF]->(dal)\": {\"since\": \"forever\"}, \"{People}->(doc)\": {\"name\": \"The Doctor\"} }";
        Descriptor descriptor = Descriptor.from(descString);
        Assert.assertTrue(descriptor instanceof CompositeDescriptor);
        CompositeDescriptor desc = (CompositeDescriptor)descriptor;
        Assert.assertEquals(desc.length(), 4);
    }

    @Test(expected = BadDescriptorException.class)
    public void testIfDescriptorFactoryFailsOnCompositeDescriptorWithBadJSON()
    throws BadDescriptorException
    {
        String descString = "{\"(doc)\": {\"name\"; \"doctor\"}, \"(dal)\": {\"name\": \"dalek\"}," +
            "\"(doc)-[:ENEMY_OF]->(dal)\": {\"since\": \"forever\"}, \"{People}->(doc)\": {\"name\": \"The Doctor\"} }";
        Descriptor descriptor = Descriptor.from(descString);
        Assert.assertTrue(descriptor instanceof CompositeDescriptor);
        CompositeDescriptor desc = (CompositeDescriptor)descriptor;
        Assert.assertEquals(desc.length(), 4);
    }

    @Test(expected = BadDescriptorException.class)
    public void testIfDescriptorFactoryFailsOnUnrecognisableDescriptor()
    throws BadDescriptorException
    {
        Descriptor descriptor = Descriptor.from("@!$#");
    }

}
