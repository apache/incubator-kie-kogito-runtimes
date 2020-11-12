package org.kie.kogito.monitoring.core.springboot;

import org.drools.core.config.DefaultRuleEventListenerConfig;
import org.kie.kogito.monitoring.core.api.process.MonitoringProcessEventListenerConfig;
import org.kie.kogito.monitoring.core.api.rule.MonitoringRuleEventListenerConfig;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value="org.kie.kogito.monitoring.rule.deafult.bean.create",
        havingValue = "true",
        matchIfMissing = true)
public class SpringbootRuleEventListenerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringbootRuleEventListenerFactory.class);

    @Bean
    public DefaultRuleEventListenerConfig produceRuleListener() {
        LOGGER.info("Producing default listener for rule monitoring.");
        return new MonitoringRuleEventListenerConfig();
    }

}
