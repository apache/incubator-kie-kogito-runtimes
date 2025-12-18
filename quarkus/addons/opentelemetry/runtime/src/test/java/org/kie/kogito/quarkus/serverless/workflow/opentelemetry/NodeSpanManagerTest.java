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
package org.kie.kogito.quarkus.serverless.workflow.opentelemetry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.quarkus.serverless.workflow.opentelemetry.config.SonataFlowOtelConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.*;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.SONATAFLOW_TRANSACTION_ID;
import static org.mockito.Mockito.*;

public class NodeSpanManagerTest {

    @AfterEach
    public void cleanup() {
        OtelContextHolder.clearRootContext("test-instance");
    }

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
        SonataFlowOtelConfig mockConfig = createMockConfig(true, false);
        NodeSpanManager spanManager = new NodeSpanManager(mockTracer, mockConfig);

        io.opentelemetry.api.trace.Span result = spanManager.createStateSpan("test", "test", "1.0", "ACTIVE", "TestState");

        org.junit.jupiter.api.Assertions.assertNull(result);
    }

    @Test
    public void shouldAddProcessEventToSpan() throws Exception {
        io.opentelemetry.api.trace.Tracer mockTracer = mock(io.opentelemetry.api.trace.Tracer.class);
        io.opentelemetry.api.trace.Span mockSpan = mock(io.opentelemetry.api.trace.Span.class);
        SonataFlowOtelConfig mockConfig = createMockConfig(true, true);

        setupMockSpanBuilder(mockTracer, mockSpan);

        NodeSpanManager spanManager = new NodeSpanManager(mockTracer, mockConfig);

        io.opentelemetry.api.trace.Span span = spanManager.createStateSpan("test-instance", "test-process", "1.0", "ACTIVE", "TestState");
        spanManager.addProcessEvent(span, "process.started", "Process execution started");

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

        io.opentelemetry.api.trace.Span span = spanManager.createStateSpan("test-instance", "test-process", "1.0", "ERROR", "TestState");
        spanManager.setSpanError(span, new RuntimeException("Test error"), "Node execution failed");

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

        java.util.Map<String, String> headerContext = java.util.Map.of(
                "transaction.id", "txn-123",
                "tracker.user", "john.doe");

        io.opentelemetry.api.trace.Span span = spanManager.createStateSpanWithContext(
                "test-instance", "test-process", "1.0", "ACTIVE", "TestState", headerContext, false);

        verify(mockSpan).setAttribute(SONATAFLOW_TRANSACTION_ID, "txn-123");
        verify(mockSpan).setAttribute(org.mockito.ArgumentMatchers.eq("sonataflow.tracker.user"), org.mockito.ArgumentMatchers.eq("john.doe"));
    }

    @Test
    public void shouldUseRootContextForRegularNodes() throws Exception {
        io.opentelemetry.api.trace.Tracer mockTracer = mock(io.opentelemetry.api.trace.Tracer.class);
        io.opentelemetry.api.trace.Span mockSpan = mock(io.opentelemetry.api.trace.Span.class);
        io.opentelemetry.api.trace.SpanBuilder mockSpanBuilder = setupMockSpanBuilder(mockTracer, mockSpan);
        SonataFlowOtelConfig mockConfig = createMockConfig(true, true);

        NodeSpanManager spanManager = new NodeSpanManager(mockTracer, mockConfig);

        spanManager.createStateSpan("test-instance", "test-process", "1.0", "ACTIVE", "State1", false);

        spanManager.createStateSpan("test-instance", "test-process", "1.0", "ACTIVE", "State2", false);

        io.opentelemetry.context.Context rootContext = OtelContextHolder.getRootContext("test-instance");
        assertNotNull(rootContext, "Root context should be stored");

        verify(mockSpanBuilder, atLeastOnce()).setParent(rootContext);
    }

    @Test
    public void shouldUseCurrentContextForSubprocessNodes() throws Exception {
        io.opentelemetry.api.trace.Tracer mockTracer = mock(io.opentelemetry.api.trace.Tracer.class);
        io.opentelemetry.api.trace.Span mockSpan = mock(io.opentelemetry.api.trace.Span.class);
        io.opentelemetry.api.trace.SpanBuilder mockSpanBuilder = setupMockSpanBuilder(mockTracer, mockSpan);
        SonataFlowOtelConfig mockConfig = createMockConfig(true, true);

        NodeSpanManager spanManager = new NodeSpanManager(mockTracer, mockConfig);

        spanManager.createStateSpan("test-instance", "test-process", "1.0", "ACTIVE", "SubprocessState", true);

        verify(mockSpanBuilder).setParent(io.opentelemetry.context.Context.current());
    }

    @Test
    public void shouldCaptureRootContextOnFirstNode() throws Exception {
        io.opentelemetry.api.trace.Tracer mockTracer = mock(io.opentelemetry.api.trace.Tracer.class);
        io.opentelemetry.api.trace.Span mockSpan = mock(io.opentelemetry.api.trace.Span.class);
        setupMockSpanBuilder(mockTracer, mockSpan);
        SonataFlowOtelConfig mockConfig = createMockConfig(true, true);

        NodeSpanManager spanManager = new NodeSpanManager(mockTracer, mockConfig);

        io.opentelemetry.context.Context initialContext = OtelContextHolder.getRootContext("test-instance");
        org.junit.jupiter.api.Assertions.assertNull(initialContext, "Root context should not exist before first node");

        spanManager.createStateSpan("test-instance", "test-process", "1.0", "ACTIVE", "State1", false);

        io.opentelemetry.context.Context capturedContext = OtelContextHolder.getRootContext("test-instance");
        assertNotNull(capturedContext, "Root context should be captured on first node");
    }

    @Test
    public void shouldClearRootContextOnProcessCompletion() throws Exception {
        io.opentelemetry.api.trace.Tracer mockTracer = mock(io.opentelemetry.api.trace.Tracer.class);
        io.opentelemetry.api.trace.Span mockSpan = mock(io.opentelemetry.api.trace.Span.class);
        setupMockSpanBuilder(mockTracer, mockSpan);
        SonataFlowOtelConfig mockConfig = createMockConfig(true, true);

        NodeSpanManager spanManager = new NodeSpanManager(mockTracer, mockConfig);

        spanManager.createStateSpan("test-instance", "test-process", "1.0", "ACTIVE", "State1", false);

        io.opentelemetry.context.Context storedContext = OtelContextHolder.getRootContext("test-instance");
        assertNotNull(storedContext, "Root context should be stored");

        spanManager.endRemainingSpans("test-instance");

        io.opentelemetry.context.Context clearedContext = OtelContextHolder.getRootContext("test-instance");
        org.junit.jupiter.api.Assertions.assertNull(clearedContext, "Root context should be cleared on process completion");
    }

    @Test
    public void shouldCreateStateSpanWithCorrectAttributes() throws Exception {
        io.opentelemetry.api.trace.Tracer mockTracer = mock(io.opentelemetry.api.trace.Tracer.class);
        io.opentelemetry.api.trace.Span mockSpan = mock(io.opentelemetry.api.trace.Span.class);
        SonataFlowOtelConfig mockConfig = createMockConfig(true, true);

        io.opentelemetry.api.trace.SpanBuilder mockSpanBuilder = setupMockSpanBuilder(mockTracer, mockSpan);

        NodeSpanManager spanManager = new NodeSpanManager(mockTracer, mockConfig);

        spanManager.createStateSpan("test-instance", "test-process", "1.0", "ACTIVE", "TestState", false);

        verify(mockSpanBuilder).setAttribute(SONATAFLOW_WORKFLOW_STATE, "TestState");
    }

    @Test
    public void shouldGetActiveStateSpan() throws Exception {
        io.opentelemetry.api.trace.Tracer mockTracer = mock(io.opentelemetry.api.trace.Tracer.class);
        io.opentelemetry.api.trace.Span mockSpan = mock(io.opentelemetry.api.trace.Span.class);
        SonataFlowOtelConfig mockConfig = createMockConfig(true, true);

        setupMockSpanBuilder(mockTracer, mockSpan);

        NodeSpanManager spanManager = new NodeSpanManager(mockTracer, mockConfig);

        spanManager.createStateSpan("test-instance", "test-process", "1.0", "ACTIVE", "TestState", false);

        io.opentelemetry.api.trace.Span retrievedSpan = spanManager.getActiveStateSpan("test-instance", "TestState");

        assertNotNull(retrievedSpan, "Should retrieve active state span");
        org.junit.jupiter.api.Assertions.assertEquals(mockSpan, retrievedSpan, "Retrieved span should match created span");
    }

    @Test
    public void shouldEndStateSpan() throws Exception {
        io.opentelemetry.api.trace.Tracer mockTracer = mock(io.opentelemetry.api.trace.Tracer.class);
        io.opentelemetry.api.trace.Span mockSpan = mock(io.opentelemetry.api.trace.Span.class);
        SonataFlowOtelConfig mockConfig = createMockConfig(true, true);

        setupMockSpanBuilder(mockTracer, mockSpan);

        NodeSpanManager spanManager = new NodeSpanManager(mockTracer, mockConfig);

        spanManager.createStateSpan("test-instance", "test-process", "1.0", "ACTIVE", "TestState", false);

        spanManager.endStateSpan("test-instance", "TestState");

        verify(mockSpan).end();

        io.opentelemetry.api.trace.Span retrievedSpan = spanManager.getActiveStateSpan("test-instance", "TestState");
        org.junit.jupiter.api.Assertions.assertNull(retrievedSpan, "State span should be removed after ending");
    }
}
