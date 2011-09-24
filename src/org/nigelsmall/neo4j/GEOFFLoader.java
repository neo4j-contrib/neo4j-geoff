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
