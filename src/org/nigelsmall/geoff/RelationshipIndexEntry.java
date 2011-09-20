package org.nigelsmall.geoff;

import java.util.Map;

public class RelationshipIndexEntry extends Descriptor {

	protected final String indexName;
	protected final String relationshipName;
	
	protected RelationshipIndexEntry(String indexName, String relationshipName, Map<String,Object> data) {
		super(data);
		this.indexName = indexName;
		this.relationshipName = relationshipName;
	}

	/**
     * Return the name of the Index described
     * 
     * @return the Index name
     */
    public String getIndexName() {
		return this.indexName;
	}

    /**
     * Return the name of the Relationship described
     * 
     * @return the Relationship name
     */
    public String getRelationshipName() {
		return this.relationshipName;
	}

	@Override
	public String toString() {
		return String.format("{%s}->[%s]",
			this.indexName,
		    this.relationshipName
        );
    }

}
