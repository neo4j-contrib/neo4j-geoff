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

import org.neo4j.geoff.util.JSON;
import org.neo4j.geoff.util.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Rule is a Descriptor:Data pair
 *
 */
public class Rule {

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
			throw new RuleFormatException(0, "Syntax error in rule", e);
		} catch (JSONException e) {
			throw new RuleFormatException(0, "Unparsable JSON in rule", e);
		}
	}

	public static List<Rule> listFrom(String text) throws RuleFormatException {
		try {
			List<String> strings = JSON.toListOfStrings(text);
			ArrayList<Rule> rules = new ArrayList<Rule>(strings.size());
			for(String string : strings) {
				rules.add(Rule.from(string));
			}
			return rules;
		} catch (JSONException e) {
			throw new RuleFormatException(0, "Cannot parse JSON list", e);
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
