package org.kie.kogito.monitoring.core.common.rule;

import org.drools.core.config.DefaultRuleEventListenerConfig;

public class RuleMetricsListenerConfig extends DefaultRuleEventListenerConfig {

    public RuleMetricsListenerConfig() {
        super(new RuleMetricsListener("default-rule-monitoring-listener"));
    }
}
