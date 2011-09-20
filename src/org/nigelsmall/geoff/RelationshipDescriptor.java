package org.nigelsmall.geoff;

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

	public String getStartNodeName() {
		return this.startNodeName;
	}

	public String getRelationshipName() {
		return this.relationshipName;
	}

	public String getRelationshipType() {
		return this.relationshipType;
	}

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
