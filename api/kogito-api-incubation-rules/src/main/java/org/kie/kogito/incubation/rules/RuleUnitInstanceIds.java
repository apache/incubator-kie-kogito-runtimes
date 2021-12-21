package org.kie.kogito.incubation.rules;

public class RuleUnitInstanceIds {
    private final RuleUnitId ruleUnitId;

    public RuleUnitInstanceIds(RuleUnitId ruleUnitId) {
        this.ruleUnitId = ruleUnitId;
    }

    public RuleUnitInstanceId get(String ruleUnitInstanceId) {
        return new RuleUnitInstanceId(ruleUnitId, ruleUnitInstanceId);
    }

}
