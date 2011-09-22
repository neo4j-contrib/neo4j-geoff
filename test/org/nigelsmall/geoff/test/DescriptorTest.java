package org.nigelsmall.geoff.test;

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.server.rest.domain.JsonParseException;
import org.nigelsmall.geoff.BadDescriptorException;
import org.nigelsmall.geoff.Descriptor;
import org.nigelsmall.geoff.NodeDescriptor;
import org.nigelsmall.geoff.NodeIndexEntry;
import org.nigelsmall.geoff.RelationshipDescriptor;
import org.nigelsmall.geoff.RelationshipIndexEntry;


public class DescriptorTest {

	@Test
	public void testIfDescriptorFactoryUnderstandsBlankLines()
	throws JsonParseException, BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from(1, "");
		Assert.assertNull(descriptor);
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsLinesOfWhitespace()
	throws JsonParseException, BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from(1, "\t    ");
		Assert.assertNull(descriptor);
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsComments()
	throws JsonParseException, BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from(1, "# this is a comment");
		Assert.assertNull(descriptor);
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNodeDescriptors()
	throws JsonParseException, BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from(1, "(foo)");
		Assert.assertTrue(descriptor instanceof NodeDescriptor);
		NodeDescriptor nodeDescriptor = (NodeDescriptor)descriptor;
		Assert.assertEquals(nodeDescriptor.getNodeName(), "foo");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNodeDescriptorsWithData()
	throws JsonParseException, BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from(1, "(foo) {\"pi\":3.1415}");
		Assert.assertTrue(descriptor instanceof NodeDescriptor);
		NodeDescriptor nodeDescriptor = (NodeDescriptor)descriptor;
		Assert.assertEquals(nodeDescriptor.getNodeName(), "foo");
		Assert.assertTrue(nodeDescriptor.getData().containsKey("pi"));
		Assert.assertEquals(nodeDescriptor.getData().get("pi"), 3.1415);
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNodeIndexEntries()
	throws JsonParseException, BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from(1, "{index1}->(bob) {\"foo\":\"bar\"}");
		Assert.assertTrue(descriptor instanceof NodeIndexEntry);
		NodeIndexEntry nodeIndexEntry = (NodeIndexEntry)descriptor;
		Assert.assertEquals(nodeIndexEntry.getIndexName(), "index1");
		Assert.assertEquals(nodeIndexEntry.getNodeName(), "bob");
		Assert.assertTrue(nodeIndexEntry.getData().containsKey("foo"));
		Assert.assertEquals(nodeIndexEntry.getData().get("foo"), "bar");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsUnnamedRelationshipDescriptors()
	throws JsonParseException, BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from(1, "(foo)-[:KNOWS]->(bar)");
		Assert.assertTrue(descriptor instanceof RelationshipDescriptor);
		RelationshipDescriptor relDescriptor = (RelationshipDescriptor)descriptor;
		Assert.assertEquals(relDescriptor.getStartNodeName(), "foo");
		Assert.assertEquals(relDescriptor.getRelationshipName(), "");
		Assert.assertEquals(relDescriptor.getRelationshipType(), "KNOWS");
		Assert.assertEquals(relDescriptor.getEndNodeName(), "bar");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsNamedRelationshipDescriptors()
	throws JsonParseException, BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from(1, "(foo)-[bob:KNOWS]->(bar)");
		Assert.assertTrue(descriptor instanceof RelationshipDescriptor);
		RelationshipDescriptor relDescriptor = (RelationshipDescriptor)descriptor;
		Assert.assertEquals(relDescriptor.getStartNodeName(), "foo");
		Assert.assertEquals(relDescriptor.getRelationshipName(), "bob");
		Assert.assertEquals(relDescriptor.getRelationshipType(), "KNOWS");
		Assert.assertEquals(relDescriptor.getEndNodeName(), "bar");
	}

	@Test
	public void testIfDescriptorFactoryUnderstandsRelationshipDescriptorsWithData()
	throws JsonParseException, BadDescriptorException
	{
		Descriptor descriptor = Descriptor.from(1, "(foo)-[bob:KNOWS]->(bar) {\"pi\":3.1415}");
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
	throws BadDescriptorException, JsonParseException
	{
		Descriptor descriptor = Descriptor.from(1, "{index1}->[bob] {\"foo\":\"bar\"}");
		Assert.assertTrue(descriptor instanceof RelationshipIndexEntry);
		RelationshipIndexEntry relIndexEntry = (RelationshipIndexEntry)descriptor;
		Assert.assertEquals(relIndexEntry.getIndexName(), "index1");
		Assert.assertEquals(relIndexEntry.getRelationshipName(), "bob");
		Assert.assertTrue(relIndexEntry.getData().containsKey("foo"));
		Assert.assertEquals(relIndexEntry.getData().get("foo"), "bar");
	}

}
