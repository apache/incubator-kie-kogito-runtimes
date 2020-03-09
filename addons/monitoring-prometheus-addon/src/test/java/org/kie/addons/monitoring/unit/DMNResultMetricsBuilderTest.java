package org.kie.addons.monitoring.unit;

import org.junit.jupiter.api.Test;
import org.kie.addons.monitoring.system.metrics.DMNResultMetricsBuilder;
import org.kie.kogito.dmn.rest.DMNResult;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class DMNResultMetricsBuilderTest {

    @Test
    public void GivenANewSample_WhenMetricsAreRegistered_ThenNullValuesAreHandled() {
        // Assert
        assertDoesNotThrow(() -> DMNResultMetricsBuilder.generateMetrics(null));
        assertDoesNotThrow(() -> DMNResultMetricsBuilder.generateMetrics(null));
        assertDoesNotThrow(() -> DMNResultMetricsBuilder.generateMetrics(new DMNResult()));
    }
}
