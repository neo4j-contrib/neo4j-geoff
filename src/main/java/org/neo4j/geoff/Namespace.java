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

/**
 * Provides context for items to be added to a database and retained by
 * name so that they may be referred to from within the same context.
 * 
 * @author Nigel Small
 *
 */
public interface Namespace {

    /**
     * Update the properties on a pre-existing Node or Relationship
     *
     * @param descriptor a pointer to the entity to update 
     * @throws UnknownEntityException when the referenced entity cannot be found
     */
    public void updateEntity(HookDescriptor descriptor)
    throws UnknownEntityException;

    /**
	 * Add a Node to the database and keep a reference to it, indexed by name
	 *
	 * @param descriptor details of the Node to be created
     * @throws DuplicateNameException when the supplied Node name already exists
	 */
    public void createNode(NodeDescriptor descriptor)
    throws DuplicateNameException;

    /**
     * Add a Relationship to the database and keep a reference to it, indexed
	 * by name, if it has a name
	 *
	 * @param descriptor details of the Relationship to be created
     * @throws DuplicateNameException when the supplied Relationship name already exists
     * @throws UnknownEntityException when an end point Node cannot be identified
     */
    public void createRelationship(RelationshipDescriptor<Connectable,Connectable> descriptor)
    throws DuplicateNameException, UnknownEntityException;

    /**
     * Add an entry to an Index
     *
     * @param indexEntry details of the Index entry to be added
     * @throws UnknownEntityException when no Node exists with the name specified
     */
    public void addIndexEntry(IndexEntry<Indexable> indexEntry)
    throws UnknownEntityException;

}
