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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to support named maps of input and output parameters
 */
public abstract class GeoffParams {

	/**
	 * Resolve supplied parameters against the specified database
	 *
	 * @param params
	 * @param graphDB
	 * @return
	 */
	public static Map<String, PropertyContainer> toEntities(Map params, GraphDatabaseService graphDB)
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
						throw new IllegalArgumentException("Cannot resolve parameter: " + key);
					}
				} else {
					throw new IllegalArgumentException("Cannot read parameters");
				}
			} else {
				throw new IllegalArgumentException("Cannot read parameters");
			}
		}
		return p2;
	}

	/**
	 * Convert output entities back into relative URIs (e.g. Node(123) -> "/node/123")
	 *
	 * @param params
	 * @return
	 */
	public static Map<String, String> toRelativeURIs(Map<String, ? extends PropertyContainer> params)
	{
		if (params == null) return null;
		HashMap<String, String> p2 = new HashMap<String, String>(params.size());
		for (Map.Entry<String, ? extends PropertyContainer> param : params.entrySet()) {
			String key = param.getKey();
			if (param.getValue() instanceof Node) {
				p2.put(key, String.format("/node/%d", ((Node) param.getValue()).getId()));
			} else if (param.getValue() instanceof Relationship) {
				p2.put(key, String.format("/relationship/%d", ((Relationship) param.getValue()).getId()));
			} else {
				throw new IllegalArgumentException("Illegal parameter type: " + param.getValue().getClass().getName());
			}
		}
		return p2;
	}

}
