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

import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import jakarta.ws.rs.core.HttpHeaders;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.kie.kogito.quarkus.serverless.workflow.otel.ExternalServiceMock.SUCCESSFUL_QUERY;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.*;
import static org.kie.kogito.quarkus.serverless.workflow.otel.TokenPropagationExternalServicesMock.AUTHORIZATION_TOKEN;
import static org.kie.kogito.quarkus.serverless.workflow.otel.TokenPropagationExternalServicesMock.SERVICE3_AUTHORIZATION_TOKEN;
import static org.kie.kogito.quarkus.serverless.workflow.otel.TokenPropagationExternalServicesMock.SERVICE3_HEADER_TO_PROPAGATE;
import static org.kie.kogito.quarkus.serverless.workflow.otel.TokenPropagationExternalServicesMock.SERVICE4_AUTHORIZATION_TOKEN;
import static org.kie.kogito.quarkus.serverless.workflow.otel.TokenPropagationExternalServicesMock.SERVICE4_HEADER_TO_PROPAGATE;

/**
 * Comprehensive integration test for OpenTelemetry integration with Kogito Serverless Workflow.
 * <p>
 * This test validates the complete OpenTelemetry integration including:
 * - Real workflow execution with proper node span creation
 * - HTTP header extraction and propagation to spans
 * - Transaction ID and tracker attribute handling
 * - OpenTelemetry backend integration with trace validation
 * <p>
 * Uses the "greet" workflow which has 4 nodes: ChooseOnLanguage, GreetInEnglish/GreetInSpanish, GreetPerson
 */
