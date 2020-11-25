package org.kie.kogito.monitoring.core.springboot;

import org.drools.core.config.DefaultRuleEventListenerConfig;
import org.kie.kogito.monitoring.core.common.rule.RuleMetricsListenerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value = "org.kie.kogito.monitoring.rule.deafult.bean.create",
        havingValue = "true",
        matchIfMissing = true)
public class SpringbootRuleEventListenerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringbootRuleEventListenerFactory.class);

    @Bean
    public DefaultRuleEventListenerConfig produceRuleListener() {
        LOGGER.info("Producing default listener for rule monitoring.");
        return new RuleMetricsListenerConfig();
    }
}
