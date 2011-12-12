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
import org.neo4j.geoff.CompositeDescriptor;
import org.neo4j.geoff.Descriptor;


public class CompositeDescriptorTest {

	@Test
	public void testIfDescriptorFactoryUnderstandsCompositeDescriptors()
			throws BadDescriptorException {
		String descString = "{\"(doc)\": {\"name\": \"doctor\"}, \"(dal)\": {\"name\": \"dalek\"}," +
				"\"(doc)-[:ENEMY_OF]->(dal)\": {\"since\": \"forever\"}, \"(doc)<=|People|\": {\"name\": \"The Doctor\"} }";
		Descriptor descriptor = Descriptor.from(descString);
		Assert.assertTrue(descriptor instanceof CompositeDescriptor);
		CompositeDescriptor desc = (CompositeDescriptor) descriptor;
		Assert.assertEquals(desc.length(), 4);
	}

	@Test(expected = BadDescriptorException.class)
	public void testIfDescriptorFactoryFailsOnCompositeDescriptorWithBadJSON()
			throws BadDescriptorException {
		String descString = "{\"(doc)\": {\"name\"; \"doctor\"}, \"(dal)\": {\"name\": \"dalek\"}," +
				"\"(doc)-[:ENEMY_OF]->(dal)\": {\"since\": \"forever\"}, \"(doc)<=|People|\": {\"name\": \"The Doctor\"} }";
		Descriptor descriptor = Descriptor.from(descString);
		Assert.assertTrue(descriptor instanceof CompositeDescriptor);
		CompositeDescriptor desc = (CompositeDescriptor) descriptor;
		Assert.assertEquals(desc.length(), 4);
	}

}
