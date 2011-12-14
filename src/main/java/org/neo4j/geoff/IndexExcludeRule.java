package org.neo4j.geoff;

import java.util.Map;

public class IndexExcludeRule<T extends Indexable> extends IndexRule {

	protected IndexExcludeRule(T entity, IndexRef index, Map<String, Object> data) {
		super(entity, index, data);
	}

}
