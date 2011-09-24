package org.nigelsmall.geoff;

public class BadDescriptorException extends Exception {

	protected final int lineNumber;
	protected final String source;
	
	public BadDescriptorException(int lineNumber, String source) {
		super(String.format("A bad descriptor was found on line %d: %s", lineNumber, source));
		this.lineNumber = lineNumber;
		this.source = source;
	}

    public BadDescriptorException(int lineNumber, String source, Exception cause) {
        super(String.format("A bad descriptor was found on line %d: %s", lineNumber, source), cause);
		this.lineNumber = lineNumber;
		this.source = source;
    }

	public int getLineNumber() {
		return this.lineNumber;
	}

	public String getSource() {
		return this.source;
	}
	
}
