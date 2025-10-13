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
package org.kie.kogito.test.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Base class for process instance logging tests providing common functionality.
 * This class eliminates code duplication between different logging test implementations.
 */
public abstract class ProcessInstanceLoggingTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInstanceLoggingTestBase.class);

    /**
     * Get the configured log file path.
     *
     * @return Path to the Quarkus log file
     */
    protected Path getLogFilePath() {
        String logPath = System.getProperty("quarkus.log.file.path", "target/quarkus.log");
        return Paths.get(logPath);
    }

    /**
     * Clear the log file to start with a clean slate.
     *
     * @param logFile Path to the log file
     * @throws IOException if file operations fail
     */
    protected void clearLogFile(Path logFile) throws IOException {
        if (Files.exists(logFile)) {
            Files.write(logFile, new byte[0]);
        }
    }

    /**
     * Wait for logs to be flushed to disk.
     *
     * @throws InterruptedException if thread is interrupted
     */
    protected void waitForLogFlush() throws InterruptedException {
        Thread.sleep(1000);
    }

    /**
     * Wait for logs to be flushed to disk with custom duration.
     *
     * @param millis milliseconds to wait
     * @throws InterruptedException if thread is interrupted
     */
    protected void waitForLogFlush(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    /**
     * Parse log file and perform basic validation.
     *
     * @param logFile Path to log file
     * @return Parsed log entries
     * @throws IOException if file operations fail
     */
    protected List<ProcessInstanceLogAnalyzer.LogEntry> parseAndValidateLogs(Path logFile) throws IOException {
        List<ProcessInstanceLogAnalyzer.LogEntry> entries =
                ProcessInstanceLogAnalyzer.parseLogFileWithMultilineSupport(logFile);

        assertThat(entries).as("Should have log entries").isNotEmpty();
        ProcessInstanceLogAnalyzer.validateLogFormat(entries);

        return entries;
    }

    /**
     * Calculate and log statistics for debugging.
     *
     * @param entries Log entries to analyze
     */
    protected void logStatistics(List<ProcessInstanceLogAnalyzer.LogEntry> entries) {
        ProcessInstanceLogAnalyzer.LogStatistics stats =
                ProcessInstanceLogAnalyzer.calculateStatistics(entries);
        LOGGER.info("Log analysis: {}", stats);
    }

    /**
     * Validate that each process instance has distinct log entries.
     *
     * @param logFile Path to log file
     * @param processInstanceIds Process instance IDs to validate
     * @throws IOException if file operations fail
     */
    protected void validateProcessInstanceLogs(Path logFile, String... processInstanceIds) throws IOException {
        List<ProcessInstanceLogAnalyzer.LogEntry> entries = parseAndValidateLogs(logFile);

        Map<String, List<ProcessInstanceLogAnalyzer.LogEntry>> entriesByProcess =
                ProcessInstanceLogAnalyzer.groupByProcessInstance(entries);

        ProcessInstanceLogAnalyzer.validateProcessInstanceSegregation(
                entriesByProcess, Arrays.asList(processInstanceIds));

        logStatistics(entries);

        // Verify that all process instances generated logs with their respective IDs
        for (String processInstanceId : processInstanceIds) {
            List<ProcessInstanceLogAnalyzer.LogEntry> processLogs = entriesByProcess.get(processInstanceId);
            assertThat(processLogs)
                    .as("Process instance " + processInstanceId + " should have dedicated log entries")
                    .isNotEmpty();
        }

        LOGGER.info("Process instance logging validation completed for {} process instances", processInstanceIds.length);
    }

    /**
     * Validate that general context logs (without process instance ID) exist.
     *
     * @param logFile Path to log file
     * @throws IOException if file operations fail
     */
    protected void validateGeneralContextLogs(Path logFile) throws IOException {
        // Parse log file using ProcessInstanceLogAnalyzer
        List<ProcessInstanceLogAnalyzer.LogEntry> entries =
                ProcessInstanceLogAnalyzer.parseLogFileWithMultilineSupport(logFile);

        LOGGER.info("ProcessInstanceLogAnalyzer parsed {} entries from log file", entries.size());

        if (entries.isEmpty()) {
            // If no structured entries found, fall back to raw log line analysis
            LOGGER.info("No structured log entries found, checking raw log lines");
            List<String> logLines = Files.readAllLines(logFile);
            LOGGER.info("Analyzing {} raw log lines for general context patterns", logLines.size());

            // Debug: Test the regex pattern against the first log line
            if (!logLines.isEmpty()) {
                String firstLine = logLines.get(0);
                LOGGER.info("First log line: '{}'", firstLine);

                // Test if the line matches the expected pattern
                Pattern pattern = Pattern.compile(
                        "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\|" + // Group 1: timestamp
                                "(\\w+)\\|" + // Group 2: level
                                "([^\\|]*)\\|" + // Group 3: processInstanceId (empty for general)
                                "([^\\|]+)\\|" + // Group 4: logger
                                "(.*)" // Group 5: message (may include newlines)
                );
                Matcher matcher = pattern.matcher(firstLine);
                LOGGER.info("Regex pattern matches: {}", matcher.matches());
                if (matcher.matches()) {
                    LOGGER.info("Groups: timestamp='{}', level='{}', processInstanceId='{}', logger='{}', message='{}'",
                            matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                }
            }

            // Look for logs with empty context (pattern: timestamp|LEVEL||logger|message)
            // Simplified approach: count lines that contain || indicating empty process instance ID
            long generalContextLogs = logLines.stream()
                    .filter(line -> line.contains("||"))
                    .count();
            assertThat(generalContextLogs).as("Should have general context logs").isGreaterThan(0);
        } else {
            // Validate log format if we have structured entries
            ProcessInstanceLogAnalyzer.validateLogFormat(entries);

            long generalContextLogs = entries.stream()
                    .filter(ProcessInstanceLogAnalyzer.LogEntry::isGeneralContext)
                    .count();

            LOGGER.info("Found {} general context logs", generalContextLogs);

            // Debug: Log the first few entries to understand what's being parsed
            LOGGER.info("First 3 entries parsed:");
            entries.stream().limit(3).forEach(entry -> LOGGER.info("Entry: processInstanceId='{}', level='{}', logger='{}'",
                    entry.processInstanceId, entry.level, entry.logger));

            assertThat(generalContextLogs).as("Should have general context logs").isGreaterThan(0);
        }
    }

    /**
     * Execute a workflow and wait for its completion.
     *
     * @param workflowPath REST path for the workflow (e.g., "/greet")
     * @param workflowInput JSON input as String
     * @param timeout Maximum time to wait for completion
     * @return Process instance ID
     */
    protected String executeWorkflowAndWaitForCompletion(String workflowPath, String workflowInput, Duration timeout) {
        JsonPath jsonPath = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(workflowInput)
                .when()
                .post(workflowPath)
                .then()
                .statusCode(201)
                .extract()
                .jsonPath();

        String processInstanceId = jsonPath.getString("id");
        assertThat(processInstanceId).isNotBlank();

        waitForWorkflowCompletion(workflowPath, processInstanceId, timeout);

        return processInstanceId;
    }

    /**
     * Wait for workflow completion using polling.
     *
     * @param workflowPath REST path for the workflow
     * @param processInstanceId Process instance ID
     * @param timeout Maximum time to wait
     */
    protected void waitForWorkflowCompletion(String workflowPath, String processInstanceId, Duration timeout) {
        await()
                .atMost(timeout)
                .pollInterval(Duration.ofMillis(500))
                .until(() -> {
                    try {
                        int statusCode = given()
                                .contentType(ContentType.JSON)
                                .accept(ContentType.JSON)
                                .when()
                                .get(workflowPath + "/" + processInstanceId)
                                .then()
                                .extract()
                                .statusCode();
                        return statusCode == 404; // Completed and cleaned up
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    /**
     * Build input for greet workflow.
     *
     * @param name The name parameter
     * @param language The language parameter
     * @return JSON input string
     */
    protected String buildGreetWorkflowInput(String name, String language) {
        return String.format("{\"name\": \"%s\", \"language\": \"%s\"}", name, language);
    }

    /**
     * Build input for parallel workflow.
     *
     * @param numCompleted The numCompleted parameter
     * @return JSON input string
     */
    protected String buildParallelWorkflowInput(int numCompleted) {
        return String.format("{\"numCompleted\": %d}", numCompleted);
    }

    /**
     * Build empty input for simple workflows like helloworld.
     *
     * @return Empty JSON object string
     */
    protected String buildEmptyWorkflowInput() {
        return "{}";
    }

    /**
     * Build input for token exchange workflow.
     *
     * @param query The query parameter
     * @return JSON input string
     */
    protected String buildTokenExchangeWorkflowInput(String query) {
        return "{\"workflowdata\": {\"query\": \"" + query + "\"} }";
    }
}
