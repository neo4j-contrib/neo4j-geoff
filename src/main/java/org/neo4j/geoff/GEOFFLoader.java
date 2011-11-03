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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

public class GEOFFLoader<NS extends Namespace> {

	/**
	 * Static method to kick off loading a GEOFF file into the specified
	 * GraphDatabaseService, taking data from the supplied Reader
	 * 
	 * @param reader the reader to grab data from
	 * @param graphDB the database to put stuff into
	 * @return the Namespace used to store all named entities
	 * @throws BadDescriptorException when a badly-formed descriptor is encountered
	 * @throws IOException
	 * @throws UnknownNodeException
	 * @throws UnknownRelationshipException
	 */
	public static Neo4jNamespace loadIntoNeo4j(Reader reader, GraphDatabaseService graphDB)
	throws BadDescriptorException, IOException, UnknownNodeException, UnknownRelationshipException
	{
        Transaction tx = graphDB.beginTx();
        try {
    		GEOFFLoader<Neo4jNamespace> loader = new GEOFFLoader<Neo4jNamespace>(reader, new Neo4jNamespace(graphDB));
            tx.success();
            return loader.getNamespace();
        } finally {
            tx.finish();
        }
    }
	
	
	/**
     * Static method to kick off loading a GEOFF file into the specified
     * GraphDatabaseService, taking data from the supplied Reader
     * 
     * @param reader the reader to grab data from
     * @param graphDB the database to put stuff into
     * @return the Namespace used to store all named entities
     * @throws BadDescriptorException when a badly-formed descriptor is encountered
     * @throws IOException
     * @throws UnknownNodeException
     * @throws UnknownRelationshipException
     */
    public static Neo4jNamespace loadIntoNeo4j(Map geoff, GraphDatabaseService graphDB)
    throws BadDescriptorException, IOException, UnknownNodeException, UnknownRelationshipException
    {
        Transaction tx = graphDB.beginTx();
        try {
            GEOFFLoader<Neo4jNamespace> loader = new GEOFFLoader<Neo4jNamespace>(geoff, new Neo4jNamespace(graphDB));
            tx.success();
            return loader.getNamespace();
        } finally {
            tx.finish();
        }
    }

    private final NS namespace;

    private GEOFFLoader(Reader reader, NS namespace)
	throws BadDescriptorException, IOException, UnknownNodeException, UnknownRelationshipException
	{
        BufferedReader bufferedReader = new BufferedReader(reader);
        this.namespace = namespace;
        int lineNumber = 0;
        String line;
        Descriptor descriptor;
        try {
        	// iterate through every line in the source data
            do {
                line = bufferedReader.readLine();
                lineNumber++;
                if(line != null) {
                	// turn the line of text into a Descriptor
                    try {
                        descriptor = Descriptor.from(line);
                    } catch(BadDescriptorException e) {
                        // if something goes wrong, attach the line number and re-throw
                        e.setLineNumber(lineNumber);
                        throw e;
                    }
                    // add the described data to the namespace
                    this.add(descriptor);
                }
            } while(line != null);
        } finally {
            bufferedReader.close();
        }
    }
    
    /**
     * Load a graph form a map for GEOFF statements.
     * 
     * @param geoff the map of GEOFF
     * @param namespace
     * @throws BadDescriptorException
     * @throws IOException
     * @throws UnknownNodeException
     * @throws UnknownRelationshipException
     */
    private GEOFFLoader(Map<String, Map> geoff, NS namespace)
            throws BadDescriptorException, IOException, UnknownNodeException, UnknownRelationshipException
            {
                this.namespace = namespace;
                Descriptor descriptor;
                    for (String key : geoff.keySet())
                            this.add(Descriptor.from(key, geoff.get( key )));
            }

    /**
     * Add a descriptor to the namespace associated with this loader
     *
     * @param descriptor the descriptor to add
     * @throws UnknownNodeException when an unknown node is referenced
     * @throws UnknownRelationshipException when an unknown relationship is referenced
     */
    private void add(Descriptor descriptor)
    throws UnknownNodeException, UnknownRelationshipException
    {
        if(descriptor instanceof CompositeDescriptor) {
            CompositeDescriptor composite = (CompositeDescriptor) descriptor;
            // iterate multiple times to avoid dependency issues
            for(NodeDescriptor d : composite.nodeDescriptors) {
                this.namespace.createNode(d);
            }
            for(NodeIndexEntry d : composite.nodeIndexEntries) {
                this.namespace.addNodeIndexEntry(d);
            }
            for(RelationshipDescriptor d : composite.relationshipDescriptors) {
                this.namespace.createRelationship(d);
            }
            for(RelationshipIndexEntry d : composite.relationshipIndexEntries) {
                this.namespace.addRelationshipIndexEntry(d);
            }
        } else if(descriptor instanceof NodeDescriptor) {
            this.namespace.createNode((NodeDescriptor)descriptor);
        } else if(descriptor instanceof NodeIndexEntry) {
            this.namespace.addNodeIndexEntry((NodeIndexEntry)descriptor);
        } else if(descriptor instanceof RelationshipDescriptor) {
            this.namespace.createRelationship((RelationshipDescriptor)descriptor);
        } else if(descriptor instanceof RelationshipIndexEntry) {
            this.namespace.addRelationshipIndexEntry((RelationshipIndexEntry)descriptor);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public NS getNamespace() {
        return this.namespace;
    }

}
