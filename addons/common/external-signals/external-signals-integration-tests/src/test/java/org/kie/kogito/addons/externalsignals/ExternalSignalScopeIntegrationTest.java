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
package org.kie.kogito.addons.externalsignals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.definition.process.WorkflowElementIdentifier;
import org.kie.kogito.addons.externalsignals.runtime.ExternalSignalConfigImpl;
import org.kie.kogito.addons.externalsignals.runtime.ExternalSignalWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemManager;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Integration tests for external signal scope functionality.
 * 
 * These tests verify that the external signal scope implementation works correctly
 * in various scenarios, including handler invocation, configuration, metadata extraction,
 * error handling, and multiple signal dispatching.
 *
 * Test Coverage:
 * <ul>
 * <li>Handler invocation with correct parameters</li>
 * <li>Signal-to-topic mapping from configuration</li>
 * <li>Metadata extraction and inclusion in events</li>
 * <li>Graceful handling when no handler is registered</li>
 * <li>Multiple signals dispatched in sequence</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class ExternalSignalScopeIntegrationTest {

    @Mock
    private KogitoWorkItemManager workItemManager;

    @Mock
    private KogitoWorkItem workItem;

    private ExternalSignalConfig config;
    private TestExternalSignalDispatcher testDispatcher;
    private ExternalSignalWorkItemHandler handler;

    @BeforeEach
    public void setUp() {
        // Create test configuration
        Properties props = new Properties();
        props.setProperty("kogito.external-signals.default-prefix", "test-signals");
        props.setProperty("kogito.external-signals.mapping.OrderApproval", "order-approval-topic");
        props.setProperty("kogito.external-signals.mapping.PaymentProcessed", "payment-topic");
        config = new ExternalSignalConfigImpl(props);

        // Create test dispatcher that captures events
        testDispatcher = new TestExternalSignalDispatcher(config);

        // Create handler with test dispatcher
        handler = new ExternalSignalWorkItemHandler(testDispatcher);
    }

    /**
     * Verifies that the handler is invoked with correct parameters and
     * that the work item is completed successfully.
     */
    @Test
    public void testExternalSignalScopeWithHandler() {
        // Setup work item with signal parameters
        when(workItem.getId()).thenReturn(1L);
        when(workItem.getStringId()).thenReturn("work-item-1");
        when(workItem.getProcessInstanceId()).thenReturn("pid-123");
        when(workItem.getProcessInstanceStringId()).thenReturn("pid-123");
        KogitoNodeInstance nodeInstance1 = mock(KogitoNodeInstance.class);
        when(workItem.getNodeInstance()).thenReturn(nodeInstance1);
        when(nodeInstance1.getStringId()).thenReturn("node-456");
        WorkflowElementIdentifier nodeId1 = mock(WorkflowElementIdentifier.class);
        when(nodeId1.toString()).thenReturn("789");
        when(nodeInstance1.getNodeId()).thenReturn(nodeId1);
        when(workItem.getParameter("Signal")).thenReturn("TestSignal");
        when(workItem.getParameter("Data")).thenReturn("test-data");
        when(workItem.getPhaseStatus()).thenReturn("Activated");

        // Execute handler
        var result = handler.activateWorkItemHandler(workItemManager, handler, workItem, null);

        // Verify handler was invoked and work item completed
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("complete");

        // Verify event was dispatched
        assertThat(testDispatcher.getDispatchedEvents()).hasSize(1);
        ExternalSignalEvent event = testDispatcher.getDispatchedEvents().get(0);

        assertThat(event.getSignalName()).isEqualTo("TestSignal");
        assertThat(event.getSignalData()).isEqualTo("test-data");
        assertThat(event.getSourceProcessInstanceId()).isEqualTo("pid-123");
        assertThat(event.getCorrelationId()).isEqualTo("work-item-1");
        assertThat(event.getTimestamp()).isNotNull();

        // Verify metadata
        assertThat(event.getMetadata()).containsEntry("workItemId", "work-item-1");
        assertThat(event.getMetadata()).containsEntry("nodeInstanceId", "node-456");
        assertThat(event.getMetadata()).containsEntry("nodeId", "789");
    }

    /**
     * Tests signal-to-topic mapping from configuration and verifies
     * correct topic resolution.
     */
    @Test
    public void testExternalSignalScopeWithConfiguration() {
        // Test explicit mapping
        String topic1 = testDispatcher.resolveTrigger("OrderApproval");
        assertThat(topic1).isEqualTo("order-approval-topic");

        String topic2 = testDispatcher.resolveTrigger("PaymentProcessed");
        assertThat(topic2).isEqualTo("payment-topic");

        // Test default naming convention
        String topic3 = testDispatcher.resolveTrigger("UnmappedSignal");
        assertThat(topic3).isEqualTo("test-signals-UnmappedSignal");

        // Setup work item with mapped signal
        when(workItem.getId()).thenReturn(2L);
        when(workItem.getStringId()).thenReturn("work-item-2");
        when(workItem.getProcessInstanceId()).thenReturn("pid-456");
        when(workItem.getProcessInstanceStringId()).thenReturn("pid-456");
        KogitoNodeInstance nodeInstance2 = mock(KogitoNodeInstance.class);
        when(workItem.getNodeInstance()).thenReturn(nodeInstance2);
        when(nodeInstance2.getStringId()).thenReturn("node-789");
        WorkflowElementIdentifier nodeId2 = mock(WorkflowElementIdentifier.class);
        when(nodeId2.toString()).thenReturn("123");
        when(nodeInstance2.getNodeId()).thenReturn(nodeId2);
        when(workItem.getParameter("Signal")).thenReturn("OrderApproval");
        when(workItem.getParameter("Data")).thenReturn(Map.of("orderId", "ORD-123"));
        when(workItem.getPhaseStatus()).thenReturn("Activated");

        // Execute handler
        var result = handler.activateWorkItemHandler(workItemManager, handler, workItem, null);

        // Verify event was dispatched with correct signal name
        assertThat(result).isPresent();
        assertThat(testDispatcher.getDispatchedEvents()).hasSize(1);
        ExternalSignalEvent event = testDispatcher.getDispatchedEvents().get(0);

        assertThat(event.getSignalName()).isEqualTo("OrderApproval");
        assertThat(testDispatcher.resolveTrigger(event.getSignalName()))
                .isEqualTo("order-approval-topic");
    }

    /**
     * Tests external signal with all optional parameters (processInstanceId,
     * workItemId, deploymentId) and verifies metadata is correctly extracted
     * and included in the event.
     */
    @Test
    public void testExternalSignalScopeWithMetadata() {
        // Setup work item with all optional parameters
        when(workItem.getId()).thenReturn(3L);
        when(workItem.getStringId()).thenReturn("work-item-3");
        when(workItem.getProcessInstanceId()).thenReturn("pid-789");
        when(workItem.getProcessInstanceStringId()).thenReturn("pid-789");
        KogitoNodeInstance nodeInstance3 = mock(KogitoNodeInstance.class);
        when(workItem.getNodeInstance()).thenReturn(nodeInstance3);
        when(nodeInstance3.getStringId()).thenReturn("node-123");
        WorkflowElementIdentifier nodeId3 = mock(WorkflowElementIdentifier.class);
        when(nodeId3.toString()).thenReturn("456");
        when(nodeInstance3.getNodeId()).thenReturn(nodeId3);
        when(workItem.getParameter("Signal")).thenReturn("ComplexSignal");
        when(workItem.getParameter("Data")).thenReturn(Map.of("key", "value"));
        when(workItem.getParameter("SignalProcessInstanceId")).thenReturn("target-pid-123");
        when(workItem.getParameter("SignalWorkItemId")).thenReturn("target-workitem-456");
        when(workItem.getParameter("SignalDeploymentId")).thenReturn("deployment-v1.0");
        when(workItem.getPhaseStatus()).thenReturn("Activated");

        // Execute handler
        var result = handler.activateWorkItemHandler(workItemManager, handler, workItem, null);

        // Verify event was dispatched with all metadata
        assertThat(result).isPresent();
        assertThat(testDispatcher.getDispatchedEvents()).hasSize(1);
        ExternalSignalEvent event = testDispatcher.getDispatchedEvents().get(0);

        assertThat(event.getSignalName()).isEqualTo("ComplexSignal");
        assertThat(event.getSourceProcessInstanceId()).isEqualTo("pid-789");
        assertThat(event.getCorrelationId()).isEqualTo("work-item-3");

        // Verify all metadata fields
        Map<String, Object> metadata = event.getMetadata();
        assertThat(metadata).containsEntry("targetProcessInstanceId", "target-pid-123");
        assertThat(metadata).containsEntry("targetWorkItemId", "target-workitem-456");
        assertThat(metadata).containsEntry("targetDeploymentId", "deployment-v1.0");
        assertThat(metadata).containsEntry("workItemId", "work-item-3");
        assertThat(metadata).containsEntry("nodeInstanceId", "node-123");
        assertThat(metadata).containsEntry("nodeId", "456");
    }

    /**
     * Tests behavior when signal parameter is missing or invalid.
     * Should handle gracefully by aborting the work item.
     */
    @Test
    public void testExternalSignalScopeWithoutHandler() {
        // Test with missing Signal parameter
        when(workItem.getId()).thenReturn(4L);
        when(workItem.getParameter("Signal")).thenReturn(null); // Missing signal
        when(workItem.getPhaseStatus()).thenReturn("Activated");

        // Execute handler
        var result = handler.activateWorkItemHandler(workItemManager, handler, workItem, null);

        // Verify work item was aborted (not completed)
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("abort");

        // Verify no event was dispatched
        assertThat(testDispatcher.getDispatchedEvents()).isEmpty();
    }

    /**
     * Tests multiple external signals dispatched in sequence and verifies
     * each is dispatched correctly.
     */
    @Test
    public void testExternalSignalScopeMultipleSignals() {
        List<String> signalNames = List.of("Signal1", "Signal2", "Signal3");

        for (int i = 0; i < signalNames.size(); i++) {
            String signalName = signalNames.get(i);

            // Setup work item for each signal
            when(workItem.getId()).thenReturn((long) (i + 1));
            when(workItem.getStringId()).thenReturn("work-item-" + (i + 1));
            when(workItem.getProcessInstanceId()).thenReturn("pid-" + (i + 1));
            when(workItem.getProcessInstanceStringId()).thenReturn("pid-" + (i + 1));
            KogitoNodeInstance nodeInstanceLoop = mock(KogitoNodeInstance.class);
            when(workItem.getNodeInstance()).thenReturn(nodeInstanceLoop);
            when(nodeInstanceLoop.getStringId()).thenReturn("node-" + (i + 1));
            WorkflowElementIdentifier nodeIdLoop = mock(WorkflowElementIdentifier.class);
            when(nodeIdLoop.toString()).thenReturn(String.valueOf(i + 1));
            when(nodeInstanceLoop.getNodeId()).thenReturn(nodeIdLoop);
            when(workItem.getParameter("Signal")).thenReturn(signalName);
            when(workItem.getParameter("Data")).thenReturn("data-" + (i + 1));
            when(workItem.getPhaseStatus()).thenReturn("Activated");

            // Execute handler
            var result = handler.activateWorkItemHandler(workItemManager, handler, workItem, null);

            // Verify work item completed
            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo("complete");
        }

        // Verify all events were dispatched
        assertThat(testDispatcher.getDispatchedEvents()).hasSize(3);

        for (int i = 0; i < signalNames.size(); i++) {
            ExternalSignalEvent event = testDispatcher.getDispatchedEvents().get(i);
            assertThat(event.getSignalName()).isEqualTo(signalNames.get(i));
            assertThat(event.getSignalData()).isEqualTo("data-" + (i + 1));
            assertThat(event.getSourceProcessInstanceId()).isEqualTo("pid-" + (i + 1));
        }
    }

    /**
     * Tests behavior when signal name is empty string.
     * Should abort the work item.
     */
    @Test
    public void testExternalSignalScopeWithEmptySignalName() {
        when(workItem.getId()).thenReturn(5L);
        when(workItem.getParameter("Signal")).thenReturn("   "); // Empty/whitespace
        when(workItem.getPhaseStatus()).thenReturn("Activated");

        // Execute handler
        var result = handler.activateWorkItemHandler(workItemManager, handler, workItem, null);

        // Verify work item was aborted
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("abort");

        // Verify no event was dispatched
        assertThat(testDispatcher.getDispatchedEvents()).isEmpty();
    }

    /**
     * Tests behavior when signal parameter is not a String.
     * Should abort the work item.
     */
    @Test
    public void testExternalSignalScopeWithInvalidSignalType() {
        when(workItem.getId()).thenReturn(6L);
        when(workItem.getParameter("Signal")).thenReturn(12345); // Not a String
        when(workItem.getPhaseStatus()).thenReturn("Activated");

        // Execute handler
        var result = handler.activateWorkItemHandler(workItemManager, handler, workItem, null);

        // Verify work item was aborted
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("abort");

        // Verify no event was dispatched
        assertThat(testDispatcher.getDispatchedEvents()).isEmpty();
    }

    /**
     * Tests behavior when dispatcher throws an exception.
     * Should abort the work item.
     */
    @Test
    public void testExternalSignalScopeWithDispatcherException() {
        // Create dispatcher that throws exception
        ExternalSignalDispatcher failingDispatcher = new ExternalSignalDispatcher() {
            @Override
            public void dispatch(ExternalSignalEvent event) {
                throw new ExternalSignalDispatchException("Simulated dispatch failure",
                        new RuntimeException("Network error"));
            }

            @Override
            public String resolveTrigger(String signalName) {
                return "test-topic";
            }
        };

        ExternalSignalWorkItemHandler failingHandler = new ExternalSignalWorkItemHandler(failingDispatcher);

        when(workItem.getId()).thenReturn(7L);
        when(workItem.getStringId()).thenReturn("work-item-7");
        when(workItem.getProcessInstanceId()).thenReturn("pid-777");
        when(workItem.getProcessInstanceStringId()).thenReturn("pid-777");
        KogitoNodeInstance nodeInstance777 = mock(KogitoNodeInstance.class);
        when(workItem.getNodeInstance()).thenReturn(nodeInstance777);
        when(nodeInstance777.getStringId()).thenReturn("node-777");
        WorkflowElementIdentifier nodeId777 = mock(WorkflowElementIdentifier.class);
        when(nodeId777.toString()).thenReturn("777");
        when(nodeInstance777.getNodeId()).thenReturn(nodeId777);
        when(workItem.getParameter("Signal")).thenReturn("FailingSignal");
        when(workItem.getPhaseStatus()).thenReturn("Activated");

        // Execute handler
        var result = failingHandler.activateWorkItemHandler(workItemManager, failingHandler, workItem, null);

        // Verify work item was aborted due to exception
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("abort");
    }

    /**
     * Verifies the complete structure of the dispatched event including
     * CloudEvent-compatible fields.
     */
    @Test
    public void testExternalSignalScopeEventStructure() {
        when(workItem.getId()).thenReturn(8L);
        when(workItem.getStringId()).thenReturn("work-item-8");
        when(workItem.getProcessInstanceId()).thenReturn("pid-888");
        when(workItem.getProcessInstanceStringId()).thenReturn("pid-888");
        KogitoNodeInstance nodeInstance888 = mock(KogitoNodeInstance.class);
        when(workItem.getNodeInstance()).thenReturn(nodeInstance888);
        when(nodeInstance888.getStringId()).thenReturn("node-888");
        WorkflowElementIdentifier nodeId888 = mock(WorkflowElementIdentifier.class);
        when(nodeId888.toString()).thenReturn("888");
        when(nodeInstance888.getNodeId()).thenReturn(nodeId888);
        when(workItem.getParameter("Signal")).thenReturn("StructureTest");
        when(workItem.getParameter("Data")).thenReturn(Map.of("test", "data"));
        when(workItem.getPhaseStatus()).thenReturn("Activated");

        // Execute handler
        handler.activateWorkItemHandler(workItemManager, handler, workItem, null);

        // Verify event structure
        assertThat(testDispatcher.getDispatchedEvents()).hasSize(1);
        ExternalSignalEvent event = testDispatcher.getDispatchedEvents().get(0);

        // CloudEvent-compatible fields
        assertThat(event.getType()).isEqualTo("org.kie.kogito.signal.external");
        assertThat(event.getSource()).hasToString("kogito://process/pid-888");
        assertThat(event.getId()).isEqualTo("work-item-8");

        // Core fields
        assertThat(event.getSignalName()).isEqualTo("StructureTest");
        assertThat(event.getSignalData()).isInstanceOf(Map.class);
        assertThat(event.getSourceProcessInstanceId()).isEqualTo("pid-888");
        assertThat(event.getCorrelationId()).isEqualTo("work-item-8");
        assertThat(event.getTimestamp()).isBeforeOrEqualTo(Instant.now());

        // Metadata
        assertThat(event.getMetadata()).isNotNull();
        assertThat(event.getMetadata()).isNotEmpty();
    }

    /**
     * Test dispatcher implementation that captures dispatched events for verification.
     */
    private static class TestExternalSignalDispatcher implements ExternalSignalDispatcher {
        private final ExternalSignalConfig config;
        private final List<ExternalSignalEvent> dispatchedEvents = new ArrayList<>();

        public TestExternalSignalDispatcher(ExternalSignalConfig config) {
            this.config = config;
        }

        @Override
        public void dispatch(ExternalSignalEvent event) {
            dispatchedEvents.add(event);
        }

        @Override
        public String resolveTrigger(String signalName) {
            // Use config to resolve trigger
            return config.resolveTrigger(signalName);
        }

        public List<ExternalSignalEvent> getDispatchedEvents() {
            return dispatchedEvents;
        }
    }
}
