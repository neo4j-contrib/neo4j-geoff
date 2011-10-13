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
package org.nigelsmall.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.nigelsmall.geoff.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class GEOFFLoader {
	
	/**
	 * Static method to kick off loading a GEOFF file into the specified
	 * GraphDatabaseService, taking data from the supplied Reader
	 * 
	 * @param reader the reader to grab data from
	 * @param graphDB the database to put stuff into
	 * @return the Namespace used to store all named entities
	 * @throws BadDescriptorException
	 * @throws IOException
	 * @throws UnknownNodeException
	 * @throws UnknownRelationshipException
	 */
	public static Neo4jNamespace load(Reader reader, GraphDatabaseService graphDB)
	throws BadDescriptorException, IOException, UnknownNodeException, UnknownRelationshipException
	{
		// better to use a BufferedReader so we can grab one line at a time
        BufferedReader bufferedReader = new BufferedReader(reader);
        // initialise the Namespace for use with this GraphDatabaseService
        Neo4jNamespace namespace = new Neo4jNamespace(graphDB);
        // and start a lovely new transaction
        Transaction tx = graphDB.beginTx();
        // move along, nothing to see here
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
                    descriptor = Descriptor.from(lineNumber, line);
                    // and process accordingly
                    if(descriptor instanceof NodeDescriptor) {
                        namespace.createNode((NodeDescriptor)descriptor);
                    } else if(descriptor instanceof NodeIndexEntry) {
                        namespace.addNodeIndexEntry((NodeIndexEntry)descriptor);
                    } else if(descriptor instanceof RelationshipDescriptor) {
                        namespace.createRelationship((RelationshipDescriptor)descriptor);
                    } else if(descriptor instanceof RelationshipIndexEntry) {
                        namespace.addRelationshipIndexEntry((RelationshipIndexEntry)descriptor);
                    }
                }
            } while(line != null);
            // if we're here, nothing has gone wrong so label it a success
            tx.success();
            return namespace;
        } finally {
            tx.finish();
            bufferedReader.close();
        }
    }
    
}
