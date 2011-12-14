package org.neo4j.geoff;

import java.util.Map;

public class IndexIncludeRule<T extends Indexable> extends IndexRule {

	protected IndexIncludeRule(T entity, IndexRef index, Map<String, Object> data) {
		super(entity, index, data);
	}

}
