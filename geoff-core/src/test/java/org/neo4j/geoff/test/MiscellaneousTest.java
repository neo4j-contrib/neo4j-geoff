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
package org.neo4j.geoff.test;

import org.junit.Test;
import org.neo4j.geoff.Geoff;
import org.neo4j.geoff.Rule;
import org.neo4j.geoff.Subgraph;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.geoff.test.TestDatabase.*;

public class MiscellaneousTest extends TestBase {


    @Test
    public void canCreateGraphFromSingleString() throws Exception {
        Subgraph subgraph = new Subgraph("" +
                "(doc) {\"name\": \"doctor\"}\n" +
                "(dal) {\"name\": \"dalek\"}\n" +
                "(doc)-[:ENEMY_OF]->(dal) {\"since\":\"forever\"}\n" +
                "(doc)<=|People|     {\"name\": \"The Doctor\"}\n" +
                "");
        Geoff.insertIntoNeo4j(subgraph, db, null);
        Transaction tx = db.beginTx();
        try {
            assertTrue(db.index().existsForNodes("People"));
            assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
            assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
            tx.success();
        } finally {
            tx.close();
        }

    }

    @Test
    public void canCreateGraphWithHookToReferenceNode() throws Exception {
        Subgraph subgraph = new Subgraph("" +
                "(doc) {\"name\": \"doctor\"}\n" +
                "(dal) {\"name\": \"dalek\"}\n" +
                "(doc)-[:ENEMY_OF]->(dal) {\"since\":\"forever\"}\n" +
                "(doc)<=|People|     {\"name\": \"The Doctor\"}\n" +
                "(ref)-[:TIMELORD]->(doc)");
        HashMap<String, PropertyContainer> hooks = new HashMap<String, PropertyContainer>(1);
        Transaction tx = db.beginTx();
        try {
            hooks.put("ref", db.getReferenceNode());
            Geoff.insertIntoNeo4j(subgraph, db, hooks);
            assertTrue(db.index().existsForNodes("People"));
            assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
            assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
            assertTrue(db.getReferenceNode().hasRelationship(DynamicRelationshipType.withName("TIMELORD")));
            tx.success();
        } finally {
            tx.close();
        }
    }

    @Test
    public void canLoadRulesCreatedFromValues() throws Exception {
        Subgraph rules = new Subgraph();
        rules.add(Rule.fromValues("(doc)", "name", "doctor", "age", 991));
        rules.add(Rule.fromValues("(doc)<=|People|", "name", "The Doctor"));
        Geoff.insertIntoNeo4j(rules, db, null);
        Transaction tx = db.beginTx();
        try {
            assertTrue(db.index().existsForNodes("People"));
            assertTrue(db.index().forNodes("People").get("name", "The Doctor").hasNext());
            assertEquals("doctor", db.index().forNodes("People").get("name", "The Doctor").getSingle().getProperty("name"));
            tx.success();
        } finally {
            tx.close();
        }
    }

    @Test
    public void canCreateNodesBeforeRelationship() throws Exception {
        TestDatabase db = new TestDatabase();
        Subgraph geoff = new Subgraph();
        geoff.add("(A)                {\"name\": \"Alice Allison\"}");
        geoff.add("(B)                {\"name\": \"Bob Robertson\"}");
        geoff.add("(A)-[R:KNOWS]->(B) {\"since\": 1977}");
        Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
        Transaction tx = db.beginTx();
        try {
            assertNodesExist(out, "(A)", "(B)");
            assertAlice((Node) out.get("(A)"));
            assertBob((Node) out.get("(B)"));
            assertRelationshipsExist(out, "[R]");
            assertTrue(out.get("[R]").hasProperty("since"));
            assertEquals(1977, out.get("[R]").getProperty("since"));
            db.assertCounts(3, 1);
            tx.success();
        } finally {
            tx.close();
        }
    }

    @Test
    public void canCreateRelationshipBeforeNodes() throws Exception {
        TestDatabase db = new TestDatabase();
        Subgraph geoff = new Subgraph();
        geoff.add("(A)-[R:KNOWS]->(B) {\"since\": 1977}");
        geoff.add("(A)                {\"name\": \"Alice Allison\"}");
        geoff.add("(B)                {\"name\": \"Bob Robertson\"}");
        Map<String, PropertyContainer> out = Geoff.insertIntoNeo4j(geoff, db, null);
        Transaction tx = db.beginTx();
        try {
            assertNodesExist(out, "(A)", "(B)");
            assertAlice((Node) out.get("(A)"));
            assertBob((Node) out.get("(B)"));
            assertRelationshipsExist(out, "[R]");
            assertTrue(out.get("[R]").hasProperty("since"));
            assertEquals(1977, out.get("[R]").getProperty("since"));
            db.assertCounts(3, 1);
            tx.success();
        } finally {
            tx.close();
        }
    }

}
