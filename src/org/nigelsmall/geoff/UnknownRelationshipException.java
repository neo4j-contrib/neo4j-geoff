package org.nigelsmall.geoff;

public class UnknownRelationshipException extends Exception {

    private final String relationshipName;

    public UnknownRelationshipException(String relationshipName) {
        super();
        this.relationshipName = relationshipName;
    }

    public String getRelationshipName() {
        return this.relationshipName;
    }

}
