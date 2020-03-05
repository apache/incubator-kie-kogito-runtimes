package org.kie.addons.monitoring.integration;

import java.util.ArrayList;
import java.util.List;

import io.prometheus.client.CollectorRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.addons.monitoring.mocks.DMNDecisionResultMock;
import org.kie.addons.monitoring.system.metrics.DMNResultMetricsBuilder;
import org.kie.addons.monitoring.system.metrics.MetricsConstants;
import org.kie.kogito.codegen.grafana.SupportedDecisionTypes;
import org.kie.kogito.dmn.rest.DMNResult;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DMNResultMetricsBuilderTest {

    private static final String ENDPOINT_NAME = "hello";

    CollectorRegistry registry;

    @BeforeEach
    public void setUp() {
        registry = CollectorRegistry.defaultRegistry;
    }

    @Test
    public void GivenADMNResult_WhenMetricsAreStored_ThenTheCollectorsAreProperlyWorking(){
        // Arrange
        DMNResult dmnResult = new DMNResult();
        List<DMNDecisionResultMock> decisions = new ArrayList<>();
        decisions.add(new DMNDecisionResultMock("AlphabetDecision", "A"));
        decisions.add(new DMNDecisionResultMock("DictionaryDecision","Hello"));
        decisions.add(new DMNDecisionResultMock("DictionaryDecision","Hello"));
        decisions.add(new DMNDecisionResultMock("DictionaryDecision", "World"));

        dmnResult.setDecisionResults(decisions);

        int expectedAlphabetDecisionA = 1;
        int expectedDictionaryDecisionHello = 2;
        int expectedDictionaryDecisionWorld = 1;

        // Act
        DMNResultMetricsBuilder.generateMetrics(dmnResult);

        // Assert
        assertEquals(expectedAlphabetDecisionA, getLabelsValue(SupportedDecisionTypes.fromInternalToStandard(String.class), "AlphabetDecision", "A"));
        assertEquals(expectedDictionaryDecisionHello, getLabelsValue(SupportedDecisionTypes.fromInternalToStandard(String.class), "DictionaryDecision", "Hello"));
        assertEquals(expectedDictionaryDecisionWorld, getLabelsValue(SupportedDecisionTypes.fromInternalToStandard(String.class), "DictionaryDecision", "World"));

    }

    private Double getLabelsValue(String name, String decisionName, String labelValue) {
        return registry.getSampleValue(name + MetricsConstants.DECISIONS_NAME_SUFFIX, MetricsConstants.HANDLER_IDENTIFIER_LABELS, new String[]{decisionName, labelValue});
    }
}
