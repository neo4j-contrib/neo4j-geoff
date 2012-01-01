/**
 * Copyright (c) 2002-2011 "Neo Technology,"
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

import org.neo4j.cypher.SyntaxException;
import org.neo4j.geoff.GEOFF;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.*;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;

@Description("Plugin to handle GEOFF data insertion and emits")
public class GeoffPlugin extends ServerPlugin {

	@Name("load.string")
	@Description("Load GEOFF rules into the database")
	@PluginTarget(GraphDatabaseService.class)
	public String loadFromString(
			@Source GraphDatabaseService graphDB,
			@Description("GEOFF rules to load") @Parameter(name = "rules", optional = false) String rules,
			@Description("Named entity references to pass into load routine") @Parameter(name = "params", optional = true) Map params
	)
			throws SyntaxException {
		try {
			if (rules != null && !rules.isEmpty()) {
				GEOFF.loadIntoNeo4j(new StringReader(rules), graphDB, null);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "OK";
	}

	@Name("load.array")
	@Description("Load GEOFF rules into the database")
	@PluginTarget(GraphDatabaseService.class)
	public String loadFromArray(
			@Source GraphDatabaseService graphDB,
			@Description("GEOFF rules to load") @Parameter(name = "rules", optional = false) String[] rules,
			@Description("Named entity references to pass into load routine") @Parameter(name = "params", optional = true) Map params
	)
			throws SyntaxException {
		try {
			if (rules != null && rules.length > 0) {
				GEOFF.loadIntoNeo4j(Arrays.asList(rules), graphDB, null);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "OK";
	}

	@Name("load.object")
	@Description("Load GEOFF rules into the database")
	@PluginTarget(GraphDatabaseService.class)
	public String loadFromObject(
			@Source GraphDatabaseService graphDB,
			@Description("GEOFF rules to load") @Parameter(name = "rules", optional = false) Map rules,
			@Description("Named entity references to pass into load routine") @Parameter(name = "params", optional = true) Map params
	)
			throws SyntaxException {
		try {
			if (rules != null && !rules.isEmpty()) {
				GEOFF.loadIntoNeo4j((Map<String, Map<String, Object>>) rules, graphDB, null);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "OK";
	}

}
