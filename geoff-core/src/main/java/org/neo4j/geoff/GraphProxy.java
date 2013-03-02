/**
 * Copyright (c) 2002-2013 "Neo Technology,"
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

import java.util.Map;

/**
 * Provides a proxy for an underlying graph database with maintained maps of
 * named entities within that graph.
 *
 * @author Nigel Small
 */
public interface GraphProxy<T> {

	/**
	 * Store parameters against the current graph, attaching names to existing graph entities.
	 *
	 * @param params map of names and associated entities
	 */
	public void inputParams(Map<String, T> params);

	/**
	 * Retrieve parameters stored against the current graph.
	 *
	 * @return map of names and associated entities
	 */
	public Map<String, T> outputParams();

	/**
	 * Merge a subgraph into the attached graph
	 *
	 * @param subgraph the subgraph to merge
	 * @throws SubgraphError if an error occurs while processing the subgraph
	 */
	public void merge(Subgraph subgraph) throws SubgraphError;

	/**
	 * Insert a subgraph into the attached graph
	 *
	 * @param subgraph the subgraph to insert
	 * @throws SubgraphError if an error occurs while processing the subgraph
	 */
	public void insert(Subgraph subgraph) throws SubgraphError;

	/**
	 * Delete a subgraph from the attached graph
	 *
	 * @param subgraph the subgraph to delete
	 * @throws SubgraphError if an error occurs while processing the subgraph
	 */
	public void delete(Subgraph subgraph) throws SubgraphError;

}
