package org.nigelsmall.geoff;

import java.util.Map;

public class NodeIndexEntry extends Descriptor {

	protected final String indexName;
	protected final String nodeName;
	
	protected NodeIndexEntry(String indexName, String nodeName, Map<String,Object> data) {
		super(data);
		this.indexName = indexName;
		this.nodeName = nodeName;
	}

	public String getIndexName() {
		return this.indexName;
	}

	public String getNodeName() {
		return this.nodeName;
	}
	
}
