/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.monitoring.prometheus.springboot.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SpringbootMetricsResourceTest {

    @Test
    public void getMetrics() {
        SpringbootMetricsResource resource = new MockSpringbootMetricsResource("metric");

        ResponseEntity<?> metrics = resource.getMetrics();
        assertThat(metrics.getStatusCodeValue()).isEqualTo(200);
        assertThat(metrics.getBody()).isEqualTo("metric");
    }

    static class MockSpringbootMetricsResource extends SpringbootMetricsResource {

        final String expectedValue;

        MockSpringbootMetricsResource(String expectedValue) {
            this.expectedValue = expectedValue;
        }

        @Override
        public String scrape() {
            return expectedValue;
        }
    }

}