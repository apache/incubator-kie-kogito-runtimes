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

package org.kie.kogito.monitoring.core.system.metrics;

import java.util.ArrayList;
import java.util.List;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Tag;
import org.kie.kogito.monitoring.core.MonitoringRegistry;

public class SystemMetricsCollector {

    private static final String STATUS_CODE_NAME = "api_http_response_code";

    private static final String STATUS_CODE_HELP = "Request status code.";

    private static final String ELAPSED_TIME_NAME = "api_execution_elapsed_nanosecond";

    private static final String ELAPSED_TIME_HELP = "Endpoint execution elapsed nanoseconds, 3 minutes time window.";

    private static final String EXCEPTIONS_NAME = "api_http_stacktrace_exceptions";

    private static final String EXCEPTIONS_HELP = "System exceptions details.";

    private static Counter getRequestStatusCodeCounter(String endpoint, String identifier){
        List<Tag> tags = new ArrayList<Tag>() {
            {
                add(Tag.of("endpoint", endpoint));
                add(Tag.of("identifier", identifier));
            }
        };

        return Counter.builder(STATUS_CODE_NAME)
                .description(STATUS_CODE_HELP)
                .tags(tags)
                .register(MonitoringRegistry.getCompositeMeterRegistry());
    }

    private static Counter getExceptionsCounter(String endpoint, String identifier){
        List<Tag> tags = new ArrayList<Tag>() {
            {
                add(Tag.of("endpoint", endpoint));
                add(Tag.of("identifier", identifier));
            }
        };

        return Counter.builder(EXCEPTIONS_NAME)
                .description(EXCEPTIONS_HELP)
                .tags(tags)
                .register(MonitoringRegistry.getCompositeMeterRegistry());
    }

    private static DistributionSummary getElapsedTimeSummary(String endpoint){
        List<Tag> tags = new ArrayList<Tag>() {
            {
                add(Tag.of("endpoint", endpoint));
            }
        };

        return DistributionSummary.builder(ELAPSED_TIME_NAME)
                .description(ELAPSED_TIME_HELP)
                .tags(tags)
                .register(MonitoringRegistry.getCompositeMeterRegistry());
    }

    private SystemMetricsCollector() {
    }

    public static void registerStatusCodeRequest(String endpoint, String statusCode) {
        getRequestStatusCodeCounter(endpoint, statusCode).increment();
    }

    public static void registerElapsedTimeSampleMetrics(String endpoint, double elapsedTime) {
        getElapsedTimeSummary(endpoint).record(elapsedTime);
    }

    public static void registerException(String endpoint, String stackTrace) {
        getExceptionsCounter(endpoint, stackTrace).increment();
    }
}