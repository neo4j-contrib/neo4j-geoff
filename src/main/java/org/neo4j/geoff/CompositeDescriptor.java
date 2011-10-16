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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A composite descriptor is a single line within a GEOFF file which can represent multiple
 * entities. It is a 100% JSON-compatible representation of descriptors and data held as key:value
 * pairs within a JSON object. For example:
 *
 * {"(doc)": {"name": "doctor"}, "(dal)": {"name": "dalek"}, "(doc)-[:ENEMY_OF]->(dal)": {"since": "forever"}, "{People}->(doc)": {"name": "The Doctor"}}
 *
 * Note that line breaks within the whitespace parts of the data are not allowed as they are
 * seen as separate lines by the GEOFF parser.
 *
 */
public class CompositeDescriptor extends Descriptor {

    protected final int count;
    protected final Set<NodeDescriptor> nodeDescriptors;
    protected final Set<NodeIndexEntry> nodeIndexEntries;
    protected final Set<RelationshipDescriptor> relationshipDescriptors;
    protected final Set<RelationshipIndexEntry> relationshipIndexEntries;

    protected CompositeDescriptor(Map<String,Map<String,Object>> descriptors)
    throws BadDescriptorException
    {
        super();
        this.count = descriptors.size();
        this.nodeDescriptors = new HashSet<NodeDescriptor>(this.count);
        this.nodeIndexEntries = new HashSet<NodeIndexEntry>(this.count);
        this.relationshipDescriptors = new HashSet<RelationshipDescriptor>(this.count);
        this.relationshipIndexEntries = new HashSet<RelationshipIndexEntry>(this.count);
        for(Map.Entry<String,Map<String, Object>> descriptor : descriptors.entrySet()) {
            Descriptor d = Descriptor.from(descriptor.getKey(), descriptor.getValue());
            if(d instanceof NodeDescriptor) {
                this.nodeDescriptors.add((NodeDescriptor) d);
            } else if(d instanceof NodeIndexEntry) {
                this.nodeIndexEntries.add((NodeIndexEntry) d);
            } else if(d instanceof RelationshipDescriptor) {
                this.relationshipDescriptors.add((RelationshipDescriptor) d);
            } else if(d instanceof RelationshipIndexEntry) {
                this.relationshipIndexEntries.add((RelationshipIndexEntry) d);
            } else {
                throw new UnsupportedOperationException();
            }
        }
	}

    public int length() {
        return this.count;
    }

    public Iterator<NodeDescriptor> nodeDescriptors() {
        return this.nodeDescriptors.iterator();
    }

    public Iterator<NodeIndexEntry> nodeIndexEntries() {
        return this.nodeIndexEntries.iterator();
    }

    public Iterator<RelationshipDescriptor> relationshipDescriptors() {
        return this.relationshipDescriptors.iterator();
    }

    public Iterator<RelationshipIndexEntry> relationshipIndexEntries() {
        return this.relationshipIndexEntries.iterator();
    }

}
