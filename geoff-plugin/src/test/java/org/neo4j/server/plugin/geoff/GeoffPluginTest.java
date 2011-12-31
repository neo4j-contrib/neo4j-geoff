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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.geoff.util.JSON;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.server.rest.repr.OutputFormat;
import org.neo4j.server.rest.repr.formats.JsonFormat;
import org.neo4j.test.GraphDescription;
import org.neo4j.test.GraphHolder;
import org.neo4j.test.ImpermanentGraphDatabase;
import org.neo4j.test.TestData;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GeoffPluginTest implements GraphHolder
{

    private static ImpermanentGraphDatabase db;
    public @Rule
    TestData<Map<String, Node>> data = TestData.producedThrough( GraphDescription.createGraphFor(
            this, true ) );
    private GeoffPlugin plugin;
    private OutputFormat json;

    @Before
    public void setUp() throws Exception
    {
        db = new ImpermanentGraphDatabase();
        plugin = new GeoffPlugin();
        json = new OutputFormat( new JsonFormat(),
                new URI( "http://localhost/" ), null );
    }

    @Test
    public void runSimpleQuery() throws Exception
    {
        db.cleanContent(false);
        expectNodes( 0 );
        Node i = data.get().get( "I" );
        //Representation result = testQuery( JSON.toObject( "{\"(Joe)\":{\"name\":\"Joe\"}}" ) );
	    List<String> rules = JSON.toArrayOfStrings("[\"(Joe) {\\\"name\\\":\\\"Joe\\\"}\"]");
	    plugin.apply(db, null, rules.toArray(new String[rules.size()]), null);
        expectNodes( 1 );
    }

    private void expectNodes( int i )
    {
        int count = 0;
        Iterator<Node> allNodes = db.getAllNodes().iterator();
        while ( allNodes.hasNext() )
        {
            allNodes.next();
            count++;
        }
        assertEquals(i, count );

    }

    @Override
    public GraphDatabaseService graphdb()
    {
        return db;
    }

    @BeforeClass
    public static void startDatabase()
    {
        db = new ImpermanentGraphDatabase(  );
        db.cleanContent(false);

    }

}
