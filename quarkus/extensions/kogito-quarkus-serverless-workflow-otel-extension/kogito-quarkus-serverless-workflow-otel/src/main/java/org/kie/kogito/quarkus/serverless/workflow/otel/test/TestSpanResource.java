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
package org.kie.kogito.quarkus.serverless.workflow.otel.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.opentelemetry.sdk.trace.data.SpanData;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Test REST resource to expose collected spans for integration tests.
 * This enables @QuarkusIntegrationTest to access spans from the application JVM.
 */
@ApplicationScoped
@Path("/test/spans")
public class TestSpanResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> getSpans() {
        List<SpanData> spans = TestSpanExporterProducer.getSpans();

        // Convert SpanData to simple maps for JSON serialization
        return spans.stream()
                .map(span -> {
                    Map<String, Object> spanMap = new HashMap<>();
                    spanMap.put("name", span.getName());
                    spanMap.put("traceId", span.getTraceId());
                    spanMap.put("spanId", span.getSpanId());
                    spanMap.put("parentSpanId", span.getParentSpanId());
                    spanMap.put("startEpochNanos", span.getStartEpochNanos());
                    spanMap.put("endEpochNanos", span.getEndEpochNanos());
                    spanMap.put("attributes", span.getAttributes().asMap());

                    List<Map<String, Object>> events = span.getEvents().stream()
                            .map(event -> Map.of(
                                    "name", event.getName(),
                                    "epochNanos", (Object) event.getEpochNanos(),
                                    "attributes", event.getAttributes().asMap()))
                            .collect(Collectors.toList());
                    spanMap.put("events", events);

                    return spanMap;
                })
                .collect(Collectors.toList());
    }

    @GET
    @Path("/count")
    @Produces(MediaType.TEXT_PLAIN)
    public int getSpanCount() {
        return TestSpanExporterProducer.getSpans().size();
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public String clearSpans() {
        TestSpanExporterProducer.clearSpans();

        int retries = 20;
        while (retries-- > 0 && TestSpanExporterProducer.getSpans().size() > 0) {
            try {
                Thread.sleep(100);
                TestSpanExporterProducer.clearSpans();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        int remainingSpans = TestSpanExporterProducer.getSpans().size();
        return "Spans cleared. Remaining: " + remainingSpans;
    }
}
