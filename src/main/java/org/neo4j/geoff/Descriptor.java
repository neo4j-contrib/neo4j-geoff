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
package org.neo4j.geoff;

import org.neo4j.geoff.util.JSON;
import org.neo4j.geoff.util.JSONException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Descriptor {

	// the regular expressions that make all the parsing magic happen...
	private static final Pattern IGNORABLE_LINE = Pattern.compile("^(\\s*|(#.*))$");
    private static final Pattern COMPOSITE_DESCRIPTOR = Pattern.compile("^(\\{\\s*\".+\"\\s*:\\s*\\{.*\\}\\s*\\})");
	private static final Pattern NODE_DESCRIPTOR = Pattern.compile("^\\((\\w*)\\)$");
	private static final Pattern NODE_INDEX_ENTRY = Pattern.compile("^\\{(\\w+)\\}->\\((\\w+)\\)$");
	private static final Pattern RELATIONSHIP_DESCRIPTOR = Pattern.compile("^\\((\\w+)\\)-\\[(\\w*):(\\w+)\\]->\\((\\w+)\\)$");
	private static final Pattern RELATIONSHIP_INDEX_ENTRY = Pattern.compile("^\\{(\\w+)\\}->\\[(\\w+)\\]$");

	/**
	 * Factory method to produce a Descriptor object from a given line of
	 * GEOFF source
	 * 
	 * @param source the line of source to be parsed
	 * @return a Descriptor of an appropriate type
	 * @throws BadDescriptorException if this line doesn't match any known pattern
	 */
	public static Descriptor from(String source)
	throws BadDescriptorException
	{
        Matcher m = IGNORABLE_LINE.matcher(source);
        if(m.find()) {
            return null;
        }
        m = COMPOSITE_DESCRIPTOR.matcher(source);
        if(m.find()) {
            try {
                return new CompositeDescriptor(JSON.toObjectOfObjects(m.group(1)));
            } catch(JSONException e) {
                throw new BadDescriptorException(source, e);
            }
        }
        String[] bits = source.split("\\s+", 2);
        if(bits.length == 1) {
            return Descriptor.from(bits[0], null);
        } else {
            try {
                return Descriptor.from(bits[0], JSON.toObject(bits[1]));
            } catch(JSONException e) {
                throw new BadDescriptorException(source, e);
            }
        }
    }

    public static Descriptor from(String descriptor, Map<String,Object> data)
    throws BadDescriptorException
    {
        Matcher m = NODE_DESCRIPTOR.matcher(descriptor);
        if(m.find()) {
            return new NodeDescriptor(m.group(1), data);
        }
        m = NODE_INDEX_ENTRY.matcher(descriptor);
        if(m.find()) {
            return new NodeIndexEntry(m.group(1), m.group(2), data);
        }
        m = RELATIONSHIP_DESCRIPTOR.matcher(descriptor);
        if(m.find()) {
            return new RelationshipDescriptor(m.group(1), m.group(2), m.group(3), m.group(4), data);
        }
        m = RELATIONSHIP_INDEX_ENTRY.matcher(descriptor);
        if(m.find()) {
            return new RelationshipIndexEntry(m.group(1), m.group(2), data);
        }
        // nothing left to match against, must be invalid
        throw new BadDescriptorException(descriptor);
    }

}
