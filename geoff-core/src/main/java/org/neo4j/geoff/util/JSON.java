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

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JSON {

	/**
	 * Parse the supplied text as a JSON array of arrays; might validly be empty so
	 * fail gracefully in that case
	 *
	 * @param json the JSON json to parse
	 * @return a list of lists
	 * @throws JSONException when all hope is gone...
	 */
	public static List<String> toListOfStrings(String json)
			throws JSONException {
		if (json == null || json.isEmpty()) {
			return null;
		} else {
			ObjectMapper mapper = new ObjectMapper();
			try {
				return (List<String>) mapper.readValue(json, Object.class);
			} catch (ClassCastException e) {
				throw new JSONException("Unable to cast JSON to array", e);
			} catch (IOException e) {
				throw new JSONException("Unable to read JSON", e);
			}
		}
	}

	/**
	 * Parse the supplied text as a JSON object; might validly be empty so
	 * fail gracefully in that case
	 *
	 * @param json the JSON json to parse
	 * @return a String:Object collection
	 * @throws JSONException when all hope is gone...
	 */
	public static Map<String, Object> toObject(String json)
			throws JSONException {
		if (json == null || json.isEmpty()) {
			return null;
		} else {
			ObjectMapper mapper = new ObjectMapper();
			try {
				return (Map<String, Object>) mapper.readValue(json, Object.class);
			} catch (ClassCastException e) {
				throw new JSONException("Unable to cast JSON to Map<String,Object>", e);
			} catch (IOException e) {
				throw new JSONException("Unable to read JSON", e);
			}
		}
	}

	/**
	 * Parse the supplied text as a JSON object containing other sub-objects;
	 * might validly be empty so fail gracefully in that case
	 *
	 * @param json
	 * @return
	 * @throws JSONException
	 */
	public static Map<String, Map<String, Object>> toObjectOfObjects(String json)
			throws JSONException {
		if (json == null || json.isEmpty()) {
			return null;
		} else {
			ObjectMapper mapper = new ObjectMapper();
			try {
				return (Map<String, Map<String, Object>>) mapper.readValue(json, Object.class);
			} catch (ClassCastException e) {
				throw new JSONException("Unable to cast JSON to Map<String,Map<String, Object>>", e);
			} catch (IOException e) {
				throw new JSONException("Unable to read JSON", e);
			}
		}
	}

}
