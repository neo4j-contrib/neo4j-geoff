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

import org.neo4j.geoff.except.SyntaxError;
import org.neo4j.geoff.tokens.Token;
import org.neo4j.geoff.tokens.TokenReader;

import java.io.IOException;
import java.io.StringReader;

public class Descriptor {

	private final String text;
	private final Token[] tokens;
	private final String pattern;

	public Descriptor(String text) throws SyntaxError {
		this.text = text;
		TokenReader reader = new TokenReader(new StringReader(text));
		try {
			this.tokens = reader.readTokens();
		} catch(IOException e) {
			throw new IllegalArgumentException("Unparsable descriptor text", e);
		}
		StringBuilder str = new StringBuilder(tokens.length);
		for(Token token : tokens) {
			str.append(token.getTokenType().getSymbol());
		}
		this.pattern = str.toString();
	}

	public String getText() {
		return this.text;
	}

	public Token[] getTokens() {
		return this.tokens;
	}

	public Token getToken(int index) {
		return this.tokens[index];
	}

	public String getPattern() {
		return this.pattern;
	}

	@Override
	public String toString() {
		return this.text;
	}

}
