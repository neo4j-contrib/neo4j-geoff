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
import org.neo4j.geoff.store.Token;
import org.neo4j.geoff.store.TokenReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * A pattern consisting of tokens and symbols, used to denote a Node,
 * Relationship or Index entry within a Subgraph. Heavily influenced
 * by Cypher, a Geoff Descriptor uses parentheses to denote a Node and
 * square brackets for a Relationship while enclosing Index names
 * within pipe symbols. The following summary illustrates the main
 * combinations available:
 *
 * (A)
 * [R]
 * [R:TYPE]
 * (A)-[R]->(B)
 * (A)-[:TYPE]->(B)
 * (A)-[R:TYPE]->(B)
 * (A)<=|Index|
 * [R]<=|Index|
 * [R:TYPE]<=|Index|
 */
public class Descriptor {

    private final StringBuilder text = new StringBuilder();
    private final ArrayList<Token> tokens = new ArrayList<Token>();
    private final StringBuilder pattern = new StringBuilder();

    public Descriptor(String text) throws SyntaxError {
        append(text);
    }

    public void append(String text) throws SyntaxError {
        this.text.append(text);
        TokenReader reader = new TokenReader(new StringReader(text));
        try {
            this.tokens.addAll(reader.readTokens());
        } catch (IOException e) {
            throw new IllegalArgumentException("Unparsable descriptor text", e);
        }
        for (Token token : tokens) {
            this.pattern.append(token.getTokenType().getSymbol());
        }
    }

    public void append(Descriptor descriptor) {
        this.text.append(descriptor.text);
        this.tokens.addAll(descriptor.tokens);
        this.pattern.append(descriptor.pattern);
    }

    public boolean startsWith(char... chars) {
        for (char ch : chars) {
            if (this.text.charAt(0) == ch) {
                return true;
            }
        }
        return false;
    }

    public boolean endsWith(char... chars) {
        int end = this.text.length() - 1;
        for (char ch : chars) {
            if (this.text.charAt(end) == ch) {
                return true;
            }
        }
        return false;
    }

    public Token getToken(int index) {
        return this.tokens.get(index);
    }

    public String getPattern() {
        return this.pattern.toString();
    }

    @Override
    public String toString() {
        return this.text.toString();
    }

}
