package org.nigelsmall.neo4j.plugins;

import java.io.IOException;
import java.io.StringReader;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;
import org.neo4j.server.rest.domain.JsonParseException;
import org.nigelsmall.geoff.BadDescriptorException;
import org.nigelsmall.geoff.GEOFFLoader;
import org.nigelsmall.geoff.UnknownNodeException;
import org.nigelsmall.geoff.UnknownRelationshipException;

public class GEOFFPlugin extends ServerPlugin {

	@Name("load")
    @Description("Load definitions from GEOFF file into Neo4j graph database")
    @PluginTarget(GraphDatabaseService.class)
    public Node load(@Source GraphDatabaseService graphDB,
    		         @Parameter(name="source") String source)
	throws BadDescriptorException, IOException, JsonParseException,
           UnknownNodeException, UnknownRelationshipException
    {
		return GEOFFLoader.load(new StringReader(source), graphDB).getFirstNode();
    }

}
