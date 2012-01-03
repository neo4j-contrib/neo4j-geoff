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

import org.neo4j.geoff.util.JSONException;
import org.neo4j.geoff.util.SyntaxError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class GEOFFLoader<NS extends Namespace> {

	private final NS namespace;
	
	NS getNamespace() {
		return this.namespace;
	}
	
	GEOFFLoader(Reader ruleReader, NS namespace)
			throws IOException, GEOFFLoadException {
		this.namespace = namespace;
		load(ruleReader);
	}

	GEOFFLoader(Iterable<?> rules, NS namespace)
			throws IOException, GEOFFLoadException {
		this.namespace = namespace;
		load(rules);
	}

	/**
	 * Load rules from a (potentially) eclectic collection of sources; each item may be a Rule, Iterable, Reader or
	 * CharSequence
	 *
	 * @param rules a collection of rules and nested rules to load
	 * @throws IOException
	 * @throws GEOFFLoadException
	 */
	void load(Iterable<?> rules) throws IOException, GEOFFLoadException {
		StringReader reader;
		try {
			for(Object item : rules) {
				if (item instanceof Rule) {
					this.namespace.apply((Rule) item);
				} else if (item instanceof Iterable) {
					load((Iterable) item);
				} else if (item instanceof Reader) {
					load((Reader) item);
				} else if (item instanceof CharSequence) {
					reader = new StringReader(item.toString());
					try {
						load(reader);
					} finally {
						reader.close();
					}
				} else {
					throw new IllegalArgumentException("Cannot process rules of type " + item.getClass().getName());
				}
			}
		} catch (VampiricException e) {
			// nothing reflected - carry on for now, might log or raise warning at some point in future
		}
	}

	/**
	 * Load rules read from a Reader
	 *
	 * @param ruleReader the Reader to read rules from
	 * @throws IOException
	 * @throws GEOFFLoadException
	 */
	void load(Reader ruleReader) throws IOException, GEOFFLoadException {
		BufferedReader bufferedReader = new BufferedReader(ruleReader);
		try {
			String ruleString = bufferedReader.readLine();
			while (ruleString != null) {
				load(ruleString);
				ruleString = bufferedReader.readLine();
			}
		} finally {
			bufferedReader.close();
		}
	}

	/**
	 * Load rules contained within a String; may contain single or multiple rules, separated by newline characters
	 *
	 * @param ruleString the rule or rules to load
	 * @throws IOException
	 * @throws GEOFFLoadException
	 */
	void load(String ruleString) throws IOException, GEOFFLoadException {
		if (ruleString.contains("\n")) {
			StringReader reader = new StringReader(ruleString);
			try {
				load(reader);
			} finally {
				reader.close();
			}
		} else {
			try {
				if (ruleString.trim().isEmpty() || ruleString.charAt(0) == '#') {
					// ignorable line
				} else if (ruleString.startsWith("[\"")) {
					this.namespace.apply(Rule.listFrom(ruleString));
				} else {
					this.namespace.apply(Rule.from(ruleString));
				}
			} catch (JSONException e) {
				throw new GEOFFLoadException(this.namespace.getRuleNumber(), "JSON parsing error", e);
			} catch (SyntaxError e) {
				throw new GEOFFLoadException(this.namespace.getRuleNumber(), "Syntax error", e);
			} catch (VampiricException e) {
				// nothing reflected - carry on for now, might log or raise warning at some point in future
			}
		}
	}

}
