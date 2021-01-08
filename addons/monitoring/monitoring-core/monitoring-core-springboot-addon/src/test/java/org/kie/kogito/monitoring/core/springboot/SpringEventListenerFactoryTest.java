/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.monitoring.core.springboot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.monitoring.core.common.process.MonitoringProcessEventListenerConfig;
import org.kie.kogito.monitoring.core.common.rule.RuleMetricsListenerConfig;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SpringEventListenerFactoryTest {

    SpringbootEventListenerFactory factory;

    @BeforeEach
    public void init() {
        factory = new SpringbootEventListenerFactory();
    }

    @Test
    public void produceRuleListener() {
        assertThat(factory.produceRuleListener()).isInstanceOf(RuleMetricsListenerConfig.class);
    }

    @Test
    public void produceProcessListener() {
        assertThat(factory.produceProcessListener()).isInstanceOf(MonitoringProcessEventListenerConfig.class);
    }
}