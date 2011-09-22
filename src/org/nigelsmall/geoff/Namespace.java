package org.nigelsmall.geoff;

/**
 * Provides context for items to be added to a database and retained by name
 * so that they may be referred to from within the same context 
 * 
 * @author Nigel Small
 *
 */
public interface Namespace<N,R> {

	/**
	 * Return the first Node added to this Namespace
	 * 
	 * @return the Node object
	 */
	public N getFirstNode();

	/**
	 * Look up a previously created Node by name
	 * 
	 * @param name the name of the Node to find
	 * @return the Node object
	 * @throws UnknownNodeException if no Node exists with that name
	 */
	public N getNode(String name) throws UnknownNodeException;

	/**
	 * Add a Node to the database and keep a reference to it, indexed by name
	 * 
	 * @param descriptor details of the Node to be created
	 */
	public void createNode(NodeDescriptor descriptor);

	/**
	 * Add an entry to an Index of Nodes
	 * 
	 * @param entry details of the Index entry to be added
	 * @throws UnknownNodeException if no Node exists with the name specified
	 */
	public void addNodeIndexEntry(NodeIndexEntry entry) throws UnknownNodeException;
	
	/**
	 * Look up a previously created Relationship by name
	 * 
	 * @param name the name of the Relationship to find
	 * @return the Relationship object
	 * @throws UnknownRelationshipException if no Relationship exists with that name
	 */
	public R getRelationship(String name) throws UnknownRelationshipException;

	/**
	 * Add a Relationship to the database and keep a reference to it, indexed
	 * by name, if it has a name
	 * 
	 * @param descriptor details of the Relationship to be created
	 */
	public void createRelationship(RelationshipDescriptor descriptor) throws UnknownNodeException;

	/**
	 * Add an entry to an Index of Relationships
	 * 
	 * @param entry details of the Index entry to be added
	 * @throws UnknownRelationshipException if no Relationship exists with the name specified
	 */
	public void addRelationshipIndexEntry(RelationshipIndexEntry entry) throws UnknownRelationshipException;

}
