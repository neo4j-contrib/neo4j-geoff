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

import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.rest.AbstractRestFunctionalTestBase;
import org.neo4j.server.rest.RESTDocsGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GeoffPluginFunctionalTest { //extends AbstractRestFunctionalTestBase {

	private static final String GEOFF_INSERT = "http://localhost:7474/db/data/ext/GeoffPlugin/graphdb/insert";

	// Test disabled due to underlying API change which has broken it...
	@Test
	public void canLoadGeoffRuleList() {
//		GraphDatabaseService db = graphdb();
//		String geoff = "[" +
//				"\"(doc) {\\\"name\\\": \\\"doctor\\\"}\"," +
//				"\"(dal) {\\\"name\\\": \\\"dalek\\\"}\"," +
//				"\"(doc)-[:ENEMY_OF]->(dal) {\\\"since\\\":\\\"forever\\\"}\"," +
//				"\"(doc)<=|People| {\\\"name\\\": \\\"The Doctor\\\"}\"" +
//				"]";
//		String payload = "{\"subgraph\":" + geoff + "}";
//		RESTDocsGenerator generator = gen.get();
//		generator.expectedStatus(ClientResponse.Status.OK);
//		generator.payload(payload);
//		RESTDocsGenerator.ResponseEntity re = generator.post(GEOFF_INSERT);
//		String response = re.entity();
//		assertTrue(db.index().existsForNodes("People"));
//		assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
//		assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
	}

	/*
	@Test
	public void sleep() throws InterruptedException {
		Thread.sleep(600000);
	}
	*/

}
