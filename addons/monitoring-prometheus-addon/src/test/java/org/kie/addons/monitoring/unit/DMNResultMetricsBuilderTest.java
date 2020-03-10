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
