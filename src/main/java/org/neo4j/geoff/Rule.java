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
import java.util.Map;

/**
 * A Rule is a Descriptor:Data pair
 *
 */
public class Rule {

	public static Rule from(String text) throws SyntaxError {
		String[] bits = text.split("\\s+", 2);
		if (bits.length == 1) {
			return new Rule(new Descriptor(bits[0]), null);
		} else {
			try {
				return new Rule(new Descriptor(bits[0]), JSON.toObject(bits[1]));
			} catch (JSONException e) {
				throw new SyntaxError("Data parsing error", e);
			}
		}
	}

	private final Descriptor descriptor;
	private final Map<String, Object> data;

	public Rule(Descriptor descriptor, Map<String, Object> data) {
		this.descriptor = descriptor;
		if(data == null) {
			this.data = new HashMap<String, Object>();
		} else {
			this.data = data;
		}
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
