/**
 * Copyright (c) 2002-2012 "Neo Technology,"
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
import org.neo4j.geoff.except.GeoffLoadException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.*;
import org.neo4j.server.rest.repr.Representation;

import java.io.IOException;
import java.util.Map;

@Description("Plugin to handle Geoff data insertion and emits")
public class GeoffPlugin extends ServerPlugin {

	@Name("merge_from_string")
	@Description("Merge Geoff subgraph into the database from a newline-delimited string")
	@PluginTarget(GraphDatabaseService.class)
	public Representation mergeFromString(
			@Source GraphDatabaseService graphDB,
			@Description("Geoff subgraph to merge")
			@Parameter(name = "subgraph", optional = false) String subgraph,
			@Description("Named entity references to pass into merge routine")
			@Parameter(name = "params", optional = true) Map params
	)
	throws GeoffLoadException, IOException
	{
		return new GeoffResultRepresentation(
			Geoff.mergeIntoNeo4j(new Subgraph(subgraph), graphDB, GeoffParams.toEntities(params, graphDB))
		);
	}

	@Name("merge_from_list")
	@Description("Merge Geoff subgraph into the database from a list of rule strings")
	@PluginTarget(GraphDatabaseService.class)
	public Representation mergeFromList(
			@Source GraphDatabaseService graphDB,
			@Description("Geoff subgraph to merge")
			@Parameter(name = "subgraph", optional = false) String[] subgraph,
			@Description("Named entity references to pass into merge routine")
			@Parameter(name = "params", optional = true) Map params
	)
	throws GeoffLoadException, IOException
	{
		return new GeoffResultRepresentation(
			Geoff.mergeIntoNeo4j(new Subgraph(subgraph), graphDB, GeoffParams.toEntities(params, graphDB))
		);
	}

}
