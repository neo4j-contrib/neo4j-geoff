/**
 * Copyright (c) 2002-2013 "Neo Technology,"
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

import org.neo4j.geoff.Subgraph;
import org.neo4j.geoff.except.SyntaxError;

import java.io.IOException;

public class TestTransaction extends Subgraph {

	private static final String[] FAMILY_NAMES = new String[] {
		"Smith", "Jones", "Taylor", "Williams", "Brown", "Davies", "Evans",
		"Wilson", "Thomas", "Roberts", "Johnson", "Lewis", "Walker", "Robinson",
		"Wood", "Thompson", "White", "Watson", "Jackson", "Wright"
	};

	private static final String[] GIVEN_NAMES = new String[] {
		"Oliver", "Jack", "Harry", "Charlie", "Thomas", "Alfie", "William",
		"James", "Joshua", "George", "Olivia", "Sophie", "Lily", "Emily", "Ruby",
		"Jessica", "Amelia", "Chloe", "Grace", "Mia"
	};

	private static final String[] PRODUCTS = new String[] {
		"bag of apples", "bunch of bananas", "pineapple",
		"papaya drink", "strawberries & cream", "blueberry pie",
		"raspberry ripple ice cream", "plum crumble",
		"tin of peaches", "pair of pears"
	
	};
	
	private String getRandomName() {
		return GIVEN_NAMES[(int) Math.floor(GIVEN_NAMES.length * Math.random())] + " " +
		       FAMILY_NAMES[(int) Math.floor(FAMILY_NAMES.length * Math.random())];
	}

	private int getRandomProductID() {
		return (int) Math.floor(PRODUCTS.length * Math.random());
	}

	public TestTransaction(int transactionID) throws IOException, SyntaxError {
		this.add("(customer) {\"name\": \"" + getRandomName() + "\"}");
		int productID = getRandomProductID();
		String productDescription = PRODUCTS[productID];
		this.add("(product)  {\"product_id\": " + Integer.toString(101 + productID) + ", \"description\": \"" + productDescription + "\"}");
		this.add("(customer)-[txn:BOUGHT]->(product) {\"price\": " + ((50 + Math.floor(100 * Math.random())) / 100) + " }");
		this.add("[txn]<=|Transactions| {\"txn_id\": " + transactionID + "}");
	}

}
