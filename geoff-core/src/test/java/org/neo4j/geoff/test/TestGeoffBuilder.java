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
package org.neo4j.geoff.test;

import org.neo4j.geoff.GeoffBuilder;

class TestGeoffBuilder extends GeoffBuilder {

	TestGeoffBuilder(String... ruleStrings) {
		super(ruleStrings);
	}

	void appendAlice() {
		this.append("(A) {\"name\": \"" + TestName.ALICE + "\"}");
	}

	void appendBob() {
		this.append("(B) {\"name\": \"" + TestName.BOB + "\"}");
	}

	void appendCarol() {
		this.append("(C) {\"name\": \"" + TestName.CAROL + "\"}");
	}

	void appendRelationship(boolean named, boolean typed) {
		String rel = "";
		if (named) rel += "R";
		if (typed) rel += ":KNOWS";
		this.append("(A)-[" + rel + "]->(B) {\"since\": 1977}");
	}

}
