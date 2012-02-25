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
 * Main entry point for high-level functions. A {@link Subgraph} may be merged into,
 * inserted into or deleted from a Neo4j graph database using one of the static
 * methods contained within this class.
 */
public class Geoff {

	// debug switch used for conditional compilation
	static final boolean DEBUG = false;

	private Geoff() { }

	/**
	 * Merge a subgraph into a graph database. The following rules describe specific behaviour:
	 *
	 * <h3>Nodes</h3>
	 * <table border="1" cellpadding="4" style="border: 1px solid #000; border-collapse: collapse; font-size: small; text-align: center;">
	 *     <tr style="background-color: #DDD;"><th>Rule</th><th>(A) defined</th><th>(A) undefined</th></tr>
	 *     <tr><td>(A)</td><td>update (A)</td><td>define (A) as new node</td></tr>
	 * </table>
	 *
	 * <h3>Named Relationships</h3>
	 * <table border="1" cellpadding="4" style="border: 1px solid #000; border-collapse: collapse; font-size: small; text-align: center;">
	 *     <tr style="background-color: #DDD;"><th>Rule</th><th>[R] defined</th><th>[R] undefined</th></tr>
	 *     <tr><td>[R]</td><td>update [R]</td><td>~</td></tr>
	 *     <tr><td>(A)-[R]->(B)</td><td>update [R]</td><td>define and update [R] as existing relationship between
	 *     (A) and (B) if one exists</td></tr>
	 *     <tr><td>[R:TYPE]</td><td>update [R]</td><td>define [R] as new relationship of type TYPE between two new
	 *     nodes</td></tr>
	 *     <tr><td>(A)-[R:TYPE]->(B)</td><td>update [R]</td><td>define and update [R] as existing relationship of type TYPE
	 *     between (A) and (B) if one exists, or create new relationship otherwise</td></tr>
	 * </table>
	 *
	 * <h3>Unnamed Relationships</h3>
	 * <table border="1" cellpadding="4" style="border: 1px solid #000; border-collapse: collapse; font-size: small; text-align: center;">
	 *     <tr style="background-color: #DDD;"><th>Rule</th><th>Behaviour</th></tr>
	 *     <tr><td>[]</td><td>~</td></tr>
	 *     <tr><td>(A)-[]->(B)</td><td>~</td></tr>
	 *     <tr><td>[:TYPE]</td><td>create relationship of type TYPE between two new nodes</td></tr>
	 *     <tr><td>(A)-[:TYPE]->(B)</td><td>create relationship of type TYPE between (A) and (B)</td></tr>
	 * </table>
	 *
	 * @param subgraph the subgraph to merge
	 * @param graphDB the database into which to merge
	 * @param params the input parameters for the merge operation
	 * @return the output parameters from the merge operation
	 * @throws SubgraphError if there is an error processing the subgraph provided
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
	 * Insert a subgraph into a graph database. The following rules describe specific behaviour:
	 *
	 * @param subgraph the subgraph to insert
	 * @param graphDB the database into which to insert
	 * @param params the input parameters for the insert operation
	 * @return the output parameters from the insert operation
	 * @throws SubgraphError if there is an error processing the subgraph provided
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
	 * Delete a subgraph from a graph database. The following rules describe specific behaviour:
	 *
	 * @param subgraph the subgraph to delete
	 * @param graphDB the database from which to delete
	 * @param params the input parameters for the delete operation
	 * @return the output parameters from the delete operation
	 * @throws SubgraphError if there is an error processing the subgraph provided
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
