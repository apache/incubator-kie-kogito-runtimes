package org.kie.addons.monitoring.integration;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.stream.IntStream;

import ch.obermuhlner.math.big.stream.BigDecimalStream;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.addons.monitoring.system.metrics.MetricsConstants;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.BigDecimalHandler;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.BooleanHandler;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.StringHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BooleanHandlerTest {

    private static final String ENDPOINT_NAME = "hello";

    CollectorRegistry registry;
    BooleanHandler handler;

    @BeforeEach
    public void setUp() {
        registry = new CollectorRegistry();
        handler = new BooleanHandler("hello", registry);
    }

    @AfterEach
    public void destroy(){
        registry.clear();
    }

    @Test
    public void GivenSomeBooleanMetrics_WhenMetricsAreStored_ThenTheCountIsCorrect(){
        // Arrange
        Double expectedTrue = 3.0;
        Double expectedFalse = 2.0;

        // Act
        IntStream.rangeClosed(1, 3).forEach(x -> handler.record(ENDPOINT_NAME, true));
        IntStream.rangeClosed(1, 2).forEach(x -> handler.record(ENDPOINT_NAME, false));

        // Assert
        assertEquals(expectedTrue, getLabelsValue(ENDPOINT_NAME, "true"));
        assertEquals(expectedFalse, getLabelsValue(ENDPOINT_NAME, "false"));
    }

    private Double getLabelsValue(String name, String labelValue) {
        return registry.getSampleValue(name + MetricsConstants.DECISIONS_NAME_SUFFIX, MetricsConstants.HANDLER_IDENTIFIER_LABELS, new String[]{name, labelValue});
    }

}
