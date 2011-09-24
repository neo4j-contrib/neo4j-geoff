package org.nigelsmall.geoff;

public class UnknownNodeException extends Exception {

    private final String nodeName;

    public UnknownNodeException(String nodeName) {
        super();
        this.nodeName = nodeName;
    }

    public String getNodeName() {
        return this.nodeName;
    }

}
