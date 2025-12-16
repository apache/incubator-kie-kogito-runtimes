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

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.internal.process.event.KogitoProcessEventListener;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.quarkus.serverless.workflow.opentelemetry.config.SonataFlowOtelConfig;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NodeOtelEventListenerTest {

    @Mock
    private NodeSpanManager spanManager;

    @Mock
    private SonataFlowOtelConfig config;

    @Mock
    private SonataFlowOtelConfig.EventConfig eventConfig;

    @Mock
    private Span mockSpan;

    @Mock
    private KogitoNodeInstance nodeInstance;

    @Mock
    private KogitoProcessInstance processInstance;

    private NodeOtelEventListener eventListener;

    @Mock
    private HeaderContextExtractor headerExtractor;

    @BeforeEach
    public void setUp() {
        // Only set up basic config mocking - specific test behaviors will be set up in individual tests
        eventListener = new NodeOtelEventListener(spanManager, config, headerExtractor);
    }

    @Test
    public void shouldImplementKogitoProcessEventListener() {
        assertTrue(eventListener instanceof KogitoProcessEventListener);
    }

    @Test
    public void shouldCreateNodeSpanOnBeforeNodeTriggered() {
        // Mock event data - using regular business node (not "Start")
        when(nodeInstance.getNodeName()).thenReturn("node-1");
        when(processInstance.getId()).thenReturn("process-instance-1");
        when(processInstance.getProcessId()).thenReturn("test-process");
        when(processInstance.getProcessVersion()).thenReturn("1.0.0");
        when(processInstance.getState()).thenReturn(ProcessInstance.STATE_ACTIVE);

        when(spanManager.createNodeSpanWithContext(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(mockSpan);

        // Create mock event
        org.kie.api.event.process.ProcessNodeTriggeredEvent event =
                org.mockito.Mockito.mock(org.kie.api.event.process.ProcessNodeTriggeredEvent.class);
        when(event.getNodeInstance()).thenReturn(nodeInstance);
        when(event.getProcessInstance()).thenReturn(processInstance);

        // Execute
        eventListener.beforeNodeTriggered(event);

        // Verify
        verify(spanManager).createNodeSpanWithContext("process-instance-1", "test-process", "1.0.0", "ACTIVE", "node-1", new HashMap<>());
        verify(spanManager).addProcessEvent(mockSpan, "node.started", "Node execution started: node-1");
    }

    @Test
    public void shouldAddNodeCompletedEventOnAfterNodeLeft() {
        // Mock event data
        when(nodeInstance.getNodeName()).thenReturn("node-1");
        when(processInstance.getId()).thenReturn("process-instance-1");
        when(processInstance.getState()).thenReturn(ProcessInstance.STATE_ACTIVE);

        // Create mock event
        org.kie.api.event.process.ProcessNodeLeftEvent event =
                org.mockito.Mockito.mock(org.kie.api.event.process.ProcessNodeLeftEvent.class);
        when(event.getNodeInstance()).thenReturn(nodeInstance);
        when(event.getProcessInstance()).thenReturn(processInstance);

        // Mock active span
        when(spanManager.getActiveNodeSpan("process-instance-1", "node-1")).thenReturn(mockSpan);

        // Execute
        eventListener.afterNodeLeft(event);

        // Verify - afterNodeLeft only adds an event, spans are ended during process completion
        verify(spanManager).getActiveNodeSpan("process-instance-1", "node-1");
        verify(spanManager).addProcessEvent(mockSpan, "node.completed", "Node execution completed: node-1");
    }

    @Test
    public void shouldAddProcessStartEventOnlyForStartNode() {
        // Set up config mocking
        when(config.events()).thenReturn(eventConfig);
        when(eventConfig.enabled()).thenReturn(true);

        // Mock event data - using "Start" node
        when(nodeInstance.getNodeName()).thenReturn("Start");
        when(processInstance.getId()).thenReturn("process-instance-1");
        when(processInstance.getProcessId()).thenReturn("test-process");
        when(processInstance.getProcessVersion()).thenReturn("1.0.0");
        when(processInstance.getState()).thenReturn(ProcessInstance.STATE_ACTIVE);

        when(spanManager.createNodeSpanWithContext(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(mockSpan);

        // Create mock event
        org.kie.api.event.process.ProcessNodeTriggeredEvent event =
                org.mockito.Mockito.mock(org.kie.api.event.process.ProcessNodeTriggeredEvent.class);
        when(event.getNodeInstance()).thenReturn(nodeInstance);
        when(event.getProcessInstance()).thenReturn(processInstance);

        // Execute
        eventListener.beforeNodeTriggered(event);

        // Verify - Start node should trigger process.instance.start event
        verify(spanManager).createNodeSpanWithContext("process-instance-1", "test-process", "1.0.0", "ACTIVE", "Start", new HashMap<>());
        verify(spanManager).addProcessEvent(mockSpan, "node.started", "Node execution started: Start");
        verify(spanManager).addProcessEvent(eq(mockSpan), eq("process.instance.start"), any(Attributes.class));
    }

    @Test
    public void shouldNotAddProcessStartEventForBusinessNodes() {
        // Mock event data - using regular business node (not "Start")
        when(nodeInstance.getNodeName()).thenReturn("ChooseOnLanguage");
        when(processInstance.getId()).thenReturn("process-instance-1");
        when(processInstance.getProcessId()).thenReturn("test-process");
        when(processInstance.getProcessVersion()).thenReturn("1.0.0");
        when(processInstance.getState()).thenReturn(ProcessInstance.STATE_ACTIVE);

        when(spanManager.createNodeSpanWithContext(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(mockSpan);

        // Create mock event
        org.kie.api.event.process.ProcessNodeTriggeredEvent event =
                org.mockito.Mockito.mock(org.kie.api.event.process.ProcessNodeTriggeredEvent.class);
        when(event.getNodeInstance()).thenReturn(nodeInstance);
        when(event.getProcessInstance()).thenReturn(processInstance);

        // Execute
        eventListener.beforeNodeTriggered(event);

        // Verify - Non-Start node should NOT trigger process.instance.start event
        verify(spanManager).createNodeSpanWithContext("process-instance-1", "test-process", "1.0.0", "ACTIVE", "ChooseOnLanguage", new HashMap<>());
        verify(spanManager).addProcessEvent(mockSpan, "node.started", "Node execution started: ChooseOnLanguage");
        verify(spanManager, never()).addProcessEvent(eq(mockSpan), eq("process.instance.start"), any(Attributes.class));
    }

}