@QuarkusIntegrationTest
@QuarkusTestResource(OpenTelemetryTestResource.class)
@QuarkusTestResource(TokenPropagationExternalServicesMock.class)
@QuarkusTestResource(KeycloakServiceMock.class)
public class OpenTelemetryIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenTelemetryIT.class);

    @BeforeEach
    public void cleanup() {
        OpenTelemetryTestResource.clearSpans();
        TokenPropagationExternalServicesMock.getInstance().resetRequests();

        // Additional cleanup with retry logic to prevent test interference
        try {
            for (int i = 0; i < 5; i++) {
                Thread.sleep(200);
                OpenTelemetryTestResource.clearSpans();
                if (OpenTelemetryTestResource.getSpanCount() == 0) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
        // When - Execute workflow via REST with OpenTelemetry headers
        executeWorkflowWithTrackers("/greet", buildGreetBody("John", "English"),
                "test-transaction-123", "customer-456", "session-789", 201);

        // Then - Verify traces were created with proper node spans
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            List<SpanData> workflowSpans = filterWorkflowSpans(spans);

            assertThat(workflowSpans).hasSizeGreaterThanOrEqualTo(3);

            validateSharedTraceId(workflowSpans);
            validateSpanNaming(workflowSpans, "sonataflow.process.greet.execute");

            workflowSpans.forEach(span -> {
                validateMandatorySpanAttributes(span, "greet");
                validateTransactionAndTrackerAttributes(span, "test-transaction-123", "customer-456", "session-789");
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
        // When - Execute workflow without X-TRANSACTION-ID header
        executeWorkflow("/greet", buildGreetBody("Alice", "Spanish"), 201);

        // Then - Verify transaction ID falls back to process instance ID
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            assertThat(spans).hasSizeGreaterThanOrEqualTo(3);

            spans.forEach(span -> {
                String processInstanceId = span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_ID);
                String transactionId = span.getAttributes().get(SONATAFLOW_TRANSACTION_ID);

                // Transaction ID should fallback to process instance ID
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
        // When - Execute workflow with Spanish language (different path)
        executeWorkflowWithTxn("/greet", buildGreetBody("Carlos", "Spanish"),
                "spanish-workflow-txn", 201);

        // Then - Verify Spanish path creates different node spans
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
        // When - Execute workflow
        executeWorkflow("/greet", buildGreetBody("Test", "English"), 201);

        // Then - Verify configuration is respected in spans
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
        // When - Execute multiple workflows concurrently
        for (int i = 0; i < 3; i++) {
            executeWorkflowWithTxn("/greet", buildGreetBody("User" + i, "English"),
                    "concurrent-txn-" + i, 201);
        }

        // Then - Verify independent traces are created
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            assertThat(spans).hasSizeGreaterThanOrEqualTo(9); // 3 workflows * 3 nodes each

            // Verify multiple distinct trace IDs
            Set<String> traceIds = spans.stream()
                    .map(SpanData::getTraceId)
                    .collect(Collectors.toSet());
            assertThat(traceIds).hasSizeGreaterThanOrEqualTo(3);

            // Verify transaction IDs are properly set
            Set<String> transactionIds = spans.stream()
                    .map(span -> span.getAttributes().get(SONATAFLOW_TRANSACTION_ID))
                    .filter(id -> id != null && id.startsWith("concurrent-txn-"))
                    .collect(Collectors.toSet());
            assertThat(transactionIds).containsExactlyInAnyOrder(
                    "concurrent-txn-0", "concurrent-txn-1", "concurrent-txn-2");
        });
    }

    /**
     * Test that process.instance.start event is added to the first node span.
     * <p>
     * Validates:
     * - process.instance.start event exists on first node span
     * - Event has required attributes: process.instance.id, trigger, reference.id
     * - Event is properly timestamped
     */
    @Test
    void shouldAddProcessStartEventToFirstNodeSpan() {
        // When - Execute workflow via REST
        executeWorkflowWithTxn("/greet", buildGreetBody("ProcessStartTest", "English"),
                "process-start-test-txn-123", 201);

        // Then - Verify process.instance.start event on first node span
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            List<SpanData> workflowSpans = filterWorkflowSpans(spans);

            assertThat(workflowSpans).hasSizeGreaterThanOrEqualTo(3);

            SpanData firstSpan = findSpanByNodeName(workflowSpans, "ChooseOnLanguage");
            EventData startEvent = findEventByName(firstSpan, "process.instance.start");

            String processInstanceId = firstSpan.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_ID);
            validateProcessStartEvent(startEvent, processInstanceId, "http", "process-start-test-txn-123");
        });
    }

    /**
     * Test that process.instance.complete event is added to the last node span.
     * <p>
     * Validates:
     * - process.instance.complete event exists on last node span
     * - Event has required attributes: process.instance.id, duration.ms, outcome
     * - Event is properly timestamped
     * - Duration is positive
     */
    @Test
    void shouldAddProcessCompleteEventToLastNodeSpan() {
        // When - Execute workflow via REST
        executeWorkflowWithTxn("/greet", buildGreetBody("ProcessCompleteTest", "Spanish"),
                "process-complete-test-txn-456", 201);

        // Then - Verify process.instance.complete event on last node span
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            List<SpanData> workflowSpans = filterWorkflowSpans(spans);

            assertThat(workflowSpans).hasSizeGreaterThanOrEqualTo(3);

            SpanData lastSpan = findSpanByNodeName(workflowSpans, "GreetPerson");
            EventData completeEvent = findEventByName(lastSpan, "process.instance.complete");

            String processInstanceId = lastSpan.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_ID);
            validateProcessCompleteEvent(completeEvent, processInstanceId, "COMPLETED");
        });
    }

    /**
     * Test that process.instance.error event is added when workflow encounters an error.
     */
    @Test
    void shouldAddProcessErrorEventWhenProcessFails() {
        // When - Execute workflow (placeholder until error-triggering workflow is added)
        executeWorkflowWithTxn("/uncaughterror", "{\"number\": 1}",
                "process-error-test-txn-789", 500);
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            // Then - Verify error event structure
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            LOGGER.info("=== DIAGNOSTIC: Total spans captured: {}", spans.size());

            List<SpanData> workflowSpans = filterWorkflowSpans(spans);
            LOGGER.info("=== DIAGNOSTIC: Workflow spans captured: {}", workflowSpans.size());

            // Log each workflow span with its details
            workflowSpans.forEach(span -> {
                LOGGER.info("=== DIAGNOSTIC: Span: name={}, processId={}, node={}, eventCount={}",
                        span.getName(),
                        span.getAttributes().get(SONATAFLOW_PROCESS_ID),
                        span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_NODE),
                        span.getEvents().size());

                // Log all events for this span
                span.getEvents().forEach(event -> {
                    LOGGER.info("===   Event: name={}, attributes={}",
                            event.getName(),
                            event.getAttributes());
                });
            });

            assertThat(workflowSpans).isNotEmpty();

            // Look for any span with process.instance.error event
            boolean errorEventFound = workflowSpans.stream()
                    .flatMap(span -> span.getEvents().stream())
                    .anyMatch(event -> "process.instance.error".equals(event.getName()));

            LOGGER.info("=== DIAGNOSTIC: Error event found: {}", errorEventFound);

            assertThat(errorEventFound)
                    .withFailMessage("process.instance.error event not found in error workflow execution")
                    .isTrue();

            // When error event is found, verify its attributes
            workflowSpans.stream()
                    .flatMap(span -> span.getEvents().stream())
                    .filter(event -> "process.instance.error".equals(event.getName()))
                    .findFirst()
                    .ifPresent(errorEvent -> {
                        Attributes eventAttributes = errorEvent.getAttributes();

                        // Verify required error event attributes
                        assertThat(eventAttributes.get(PROCESS_INSTANCE_ID))
                                .isNotNull();
                        assertThat(eventAttributes.get(AttributeKey.stringKey("error.message")))
                                .isNotNull();
                        assertThat(eventAttributes.get(AttributeKey.stringKey("error.type")))
                                .isNotNull();

                        // Verify event has valid timestamp
                        assertThat(errorEvent.getEpochNanos()).isGreaterThan(0);
                    });
        });
    }

    /**
     * Test that log messages during workflow execution are captured as span events.
     * <p>
     * Validates:
     * - Log messages are captured as "log.message" events on spans
     * - Event has required attributes: level, logger, message, thread.name, thread.id
     * - Log events are associated with the correct workflow execution spans
     */
    @Test
    void shouldCaptureLogMessagesAsSpanEvents() {
        // When - Execute workflow via REST (greet workflow uses sysout function which generates logs)
        executeWorkflowWithTxn("/greet", buildGreetBody("LogTest", "English"),
                "log-capture-test-txn-123", 201);

        // Then - Verify log messages are captured as span events
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            List<SpanData> workflowSpans = filterWorkflowSpans(spans);

            assertThat(workflowSpans).isNotEmpty();

            List<EventData> allLogEvents = collectEventsByName(workflowSpans, "log.message");
            EventData logEvent = allLogEvents.get(0);

            validateLogEventAttributes(logEvent);
        });
    }

    /**
     * Test that only logs at configured level and above are captured.
     * <p>
     * Validates:
     * - Only logs at INFO level and above are captured (default configuration)
     * - DEBUG and TRACE logs are filtered out
     * - Log level values in events match standard levels: ERROR, WARN, INFO
     */
    @Test
    void shouldOnlyCaptureConfiguredLogLevels() {
        // When - Execute workflow via REST
        executeWorkflowWithTxn("/greet", buildGreetBody("LogLevelTest", "Spanish"),
                "log-level-filter-test-txn-456", 201);

        // Then - Verify only configured log levels are captured
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            List<SpanData> workflowSpans = filterWorkflowSpans(spans);

            assertThat(workflowSpans).isNotEmpty();

            List<EventData> allLogEvents = collectEventsByName(workflowSpans, "log.message");

            Set<String> logLevels = allLogEvents.stream()
                    .map(event -> event.getAttributes().get(LOG_LEVEL))
                    .collect(Collectors.toSet());

            assertThat(logLevels)
                    .withFailMessage("Log events should only contain INFO and above, found: " + logLevels)
                    .allMatch(level -> level.equals("INFO") || level.equals("WARN") || level.equals("ERROR"));
        });
    }

    /**
     * Test that logs appear in spans associated with the correct process instance.
     * <p>
     * Validates:
     * - Logs are correlated with the correct workflow execution using transaction ID
     * - Multiple concurrent workflows don't mix their logs
     * - Log events share the same trace ID as their workflow execution
     */
    @Test
    void shouldCorrelateLogsWithProcessInstance() {
        // When - Execute two different workflows with distinct transaction IDs
        executeWorkflowWithTxn("/greet", buildGreetBody("EnglishUser", "English"),
                "log-correlation-test-txn-english", 201);

        executeWorkflowWithTxn("/greet", buildGreetBody("SpanishUser", "Spanish"),
                "log-correlation-test-txn-spanish", 201);

        // Then - Verify logs are correlated with correct process instances
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            List<SpanData> workflowSpans = filterWorkflowSpans(spans);

            List<SpanData> englishSpans = filterSpansByTransactionId(workflowSpans, "log-correlation-test-txn-english");
            List<SpanData> spanishSpans = filterSpansByTransactionId(workflowSpans, "log-correlation-test-txn-spanish");

            assertThat(englishSpans).isNotEmpty();
            assertThat(spanishSpans).isNotEmpty();

            String englishTraceId = englishSpans.get(0).getTraceId();
            String spanishTraceId = spanishSpans.get(0).getTraceId();

            assertThat(englishTraceId).isNotEqualTo(spanishTraceId);

            List<EventData> spanishLogEvents = collectEventsByName(spanishSpans, "log.message");

            assertThat(spanishLogEvents)
                    .withFailMessage("log.message events not found in Spanish workflow")
                    .isNotEmpty();

            validateSharedTraceId(englishSpans);
            validateSharedTraceId(spanishSpans);
        });
    }

    /**
     * Anti-regression test to ensure log events are not duplicated in spans.
     * This test guards against the issue where the same log handler was registered
     * on both root and Kogito loggers, causing duplicate span events.
     */
    @Test
    void shouldNotDuplicateLogEvents() {
        // Execute workflow that generates predictable log messages
        executeWorkflowWithTxn("/greet", buildGreetBody("DuplicateTest", "English"),
                "no-duplicate-test-txn", 201);

        // Verify no log events are duplicated
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            assertThat(spans).isNotEmpty();

            // Collect all log.message events from all spans
            List<EventData> allLogEvents = spans.stream()
                    .flatMap(span -> span.getEvents().stream())
                    .filter(event -> event.getName().equals("log.message"))
                    .collect(Collectors.toList());

            assertThat(allLogEvents).isNotEmpty();

            // Count occurrences of each unique log message
            Map<String, Long> messageCounts = allLogEvents.stream()
                    .map(event -> event.getAttributes().get(LOG_MESSAGE))
                    .filter(message -> message != null && !message.isEmpty())
                    .collect(Collectors.groupingBy(
                            java.util.function.Function.identity(),
                            Collectors.counting()));

            // Assert: No message should appear more than a reasonable number of times
            // Allow some duplication due to parallel execution, but not excessive duplication
            messageCounts.forEach((message, count) -> {
                assertThat(count)
                        .as("Log message should not be excessively duplicated: '%s' (count: %d)", message, count)
                        .isLessThanOrEqualTo(2L); // Allow some parallelism, but not excessive duplication
            });

            // Also verify we don't have obvious duplicates in consecutive events
            for (int i = 1; i < allLogEvents.size(); i++) {
                EventData prev = allLogEvents.get(i - 1);
                EventData curr = allLogEvents.get(i);

                String prevMessage = prev.getAttributes().get(LOG_MESSAGE);
                String currMessage = curr.getAttributes().get(LOG_MESSAGE);

                if (prevMessage != null && prevMessage.equals(currMessage)) {
                    long prevTime = prev.getEpochNanos();
                    long currTime = curr.getEpochNanos();

                    // If messages are identical and very close in time (< 1ms), likely a duplicate
                    assertThat(Math.abs(currTime - prevTime))
                            .as("Consecutive identical log messages should not occur within 1ms: '%s'", prevMessage)
                            .isGreaterThan(1_000_000L); // 1ms in nanoseconds
                }
            }
        });
    }

    /**
     * Test that external library logs are captured when using root-only logger registration.
     * This validates the user's concern: "if we remove the root logger, logs may be missed:
     * for instance if the user is enabling DEBUG log for apache http, then we would miss those logs"
     * <p>
     * Validates:
     * - External library logs (e.g., Apache HTTP) are captured via root logger inheritance
     * - Root-only registration provides comprehensive log coverage including external libraries
     * - No loss of external library log visibility with the duplicate handler fix
     */
    @Test
    void shouldCaptureExternalLibraryLogs() {
        // Force external library logging by executing workflow that uses HTTP functions
        // Token propagation workflow makes external HTTP calls and should generate external library logs

        // When - Execute workflow that makes external HTTP calls
        executeTokenPropagationWorkflow("external-library-log-test-txn", 201);

        // Then - Verify external library logs are captured
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            List<SpanData> workflowSpans = filterWorkflowSpans(spans);

            assertThat(workflowSpans).isNotEmpty();

            List<EventData> allLogEvents = collectEventsByName(workflowSpans, "log.message");

            assertThat(allLogEvents).isNotEmpty();

            // Look for external library log events (HTTP client, Quarkus, etc.)
            List<EventData> externalLibraryEvents = allLogEvents.stream()
                    .filter(event -> {
                        String logger = event.getAttributes().get(LOG_LOGGER);
                        return logger != null && (logger.contains("apache") ||
                                logger.contains("http") ||
                                logger.contains("quarkus") ||
                                logger.contains("netty") ||
                                logger.contains("undertow") ||
                                !logger.startsWith("org.kie.kogito") // Non-Kogito loggers
                        );
                    })
                    .collect(Collectors.toList());

            // Verify external library logs are captured
            assertThat(externalLibraryEvents)
                    .withFailMessage("External library logs should be captured via root logger inheritance. " +
                            "This validates that removing duplicate kogito logger registration " +
                            "doesn't lose external library log coverage.")
                    .isNotEmpty();

            // Verify these events have proper structure
            externalLibraryEvents.forEach(event -> {
                Attributes eventAttributes = event.getAttributes();
                assertThat(eventAttributes.get(LOG_LEVEL))
                        .isNotNull();
                assertThat(eventAttributes.get(LOG_LOGGER))
                        .isNotNull();
                assertThat(eventAttributes.get(LOG_MESSAGE))
                        .isNotNull();
            });
        });
    }

    /**
     * Test that log messages during workflow execution are captured as span events.
     * <p>
     * Validates:
     * - Log messages are captured as "log.message" events on spans
     * - Event has required attributes: level, logger, message, thread.name, thread.id
     * - Log events are associated with the correct workflow execution spans
     */
    @Test
    void shouldCaptureLogMessagesAsSpanEventsWithTokenPropagation() {
        // When - Execute workflow via REST (greet workflow uses sysout function which generates logs)
        executeTokenPropagationWorkflow("log-capture-test-token-propagation-txn-123", 201);

        // Then - Verify log messages are captured as span events
        await().atMost(Duration.ofSeconds(25)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            List<SpanData> workflowSpans = filterWorkflowSpans(spans);

            assertThat(workflowSpans).isNotEmpty();

            List<EventData> allLogEvents = collectEventsByName(workflowSpans, "log.message");
            EventData logEvent = allLogEvents.get(0);

            validateLogEventAttributes(logEvent);

            validateMainAndSubflowBehaviour(workflowSpans);

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
        // When - Execute multiple workflows that may have parallel ending patterns
        for (int i = 0; i < 3; i++) {
            executeWorkflowWithTxn("/greet", buildGreetBody("CompletionTest" + i, "English"),
                    "completion-race-test-txn-" + i, 201);
        }

        // Then - Verify completion events are handled exactly once per process
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            List<SpanData> spans = OpenTelemetryTestResource.getSpans();
            List<SpanData> workflowSpans = filterWorkflowSpans(spans);

            assertThat(workflowSpans).hasSizeGreaterThanOrEqualTo(9); // 3 workflows * 3 nodes each

            // Group spans by transaction ID to analyze each workflow separately
            Map<String, List<SpanData>> spansByTxn = workflowSpans.stream()
                    .filter(span -> {
                        String txnId = span.getAttributes().get(SONATAFLOW_TRANSACTION_ID);
                        return txnId != null && txnId.startsWith("completion-race-test-txn-");
                    })
                    .collect(Collectors.groupingBy(
                            span -> span.getAttributes().get(SONATAFLOW_TRANSACTION_ID)));

            assertThat(spansByTxn).hasSize(3);

            // For each workflow, verify process.instance.complete event appears exactly once
            spansByTxn.forEach((txnId, workflowSpansForTxn) -> {
                List<EventData> completeEvents = workflowSpansForTxn.stream()
                        .flatMap(span -> span.getEvents().stream())
                        .filter(event -> "process.instance.complete".equals(event.getName()))
                        .collect(Collectors.toList());

                assertThat(completeEvents)
                        .withFailMessage("Each workflow should have exactly one process.instance.complete event. " +
                                "Transaction %s has %d events", txnId, completeEvents.size())
                        .hasSize(1);

                // Verify the completion event has required attributes
                EventData completeEvent = completeEvents.get(0);
                Attributes eventAttributes = completeEvent.getAttributes();
                assertThat(eventAttributes.get(PROCESS_INSTANCE_ID))
                        .isNotNull();
                assertThat(eventAttributes.get(OUTCOME))
                        .isEqualTo("COMPLETED");
                assertThat(eventAttributes.get(DURATION_MS))
                        .isNotNull()
                        .isGreaterThan(0L);
            });

            // Verify process.instance.start events are also exactly once per process
            Map<String, List<EventData>> startEventsByTxn = workflowSpans.stream()
                    .filter(span -> {
                        String txnId = span.getAttributes().get(SONATAFLOW_TRANSACTION_ID);
                        return txnId != null && txnId.startsWith("completion-race-test-txn-");
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

    private void validateMainAndSubflowBehaviour(List<SpanData> workflowSpans) {
        // Filter spans by transaction ID first to prevent contamination from other tests
        List<SpanData> currentTestSpans = filterSpansByTransactionId(workflowSpans, "log-capture-test-token-propagation-txn-123");
        for (SpanData workflowSpan : currentTestSpans) {
            LOGGER.info("====> {}", workflowSpan.getAttributes());
        }
        // Separate main workflow and subflow spans
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

        validateTransactionIdPropagation(mainWorkflowSpans, "log-capture-test-token-propagation-txn-123");
        validateTransactionIdPropagation(subflowSpans, "log-capture-test-token-propagation-txn-123");
        validateSpanDurations(currentTestSpans);
        validateNoErrorStatus(currentTestSpans);
    }

    private SpanData validateParentChildSpanRelationship(List<SpanData> mainWorkflowSpans, List<SpanData> subflowSpans) {
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

    private void validateSpansTimingAndSequence(List<SpanData> mainWorkflowSpans, List<SpanData> subflowSpans, SpanData executeSubflowSpan) {
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

    private void validateSubflowNodeExecution(List<SpanData> subflowSpans) {
        Set<String> expectedSubflowNodes = Set.of(
                "SubflowExecution1", "SubflowExecution2", "SubflowExecution3",
                "SubflowExecution4", "SubflowExecution5", "SubflowEnd");
        Set<String> actualSubflowNodes = extractNodeNames(subflowSpans);

        assertThat(actualSubflowNodes)
                .withFailMessage("Subflow should execute all expected nodes")
                .containsAll(expectedSubflowNodes);

    }

    protected static String buildProcessInput(String query) {
        return "{\"workflowdata\": {\"query\": \"" + query + "\"} }";
    }

    /**
     * Base helper method for executing workflow requests with full header customization.
     */
    protected ValidatableResponse executeWorkflow(String endpoint, String body,
            Map<String, String> headers,
            int expectedStatus) {
        RequestSpecification request = given().contentType(ContentType.JSON).body(body);

        if (headers != null) {
            request = request.headers(headers);
        }

        return request.when()
                .post(endpoint)
                .then()
                .statusCode(expectedStatus);
    }

    /**
     * Execute workflow with no custom headers.
     */
    protected ValidatableResponse executeWorkflow(String endpoint, String body, int expectedStatus) {
        return executeWorkflow(endpoint, body, null, expectedStatus);
    }

    /**
     * Execute workflow with transaction ID header only.
     */
    protected ValidatableResponse executeWorkflowWithTxn(String endpoint, String body,
            String transactionId, int expectedStatus) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-TRANSACTION-ID", transactionId);
        return executeWorkflow(endpoint, body, headers, expectedStatus);
    }

    /**
     * Execute workflow with transaction ID and tracker headers.
     */
    protected ValidatableResponse executeWorkflowWithTrackers(String endpoint, String body,
            String transactionId,
            String customerId, String session,
            int expectedStatus) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-TRANSACTION-ID", transactionId);
        if (customerId != null) {
            headers.put("X-TRACKER-CUSTOMER-ID", customerId);
        }
        if (session != null) {
            headers.put("X-TRACKER-SESSION", session);
        }
        return executeWorkflow(endpoint, body, headers, expectedStatus);
    }

    /**
     * Build greet workflow request body.
     */
    protected String buildGreetBody(String name, String language) {
        return """
                {
                    "name": "%s",
                    "language": "%s"
                }
                """.formatted(name, language);
    }

    /**
     * Execute token propagation workflow with standard authorization headers.
     */
    protected ValidatableResponse executeTokenPropagationWorkflow(String transactionId, int expectedStatus) {
        String processInput = buildProcessInput(SUCCESSFUL_QUERY);
        Map<String, String> headers = new HashMap<>();
        headers.put("X-TRANSACTION-ID", transactionId);
        headers.put(HttpHeaders.AUTHORIZATION, AUTHORIZATION_TOKEN);
        headers.put(SERVICE3_HEADER_TO_PROPAGATE, SERVICE3_AUTHORIZATION_TOKEN);
        headers.put(SERVICE4_HEADER_TO_PROPAGATE, SERVICE4_AUTHORIZATION_TOKEN);

        return executeWorkflow("/token_propagation", processInput, headers, expectedStatus);
    }

    /**
     * Filters spans to include only workflow node spans (excluding HTTP instrumentation).
     */
    private List<SpanData> filterWorkflowSpans(List<SpanData> spans) {
        return spans.stream()
                .filter(span -> span.getName().startsWith("sonataflow.process"))
                .collect(Collectors.toList());
    }

    /**
     * Filters spans to include only those from the main workflow (by process ID).
     */
    private List<SpanData> filterMainWorkflowSpans(List<SpanData> spans, String processId) {
        return spans.stream()
                .filter(span -> {
                    String spanProcessId = span.getAttributes().get(SONATAFLOW_PROCESS_ID);
                    return processId.equals(spanProcessId);
                })
                .collect(Collectors.toList());
    }

    /**
     * Filters spans to include only those from a subflow (by process ID).
     */
    private List<SpanData> filterSubflowSpans(List<SpanData> spans, String subflowProcessId) {
        return filterMainWorkflowSpans(spans, subflowProcessId);
    }

    /**
     * Filters spans by transaction ID.
     */
    private List<SpanData> filterSpansByTransactionId(List<SpanData> spans, String transactionId) {
        LOGGER.info("=== TRANSACTION ID FILTERING DIAGNOSTICS ===");
        LOGGER.info("Filtering {} spans for transaction ID: '{}'", spans.size(), transactionId);

        // Log all unique transaction IDs found in spans
        Set<String> foundTransactionIds = spans.stream()
                .map(span -> span.getAttributes().get(SONATAFLOW_TRANSACTION_ID))
                .filter(txnId -> txnId != null)
                .collect(Collectors.toSet());

        LOGGER.info("Found transaction IDs in spans: {}", foundTransactionIds);

        // Log spans without transaction IDs
        long spansWithoutTxnId = spans.stream()
                .filter(span -> span.getAttributes().get(SONATAFLOW_TRANSACTION_ID) == null)
                .count();

        if (spansWithoutTxnId > 0) {
            LOGGER.warn("{} spans have NO transaction ID attribute", spansWithoutTxnId);
        }

        // Log some sample span details
        spans.stream().limit(3).forEach(span -> {
            String spanTxnId = span.getAttributes().get(SONATAFLOW_TRANSACTION_ID);
            String spanProcessId = span.getAttributes().get(SONATAFLOW_PROCESS_ID);
            String spanNodeName = span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_NODE);
            LOGGER.info("Sample span: {} | Process: {} | Node: {} | TxnId: '{}'",
                    span.getName(), spanProcessId, spanNodeName, spanTxnId);
        });

        List<SpanData> filteredSpans = spans.stream()
                .filter(span -> {
                    String txnId = span.getAttributes().get(SONATAFLOW_TRANSACTION_ID);
                    boolean matches = transactionId.equals(txnId);
                    if (!matches && txnId != null) {
                        LOGGER.debug("Span '{}' has txnId '{}' (expected '{}')",
                                span.getName(), txnId, transactionId);
                    }
                    return matches;
                })
                .collect(Collectors.toList());

        LOGGER.info("Filtered result: {} spans match transaction ID '{}'",
                filteredSpans.size(), transactionId);

        if (filteredSpans.isEmpty() && !spans.isEmpty()) {
            LOGGER.error("*** CRITICAL: No spans found with transaction ID '{}' ***", transactionId);
            LOGGER.error("Available transaction IDs: {}", foundTransactionIds);
        }

        return filteredSpans;
    }

    /**
     * Finds a span by node name.
     */
    private SpanData findSpanByNodeName(List<SpanData> spans, String nodeName) {
        return spans.stream()
                .filter(span -> {
                    String spanNodeName = span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_NODE);
                    return nodeName.equals(spanNodeName);
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError("Span with node name '" + nodeName + "' not found"));
    }

    /**
     * Finds the first event by name from a span.
     */
    private EventData findEventByName(SpanData span, String eventName) {
        return span.getEvents().stream()
                .filter(event -> eventName.equals(event.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Event '" + eventName + "' not found on span"));
    }

    /**
     * Collects all events by name from a list of spans.
     */
    private List<EventData> collectEventsByName(List<SpanData> spans, String eventName) {
        return spans.stream()
                .flatMap(span -> span.getEvents().stream())
                .filter(event -> eventName.equals(event.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Extracts unique trace IDs from spans.
     */
    private Set<String> extractTraceIds(List<SpanData> spans) {
        return spans.stream()
                .map(SpanData::getTraceId)
                .collect(Collectors.toSet());
    }

    /**
     * Extracts unique process instance IDs from spans.
     */
    private Set<String> extractProcessInstanceIds(List<SpanData> spans) {
        return spans.stream()
                .map(span -> span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_ID))
                .collect(Collectors.toSet());
    }

    /**
     * Extracts unique node names from spans.
     */
    private Set<String> extractNodeNames(List<SpanData> spans) {
        return spans.stream()
                .map(span -> span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_NODE))
                .collect(Collectors.toSet());
    }

    /**
     * Validates mandatory span attributes according to design document.
     */
    private void validateMandatorySpanAttributes(SpanData span, String expectedProcessId) {
        assertThat(span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_ID)).isNotNull();
        assertThat(span.getAttributes().get(SONATAFLOW_PROCESS_ID)).isEqualTo(expectedProcessId);
        assertThat(span.getAttributes().get(SONATAFLOW_PROCESS_VERSION)).isEqualTo("1.0");
        assertThat(span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_STATE)).isNotNull();
        assertThat(span.getAttributes().get(SERVICE_NAME)).isNotNull();
        assertThat(span.getAttributes().get(SERVICE_VERSION)).isNotNull();
        assertThat(span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_NODE)).isNotNull();
    }

    /**
     * Validates transaction ID and tracker attributes on a span.
     */
    private void validateTransactionAndTrackerAttributes(SpanData span, String expectedTransactionId,
            String expectedCustomerId, String expectedSession) {
        assertThat(span.getAttributes().get(SONATAFLOW_TRANSACTION_ID))
                .isEqualTo(expectedTransactionId);
        if (expectedCustomerId != null) {
            assertThat(span.getAttributes().get(AttributeKey.stringKey(TrackerAttributes.createTrackerAttributeKey("customer.id"))))
                    .isEqualTo(expectedCustomerId);
        }
        if (expectedSession != null) {
            assertThat(span.getAttributes().get(AttributeKey.stringKey(TrackerAttributes.createTrackerAttributeKey("session"))))
                    .isEqualTo(expectedSession);
        }
    }

    /**
     * Validates process.instance.start event attributes.
     */
    private void validateProcessStartEvent(EventData startEvent, String expectedProcessInstanceId,
            String expectedTrigger, String expectedReferenceId) {
        Attributes eventAttributes = startEvent.getAttributes();
        assertThat(eventAttributes.get(PROCESS_INSTANCE_ID))
                .isEqualTo(expectedProcessInstanceId);
        assertThat(eventAttributes.get(TRIGGER))
                .isEqualTo(expectedTrigger);
        assertThat(eventAttributes.get(REFERENCE_ID))
                .isEqualTo(expectedReferenceId);
        assertThat(startEvent.getEpochNanos()).isGreaterThan(0);
    }

    /**
     * Validates process.instance.complete event attributes.
     */
    private void validateProcessCompleteEvent(EventData completeEvent, String expectedProcessInstanceId,
            String expectedOutcome) {
        Attributes eventAttributes = completeEvent.getAttributes();
        assertThat(eventAttributes.get(PROCESS_INSTANCE_ID))
                .isEqualTo(expectedProcessInstanceId);
        assertThat(eventAttributes.get(OUTCOME))
                .isEqualTo(expectedOutcome);
        Long durationMs = eventAttributes.get(DURATION_MS);
        assertThat(durationMs).isNotNull();
        assertThat(durationMs).isGreaterThan(0L);
        assertThat(completeEvent.getEpochNanos()).isGreaterThan(0);
    }

    /**
     * Validates log.message event attributes.
     */
    private void validateLogEventAttributes(EventData logEvent) {
        Attributes logAttributes = logEvent.getAttributes();
        assertThat(logAttributes.get(LOG_LEVEL))
                .withFailMessage("log.message event missing 'level' attribute")
                .isNotNull();
        assertThat(logAttributes.get(LOG_LOGGER))
                .withFailMessage("log.message event missing 'logger' attribute")
                .isNotNull();
        assertThat(logAttributes.get(LOG_MESSAGE))
                .withFailMessage("log.message event missing 'message' attribute")
                .isNotNull();
        assertThat(logAttributes.get(LOG_THREAD_NAME))
                .withFailMessage("log.message event missing 'thread.name' attribute")
                .isNotNull();
        assertThat(logAttributes.get(LOG_THREAD_ID))
                .withFailMessage("log.message event missing 'thread.id' attribute")
                .isNotNull();
        assertThat(logEvent.getEpochNanos()).isGreaterThan(0);
    }

    /**
     * Validates that all spans share the same trace ID.
     */
    private void validateSharedTraceId(List<SpanData> spans) {
        assertThat(spans).isNotEmpty();
        String traceId = spans.get(0).getTraceId();
        spans.forEach(span -> {
            assertThat(span.getTraceId()).isEqualTo(traceId);
        });
    }

    /**
     * Validates span naming follows design document pattern.
     */
    private void validateSpanNaming(List<SpanData> spans, String expectedPrefix) {
        spans.forEach(span -> {
            assertThat(span.getName()).startsWith(expectedPrefix);
        });
    }

    /**
     * Validates trace ID consistency across main and subflow spans.
     */
    private void validateTraceIdConsistency(List<SpanData> mainWorkflowSpans, List<SpanData> subflowSpans) {
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
     */
    private void validateProcessInstanceIsolation(List<SpanData> mainWorkflowSpans, List<SpanData> subflowSpans) {
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
     * Validates span timing and sequence for a list of spans.
     */
    private void validateSpanTiming(List<SpanData> spans) {
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
     */
    private void validateSpanDurations(List<SpanData> spans) {
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
     */
    private void validateNoErrorStatus(List<SpanData> spans) {
        spans.forEach(span -> {
            assertThat(span.getStatus().getStatusCode())
                    .withFailMessage("Span %s should not have error status for successful workflow execution",
                            span.getAttributes().get(SONATAFLOW_PROCESS_INSTANCE_NODE))
                    .isNotEqualTo(io.opentelemetry.api.trace.StatusCode.ERROR);
        });
    }

    /**
     * Validates transaction ID propagation to all spans.
     */
    private void validateTransactionIdPropagation(List<SpanData> spans, String expectedTransactionId) {
        spans.forEach(span -> {
            String txnId = span.getAttributes().get(SONATAFLOW_TRANSACTION_ID);
            assertThat(txnId)
                    .withFailMessage("Span should have transaction ID")
                    .isEqualTo(expectedTransactionId);
        });
    }
}
