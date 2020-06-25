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

package org.kie.kogito.monitoring.integration;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.IntStream;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.monitoring.system.metrics.dmnhandlers.DecisionConstants;
import org.kie.kogito.monitoring.system.metrics.dmnhandlers.LocalDateTimeHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalDateTimeHandlerTest {

    private static final String ENDPOINT_NAME = "hello";
    private static final String[] INTERNAL_PROMETHEUS_LABELS =
            new String[]{
                    DecisionConstants.DECISION_ENDPOINT_IDENTIFIER_LABELS[0],
                    DecisionConstants.DECISION_ENDPOINT_IDENTIFIER_LABELS[1],
                    "quantile"
            };

    CollectorRegistry registry;
    LocalDateTimeHandler handler;

    @BeforeEach
    public void setUp() {
        registry = new CollectorRegistry();
        handler = new LocalDateTimeHandler("hello", registry);
    }

    @AfterEach
    public void destroy() {
        registry.clear();
    }

    @Test
    public void GivenLocalDateTimeMetrics_WhenMetricsAreStored_ThenTheQuantilesAreCorrect() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Long expectedValue = now.toInstant(ZoneOffset.UTC).toEpochMilli();
        Double[] quantiles = new Double[]{0.1, 0.25, 0.5, 0.75, 0.9, 0.99};

        // Act
        IntStream.rangeClosed(1, 3).forEach(x -> handler.record("decision", ENDPOINT_NAME, now));

        // Assert
        for (Double key : quantiles) {
            assertEquals(expectedValue, getQuantile("decision", ENDPOINT_NAME + DecisionConstants.DECISIONS_NAME_SUFFIX, ENDPOINT_NAME, key), 5);
        }
    }

    private double getQuantile(String decision, String name, String labelValue, double q) {
        return registry.getSampleValue(name, INTERNAL_PROMETHEUS_LABELS, new String[]{decision, labelValue, Collector.doubleToGoString(q)}).doubleValue();
    }
}
