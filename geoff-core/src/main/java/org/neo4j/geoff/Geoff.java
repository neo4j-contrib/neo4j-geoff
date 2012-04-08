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

import org.neo4j.geoff.except.SubgraphError;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * Main entry point for high-level functions. A {@link Subgraph} may be {@link
 * #mergeIntoNeo4j(Subgraph, org.neo4j.graphdb.GraphDatabaseService,
 * java.util.Map) merged into}, {@link #insertIntoNeo4j(Subgraph,
 * org.neo4j.graphdb.GraphDatabaseService, java.util.Map) inserted into} or
 * {@link #deleteFromNeo4j(Subgraph, org.neo4j.graphdb.GraphDatabaseService,
 * java.util.Map) deleted from} a Neo4j graph database using one of the static
 * methods contained within this class.
 *
 * Example usage:
 * <pre>
 * {@code
 * Subgraph subgraph = new Subgraph(
 *     "(A) {\"name\": \"Alice\"}",
 *     "(B) {\"name\": \"Bob\"}",
 *     "(A)-[AB:KNOWS]->(B)"
 * );
 * Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(subgraph, graphDB, null);
 * Node alice = (Node) out.get("(A)");
 * }
 * </pre>
 */
public class Geoff {

	// debug switch used for conditional compilation
	static final boolean DEBUG = false;

	private Geoff() { }

	/**
	 * Merge a {@link Subgraph} into a graph database. Outputs a map of
	 * named entities, e.g. {"(A)": Node(123), "(B)": Node(234), "[AB]":
	 * Rel(456)}. Can accept a similar map of named entities as input
	 * parameters.
	 *
	 * @param subgraph the {@link Subgraph} to merge
	 * @param graphDB the database into which to merge
	 * @param params the input parameters for the merge operation
	 * @return the output parameters from the merge operation
	 * @throws SubgraphError if there is an error processing the {@link
	 * Subgraph} provided
	 */
	public static Map<String, PropertyContainer> mergeIntoNeo4j(
		Subgraph subgraph,
		GraphDatabaseService graphDB,
		Map<String, ? extends PropertyContainer> params
	)
	throws SubgraphError
	{
		Neo4jGraphProxy graph = new Neo4jGraphProxy(graphDB);
		if (params != null) {
			graph.inputParams(new HashMap<String, PropertyContainer>(params));
		}
		graph.merge(subgraph);
		return graph.outputParams();
	}

	/**
	 * Insert a {@link Subgraph} into a graph database. Outputs a map of
	 * named entities, e.g. {"(A)": Node(123), "(B)": Node(234), "[AB]":
	 * Rel(456)}. Can accept a similar map of named entities as input
	 * parameters.
	 *
	 * @param subgraph the {@link Subgraph} to insert
	 * @param graphDB the database into which to insert
	 * @param params the input parameters for the insert operation
	 * @return the output parameters from the insert operation
	 * @throws SubgraphError if there is an error processing the {@link
	 * Subgraph} provided
	 */
	public static Map<String, PropertyContainer> insertIntoNeo4j(
		Subgraph subgraph,
		GraphDatabaseService graphDB,
		Map<String, ? extends PropertyContainer> params
	)
	throws SubgraphError
	{
		Neo4jGraphProxy graph = new Neo4jGraphProxy(graphDB);
		if (params != null) {
			graph.inputParams(new HashMap<String, PropertyContainer>(params));
		}
		graph.insert(subgraph);
		return graph.outputParams();
	}

	/**
	 * Delete a {@link Subgraph} from a graph database. Outputs a map of
	 * named entities, e.g. {"(A)": Node(123), "(B)": Node(234), "[AB]":
	 * Rel(456)}. Can accept a similar map of named entities as input
	 * parameters.
	 *
	 * @param subgraph the {@link Subgraph} to delete
	 * @param graphDB the database from which to delete
	 * @param params the input parameters for the delete operation
	 * @return the output parameters from the delete operation
	 * @throws SubgraphError if there is an error processing the {@link
	 * Subgraph} provided
	 */
	public static Map<String, PropertyContainer> deleteFromNeo4j(
		Subgraph subgraph,
		GraphDatabaseService graphDB,
		Map<String, ? extends PropertyContainer> params
	)
	throws SubgraphError
	{
		Neo4jGraphProxy graph = new Neo4jGraphProxy(graphDB);
		if (params != null) {
			graph.inputParams(new HashMap<String, PropertyContainer>(params));
		}
		graph.delete(subgraph);
		return graph.outputParams();
	}

}
