/**
 * Copyright (c) 2002-2012 "Neo Technology,"
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
package org.neo4j.geoff;

import org.neo4j.geoff.except.GeoffLoadException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Geoff {

	// debug switch used for conditional compilation
	static final boolean DEBUG = false;

	/**
	 * Merge a subgraph into a graph database
	 *
	 * @param subgraph the subgraph to merge
	 * @param graphDB the database into which to merge
	 * @param params the input parameters for the merge operation
	 * @return the output parameters from the merge operation
	 * @throws GeoffLoadException
	 * @throws IOException
	 */
	public static Map<String, PropertyContainer> mergeIntoNeo4j(
		Subgraph subgraph,
		GraphDatabaseService graphDB,
		Map<String, ? extends PropertyContainer> params
	)
	throws GeoffLoadException, IOException
	{
		Neo4jGraphProxy graph = new Neo4jGraphProxy(graphDB);
		if (params != null) {
			graph.inputParams(new HashMap<String, PropertyContainer>(params));
		}
		graph.merge(subgraph);
		return graph.outputParams();
	}

	/**
	 * Insert a subgraph into a graph database
	 *
	 * @param subgraph the subgraph to insert
	 * @param graphDB the database into which to insert
	 * @param params the input parameters for the insert operation
	 * @return the output parameters from the insert operation
	 * @throws GeoffLoadException
	 * @throws IOException
	 */
	public static Map<String, PropertyContainer> insertIntoNeo4j(
		Subgraph subgraph,
		GraphDatabaseService graphDB,
		Map<String, ? extends PropertyContainer> params
	)
	throws GeoffLoadException, IOException
	{
		Neo4jGraphProxy graph = new Neo4jGraphProxy(graphDB);
		if (params != null) {
			graph.inputParams(new HashMap<String, PropertyContainer>(params));
		}
		graph.insert(subgraph);
		return graph.outputParams();
	}

	/**
	 * Delete a subgraph from a graph database
	 *
	 * @param subgraph the subgraph to delete
	 * @param graphDB the database from which to delete
	 * @param params the input parameters for the delete operation
	 * @return the output parameters from the delete operation
	 * @throws GeoffLoadException
	 * @throws IOException
	 */
	public static Map<String, PropertyContainer> deleteFromNeo4j(
		Subgraph subgraph,
		GraphDatabaseService graphDB,
		Map<String, ? extends PropertyContainer> params
	)
	throws GeoffLoadException, IOException
	{
		Neo4jGraphProxy graph = new Neo4jGraphProxy(graphDB);
		if (params != null) {
			graph.inputParams(new HashMap<String, PropertyContainer>(params));
		}
		graph.delete(subgraph);
		return graph.outputParams();
	}

}
