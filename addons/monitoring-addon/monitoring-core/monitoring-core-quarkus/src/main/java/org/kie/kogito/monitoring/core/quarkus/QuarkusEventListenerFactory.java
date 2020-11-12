package org.kie.kogito.monitoring.core.quarkus;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import io.quarkus.arc.properties.IfBuildProperty;
import org.drools.core.config.DefaultRuleEventListenerConfig;
import org.kie.kogito.monitoring.core.api.process.MonitoringProcessEventListenerConfig;
import org.kie.kogito.monitoring.core.api.rule.MonitoringRuleEventListenerConfig;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class QuarkusEventListenerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuarkusEventListenerFactory.class);

    @Produces
    @IfBuildProperty(name = "org.kie.kogito.monitoring.rule.deafult.bean.create", stringValue = "true", enableIfMissing = true)
    public DefaultRuleEventListenerConfig produceRuleListener() {
        LOGGER.info("Producing default listener for rule monitoring.");
        return new MonitoringRuleEventListenerConfig();
    }

    @Produces
    @IfBuildProperty(name = "org.kie.kogito.monitoring.process.deafult.bean.create", stringValue = "true", enableIfMissing = true)
    public DefaultProcessEventListenerConfig produceProcessListener() {
        LOGGER.info("Producing default listener for process monitoring.");
        return new MonitoringProcessEventListenerConfig();
    }
}
