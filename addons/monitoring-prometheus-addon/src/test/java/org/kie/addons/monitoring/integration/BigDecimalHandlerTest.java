package org.kie.addons.monitoring.integration;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;

import ch.obermuhlner.math.big.stream.BigDecimalStream;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.BigDecimalHandler;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.DecisionConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigDecimalHandlerTest {

    private static final String ENDPOINT_NAME = "hello";

    CollectorRegistry registry;
    BigDecimalHandler handler;


    @BeforeEach
    public void setUp() {
        registry = new CollectorRegistry();
        handler = new BigDecimalHandler("hello", registry);
    }

    @AfterEach
    public void destroy(){
        registry.clear();
    }

    @Test
    public void GivenSomeSamples_WhenQuantilesAreCalculated_ThenTheQuantilesAreCorrect(){
        // Arrange
        HashMap<Double, Double> expectedQuantiles = new HashMap<>();
        expectedQuantiles.put(0.1, 999.0);
        expectedQuantiles.put(0.25, 2525.0 );
        expectedQuantiles.put(0.5, 5042.0);
        expectedQuantiles.put(0.75,7551.0);
        expectedQuantiles.put(0.9, 9062.0);
        expectedQuantiles.put(0.99, 10000.0);

        // Act
        BigDecimalStream.range(BigDecimal.valueOf(1), BigDecimal.valueOf(10001), BigDecimal.ONE, MathContext.DECIMAL64).forEach(x -> handler.record(ENDPOINT_NAME, x));

        // Assert
        for (Double key : expectedQuantiles.keySet()) {
            assertEquals(expectedQuantiles.get(key), getQuantile(ENDPOINT_NAME + DecisionConstants.DECISIONS_NAME_SUFFIX, ENDPOINT_NAME, key), 5);
        }
    }

    private double getQuantile(String name, String labelValue, double q) {
        return registry.getSampleValue(name, new String[]{DecisionConstants.HANDLER_LABEL[0], "quantile"}, new String[]{labelValue, Collector.doubleToGoString(q)}).doubleValue();
    }
}
