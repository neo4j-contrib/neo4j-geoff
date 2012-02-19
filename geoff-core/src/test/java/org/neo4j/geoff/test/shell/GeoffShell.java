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
package org.neo4j.geoff.test.shell;

public class GeoffShell {
	
//	public static void main(String... args) throws Exception {
//		ImpermanentGraphDatabase graphDB = new ImpermanentGraphDatabase();
//		Neo4jNamespace namespace = new Neo4jNamespace(graphDB, null);
//		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//		Transaction tx = graphDB.beginTx();
//		try {
//			System.out.print(">>> ");
//			String line = reader.readLine();
//			while (line != null) {
//				try {
//					Rule rule = Rule.from(line);
//					namespace.apply(rule);
//					System.out.println(namespace.getInfo());
//				} catch (RuleFormatException e) {
//					System.out.println(e.getMessage());
//				} catch (RuleApplicationException e) {
//					System.out.println(e.getMessage());
//				}
//				System.out.print(">>> ");
//				line = reader.readLine();
//			}
//			tx.success();
//		} finally {
//			tx.finish();
//		}
//	}
	
}
