package org.kie.kogito.monitoring.core.quarkus;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import io.quarkus.arc.properties.IfBuildProperty;
import org.drools.core.config.DefaultRuleEventListenerConfig;
import org.kie.kogito.monitoring.core.common.process.MonitoringProcessEventListenerConfig;
import org.kie.kogito.monitoring.core.common.rule.RuleMetricsListenerConfig;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class QuarkusEventListenerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuarkusEventListenerFactory.class);

    @Produces
    @IfBuildProperty(name = "kogito.addon.monitoring.rule.deafult.bean.create", stringValue = "true", enableIfMissing = true)
    public DefaultRuleEventListenerConfig produceRuleListener() {
        LOGGER.info("Producing default listener for rule monitoring.");
        return new RuleMetricsListenerConfig();
    }

    @Produces
    @IfBuildProperty(name = "kogito.addon.monitoring.process.deafult.bean.create", stringValue = "true", enableIfMissing = true)
    public DefaultProcessEventListenerConfig produceProcessListener() {
        LOGGER.info("Producing default listener for process monitoring.");
        return new MonitoringProcessEventListenerConfig();
    }
}
