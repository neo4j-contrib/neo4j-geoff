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

    /**
     * Return the name of the Index described
     * 
     * @return the Index name
     */
    public String getIndexName() {
        return this.indexName;
    }

    /**
     * Return the name of the Node described
     * 
     * @return the Node name
     */
    public String getNodeName() {
        return this.nodeName;
    }

    @Override
    public String toString() {
        return String.format("{%s}->(%s)",
            this.indexName,
            this.nodeName
        );
    }

}
