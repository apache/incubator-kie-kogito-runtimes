package org.kie.kogito.incubation.rules;

import org.kie.kogito.incubation.common.LocalId;
import org.kie.kogito.incubation.common.LocalUriId;

public class QueryInstanceId extends LocalUriId implements LocalId {
    public static final String PREFIX = "queries";

    private final RuleUnitInstanceId ruleUnitInstanceId;
    private final String queryId;

    public QueryInstanceId(RuleUnitInstanceId ruleUnitInstanceId, String queryId) {
        super(ruleUnitInstanceId.asLocalUri().append(PREFIX).append(queryId));
        this.ruleUnitInstanceId = ruleUnitInstanceId;
        this.queryId = queryId;
    }

    public RuleUnitInstanceId ruleUnitInstanceId() {
        return ruleUnitInstanceId;
    }

    public String queryId() {
        return queryId;
    }
}
