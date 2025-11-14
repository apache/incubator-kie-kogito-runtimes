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
package org.kie.kogito.quarkus.serverless.workflow.otel;

import org.junit.jupiter.api.Test;
import org.kie.kogito.quarkus.serverless.workflow.otel.config.SonataFlowOtelConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.*;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.SONATAFLOW_TRANSACTION_ID;
import static org.mockito.Mockito.*;

public class NodeSpanManagerTest {

    private SonataFlowOtelConfig createMockConfig(boolean enabled, boolean spanEnabled) {
        SonataFlowOtelConfig mockConfig = mock(SonataFlowOtelConfig.class);
        SonataFlowOtelConfig.SpanConfig mockSpanConfig = mock(SonataFlowOtelConfig.SpanConfig.class);

        when(mockConfig.enabled()).thenReturn(enabled);
        when(mockConfig.serviceName()).thenReturn("kogito-workflow-service");
        when(mockConfig.serviceVersion()).thenReturn("unknown");
        when(mockConfig.spans()).thenReturn(mockSpanConfig);
        when(mockSpanConfig.enabled()).thenReturn(spanEnabled);

        return mockConfig;
    }

    private io.opentelemetry.api.trace.SpanBuilder setupMockSpanBuilder(
            io.opentelemetry.api.trace.Tracer mockTracer,
            io.opentelemetry.api.trace.Span mockSpan) {

        io.opentelemetry.api.trace.SpanBuilder mockSpanBuilder = org.mockito.Mockito.mock(io.opentelemetry.api.trace.SpanBuilder.class, org.mockito.Mockito.RETURNS_SELF);
        io.opentelemetry.context.Scope mockScope = org.mockito.Mockito.mock(io.opentelemetry.context.Scope.class);

        org.mockito.Mockito.when(mockTracer.spanBuilder(org.mockito.ArgumentMatchers.anyString())).thenReturn(mockSpanBuilder);
        org.mockito.Mockito.when(mockSpanBuilder.startSpan()).thenReturn(mockSpan);
        org.mockito.Mockito.when(mockSpan.makeCurrent()).thenReturn(mockScope);

        return mockSpanBuilder;
    }

    @Test
    public void shouldCreateNodeSpanManager() {
        io.opentelemetry.api.trace.Tracer mockTracer = mock(io.opentelemetry.api.trace.Tracer.class);
        SonataFlowOtelConfig mockConfig = createMockConfig(true, true);
        NodeSpanManager spanManager = new NodeSpanManager(mockTracer, mockConfig);
        assertNotNull(spanManager);
    }

    @Test
    public void shouldReturnNullWhenSpansDisabled() throws Exception {
        io.opentelemetry.api.trace.Tracer mockTracer = mock(io.opentelemetry.api.trace.Tracer.class);
        SonataFlowOtelConfig mockConfig = createMockConfig(true, false); // spans disabled
        NodeSpanManager spanManager = new NodeSpanManager(mockTracer, mockConfig);

        io.opentelemetry.api.trace.Span result = spanManager.createNodeSpan("test", "test", "1.0", "ACTIVE", "node1");

        org.junit.jupiter.api.Assertions.assertNull(result);
    }

    @Test
    public void shouldAddProcessEventToSpan() throws Exception {
        io.opentelemetry.api.trace.Tracer mockTracer = mock(io.opentelemetry.api.trace.Tracer.class);
        io.opentelemetry.api.trace.Span mockSpan = mock(io.opentelemetry.api.trace.Span.class);
        SonataFlowOtelConfig mockConfig = createMockConfig(true, true);

        setupMockSpanBuilder(mockTracer, mockSpan);

        NodeSpanManager spanManager = new NodeSpanManager(mockTracer, mockConfig);

        // Create span and add process event
        io.opentelemetry.api.trace.Span span = spanManager.createNodeSpan("test-instance", "test-process", "1.0", "ACTIVE", "node1");
        spanManager.addProcessEvent(span, "process.started", "Process execution started");

        // Verify span received event
        verify(mockSpan).addEvent("process.started", io.opentelemetry.api.common.Attributes.of(
                EVENT_DESCRIPTION, "Process execution started"));
    }

    @Test
    public void shouldSetSpanStatusOnError() throws Exception {
        io.opentelemetry.api.trace.Tracer mockTracer = mock(io.opentelemetry.api.trace.Tracer.class);
        io.opentelemetry.api.trace.Span mockSpan = mock(io.opentelemetry.api.trace.Span.class);
        SonataFlowOtelConfig mockConfig = createMockConfig(true, true);

        setupMockSpanBuilder(mockTracer, mockSpan);

        NodeSpanManager spanManager = new NodeSpanManager(mockTracer, mockConfig);

        // Create span and set error status
        io.opentelemetry.api.trace.Span span = spanManager.createNodeSpan("test-instance", "test-process", "1.0", "ERROR", "node1");
        spanManager.setSpanError(span, new RuntimeException("Test error"), "Node execution failed");

        // Verify span status set to ERROR
        verify(mockSpan).setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, "Node execution failed");
        verify(mockSpan).recordException(org.mockito.ArgumentMatchers.any(RuntimeException.class));
    }

    @Test
    public void shouldCorrelateSpanWithHeaderContext() throws Exception {
        io.opentelemetry.api.trace.Tracer mockTracer = mock(io.opentelemetry.api.trace.Tracer.class);
        io.opentelemetry.api.trace.Span mockSpan = mock(io.opentelemetry.api.trace.Span.class);
        SonataFlowOtelConfig mockConfig = createMockConfig(true, true);

        setupMockSpanBuilder(mockTracer, mockSpan);

        NodeSpanManager spanManager = new NodeSpanManager(mockTracer, mockConfig);

        // Create span with header context
        java.util.Map<String, String> headerContext = java.util.Map.of(
                "transaction.id", "txn-123",
                "tracker.user", "john.doe");

        io.opentelemetry.api.trace.Span span = spanManager.createNodeSpanWithContext(
                "test-instance", "test-process", "1.0", "ACTIVE", "node1", headerContext);

        // Verify span has correct context attributes based on the actual implementation
        verify(mockSpan).setAttribute(SONATAFLOW_TRANSACTION_ID, "txn-123");
        verify(mockSpan).setAttribute(org.mockito.ArgumentMatchers.eq("sonataflow.tracker.user"), org.mockito.ArgumentMatchers.eq("john.doe"));
    }
}
