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

package org.kie.addons.monitoring.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.prometheus.client.CollectorRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.addons.monitoring.mocks.DMNDecisionResultMock;
import org.kie.addons.monitoring.system.metrics.DMNResultMetricsBuilder;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.DecisionConstants;
import org.kie.kogito.codegen.grafana.SupportedDecisionTypes;
import org.kie.kogito.dmn.rest.DMNResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // Given that atm the two modules are dependent and there is not a clear way to extend the code generation,
    // this test covers the fact that the two classes are aligned, i.e. if you add/remove a supported type, you
    // have to update the addon as well.
    // TODO: REVIEW THIS LOGIC
    @Test
    public void alighmentWithKogitoCodegenIsOk(){
        List addonSupportedTypes = DMNResultMetricsBuilder.getHandlers().values().stream().map(x -> x.getDmnType()).collect(Collectors.toList());
        assertTrue(addonSupportedTypes.containsAll(SupportedDecisionTypes.getSupportedDMNTypes()));
    }

    private Double getLabelsValue(String name, String decisionName, String labelValue) {
        return registry.getSampleValue(name + DecisionConstants.DECISIONS_NAME_SUFFIX, DecisionConstants.HANDLER_IDENTIFIER_LABELS, new String[]{decisionName, labelValue});
    }
}
