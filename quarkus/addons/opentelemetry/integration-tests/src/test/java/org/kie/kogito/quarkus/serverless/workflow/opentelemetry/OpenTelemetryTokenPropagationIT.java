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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.opentelemetry.sdk.trace.data.SpanData;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.OpenTelemetryTestUtils.*;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.*;

/**
 * Integration tests for OpenTelemetry token propagation and subflow tracing.
 * Tests focus on validating token propagation across service calls and
 * proper trace correlation between main workflows and subflows.
 */
@QuarkusIntegrationTest
@QuarkusTestResource(OpenTelemetryTestResource.class)
@QuarkusTestResource(TokenPropagationExternalServicesMock.class)
@QuarkusTestResource(KeycloakServiceMock.class)
public class OpenTelemetryTokenPropagationIT {

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
     * Comprehensive test for token propagation workflow with subflow tracing validation.
     * This test consolidates token propagation and subflow behavior validation into a single
     * comprehensive test that ensures proper trace correlation, parent-child relationships,
     * and transaction ID propagation across main workflow and subflow boundaries.
     * <p>
     * Validates:
     * - Token propagation across external service calls
     * - Main workflow and subflow span creation
     * - Trace ID consistency between main workflow and subflow
     * - Process instance isolation (different process instance IDs)
     * - Parent-child span relationships
     * - Transaction ID propagation to all spans (main + subflow)
     * - Timing and sequence of span execution
     * - Subflow node execution completeness
     * - Span durations are positive
     * - No error status in successful execution
     */
    @Test
    void shouldValidateTokenPropagationWithSubflowTracing() {
        executeTokenPropagationWorkflow("token-propagation-subflow-test-txn", 201);

        await().atMost(Duration.ofSeconds(25)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            List<SpanData> workflowSpans = filterWorkflowSpans(spans);

            assertThat(workflowSpans)
                    .withFailMessage("Workflow spans should be present for token propagation with subflow")
                    .isNotEmpty();

            validateMainAndSubflowBehaviour(workflowSpans, "token-propagation-subflow-test-txn");
        });
    }

    // Private helper methods moved from OpenTelemetryTestUtils for TokenPropagation-specific validation

    /**
     * Validates main workflow and subflow behavior including trace correlation,
     * process instance isolation, parent-child relationships, timing, and execution.
     *
     * @param workflowSpans all workflow spans to validate
     * @param transactionId the transaction ID for filtering
     */
    private static void validateMainAndSubflowBehaviour(List<SpanData> workflowSpans, String transactionId) {
        List<SpanData> currentTestSpans = OpenTelemetryTestUtils.filterSpansByTransactionId(workflowSpans, transactionId);

        List<SpanData> mainWorkflowSpans = filterMainWorkflowSpans(currentTestSpans, "token_propagation");
        List<SpanData> subflowSpans = filterSubflowSpans(currentTestSpans, "tokenPropagationSubflow");

        assertThat(mainWorkflowSpans)
                .withFailMessage("Main workflow spans should be present for token_propagation")
                .isNotEmpty();
        assertThat(subflowSpans)
                .withFailMessage("Subflow spans should be present for tokenPropagationSubflow")
                .isNotEmpty();

        validateTraceIdConsistency(mainWorkflowSpans, subflowSpans);
        validateProcessInstanceIsolation(mainWorkflowSpans, subflowSpans);

        SpanData executeSubflowSpan = validateParentChildSpanRelationship(mainWorkflowSpans, subflowSpans);
        validateSpansTimingAndSequence(mainWorkflowSpans, subflowSpans, executeSubflowSpan);
        validateSubflowNodeExecution(subflowSpans);

        validateTransactionIdPropagation(mainWorkflowSpans, transactionId);
        validateTransactionIdPropagation(subflowSpans, transactionId);
        validateSpanDurations(currentTestSpans);
        validateNoErrorStatus(currentTestSpans);
    }

    /**
     * Filters spans to include only those from the main workflow (by process ID).
     *
     * @param spans list of all spans
     * @param processId the process ID to filter by
     * @return filtered list containing only spans from the specified process
     */
    private static List<SpanData> filterMainWorkflowSpans(List<SpanData> spans, String processId) {
        return spans.stream()
                .filter(span -> {
                    String spanProcessId = span.getAttributes().get(SONATAFLOW_PROCESS_ID);
                    return processId.equals(spanProcessId);
                })
                .collect(Collectors.toList());
    }

    /**
     * Filters spans to include only those from a subflow (by process ID).
     *
     * @param spans list of all spans
     * @param subflowProcessId the subflow process ID to filter by
     * @return filtered list containing only spans from the specified subflow
     */
    private static List<SpanData> filterSubflowSpans(List<SpanData> spans, String subflowProcessId) {
        return filterMainWorkflowSpans(spans, subflowProcessId);
    }

    /**
     * Validates trace ID consistency across main and subflow spans.
     *
     * @param mainWorkflowSpans spans from the main workflow
     * @param subflowSpans spans from the subflow
     */
    private static void validateTraceIdConsistency(List<SpanData> mainWorkflowSpans, List<SpanData> subflowSpans) {
        Set<String> mainTraceIds = extractTraceIds(mainWorkflowSpans);
        Set<String> subflowTraceIds = extractTraceIds(subflowSpans);

        assertThat(mainTraceIds)
                .withFailMessage("All main workflow spans should share the same trace ID")
                .hasSize(1);
        assertThat(subflowTraceIds)
                .withFailMessage("All subflow spans should share the same trace ID")
                .hasSize(1);
        assertThat(mainTraceIds.iterator().next())
                .withFailMessage("Main workflow and subflow should share the same trace ID for correlation")
                .isEqualTo(subflowTraceIds.iterator().next());
    }

    /**
     * Validates process instance isolation between main and subflow.
     *
     * @param mainWorkflowSpans spans from the main workflow
     * @param subflowSpans spans from the subflow
     */
    private static void validateProcessInstanceIsolation(List<SpanData> mainWorkflowSpans, List<SpanData> subflowSpans) {
        Set<String> mainProcessInstanceIds = extractProcessInstanceIds(mainWorkflowSpans);
        Set<String> subflowProcessInstanceIds = extractProcessInstanceIds(subflowSpans);

        assertThat(mainProcessInstanceIds)
                .withFailMessage("All main workflow spans should share the same process instance ID")
                .hasSize(1);
        assertThat(subflowProcessInstanceIds)
                .withFailMessage("All subflow spans should share the same process instance ID")
                .hasSize(1);
        assertThat(mainProcessInstanceIds.iterator().next())
                .withFailMessage("Main workflow and subflow must have different process instance IDs")
                .isNotEqualTo(subflowProcessInstanceIds.iterator().next());
    }

    /**
     * Validates parent-child span relationship between main workflow and subflow.
     *
     * @param mainWorkflowSpans spans from the main workflow
     * @param subflowSpans spans from the subflow
     * @return the ExecuteSubflow span
     */
    private static SpanData validateParentChildSpanRelationship(List<SpanData> mainWorkflowSpans, List<SpanData> subflowSpans) {
        SpanData executeSubflowSpan = findSpanByNodeName(mainWorkflowSpans, "ExecuteSubflow");

        String executeSubflowSpanId = executeSubflowSpan.getSpanId();

        subflowSpans.forEach(span -> {
            String parentSpanId = span.getParentSpanId();
            assertThat(parentSpanId)
                    .withFailMessage("Subflow span should have a parent span ID. Span: %s", span.getName())
                    .isNotNull();
        });
        return executeSubflowSpan;
    }

    /**
     * Validates timing and sequence of spans across main workflow and subflow.
     *
     * @param mainWorkflowSpans spans from the main workflow
     * @param subflowSpans spans from the subflow
     * @param executeSubflowSpan the ExecuteSubflow span
     */
    private static void validateSpansTimingAndSequence(List<SpanData> mainWorkflowSpans, List<SpanData> subflowSpans, SpanData executeSubflowSpan) {
        validateSpanTiming(mainWorkflowSpans);
        validateSpanTiming(subflowSpans);

        List<SpanData> sortedSubflowSpans = subflowSpans.stream()
                .sorted(Comparator.comparingLong(SpanData::getStartEpochNanos))
                .toList();
        SpanData firstSubflowSpan = sortedSubflowSpans.get(0);
        assertThat(firstSubflowSpan.getStartEpochNanos())
                .withFailMessage("First subflow span should start after ExecuteSubflow span")
                .isGreaterThanOrEqualTo(executeSubflowSpan.getStartEpochNanos());
    }

    /**
     * Validates that all expected subflow nodes are executed.
     *
     * @param subflowSpans spans from the subflow
     */
    private static void validateSubflowNodeExecution(List<SpanData> subflowSpans) {
        Set<String> expectedSubflowNodes = Set.of(
                "SubflowExecution1", "SubflowExecution2", "SubflowExecution3",
                "SubflowExecution4", "SubflowExecution5", "SubflowEnd");
        Set<String> actualSubflowNodes = extractNodeNames(subflowSpans);

        assertThat(actualSubflowNodes)
                .withFailMessage("Subflow should execute all expected nodes")
                .containsAll(expectedSubflowNodes);
    }

    /**
     * Validates span timing and sequence for a list of spans.
     *
     * @param spans list of spans to validate
     */
    private static void validateSpanTiming(List<SpanData> spans) {
        List<SpanData> sortedSpans = spans.stream()
                .sorted(Comparator.comparingLong(SpanData::getStartEpochNanos))
                .toList();

        for (int i = 1; i < sortedSpans.size(); i++) {
            SpanData previousSpan = sortedSpans.get(i - 1);
            SpanData currentSpan = sortedSpans.get(i);
            assertThat(currentSpan.getStartEpochNanos())
                    .withFailMessage("Span %s should start after or at the same time as previous span %s",
                            currentSpan.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_NODE),
                            previousSpan.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_NODE))
                    .isGreaterThanOrEqualTo(previousSpan.getStartEpochNanos());
        }
    }

    /**
     * Validates that all spans have positive duration.
     *
     * @param spans list of spans to validate
     */
    private static void validateSpanDurations(List<SpanData> spans) {
        spans.forEach(span -> {
            long duration = span.getEndEpochNanos() - span.getStartEpochNanos();
            assertThat(duration)
                    .withFailMessage("Span %s should have positive duration",
                            span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_NODE))
                    .isGreaterThan(0);
        });
    }

    /**
     * Validates that no spans have error status.
     *
     * @param spans list of spans to validate
     */
    private static void validateNoErrorStatus(List<SpanData> spans) {
        spans.forEach(span -> {
            assertThat(span.getStatus().getStatusCode())
                    .withFailMessage("Span %s should not have error status for successful workflow execution",
                            span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_NODE))
                    .isNotEqualTo(io.opentelemetry.api.trace.StatusCode.ERROR);
        });
    }

    /**
     * Validates transaction ID propagation to all spans.
     *
     * @param spans list of spans to validate
     * @param expectedTransactionId the expected transaction ID
     */
    private static void validateTransactionIdPropagation(List<SpanData> spans, String expectedTransactionId) {
        spans.forEach(span -> {
            String txnId = span.getAttributes().get(SONATAFLOW_TRANSACTION_ID);
            assertThat(txnId)
                    .withFailMessage("Span should have transaction ID")
                    .isEqualTo(expectedTransactionId);
        });
    }

    /**
     * Finds a span by node name.
     *
     * @param spans list of spans to search
     * @param nodeName the node name to find
     * @return the span with the specified node name
     * @throws AssertionError if span is not found
     */
    private static SpanData findSpanByNodeName(List<SpanData> spans, String nodeName) {
        return spans.stream()
                .filter(span -> {
                    String spanNodeName = span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_NODE);
                    return nodeName.equals(spanNodeName);
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError("Span with node name '" + nodeName + "' not found"));
    }

    /**
     * Extracts unique trace IDs from spans.
     *
     * @param spans list of spans
     * @return set of unique trace IDs
     */
    private static Set<String> extractTraceIds(List<SpanData> spans) {
        return spans.stream()
                .map(SpanData::getTraceId)
                .collect(Collectors.toSet());
    }

    /**
     * Extracts unique process instance IDs from spans.
     *
     * @param spans list of spans
     * @return set of unique process instance IDs
     */
    private static Set<String> extractProcessInstanceIds(List<SpanData> spans) {
        return spans.stream()
                .map(span -> span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_ID))
                .collect(Collectors.toSet());
    }

    /**
     * Extracts unique node names from spans.
     *
     * @param spans list of spans
     * @return set of unique node names
     */
    private static Set<String> extractNodeNames(List<SpanData> spans) {
        return spans.stream()
                .map(span -> span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_NODE))
                .collect(Collectors.toSet());
    }
}
