package org.kie.kogito.monitoring.core.quarkus;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

import org.drools.core.config.DefaultRuleEventListenerConfig;
import org.kie.kogito.monitoring.core.api.rule.RuleMetricsDroolsListener;

@Default
@ApplicationScoped
public class QuarkusMonitoringDefaultRuleEventListenerConfig extends DefaultRuleEventListenerConfig {

    public QuarkusMonitoringDefaultRuleEventListenerConfig() {
        super(new RuleMetricsDroolsListener("deafault-rule-monitoring-listener"));
    }
}
