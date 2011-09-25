package org.nigelsmall.geoff;

import org.nigelsmall.util.JSON;
import org.nigelsmall.util.JSONException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Descriptor {

	// the regular expressions that make all the parsing magic happen...
	private static final Pattern IGNORABLE_LINE = Pattern.compile("^(\\s*|(#.*))$");
	private static final Pattern NODE_DESCRIPTOR = Pattern.compile("^\\((\\w*)\\)(\\s+(.*))?$");
	private static final Pattern NODE_INDEX_ENTRY = Pattern.compile("^\\{(\\w+)\\}->\\((\\w+)\\)(\\s+(.*))?$");
	private static final Pattern RELATIONSHIP_DESCRIPTOR = Pattern.compile("^\\((\\w+)\\)-\\[(\\w*):(\\w+)\\]->\\((\\w+)\\)(\\s+(.*))?$");
	private static final Pattern RELATIONSHIP_INDEX_ENTRY = Pattern.compile("^\\{(\\w+)\\}->\\[(\\w+)\\](\\s+(.*))?$");

	/**
	 * Factory method to produce a Descriptor object from a given line of
	 * GEOFF source
	 * 
	 * @param lineNumber the line number from the original source
	 * @param source the line of source to be parsed
	 * @return a Descriptor of an appropriate type
	 * @throws BadDescriptorException if this line doesn't match any known pattern
	 */
	public static Descriptor from(int lineNumber, String source)
	throws BadDescriptorException
	{
        try {
            Matcher m = IGNORABLE_LINE.matcher(source);
            if(m.find()) {
                return null;
            }
            m = NODE_DESCRIPTOR.matcher(source);
            if(m.find()) {
                return new NodeDescriptor(m.group(1), JSON.toObject(m.group(3)));
            }
            m = NODE_INDEX_ENTRY.matcher(source);
            if(m.find()) {
                return new NodeIndexEntry(m.group(1), m.group(2), JSON.toObject(m.group(4)));
            }
            m = RELATIONSHIP_DESCRIPTOR.matcher(source);
            if(m.find()) {
                return new RelationshipDescriptor(m.group(1), m.group(2), m.group(3), m.group(4), JSON.toObject(m.group(6)));
            }
            m = RELATIONSHIP_INDEX_ENTRY.matcher(source);
            if(m.find()) {
                return new RelationshipIndexEntry(m.group(1), m.group(2), JSON.toObject(m.group(4)));
            }
            throw new BadDescriptorException(lineNumber, source);
        } catch(JSONException e) {
            throw new BadDescriptorException(lineNumber, source, e);
        }
    }

    protected final Map<String,Object> data;

    protected Descriptor(Map<String,Object> data) {
        this.data = data;
    }

    /**
     * Return the key:value pairs attached to this Descriptor
     * 
     * @return Map of key:value pairs
     */
    public Map<String,Object> getData() {
        return this.data;
    }
    
}
