/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.monitoring.core.common.rule;

import java.util.Arrays;

import org.kie.kogito.monitoring.core.common.MonitoringRegistry;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Tag;

public class RuleMetrics {

    private static final long NANOSECONDS_PER_SECONDS = 1_000_000_000;

    private static long toNano(long seconds) {
        return seconds * NANOSECONDS_PER_SECONDS;
    }

    public static DistributionSummary getDroolsEvaluationTimeHistogram(String appId, String rule) {
        DistributionSummary distributionSummary = DistributionSummary.builder("drl_match_fired_nanosecond")
                .minimumExpectedValue((double) toNano(1))
                .maximumExpectedValue((double) toNano(10))
                .description("Drools Firing Time")
                .tags(Arrays.asList(Tag.of("app_id", appId), Tag.of("rule", rule)))
                .register(MonitoringRegistry.getDefaultMeterRegistry());
        return distributionSummary;
    }
}
