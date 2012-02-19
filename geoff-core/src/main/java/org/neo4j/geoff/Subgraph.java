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

import org.neo4j.geoff.except.RuleFormatException;
import org.neo4j.geoff.except.SyntaxError;
import org.neo4j.geoff.store.Token;
import org.neo4j.geoff.store.TokenReader;
import org.neo4j.geoff.util.JSON;
import org.neo4j.geoff.util.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

/**
 * A subgraph is an ordered collection of Geoff rules
 */
public class Subgraph implements Iterable<Subgraph.Rule> {

	/**
	 * A Rule is a Descriptor:Data pair
	 *
	 */
	public static class Rule {

		/**
		 * A Descriptor is a pattern consisting of tokens and symbols which is
		 * used to denote a Node, Relationship or Index entry within a Subgraph.
		 * Heavily influenced by Cypher, a Geoff Descriptor uses parentheses to
		 * denote a Node and square brackets for a Relationship while enclosing
		 * Index names within pipe symbols. The following summary illustrates the
		 * main combinations available:
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
		 *
		 */
		public static class Descriptor {

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

		public static Rule fromValues(String descriptor, Object... data) throws SyntaxError {
			HashMap<String, Object> dataMap = new HashMap<String, Object>(data.length / 2);
			for (int i = 0; i < data.length; i += 2) {
				dataMap.put(data[i].toString(), data[i + 1]);
			}
			return new Rule(new Descriptor(descriptor), dataMap);
		}

		public static Rule from(String text) throws RuleFormatException {
			if (Geoff.DEBUG) {
				System.out.println("Parsing rule: " + text);
			}
			String[] bits = text.split("\\s+", 2);
			try {
				if (bits.length == 1) {
					return new Rule(new Descriptor(bits[0]), null);
				} else {
					return new Rule(new Descriptor(bits[0]), JSON.toObject(bits[1]));
				}
			} catch (SyntaxError e) {
				throw new RuleFormatException("Syntax error in rule", e);
			} catch (JSONException e) {
				throw new RuleFormatException("Unparsable JSON in rule", e);
			}
		}

		private final Descriptor descriptor;
		private final Map<String, Object> data;

		public Rule(Descriptor descriptor, Map<String, Object> data) {
			this.descriptor = descriptor;
			this.data = data;
		}

		public Descriptor getDescriptor() {
			return this.descriptor;
		}

		public Map<String, Object> getData() {
			return this.data;
		}

		@Override
		public String toString() {
			return this.descriptor.toString() + " {...}"; //TODO: append data in JSON format
		}

	}

	private final ArrayList<Rule> rules = new ArrayList<Rule>();

	private Subgraph() { }
	
	public Subgraph(Iterable<?> rules) throws IOException, RuleFormatException {
		this.add(rules);
	}

	public Subgraph(Reader ruleReader) throws IOException, RuleFormatException {
		this.add(ruleReader);
	}

	public Subgraph(String... ruleStrings) throws IOException, RuleFormatException {
		for (String ruleString : ruleStrings) {
			this.add(ruleString);
		}
	}

	public void add(Iterable<?> rules) throws IOException, RuleFormatException {
		StringReader reader;
		for(Object item : rules) {
			if (item instanceof Rule) {
				this.rules.add((Rule) item);
			} else if (item instanceof Iterable) {
				this.add((Iterable) item);
			} else if (item instanceof Reader) {
				this.add((Reader) item);
			} else if (item instanceof CharSequence) {
				reader = new StringReader(item.toString());
				try {
					this.add(reader);
				} finally {
					reader.close();
				}
			} else {
				throw new IllegalArgumentException("Cannot process rules of type " + item.getClass().getName());
			}
		}
	}

	public void add(Reader ruleReader) throws IOException, RuleFormatException {
		BufferedReader bufferedReader = new BufferedReader(ruleReader);
		try {
			String ruleString = bufferedReader.readLine();
			while (ruleString != null) {
				this.add(ruleString);
				ruleString = bufferedReader.readLine();
			}
		} finally {
			bufferedReader.close();
		}
	}

	public void add(Rule rule) throws RuleFormatException {
		this.rules.add(rule);
	}
	
	public void add(String... ruleStrings) throws IOException, RuleFormatException {
		for (String ruleString : ruleStrings) {
			if (ruleString.contains("\n")) {
				StringReader reader = new StringReader(ruleString);
				try {
					this.add(reader);
				} finally {
					reader.close();
				}
			} else {
				if (ruleString.trim().isEmpty() || ruleString.charAt(0) == '#') {
					// empty line or comment
				} else if (ruleString.startsWith("[\"")) {
					try {
						List<String> strings = JSON.toListOfStrings(ruleString);
						ArrayList<Rule> rules = new ArrayList<Rule>(strings.size());
						for(String string : strings) {
							rules.add(Rule.from(string));
						}
						this.add(rules);
					} catch (JSONException e) {
						throw new RuleFormatException("Cannot parse JSON list", e);
					}
				} else {
					this.rules.add(Rule.from(ruleString));
				}
			}
		}
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
	
	@Override
	public Iterator<Rule> iterator() {
		return this.rules.iterator();
	}

}
