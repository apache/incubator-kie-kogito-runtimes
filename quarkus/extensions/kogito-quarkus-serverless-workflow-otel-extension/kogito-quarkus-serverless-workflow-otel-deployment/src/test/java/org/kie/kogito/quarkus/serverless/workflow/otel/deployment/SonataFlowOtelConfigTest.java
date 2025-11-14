/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.quarkus.serverless.workflow.otel.deployment;

import org.junit.jupiter.api.Test;
import org.kie.kogito.quarkus.serverless.workflow.otel.config.SonataFlowOtelConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SonataFlowOtelConfigTest {

    @Test
    public void shouldHaveConfigMappingInterface() {
        Class<SonataFlowOtelConfig> configClass = SonataFlowOtelConfig.class;
        assertNotNull(configClass);
        assertTrue(configClass.isInterface());
    }

    @Test
    public void shouldHaveEnabledProperty() throws NoSuchMethodException {
        SonataFlowOtelConfig.class.getMethod("enabled");
    }

    @Test
    public void shouldHaveSpansProperty() throws NoSuchMethodException {
        SonataFlowOtelConfig.class.getMethod("spans");
    }

    @Test
    public void shouldHaveEventsProperty() throws NoSuchMethodException {
        SonataFlowOtelConfig.class.getMethod("events");
    }

    @Test
    public void shouldHaveAttributesProperty() throws NoSuchMethodException {
        SonataFlowOtelConfig.class.getMethod("attributes");
    }
}