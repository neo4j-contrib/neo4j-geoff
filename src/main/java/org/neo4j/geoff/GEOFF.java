/**
 * Copyright (c) 2002-2011 "Neo Technology,"
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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class GEOFF {

	public static final boolean DEBUG = false;

	/**
	 * Static method to kick off loading a GEOFF file into the specified
	 * GraphDatabaseService, taking data from the supplied Reader
	 *
	 * @param reader
	 * @param graphDB
	 * @param params
	 * @return
	 * @throws IOException
	 * @throws IllegalRuleException
	 * @throws SyntaxError
	 * @throws DependencyException
	 */
	public static Map<String, PropertyContainer> loadIntoNeo4j(
			Reader reader,
			GraphDatabaseService graphDB,
			Map<String, ? extends PropertyContainer> params
	)
			throws IOException, IllegalRuleException, SyntaxError, DependencyException {
		Transaction tx = graphDB.beginTx();
		try {
			GEOFFLoader<Neo4jNamespace> loader = new GEOFFLoader<Neo4jNamespace>(
					reader,
					new Neo4jNamespace(graphDB, params)
			);
			tx.success();
			return loader.getNamespace().getEntities();
		} finally {
			tx.finish();
		}
	}

	public static Map<String, PropertyContainer> loadIntoNeo4j(
			Map<String, Map<String, Object>> rules,
			GraphDatabaseService graphDB,
			Map<String, ? extends PropertyContainer> params
	)
			throws IOException, IllegalRuleException, SyntaxError, DependencyException {
		Transaction tx = graphDB.beginTx();
		try {
			GEOFFLoader<Neo4jNamespace> loader = new GEOFFLoader<Neo4jNamespace>(
					rules,
					new Neo4jNamespace(graphDB, params)
			);
			tx.success();
			return loader.getNamespace().getEntities();
		} finally {
			tx.finish();
		}
	}

}
