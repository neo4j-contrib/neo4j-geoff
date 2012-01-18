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
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.server.plugins.*;
import org.neo4j.server.rest.repr.Representation;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;

@Description("Plugin to handle Geoff data insertion and emits")
public class GeoffPlugin extends ServerPlugin {

	@Name("load_from_string")
	@Description("Load Geoff rules into the database from a newline-delimited string")
	@PluginTarget(GraphDatabaseService.class)
	public Representation loadFromString(
			@Source GraphDatabaseService graphDB,
			@Description("Geoff rules to load")
			@Parameter(name = "rules", optional = false) String rules,
			@Description("Named entity references to pass into load routine")
			@Parameter(name = "params", optional = true) Map params
	)
			throws GeoffLoadException, IOException {
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(
				new StringReader(rules), graphDB, GeoffParams.toEntities(params, graphDB)
		);
		return new GeoffResultRepresentation(out);
	}

	@Name("load_from_list")
	@Description("Load Geoff rules into the database from a list of rule strings")
	@PluginTarget(GraphDatabaseService.class)
	public Representation loadFromList(
			@Source GraphDatabaseService graphDB,
			@Description("Geoff rules to load")
			@Parameter(name = "rules", optional = false) String[] rules,
			@Description("Named entity references to pass into load routine")
			@Parameter(name = "params", optional = true) Map params
	)
			throws GeoffLoadException, IOException {
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(
				Arrays.asList(rules), graphDB, GeoffParams.toEntities(params, graphDB)
		);
		return new GeoffResultRepresentation(out);
	}

//	@Name("load_from_map")
//	@Description("Load GEOFF rules into the database from a set of descriptor:data pairs")
//	@PluginTarget(GraphDatabaseService.class)
//	public Representation loadFromMap(
//			@Source GraphDatabaseService graphDB,
//			@Description("GEOFF rules to load")
//			@Parameter(name = "rules", optional = false) Map rules,
//			@Description("Named entity references to pass into load routine")
//			@Parameter(name = "params", optional = true) Map params
//	)
//			throws GEOFFLoadException, IOException {
//		try {
//			Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(
//					(Map<String, Map<String, Object>>) rules, graphDB, GeoffParams.toEntities(params, graphDB)
//			);
//			return new GeoffResultRepresentation(out);
//		} catch (ClassCastException e) {
//			throw new GEOFFLoadException("Unable to cast rules to named map");
//		}
//	}

}
