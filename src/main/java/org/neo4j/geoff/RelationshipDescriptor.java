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

import java.util.Map;

public class RelationshipDescriptor extends Descriptor {

	protected final String startNodeName;
	protected final String relationshipName;
	protected final String relationshipType;
	protected final String endNodeName;
	
	protected RelationshipDescriptor(String startNodeName, String relationshipName, String relationshipType, String endNodeName, Map<String,Object> data) {
		super(data);
		this.startNodeName = startNodeName;
		this.relationshipName = relationshipName;
		this.relationshipType = relationshipType;
		this.endNodeName = endNodeName;
	}

	/**
     * Return the name of the Node at the start of the Relationship described
     * 
     * @return the start Node name
     */
    public String getStartNodeName() {
		return this.startNodeName;
	}

	/**
     * Return the name of the Relationship described
     * 
     * @return the Relationship name
     */
    public String getRelationshipName() {
		return this.relationshipName;
	}

    /**
     * Return the type of the Relationship described
     * 
     * @return the Relationship type
     */
    public String getRelationshipType() {
		return this.relationshipType;
	}

	/**
     * Return the name of the Node at the end of the Relationship described
     * 
     * @return the end Node name
     */
    public String getEndNodeName() {
		return this.endNodeName;
	}
	
	@Override
	public String toString() {
		return String.format("(%s)-[%s:%s]->(%s)",
			this.startNodeName,
			this.relationshipName,
			this.relationshipType,
			this.endNodeName
		);
	}

}
