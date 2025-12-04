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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.OpenTelemetryTestUtils.*;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.*;

/**
 * Integration tests for OpenTelemetry workflow execution behavior.
 * Tests focus on basic workflow span creation, node execution, transaction ID handling,
 * and workflow configuration.
 */
@QuarkusIntegrationTest
@QuarkusTestResource(OpenTelemetryTestResource.class)
@QuarkusTestResource(TokenPropagationExternalServicesMock.class)
@QuarkusTestResource(KeycloakServiceMock.class)
public class OpenTelemetryWorkflowIT {

    @BeforeEach
    public void cleanup() throws InterruptedException {
        OpenTelemetryTestResource.clearSpans();
        TokenPropagationExternalServicesMock.getInstance().resetRequests();

        for (int i = 0; i < 5; i++) {
            Thread.sleep(200);
            OpenTelemetryTestResource.clearSpans();
            if (OpenTelemetryTestResource.getSpanCount() == 0) {
                break;
            }
        }
    }

    /**
     * Test complete OpenTelemetry integration with real workflow execution.
     * <p>
     * This test validates:
     * - Real workflow execution through REST endpoint
     * - Node span creation for each workflow node
     * - Transaction ID propagation from X-TRANSACTION-ID header
     * - Tracker attribute propagation from X-TRACKER-* headers
     * - All mandatory span attributes according to design document
     */
    @Test
    void shouldCreateNodeSpansWithTransactionIdFromHeader() {
        executeWorkflowWithTrackers("/greet", buildGreetBody("John", "English"),
                "workflow-test-transaction-123", "customer-456", "session-789", 201);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            List<SpanData> workflowSpans = filterWorkflowSpans(spans);

            assertThat(workflowSpans).hasSizeGreaterThanOrEqualTo(3);

            validateSharedTraceId(workflowSpans);
            validateSpanNaming(workflowSpans, "sonataflow.process.greet.execute");

            workflowSpans.forEach(span -> {
                validateMandatorySpanAttributes(span, "greet");
                validateTransactionAndTrackerAttributes(span, "workflow-test-transaction-123", "customer-456", "session-789");
            });

            Set<String> nodeIds = extractNodeNames(workflowSpans);
            assertThat(nodeIds).hasSizeGreaterThanOrEqualTo(3);
        });
    }

    /**
     * Test workflow execution without headers falls back to process instance ID.
     */
    @Test
    void shouldFallbackToProcessInstanceIdWhenNoTransactionIdHeader() {
        executeWorkflow("/greet", buildGreetBody("Alice", "Spanish"), 201);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            assertThat(spans).hasSizeGreaterThanOrEqualTo(3);

            spans.forEach(span -> {
                String processInstanceId = span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_ID);
                String transactionId = span.getAttributes().get(SONATAFLOW_TRANSACTION_ID);

                assertThat(transactionId).isEqualTo(processInstanceId);
            });
        });
    }

    /**
     * Test node span creation for different workflow paths.
     * This validates that different node paths create appropriate spans.
     */
    @Test
    void shouldCreateDifferentNodeSpansForDifferentWorkflowPaths() {
        executeWorkflowWithTxn("/greet", buildGreetBody("Carlos", "Spanish"),
                "workflow-spanish-workflow-txn", 201);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            assertThat(spans).hasSizeGreaterThanOrEqualTo(3);

            Set<String> nodeIds = extractNodeNames(spans);
            assertThat(nodeIds).contains("ChooseOnLanguage", "GreetInSpanish", "GreetPerson");
        });
    }

    /**
     * Test OpenTelemetry configuration handling.
     * Validates that the integration respects configuration settings.
     */
    @Test
    void shouldRespectOpenTelemetryConfiguration() {
        executeWorkflow("/greet", buildGreetBody("Test", "English"), 201);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            List<SpanData> workflowSpans = filterWorkflowSpans(spans);

            assertThat(workflowSpans).isNotEmpty();

            workflowSpans.forEach(span -> {
                String serviceName = span.getAttributes().get(SERVICE_NAME);
                assertThat(serviceName).isNotNull();
            });
        });
    }

    /**
     * Test concurrent workflow executions create independent traces.
     */
    @Test
    void shouldCreateIndependentTracesForConcurrentWorkflows() {
        for (int i = 0; i < 3; i++) {
            executeWorkflowWithTxn("/greet", buildGreetBody("User" + i, "English"),
                    "workflow-concurrent-txn-" + i, 201);
        }

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            assertThat(spans).hasSizeGreaterThanOrEqualTo(9);

            Set<String> traceIds = spans.stream()
                    .map(SpanData::getTraceId)
                    .collect(Collectors.toSet());
            assertThat(traceIds).hasSizeGreaterThanOrEqualTo(3);

            Set<String> transactionIds = spans.stream()
                    .map(span -> span.getAttributes().get(SONATAFLOW_TRANSACTION_ID))
                    .filter(id -> id != null && id.startsWith("workflow-concurrent-txn-"))
                    .collect(Collectors.toSet());
            assertThat(transactionIds).containsExactlyInAnyOrder(
                    "workflow-concurrent-txn-0", "workflow-concurrent-txn-1", "workflow-concurrent-txn-2");
        });
    }

    /**
     * Test to verify that process completion events are handled exactly once without race conditions.
     * This test specifically targets the issue where multiple finishing nodes trigger duplicate
     * completion context warnings: "Process in terminal state but no completion context found".
     *
     * The test ensures:
     * - Process completion events are added exactly once per process
     * - No duplicate completion context warnings are generated
     * - Multiple concurrent workflows don't interfere with each other's completion handling
     */
    @Test
    void shouldHandleProcessCompletionEventsExactlyOnce() {
        for (int i = 0; i < 3; i++) {
            executeWorkflowWithTxn("/greet", buildGreetBody("CompletionTest" + i, "English"),
                    "workflow-completion-race-test-txn-" + i, 201);
        }

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            List<SpanData> workflowSpans = filterWorkflowSpans(spans);

            assertThat(workflowSpans).hasSizeGreaterThanOrEqualTo(9);

            Map<String, List<SpanData>> spansByTxn = workflowSpans.stream()
                    .filter(span -> {
                        String txnId = span.getAttributes().get(SONATAFLOW_TRANSACTION_ID);
                        return txnId != null && txnId.startsWith("workflow-completion-race-test-txn-");
                    })
                    .collect(Collectors.groupingBy(
                            span -> span.getAttributes().get(SONATAFLOW_TRANSACTION_ID)));

            assertThat(spansByTxn).hasSize(3);

            spansByTxn.forEach((txnId, workflowSpansForTxn) -> {
                List<EventData> completeEvents = workflowSpansForTxn.stream()
                        .flatMap(span -> span.getEvents().stream())
                        .filter(event -> "process.instance.complete".equals(event.getName()))
                        .collect(Collectors.toList());

                assertThat(completeEvents)
                        .withFailMessage("Each workflow should have exactly one process.instance.complete event. " +
                                "Transaction %s has %d events", txnId, completeEvents.size())
                        .hasSize(1);

                EventData completeEvent = completeEvents.get(0);
                assertThat(completeEvent.getAttributes().get(PROCESS_INSTANCE_ID))
                        .isNotNull();
                assertThat(completeEvent.getAttributes().get(OUTCOME))
                        .isEqualTo("COMPLETED");
                assertThat(completeEvent.getAttributes().get(DURATION_MS))
                        .isNotNull()
                        .isGreaterThan(0L);
            });

            Map<String, List<EventData>> startEventsByTxn = workflowSpans.stream()
                    .filter(span -> {
                        String txnId = span.getAttributes().get(SONATAFLOW_TRANSACTION_ID);
                        return txnId != null && txnId.startsWith("workflow-completion-race-test-txn-");
                    })
                    .flatMap(span -> span.getEvents().stream())
                    .filter(event -> "process.instance.start".equals(event.getName()))
                    .collect(Collectors.groupingBy(
                            event -> event.getAttributes().get(REFERENCE_ID)));

            assertThat(startEventsByTxn).hasSize(3);
            startEventsByTxn.forEach((txnId, events) -> {
                assertThat(events)
                        .withFailMessage("Each workflow should have exactly one process.instance.start event. " +
                                "Transaction %s has %d events", txnId, events.size())
                        .hasSize(1);
            });
        });
    }

    // Private helper methods moved from OpenTelemetryTestUtils for WorkflowIT-specific validation

    /**
     * Validates span naming follows design document pattern.
     *
     * @param spans list of spans to validate
     * @param expectedPrefix the expected span name prefix
     */
    private static void validateSpanNaming(List<SpanData> spans, String expectedPrefix) {
        spans.forEach(span -> {
            assertThat(span.getName()).startsWith(expectedPrefix);
        });
    }
}
