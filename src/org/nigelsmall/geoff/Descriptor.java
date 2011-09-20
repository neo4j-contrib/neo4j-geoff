package org.nigelsmall.geoff;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neo4j.server.rest.domain.JsonHelper;
import org.neo4j.server.rest.domain.JsonParseException;

public abstract class Descriptor {
	
	private static final Pattern IGNORABLE_LINE = Pattern.compile("^(\\s*|(#.*))$");
	private static final Pattern NODE_DESCRIPTOR = Pattern.compile("^\\((\\w*)\\)(\\s+(.*))?$");
	private static final Pattern NODE_INDEX_ENTRY = Pattern.compile("^\\{(\\w+)\\}->\\((\\w+)\\)(\\s+(.*))?$");
	private static final Pattern RELATIONSHIP_DESCRIPTOR = Pattern.compile("^\\((\\w+)\\)-\\[(\\w*):(\\w+)\\]->\\((\\w+)\\)(\\s+(.*))?$");
	private static final Pattern RELATIONSHIP_INDEX_ENTRY = Pattern.compile("^\\{(\\w+)\\}->\\[(\\w+)\\](\\s+(.*))?$");
	
	public static Descriptor from(int lineNumber, String source) throws BadDescriptorException, JsonParseException {
		Matcher m = IGNORABLE_LINE.matcher(source);
		if(m.find()) {
			return null;
		}
		m = NODE_DESCRIPTOR.matcher(source);
		if(m.find()) {
			return new NodeDescriptor(m.group(1), parseJSON(m.group(3)));
		}
		m = NODE_INDEX_ENTRY.matcher(source);
		if(m.find()) {
			return new NodeIndexEntry(m.group(1), m.group(2), parseJSON(m.group(4)));
		}
		m = RELATIONSHIP_DESCRIPTOR.matcher(source);
		if(m.find()) {
			return new RelationshipDescriptor(m.group(1), m.group(2), m.group(3), m.group(4), parseJSON(m.group(6)));
		}
		m = RELATIONSHIP_INDEX_ENTRY.matcher(source);
		if(m.find()) {
			return new RelationshipIndexEntry(m.group(1), m.group(2), parseJSON(m.group(4)));
		}
		throw new BadDescriptorException(lineNumber, source);
	}

	public static Map<String,Object> parseJSON(String source) throws JsonParseException {
		if(source == null || source.length() == 0) {
			return null;
		} else {
			return JsonHelper.jsonToMap(source);
		}
	}

	protected final Map<String,Object> data;

	protected Descriptor(Map<String,Object> data) {
		this.data = data;
	}

	public Map<String,Object> getData() {
		return this.data;
	}
	
}
