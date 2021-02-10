/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.monitoring.core.common.integration;

import java.time.LocalTime;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.monitoring.core.common.system.metrics.dmnhandlers.DecisionConstants;
import org.kie.kogito.monitoring.core.common.system.metrics.dmnhandlers.LocalTimeHandler;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocalTimeHandlerTest extends AbstractQuantilesTest<LocalTimeHandler> {

    @BeforeEach
    public void setUp() {
        registry = new SimpleMeterRegistry();
        handler = new LocalTimeHandler("hello", registry);
    }

    @AfterEach
    public void destroy() {
        registry.clear();
    }

    @Test
    public void givenLocalTimeMetricsWhenMetricsAreStoredThenTheQuantilesAreCorrect() {
        // Arrange
        LocalTime now = LocalTime.now();
        Double[] quantiles = new Double[]{0.1, 0.25, 0.5, 0.75, 0.9, 0.99};

        // Act
        handler.record("decision", ENDPOINT_NAME, now);

        // Assert
        assertTrue(registry.find(ENDPOINT_NAME + DecisionConstants.DECISIONS_NAME_SUFFIX).summary().max() >= 5);
        assertTrue(registry.find(ENDPOINT_NAME + DecisionConstants.DECISIONS_NAME_SUFFIX).summary().mean() >= 2);
    }
}