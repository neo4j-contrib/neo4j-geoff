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

import org.junit.Before;
import org.neo4j.geoff.Geoff;
import org.neo4j.geoff.except.SubgraphError;
import org.neo4j.geoff.except.SyntaxError;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;

public class Neo4jGraphDeleteTest extends TestBase {

    private final TestTransaction[] testTransactions;

    public Neo4jGraphDeleteTest() throws IOException, SyntaxError {
        this.testTransactions = getTestTransactions(50);
    }

    @Before
    public void setUp() throws IOException, SubgraphError {
        db = new TestDatabase();
        for (TestTransaction txn : this.testTransactions) {
            Geoff.insertIntoNeo4j(txn, db, null);
        }
    }

    public void canDeleteAllTestTransactions() throws IOException, SubgraphError {
        for (TestTransaction txn : this.testTransactions) {
            Geoff.deleteFromNeo4j(txn, db, null);
        }
        Transaction tx = db.beginTx();
        try {
            db.assertCounts(1, 0);
        } finally {
            tx.close();
        }
    }
}
