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

import org.neo4j.geoff.except.SyntaxError;
import org.neo4j.geoff.util.JSON;
import org.neo4j.geoff.util.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Descriptor:Data pair.
 */
public class Rule {

    public static Rule fromValues(String descriptor, Object... data) throws SyntaxError {
        HashMap<String, Object> dataMap = new HashMap<String, Object>(data.length / 2);
        for (int i = 0; i < data.length; i += 2) {
            dataMap.put(data[i].toString(), data[i + 1]);
        }
        return new Rule(new Descriptor(descriptor), dataMap);
    }

    /**
     * Read one or more rules from a string, returning all rules read in
     * a list. Each descriptor may or may not be followed by a data map.
     *
     * @param text the string from which to read the rules
     * @return collection of rules read
     * @throws org.neo4j.geoff.except.SyntaxError
     *          if JSON is unparsable
     */
    public static List<Rule> from(String text) throws SyntaxError {
        return (new Subgraph(text)).getRules();
    }

    private final Descriptor descriptor;
    private HashMap<String, Object> data;

    public Rule(Descriptor descriptor) {
        this.descriptor = descriptor;
        this.data = null;
    }

    public Rule(Descriptor descriptor, Map<String, Object> data) {
        this(descriptor);
        putData(data);
    }

    public Descriptor getDescriptor() {
        return this.descriptor;
    }

    public Map<String, Object> getData() {
        return this.data;
    }

    public void putData(Map<String, Object> data) {
        if (data != null) {
            if (this.data == null) {
                this.data = new HashMap<String, Object>();
            }
            this.data.putAll(data);
        }
    }

    @Override
    public String toString() {
        if (this.data == null) {
            return this.descriptor.toString();
        } else {
            return this.descriptor.toString() + " " + this.data.toString();
        }
    }

}
