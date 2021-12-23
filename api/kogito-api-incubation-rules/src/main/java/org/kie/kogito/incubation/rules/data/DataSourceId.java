package org.kie.kogito.incubation.rules.data;

import org.kie.kogito.incubation.common.LocalId;
import org.kie.kogito.incubation.common.LocalUri;
import org.kie.kogito.incubation.common.LocalUriId;
import org.kie.kogito.incubation.rules.RuleUnitId;
import org.kie.kogito.incubation.rules.RuleUnitInstanceId;

public class DataSourceId extends LocalUriId implements LocalId {
    public static final String PREFIX = "data-sources";

    private final RuleUnitInstanceId ruleUnitInstanceId;
    private final String dataSourceId;

    public DataSourceId(RuleUnitInstanceId ruleUnitInstanceId, String dataSourceId) {
        super(ruleUnitInstanceId.asLocalUri().append(PREFIX).append(dataSourceId));
        this.ruleUnitInstanceId = ruleUnitInstanceId;
        this.dataSourceId = dataSourceId;
    }

    public RuleUnitInstanceId ruleUnitInstanceId() {
        return this.ruleUnitInstanceId;
    }

    public String dataSourceId() {
        return dataSourceId;
    }

}
