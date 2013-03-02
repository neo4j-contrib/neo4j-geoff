/**
 * Copyright (c) 2002-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.plugin.geoff;

import org.neo4j.geoff.Geoff;
import org.neo4j.geoff.Subgraph;
import org.neo4j.geoff.except.SubgraphError;
import org.neo4j.geoff.except.SyntaxError;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.*;
import org.neo4j.server.rest.repr.Representation;

import java.util.Map;

@Description("Plugin to handle Geoff data insertion and emits")
public class GeoffPlugin extends ServerPlugin {

	@Name("merge")
	@Description("Merge Geoff subgraph into the database from a list of rule strings")
	@PluginTarget(GraphDatabaseService.class)
	public Representation merge(
			@Source GraphDatabaseService graphDB,
			@Description("Geoff subgraph to merge")
			@Parameter(name = "subgraph", optional = false) String[] subgraph,
			@Description("Named entity references to pass into merge routine")
			@Parameter(name = "params", optional = true) Map params
	)
	throws SubgraphError, SyntaxError
	{
		return new GeoffResultRepresentation(
			Geoff.mergeIntoNeo4j(new Subgraph(subgraph), graphDB, GeoffParams.toEntities(params, graphDB))
		);
	}

	@Name("insert")
	@Description("Insert Geoff subgraph into the database from a list of rule strings")
	@PluginTarget(GraphDatabaseService.class)
	public Representation insert(
		@Source GraphDatabaseService graphDB,
		@Description("Geoff subgraph to insert")
		@Parameter(name = "subgraph", optional = false) String[] subgraph,
		@Description("Named entity references to pass into insert routine")
		@Parameter(name = "params", optional = true) Map params
	)
	throws SubgraphError, SyntaxError
	{
		return new GeoffResultRepresentation(
			Geoff.insertIntoNeo4j(new Subgraph(subgraph), graphDB, GeoffParams.toEntities(params, graphDB))
		);
	}

	@Name("delete")
	@Description("Delete Geoff subgraph from the database as defined by a list of rule strings")
	@PluginTarget(GraphDatabaseService.class)
	public Representation delete(
		@Source GraphDatabaseService graphDB,
		@Description("Geoff subgraph to delete")
		@Parameter(name = "subgraph", optional = false) String[] subgraph,
		@Description("Named entity references to pass into delete routine")
		@Parameter(name = "params", optional = true) Map params
	)
	throws SubgraphError, SyntaxError
	{
		return new GeoffResultRepresentation(
			Geoff.deleteFromNeo4j(new Subgraph(subgraph), graphDB, GeoffParams.toEntities(params, graphDB))
		);
	}

}
