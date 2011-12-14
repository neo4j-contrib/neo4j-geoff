package org.neo4j.geoff;

import java.util.Map;

public class IndexInclusionRule<T extends Indexable> extends IndexRule {

	protected IndexInclusionRule(T entity, IndexRef index, Map<String, Object> data) {
		super(entity, index, data);
	}

}
