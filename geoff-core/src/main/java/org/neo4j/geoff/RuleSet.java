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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RuleSet {

	public static RuleSet from(String text) throws JSONException, SyntaxError {
		return RuleSet.from(JSON.toObjectOfObjects(text));
	}

	public static RuleSet from(Map<String, Map<String, Object>> map) throws SyntaxError {
		HashMap<String, Rule> rules = new HashMap<String, Rule>(map.size());
		for(Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
			rules.put(entry.getKey(), new Rule(new Descriptor(entry.getKey()), entry.getValue()));
		}
		return new RuleSet(new HashSet<Rule>(rules.values()));
	}

	private final Set<Rule> rules;

	private RuleSet(Set<Rule> rules) {
		this.rules = rules;
	}

	public Set<Rule> getRules() {
		return this.rules;
	}
	
	public int length() {
		return this.rules.size();
	}

}
