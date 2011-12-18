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
package org.neo4j.geoff.util;

import java.io.IOException;
import java.io.Reader;

public class UeberReader {

	public static class EndOfStreamException extends Exception {

		public EndOfStreamException() {
			super();
		}

	}

	public static class UnexpectedCharacterException extends Exception {

		public UnexpectedCharacterException(char ch) {
			super(Character.toString(ch));
		}

	}

	private final Reader reader;

	public UeberReader(Reader reader) {
		this.reader = reader;
	}

	/**
	 * Returns the next available character without removing it.
	 *
	 * @return the next available character
	 * @throws EndOfStreamException if no more characters can be read
	 * @throws IOException when an unexpected read error occurs
	 */
	public char peek() throws EndOfStreamException, IOException {
		this.reader.mark(1);
		int i = this.reader.read();
		this.reader.reset();
		switch(i) {
		case -1:
			throw new EndOfStreamException();
		default:
			return (char) i;
		}
	}

	/**
	 * Returns the next character in the stream.
	 *
	 * @return the character read
	 * @throws EndOfStreamException if no more characters can be read
	 * @throws IOException when an unexpected read error occurs
	 */
	public char read() throws EndOfStreamException, IOException {
		int i = this.reader.read();
		switch(i) {
		case -1:
			throw new EndOfStreamException();
		default:
			return (char) i;
		}
	}

	/**
	 * Returns the next character in the stream, assuming it matches the character supplied.
	 *
	 * @param expected the expected character
	 * @return the character read
	 * @throws EndOfStreamException if no more characters can be read
	 * @throws IOException when an unexpected read error occurs
	 * @throws UnexpectedCharacterException if the next character read does not match the one expected
	 */
	public char read(char expected) throws EndOfStreamException, IOException, UnexpectedCharacterException {
		int i = this.reader.read();
		switch(i) {
		case -1:
			throw new EndOfStreamException();
		default:
			char c = (char) i;
			if(c == expected) {
				return c;
			} else {
				throw new UnexpectedCharacterException(c);
			}
		}
	}

}
