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

import org.neo4j.geoff.GEOFF;
import org.neo4j.geoff.GEOFFLoadException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.server.plugins.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

@Description("Plugin to handle GEOFF data insertion and emits")
public class GeoffPlugin extends ServerPlugin {

	// Would prefer to be able to to this by providing alternative Content-Type (application/geoff?)
	@Name("load.string")
	@Description("Load GEOFF rules into the database from a newline-delimited string")
	@PluginTarget(GraphDatabaseService.class)
	public Iterable<Node> loadFromString(
			@Source GraphDatabaseService graphDB,
			@Description("GEOFF rules to load")
			@Parameter(name = "rules", optional = false) String rules,
			@Description("Named entity references to pass into load routine")
			@Parameter(name = "params", optional = true) Map params
	)
			throws GEOFFLoadException, IOException {
		Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(
				new StringReader(rules), graphDB, resolvedParams(params, graphDB)
		);
		// since we are limited by return types of server plugins, return only unnamed set of nodes
		return nodesFrom(out);
	}

	@Name("load.list")
	@Description("Load GEOFF rules into the database from a list of rule strings")
	@PluginTarget(GraphDatabaseService.class)
	public Iterable<Node> loadFromArray(
			@Source GraphDatabaseService graphDB,
			@Description("GEOFF rules to load")
			@Parameter(name = "rules", optional = false) String[] rules,
			@Description("Named entity references to pass into load routine")
			@Parameter(name = "params", optional = true) Map params
	)
			throws GEOFFLoadException, IOException {
		Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(
				Arrays.asList(rules), graphDB, resolvedParams(params, graphDB)
		);
		// since we are limited by return types of server plugins, return only unnamed set of nodes
		return nodesFrom(out);
	}

	@Name("load.map")
	@Description("Load GEOFF rules into the database from a set of descriptor:data pairs")
	@PluginTarget(GraphDatabaseService.class)
	public Iterable<Node> loadFromObject(
			@Source GraphDatabaseService graphDB,
			@Description("GEOFF rules to load")
			@Parameter(name = "rules", optional = false) Map rules,
			@Description("Named entity references to pass into load routine")
			@Parameter(name = "params", optional = true) Map params
	)
			throws GEOFFLoadException, IOException {
		try {
			Map<String, PropertyContainer> out = GEOFF.loadIntoNeo4j(
					(Map<String, Map<String, Object>>) rules, graphDB, resolvedParams(params, graphDB)
			);
			// since we are limited by return types of server plugins, return only unnamed set of nodes
			return nodesFrom(out);
		} catch (ClassCastException e) {
			throw new GEOFFLoadException("Unable to cast rules to named map");
		}
	}

	/**
	 * Resolve supplied parameters against the specified database
	 *
	 * @param params
	 * @param graphDB
	 * @return
	 * @throws GEOFFLoadException
	 */
	private static Map<String, PropertyContainer> resolvedParams(Map params, GraphDatabaseService graphDB)
			throws GEOFFLoadException
	{
		if (params == null) return null;
		HashMap<String, PropertyContainer> p2 = new HashMap<String, PropertyContainer>(params.size());
		for (Object param : params.entrySet()) {
			if (param instanceof Map.Entry) {
				Map.Entry entry = (Map.Entry) param;
				Object entryKey = entry.getKey();
				Object entryValue = entry.getValue();
				if (entryKey instanceof String && entryValue instanceof String) {
					String key = (String) entryKey;
					String value = (String) entryValue;
					if (value.startsWith("/node/")) {
						p2.put(key, graphDB.getNodeById(Integer.parseInt(value.substring(6))));
					} else if (value.startsWith("/relationship/")) {
						p2.put(key, graphDB.getNodeById(Integer.parseInt(value.substring(13))));
					} else {
						throw new GEOFFLoadException("Cannot resolve parameter: " + key);
					}
				} else {
					throw new GEOFFLoadException("Cannot read parameters");
				}
			} else {
				throw new GEOFFLoadException("Cannot read parameters");
			}
		}
		return p2;
	}
	
	/**
	 * Extract nodes from set of named nodes and relationships into set of unnamed nodes
	 *
	 * @param entities named map of nodes and relationships
	 * @return set of extracted nodes
	 */
	private static Set<Node> nodesFrom(Map<String, PropertyContainer> entities)
	{
		HashSet<Node> nodes = new HashSet<Node>(entities.size());
		for (PropertyContainer entity : entities.values()) {
			if(entity instanceof Node) {
				nodes.add((Node) entity);
			}
		}
		return nodes;
	}
	
}
