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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class GEOFFLoader<NS extends Namespace> {

	/**
	 * Static method to kick off loading a GEOFF file into the specified
	 * GraphDatabaseService, taking data from the supplied Reader
	 *
	 * @param reader  the reader to grab data from
	 * @param graphDB the database to put stuff into
	 * @return the Namespace used to store all named entities
	 * @throws BadDescriptorException when a badly-formed descriptor is encountered
	 * @throws IOException
	 * @throws DuplicateNameException
	 * @throws UnknownEntityException
	 */
	public static Neo4jNamespace loadIntoNeo4j(Reader reader, GraphDatabaseService graphDB, Map<String, ? extends PropertyContainer> hooks)
			throws BadDescriptorException, IOException, DuplicateNameException, UnknownEntityException {
		Transaction tx = graphDB.beginTx();
		try {
			GEOFFLoader<Neo4jNamespace> loader = new GEOFFLoader<Neo4jNamespace>(reader, new Neo4jNamespace(graphDB, hooks));
			tx.success();
			return loader.getNamespace();
		} finally {
			tx.finish();
		}
	}


	/**
	 * Static method to kick off loading a GEOFF file into the specified
	 * GraphDatabaseService, taking data from the supplied Reader
	 *
	 * @param descriptors the GEOFF descriptors
	 * @param graphDB     the database to put stuff into
	 * @return the Namespace used to store all named entities
	 * @throws BadDescriptorException when a badly-formed descriptor is encountered
	 * @throws IOException
	 * @throws DuplicateNameException
	 * @throws UnknownEntityException
	 */
	public static Neo4jNamespace loadIntoNeo4j(Map descriptors, GraphDatabaseService graphDB, Map<String, ? extends PropertyContainer> hooks)
			throws BadDescriptorException, IOException, DuplicateNameException, UnknownEntityException {
		Transaction tx = graphDB.beginTx();
		try {
			GEOFFLoader<Neo4jNamespace> loader = new GEOFFLoader<Neo4jNamespace>(descriptors, new Neo4jNamespace(graphDB, hooks));
			tx.success();
			return loader.getNamespace();
		} finally {
			tx.finish();
		}
	}

	private final NS namespace;

	private GEOFFLoader(Reader reader, NS namespace)
			throws BadDescriptorException, IOException, DuplicateNameException, UnknownEntityException {
		BufferedReader bufferedReader = new BufferedReader(reader);
		this.namespace = namespace;
		int lineNumber = 0;
		String line;
		Descriptor descriptor;
		try {
			// iterate through every line in the source data
			do {
				line = bufferedReader.readLine();
				lineNumber++;
				if (line != null) {
					// turn the line of text into a Descriptor
					try {
						descriptor = Descriptor.from(line);
					} catch (BadDescriptorException e) {
						// if something goes wrong, attach the line number and re-throw
						e.setLineNumber(lineNumber);
						throw e;
					}
					// add the described data to the namespace
					this.add(descriptor);
				}
			} while (line != null);
		} finally {
			bufferedReader.close();
		}
	}

	/**
	 * Load a graph from a Map of GEOFF descriptors
	 *
	 * @param descriptors the Map of GEOFF descriptors
	 * @param namespace   the Namespace in which to load the descriptors
	 * @throws BadDescriptorException
	 * @throws IOException
	 * @throws DuplicateNameException
	 * @throws UnknownEntityException
	 */
	private GEOFFLoader(Map<String, Map<String, Object>> descriptors, NS namespace)
			throws BadDescriptorException, IOException, DuplicateNameException, UnknownEntityException {
		this.namespace = namespace;
		this.add(new CompositeDescriptor(descriptors));
	}

	/**
	 * Add a descriptor to the namespace associated with this loader
	 *
	 * @param descriptor the descriptor to add
	 * @throws DuplicateNameException
	 * @throws UnknownEntityException
	 */
	private void add(Descriptor descriptor)
			throws DuplicateNameException, UnknownEntityException {
		if (descriptor instanceof CompositeDescriptor) {
			CompositeDescriptor composite = (CompositeDescriptor) descriptor;
			// iterate multiple times to avoid dependency issues
			for (HookDescriptor d : composite.hooks) {
				this.namespace.updateEntity(d);
			}
			for (NodeDescriptor d : composite.nodes) {
				this.namespace.createNode(d);
			}
			for (RelationshipDescriptor d : composite.relationships) {
				this.namespace.createRelationship(d);
			}
			for (IndexEntry d : composite.indexEntries) {
				this.namespace.addIndexEntry(d);
			}
		} else if (descriptor instanceof HookDescriptor) {
			this.namespace.updateEntity((HookDescriptor) descriptor);
		} else if (descriptor instanceof NodeDescriptor) {
			this.namespace.createNode((NodeDescriptor) descriptor);
		} else if (descriptor instanceof RelationshipDescriptor) {
			this.namespace.createRelationship((RelationshipDescriptor) descriptor);
		} else if (descriptor instanceof IndexEntry) {
			this.namespace.addIndexEntry((IndexEntry) descriptor);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public NS getNamespace() {
		return this.namespace;
	}

}
