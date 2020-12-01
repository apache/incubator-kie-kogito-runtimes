/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.monitoring.core.springboot;

import org.kie.kogito.monitoring.core.common.Constants;
import org.kie.kogito.monitoring.core.common.process.MonitoringProcessEventListenerConfig;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value = Constants.MONITORING_PROCESS_USE_DEFAULT,
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
