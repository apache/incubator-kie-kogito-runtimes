package org.kie.kogito.incubation.rules;

import org.kie.kogito.incubation.common.LocalId;
import org.kie.kogito.incubation.common.LocalUriId;

public class RuleUnitInstanceId extends LocalUriId implements LocalId {

    public static final String PREFIX = "instances";

    private final RuleUnitId ruleUnitId;
    private final String ruleUnitInstanceId;

    public RuleUnitInstanceId(RuleUnitId processId, String ruleUnitInstanceId) {
        super(processId.asLocalUri().append(PREFIX).append(ruleUnitInstanceId));
        LocalId localDecisionId = processId.toLocalId();
        if (!localDecisionId.asLocalUri().startsWith(RuleUnitId.PREFIX)) {
            throw new IllegalArgumentException("Not a valid process path"); // fixme use typed exception
        }

        this.ruleUnitId = processId;
        this.ruleUnitInstanceId = ruleUnitInstanceId;
    }

    @Override
    public LocalId toLocalId() {
        return this;
    }

    public RuleUnitId ruleUnitId() {
        return ruleUnitId;
    }

    public String processInstanceId() {
        return ruleUnitInstanceId;
    }

}