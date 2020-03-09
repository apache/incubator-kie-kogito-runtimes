package org.kie.addons.monitoring.integration;

import java.util.stream.IntStream;

import io.prometheus.client.CollectorRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.DecisionConstants;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.StringHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringHandlerTest {

    private static final String ENDPOINT_NAME = "hello";

    CollectorRegistry registry;
    StringHandler handler;

    @BeforeEach
    public void setUp() {
        registry = new CollectorRegistry();
        handler = new StringHandler("hello", registry);
    }

    @AfterEach
    public void destroy() {
        registry.clear();
    }

    @Test
    public void GivenSomeStringMetrics_WhenMetricsAreStored_ThenTheCountIsCorrect() {
        // Arrange
        Double expectedCountStringA = 3.0;
        Double expectedCountStringB = 2.0;
        Double expectedCountStringC = 5.0;

        // Act
        IntStream.rangeClosed(1, 3).forEach(x -> handler.record(ENDPOINT_NAME, "A"));
        IntStream.rangeClosed(1, 2).forEach(x -> handler.record(ENDPOINT_NAME, "B"));
        IntStream.rangeClosed(1, 5).forEach(x -> handler.record(ENDPOINT_NAME, "C"));

        // Assert
        assertEquals(expectedCountStringA, getLabelsValue(ENDPOINT_NAME, "A"));
        assertEquals(expectedCountStringB, getLabelsValue(ENDPOINT_NAME, "B"));
        assertEquals(expectedCountStringC, getLabelsValue(ENDPOINT_NAME, "C"));
    }

    private Double getLabelsValue(String name, String labelValue) {
        return registry.getSampleValue(name + DecisionConstants.DECISIONS_NAME_SUFFIX, DecisionConstants.HANDLER_IDENTIFIER_LABELS, new String[]{name, labelValue});
    }
}
