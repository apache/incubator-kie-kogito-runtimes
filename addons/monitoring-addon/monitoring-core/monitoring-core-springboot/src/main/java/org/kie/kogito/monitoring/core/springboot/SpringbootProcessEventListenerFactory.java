package org.kie.kogito.monitoring.core.springboot;

import org.drools.core.config.DefaultRuleEventListenerConfig;
import org.kie.kogito.monitoring.core.api.process.MonitoringProcessEventListenerConfig;
import org.kie.kogito.monitoring.core.api.rule.MonitoringRuleEventListenerConfig;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value="org.kie.kogito.monitoring.process.deafult.bean.create",
        havingValue = "true",
        matchIfMissing = true)
public class SpringbootProcessEventListenerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringbootProcessEventListenerFactory.class);

    @Bean
    public DefaultProcessEventListenerConfig produceProcessListener() {
        LOGGER.info("Producing default listener for process monitoring.");
        return new MonitoringProcessEventListenerConfig();
    }
}
