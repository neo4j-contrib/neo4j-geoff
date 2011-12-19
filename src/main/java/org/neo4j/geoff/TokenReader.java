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

import org.neo4j.geoff.util.UeberReader;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class TokenReader extends UeberReader {

	public TokenReader(Reader reader) {
		super(reader);
	}

	public Token[] readTokens() throws IOException, SyntaxError {
		ArrayList<Token> tokens = new ArrayList<Token>(20);
		boolean done = false;
		while(!done) {
			try {
				char ch = peek();
				switch(ch) {
				case '(':
					tokens.add(readNodeToken());
					break;
				case '[':
					tokens.add(readRelToken());
					break;
				case '|':
					tokens.add(readIndexToken());
					break;
				case '-':
					read('-');
					tokens.add(new Token(Token.Type.DASH));
					break;
				case '>':
					read('>');
					tokens.add(new Token(Token.Type.ARROW));
					break;
				case '<':
					read('<');
					read('=');
					tokens.add(new Token(Token.Type.INCLUDED_IN));
					break;
				case '!':
					read('!');
					if(peek() == '=') {
						read('=');
						tokens.add(new Token(Token.Type.EXCLUDED_FROM));
					} else {
						tokens.add(new Token(Token.Type.BANG));
					}
					break;
				case ':':
					read(':');
					read('=');
					tokens.add(new Token(Token.Type.REFLECTS));
					break;
				default:
					throw new SyntaxError();
				}
			} catch(EndOfStreamException e) {
				done = true;
			} catch (UnexpectedCharacterException e) {
				throw new SyntaxError("Unexpected character encountered in token", e);
			}
		}
		return tokens.toArray(new Token[tokens.size()]);
	}

	public NodeToken readNodeToken() throws IOException, EndOfStreamException, UnexpectedCharacterException {
		read('(');
		char ch = peek();
		NodeToken token;
		if(ch == '*') {
			read('*');
			token = new NodeToken("*");
		} else {
			String name = readName();
			token = new NodeToken(name);
		}
		read(')');
		return token;
	}

	public RelToken readRelToken() throws IOException, EndOfStreamException, UnexpectedCharacterException {
		read('[');
		String name = readName();
		String type = "";
		if(peek() == ':') {
			read(':');
			type = readName();
		}
		read(']');
		return new RelToken(name, type);
	}

	public Token readIndexToken() throws IOException, EndOfStreamException, UnexpectedCharacterException {
		read('|');
		String name = readName();
		read('|');
		return new IndexToken(name);
	}

	public String readName() throws IOException, EndOfStreamException {
		StringBuilder str = new StringBuilder(80);
		while(NameableToken.isValidNameChar(peek())) {
			str.append(read());
		}
		return str.toString();
	}

}
