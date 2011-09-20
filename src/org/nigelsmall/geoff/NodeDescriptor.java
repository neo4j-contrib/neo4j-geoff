package org.nigelsmall.geoff;

import java.util.Map;

public class NodeDescriptor extends Descriptor {

	protected final String nodeName;
	
	protected NodeDescriptor(String nodeName, Map<String,Object> data) {
		super(data);
		this.nodeName = nodeName;
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
		return String.format("(%s)", this.nodeName);
	}

}
