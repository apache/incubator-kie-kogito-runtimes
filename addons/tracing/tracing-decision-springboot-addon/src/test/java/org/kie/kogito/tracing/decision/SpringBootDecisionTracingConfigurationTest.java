/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kie.kogito.tracing.decision;

import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.conf.ConfigBean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SpringBootDecisionTracingConfigurationTest {

    @Test
    void testCollector() {
        SpringBootTraceEventEmitter mockedEmitter = mock(SpringBootTraceEventEmitter.class);
        ConfigBean mockedConfigBean = mock(ConfigBean.class);
        Application mockedApplication = mock(Application.class);

        SpringBootDecisionTracingConfiguration config = new SpringBootDecisionTracingConfiguration("localhost:9092", "kogito-tracing-decision", 1, (short) 1);

        SpringBootDecisionTracingCollector asyncCollector = config.collector(mockedEmitter, mockedConfigBean, mockedApplication, true);
        assertTrue(asyncCollector instanceof SpringBootDecisionTracingCollectorAsync);

        SpringBootDecisionTracingCollector syncCollector = config.collector(mockedEmitter, mockedConfigBean, mockedApplication, false);
        assertFalse(syncCollector instanceof SpringBootDecisionTracingCollectorAsync);
    }
}
