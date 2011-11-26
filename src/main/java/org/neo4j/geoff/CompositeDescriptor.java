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
    protected final Set<HookDescriptor> hooks;
    protected final Set<NodeDescriptor> nodes;
    protected final Set<RelationshipDescriptor> relationships;
    protected final Set<IndexEntry> indexEntries;

    protected CompositeDescriptor(Map<String,Map<String,Object>> descriptors)
    throws BadDescriptorException
    {
        super();
        this.count = descriptors.size();
        this.hooks = new HashSet<HookDescriptor>(this.count);
        this.nodes = new HashSet<NodeDescriptor>(this.count);
        this.relationships = new HashSet<RelationshipDescriptor>(this.count);
        this.indexEntries = new HashSet<IndexEntry>(this.count);
        for(Map.Entry<String,Map<String, Object>> descriptor : descriptors.entrySet()) {
            Descriptor d = Descriptor.from(descriptor.getKey(), descriptor.getValue());
            if(d instanceof HookDescriptor) {
                this.hooks.add((HookDescriptor) d);
            } else if(d instanceof NodeDescriptor) {
                this.nodes.add((NodeDescriptor) d);
            } else if(d instanceof RelationshipDescriptor) {
                this.relationships.add((RelationshipDescriptor) d);
            } else if(d instanceof IndexEntry) {
                this.indexEntries.add((IndexEntry) d);
            } else {
                throw new UnsupportedOperationException();
            }
        }
	}

    public int length() {
        return this.count;
    }

    public Iterator<HookDescriptor> hooks() {
        return this.hooks.iterator();
    }

    public Iterator<NodeDescriptor> nodes() {
        return this.nodes.iterator();
    }

    public Iterator<RelationshipDescriptor> relationshipDescriptors() {
        return this.relationships.iterator();
    }

    public Iterator<IndexEntry> indexEntries() {
        return this.indexEntries.iterator();
    }

}
