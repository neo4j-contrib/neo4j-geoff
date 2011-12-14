package org.neo4j.geoff;

import java.util.Map;

public class IndexExclusionRule<T extends Indexable> extends IndexRule {

	protected IndexExclusionRule(T entity, IndexRef index, Map<String, Object> data) {
		super(entity, index, data);
	}

}
