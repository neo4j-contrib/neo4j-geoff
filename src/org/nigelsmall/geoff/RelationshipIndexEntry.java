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

	public String getIndexName() {
		return this.indexName;
	}

	public String getRelationshipName() {
		return this.relationshipName;
	}
	
}
