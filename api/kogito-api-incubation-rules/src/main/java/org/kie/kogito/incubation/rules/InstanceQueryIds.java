package org.kie.kogito.incubation.rules;

public class InstanceQueryIds {
    private final RuleUnitInstanceId ruleUnitInstanceId;

    public InstanceQueryIds(RuleUnitInstanceId ruleUnitInstanceId) {
        this.ruleUnitInstanceId = ruleUnitInstanceId;
    }

    public RuleUnitInstanceId ruleUnitInstanceId() {
        return ruleUnitInstanceId;
    }

    public QueryInstanceId get(String queryId) {
        return new QueryInstanceId(ruleUnitInstanceId, queryId);
    }
}
