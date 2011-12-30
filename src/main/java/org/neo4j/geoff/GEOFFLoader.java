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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public class GEOFFLoader<NS extends Namespace> {

	private final NS namespace;

	public GEOFFLoader(Reader reader, NS namespace)
			throws IOException, GEOFFLoadException {
		BufferedReader bufferedReader = new BufferedReader(reader);
		this.namespace = namespace;
		int lineNumber = 0;
		String line;
		Rule rule;
		try {
			// iterate through every line in the source data
			do {
				line = bufferedReader.readLine();
				lineNumber++;
				try {
					if (line == null || line.trim().isEmpty()) {
						// blank line;
					} else if (line.charAt(0) == '#') {
						// comment
					} else if (line.charAt(0) == '{') {
						// TODO: allow for multi-line JSON
						RuleSet ruleSet = RuleSet.from(line);
						this.namespace.apply(ruleSet);
					} else {
						rule = Rule.from(line);
						// add the described data to the namespace
						this.namespace.apply(rule);
					}
				} catch (DependencyException e) {
					throw new GEOFFLoadException("Failed dependency on line " + lineNumber, e);
				} catch (IllegalRuleException e) {
					throw new GEOFFLoadException("Illegal rule encountered on line " + lineNumber, e);
				} catch (JSONException e) {
					throw new GEOFFLoadException("JSON parsing error on line " + lineNumber, e);
				} catch (SyntaxError e) {
					throw new GEOFFLoadException("Syntax error on line " + lineNumber, e);
				} catch (VampiricException e) {
					// nothing reflected - carry on for now, might log or raise warning at some point in future
				}
			} while (line != null);
		} finally {
			bufferedReader.close();
		}
	}

	public GEOFFLoader(Iterable<List<?>> rules, NS namespace)
			throws IOException, GEOFFLoadException {
		this.namespace = namespace;
		int lineNumber = 0;
		String descriptorString;
		Descriptor descriptor;
		Map<String, Object> data;
		Rule rule;
		// iterate through every line in the source data
		for(List items : rules) {
			if (items.size() == 0) {
				// skip empty items
				continue;
			}
			if (items.get(0) instanceof String) {
				descriptorString = (String) items.get(0);
			} else {
				throw new GEOFFLoadException("Descriptor must be a string");
			}
			lineNumber++;
			try {
				if (descriptorString == null || descriptorString.trim().isEmpty()) {
					// blank line;
				} else if (descriptorString.charAt(0) == '#') {
					// comment
				} else if (descriptorString.charAt(0) == '{') {
					// TODO: allow for multi-line JSON
					RuleSet ruleSet = RuleSet.from(descriptorString);
					this.namespace.apply(ruleSet);
				} else {
					descriptor = new Descriptor(descriptorString);
					if (items.size() > 1 && items.get(1) != null && items.get(1) instanceof Map) {
						try {
							data = (Map<String, Object>) items.get(1);
						} catch(ClassCastException e) {
							throw new GEOFFLoadException("Data must be a map of string:object pairs", e);
						}
					} else {
						data = null;
					}
					rule = new Rule(descriptor, data);
					// add the described data to the namespace
					this.namespace.apply(rule);
				}
			} catch (DependencyException e) {
				throw new GEOFFLoadException("Failed dependency on line " + lineNumber, e);
			} catch (IllegalRuleException e) {
				throw new GEOFFLoadException("Illegal rule encountered on line " + lineNumber, e);
			} catch (JSONException e) {
				throw new GEOFFLoadException("JSON parsing error on line " + lineNumber, e);
			} catch (SyntaxError e) {
				throw new GEOFFLoadException("Syntax error on line " + lineNumber, e);
			} catch (VampiricException e) {
				// nothing reflected - carry on for now, might log or raise warning at some point in future
			}
		}
	}

	/**
	 * Load a graph from a Map of GEOFF descriptors
	 *
	 */
	public GEOFFLoader(Map<String, Map<String, Object>> rules, NS namespace)
			throws GEOFFLoadException {
		this.namespace = namespace;
		try {
			this.namespace.apply(RuleSet.from(rules));
		} catch (DependencyException e) {
			throw new GEOFFLoadException("Failed dependency", e);
		} catch (IllegalRuleException e) {
			throw new GEOFFLoadException("Illegal rule", e);
		} catch (SyntaxError e) {
			throw new GEOFFLoadException("Syntax error", e);
		} catch (VampiricException e) {
			// nothing reflected - carry on for now, might log or raise warning at some point in future
		}
	}

	public NS getNamespace() {
		return this.namespace;
	}

}
