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

import org.neo4j.geoff.except.SyntaxError;
import org.neo4j.geoff.util.JSON;
import org.neo4j.geoff.util.JSONException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

/**
 * An ordered collection of Geoff rules.
 */
public class Subgraph implements Iterable<Rule> {

    private final ArrayList<Rule> rules = new ArrayList<Rule>();

    /**
     * Create an empty subgraph.
     */
    public Subgraph() { }

    /**
     * Create a subgraph from one or more {@link Rule} objects.
     *
     * @param rules initial rules to add
     */
    public Subgraph(Rule... rules) {
        this.add(rules);
    }

    /**
     * Create a subgraph from one or more String formatted rules.
     *
     * @param rules initial rules to add
     * @throws SyntaxError if a rule string is badly formatted
     */
    public Subgraph(String... rules) throws SyntaxError {
        this.add(rules);
    }

    /**
     * Create a subgraph by reading through String formatted rules.
     *
     * @param reader Reader object to read initial rules from
     * @throws IOException if a read failure occurs
     * @throws SyntaxError if a rule string is badly formatted
     */
    public Subgraph(Reader reader) throws IOException, SyntaxError {
        this.add(reader);
    }

    /**
     * Create a subgraph from a variable collection of objects.
     *
     * @param rules objects from which to obtain initial rules
     * @throws IOException if a read failure occurs
     * @throws SyntaxError if a rule string is badly formatted
     */
    public Subgraph(Iterable<?> rules) throws IOException, SyntaxError {
        this.add(rules);
    }

    /**
     * Add one or more {@link Rule} objects.
     *
     * @param rules rules to add
     */
    public void add(Rule... rules) {
        this.rules.addAll(Arrays.asList(rules));
    }

    /**
     * Add one or more String formatted rules.
     *
     * @param rules rules to add
     * @throws SyntaxError if a rule string is badly formatted
     */
    public void add(String... rules) throws SyntaxError {
        for (String ruleString : rules) {
            add(ruleString);
        }
    }

    /**
     * Add rules from a string
     * TODO: tidy this code up a bit :-/
     *
     * @param text the text to parse for rules
     * @throws SyntaxError if the text cannot be parsed
     */
    public void add(String text) throws SyntaxError {
        if (text == null) {
            return;
        }
        text = text.trim();
        while (text.length() > 0) {
            int pos;
            char ch = text.charAt(0);
            switch (ch) {
            case '#':
                pos = text.indexOf('\n', 1);
                if (pos >= 0) {
                    text = text.substring(pos + 1).trim();
                } else {
                    text = "";
                }
                break;
            case '(':
                pos = text.indexOf(')', 1);
                if (pos >= 0) {
                    addDescriptor(new Descriptor(text.substring(0, pos + 1)));
                    text = text.substring(pos + 1).trim();
                } else {
                    throw new SyntaxError("')' not found");
                }
                break;
            case '[':
                pos = text.indexOf(']', 1);
                if (pos >= 0) {
                    addDescriptor(new Descriptor(text.substring(0, pos + 1)));
                    text = text.substring(pos + 1).trim();
                } else {
                    throw new SyntaxError("']' not found");
                }
                break;
            case '|':
                pos = text.indexOf('|', 1);
                if (pos >= 0) {
                    addDescriptor(new Descriptor(text.substring(0, pos + 1)));
                    text = text.substring(pos + 1).trim();
                } else {
                    throw new SyntaxError("'|' not found");
                }
                break;
            case '{':
                Map<String, Object> data = null;
                // look for each '}' in turn, trying to parse
                // JSON up to that point; bit brute force but
                // easier than building a JSON parser...
                pos = 0;
                do {
                    pos = text.indexOf('}', pos + 1);
                    if (pos >= 0) {
                        try {
                            data = JSON.toObject(text.substring(0, pos + 1));
                        } catch(JSONException e) {
                            data = null;
                        }
                    }
                } while (pos >= 0 && data == null);
                if (data == null) {
                    throw new SyntaxError("Unparsable JSON: " + text);
                }
                // now continue parsing the string
                addData(data);
                text = text.substring(pos + 1).trim();
                break;
            case '-':
            case '<':
            case '=':
            case '>':
                pos = 1;
                while (pos < text.length() && "-<=>".indexOf(text.charAt(pos)) >= 0) {
                    pos += 1;
                }
                addDescriptor(new Descriptor(text.substring(0, pos)));
                text = text.substring(pos).trim();
                break;
            default:
                throw new SyntaxError("Unexpected character '" + ch + "' found");
            }
            text = text.trim();
        }
    }

    /**
     * Add to subgraph by reading through String formatted rules.
     *
     * @param reader Reader object to read rules from
     * @throws IOException if a read failure occurs
     * @throws SyntaxError if a rule string is badly formatted
     */
    public void add(Reader reader) throws IOException, SyntaxError {
        StringBuilder content = new StringBuilder(10000);
        char[] buf = new char[1024];
        int n;
        while((n = reader.read(buf)) != -1){
            content.append(buf, 0, n);
        }
        reader.close();
        add(content.toString());
    }

    /**
     * Add to subgraph from a variable collection of objects.
     *
     * @param rules objects from which to obtain rules
     * @throws IOException if a read failure occurs
     * @throws SyntaxError if a rule string is badly formatted
     */
    public void add(Iterable<?> rules) throws IOException, SyntaxError {
        for(Object item : rules) {
            if (item instanceof Rule) {
                this.rules.add((Rule) item);
            } else if (item instanceof Iterable) {
                this.add((Iterable) item);
            } else if (item instanceof Reader) {
                this.add((Reader) item);
            } else if (item instanceof String) {
                this.add((String) item);
            } else {
                throw new IllegalArgumentException("Cannot process rules of type " + item.getClass().getName());
            }
        }
    }

    /**
     * Add descriptor to subgraph - may create new rule containing only
     * this descriptor or may append to last descriptor in list.
     *
     * @param descriptor new Descriptor to add
     */
    public void addDescriptor(Descriptor descriptor) {
        if (this.rules.isEmpty()) {
            this.rules.add(new Rule(descriptor));
        } else {
            Rule lastRule = this.rules.get(this.rules.size() - 1);
            Descriptor lastDescriptor = lastRule.getDescriptor();
            if (descriptor.startsWith('-', '<', '=', '>') || lastDescriptor.endsWith('-', '<', '=', '>')) {
                lastDescriptor.append(descriptor);
            } else {
                this.rules.add(new Rule(descriptor));
            }
        }
    }

    /**
     * Merge data with last rule in list.
     *
     * @param data data map to merge
     * @throws SyntaxError if no rules in Subgraph
     */
    public void addData(Map<String, Object> data) throws SyntaxError {
        if (this.rules.isEmpty()) {
            throw new SyntaxError("No rule to merge data into");
        } else {
            Rule lastRule = this.rules.get(this.rules.size() - 1);
            lastRule.putData(data);
        }
    }

    public boolean isEmpty() {
        return this.rules.isEmpty();
    }
    
    public int size() {
        return this.rules.size();
    }
    
    public Subgraph copy() {
        Subgraph subgraph = new Subgraph();
        subgraph.rules.addAll(this.rules);
        return subgraph;
    }
    
    public Subgraph reverse() {
        Subgraph subgraph = this.copy();
        Collections.reverse(subgraph.rules);
        return subgraph;
    }
    
    public List<Rule> getRules() {
        return this.rules;
    }
    
    @Override
    public Iterator<Rule> iterator() {
        return this.rules.iterator();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Rule rule : this.rules) {
            b.append(rule.toString());
            b.append('\n');
        }
        return b.toString();
    }
    
}
