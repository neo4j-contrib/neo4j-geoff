package org.nigelsmall.geoff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.server.rest.domain.JsonParseException;

public class GEOFFLoader {
	
	public static Namespace load(Reader reader, GraphDatabaseService graphDB)
	throws BadDescriptorException, IOException, JsonParseException,
	       UnknownNodeException, UnknownRelationshipException
	{
		BufferedReader bufferedReader = new BufferedReader(reader);
		Namespace namespace = new Namespace(graphDB);
		Transaction tx = graphDB.beginTx();
		int lineNumber = 0;
		String line = null;
		Descriptor descriptor;
		try {
			do {
				line = bufferedReader.readLine();
				lineNumber++;
				if(line != null) {
					descriptor = Descriptor.from(lineNumber, line);
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
			tx.success();
			return namespace;
		} finally {
			tx.finish();
			bufferedReader.close();
		}
	}
	
}
