/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.quarkus.serverless.workflow.opentelemetry.test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

/**
 * CDI producer for test SpanExporter.
 * This provides an in-memory SpanExporter that Quarkus OpenTelemetry can use
 * when configured with quarkus.otel.traces.exporter=cdi.
 * This producer is only activated when the CDI exporter is explicitly configured.
 */
@ApplicationScoped
public class TestSpanExporterProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestSpanExporterProducer.class);
    private static final List<SpanData> spans = new CopyOnWriteArrayList<>();

    @Produces
    @Singleton
    public SpanExporter spanExporter() {
        return new InMemorySpanExporter();
    }

    /**
     * In-memory span exporter for testing.
     */
    public static class InMemorySpanExporter implements SpanExporter {
        private static final Logger EXPORTER_LOGGER = LoggerFactory.getLogger(InMemorySpanExporter.class);

        @Override
        public CompletableResultCode export(Collection<SpanData> spanDataList) {
            for (SpanData span : spanDataList) {
                String spanName = span.getName();
                if (isTestEndpointSpan(spanName)) {
                    EXPORTER_LOGGER.debug("  Skipping test endpoint span: {}", spanName);
                    continue;
                }
                EXPORTER_LOGGER.debug("  Span: {} - {}", spanName, span.getAttributes());
                spans.add(span);
            }
            return CompletableResultCode.ofSuccess();
        }

        private boolean isTestEndpointSpan(String spanName) {
            return spanName != null && (spanName.contains("/test/spans") || spanName.startsWith("GET /test/spans") || spanName.startsWith("DELETE /test/spans"));
        }

        @Override
        public CompletableResultCode flush() {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode shutdown() {
            return CompletableResultCode.ofSuccess();
        }
    }

    /**
     * Get the collected spans for testing.
     */
    public static List<SpanData> getSpans() {
        return List.copyOf(spans);
    }

    /**
     * Clear collected spans.
     */
    public static void clearSpans() {
        spans.clear();
    }
}
