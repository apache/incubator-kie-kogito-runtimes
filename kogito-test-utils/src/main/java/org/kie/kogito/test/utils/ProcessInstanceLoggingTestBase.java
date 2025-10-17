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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.http.ContentType;

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
     * Get all log files including rotated ones in chronological order (newest first).
     * Handles Quarkus default rotation: quarkus.log, quarkus.log.1, quarkus.log.2, etc.
     *
     * @return List of log file paths sorted by modification time (newest first)
     * @throws IOException if directory access fails
     */
    protected List<Path> getAllLogFiles() throws IOException {
        Path logFile = getLogFilePath();
        Path logDir = logFile.getParent();
        String baseName = logFile.getFileName().toString();

        List<Path> logFiles = new ArrayList<>();

        // Add main log file if it exists
        if (Files.exists(logFile)) {
            logFiles.add(logFile);
        }

        // Look for rotated files: baseName.1, baseName.2, etc.
        for (int i = 1; i <= 10; i++) { // Check up to 10 rotated files (more than default max-backup-index=5)
            Path rotatedFile = logDir.resolve(baseName + "." + i);
            if (Files.exists(rotatedFile)) {
                logFiles.add(rotatedFile);
            }
        }

        // Sort by last modified time (newest first) to ensure we read logs in chronological order
        logFiles.sort((p1, p2) -> {
            try {
                return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
            } catch (IOException e) {
                return 0; // Keep original order if we can't compare
            }
        });

        LOGGER.debug("Found {} log files: {}", logFiles.size(), logFiles);
        return logFiles;
    }

    /**
     * Clear a specific log file to start with a clean slate.
     *
     * Warning: If your test uses getAllLogFiles() or parseAllLogFiles() methods later,
     * consider using clearAllLogFiles() instead to avoid inconsistent results from
     * rotated log files.
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
     * Clear all log files including rotated ones to start with a clean slate.
     * This ensures we don't have stale logs from previous test runs affecting current test.
     *
     * This is the recommended method for test setup when using getAllLogFiles() or
     * parseAllLogFiles() methods to ensure consistent results.
     *
     * @throws IOException if file operations fail
     */
    protected void clearAllLogFiles() throws IOException {
        try {
            List<Path> logFiles = getAllLogFiles();
            for (Path logFile : logFiles) {
                if (Files.exists(logFile)) {
                    Files.write(logFile, new byte[0]);
                    LOGGER.debug("Cleared log file: {}", logFile);
                }
            }
            LOGGER.info("Cleared {} log files", logFiles.size());
        } catch (IOException e) {
            LOGGER.warn("Failed to clear some log files: {}", e.getMessage());
            // Don't fail the test, just warn
        }
    }

    /**
     * Wait for logs to be flushed to disk.
     *
     * @throws InterruptedException if thread is interrupted
     */
    protected void waitForLogFlush() throws InterruptedException {
        // Increase default wait time for CI environments
        long waitTime = isRunningInCI() ? 3000 : 1000;
        Thread.sleep(waitTime);
    }

    /**
     * Wait for logs to be flushed to disk with custom duration.
     *
     * @param millis milliseconds to wait
     * @throws InterruptedException if thread is interrupted
     */
    protected void waitForLogFlush(long millis) throws InterruptedException {
        // In CI environments, use at least the provided time or 2 seconds, whichever is higher
        long actualWaitTime = isRunningInCI() ? Math.max(millis, 2000) : millis;
        Thread.sleep(actualWaitTime);
    }

    /**
     * Detect if running in CI environment based on common CI environment variables.
     * 
     * @return true if running in CI, false otherwise
     */
    protected boolean isRunningInCI() {
        return System.getenv("CI") != null ||
                System.getenv("JENKINS_URL") != null ||
                System.getenv("GITHUB_ACTIONS") != null ||
                System.getenv("TRAVIS") != null ||
                System.getenv("CIRCLECI") != null ||
                System.getProperty("ci.environment") != null;
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
     * Parse all log files (including rotated ones) and combine results.
     * This method handles log rotation by reading all available log files.
     *
     * @return Combined log entries from all files, sorted by timestamp
     * @throws IOException if file operations fail
     */
    protected List<ProcessInstanceLogAnalyzer.LogEntry> parseAllLogFiles() throws IOException {
        List<Path> logFiles = getAllLogFiles();
        List<ProcessInstanceLogAnalyzer.LogEntry> allEntries = new ArrayList<>();

        LOGGER.info("Parsing {} log files for comprehensive analysis", logFiles.size());

        for (Path logFile : logFiles) {
            try {
                LOGGER.debug("Parsing log file: {}", logFile);
                List<ProcessInstanceLogAnalyzer.LogEntry> entries =
                        ProcessInstanceLogAnalyzer.parseLogFileWithMultilineSupport(logFile);
                allEntries.addAll(entries);
                LOGGER.debug("Added {} entries from {}", entries.size(), logFile.getFileName());
            } catch (IOException e) {
                LOGGER.warn("Failed to parse log file {}: {}", logFile, e.getMessage());
                // Continue with other files rather than failing completely
            }
        }

        // Sort all entries by timestamp to ensure chronological order
        allEntries.sort((e1, e2) -> e1.timestamp.compareTo(e2.timestamp));

        LOGGER.info("Combined {} log entries from {} files", allEntries.size(), logFiles.size());

        if (allEntries.isEmpty()) {
            throw new IOException("No log entries found in any log files: " + logFiles);
        }

        // Validate combined results
        ProcessInstanceLogAnalyzer.validateLogFormat(allEntries);

        return allEntries;
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
        List<ProcessInstanceLogAnalyzer.LogEntry> entries = parseAndValidateLogsWithRetry(logFile);

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
     * Validate that each process instance has distinct log entries using all available log files.
     * This method handles log rotation by reading all rotated log files.
     *
     * @param processInstanceIds Process instance IDs to validate
     * @throws IOException if file operations fail
     */
    protected void validateProcessInstanceLogsRobust(String... processInstanceIds) throws IOException {
        List<ProcessInstanceLogAnalyzer.LogEntry> entries = parseAllLogFilesWithRetry();

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

        LOGGER.info("Process instance logging validation completed for {} process instances (rotation-aware)", processInstanceIds.length);
    }

    /**
     * Parse log file with retry logic for CI environments.
     *
     * @param logFile Path to log file
     * @return Parsed log entries
     * @throws IOException if file operations fail after retries
     */
    protected List<ProcessInstanceLogAnalyzer.LogEntry> parseAndValidateLogsWithRetry(Path logFile) throws IOException {
        int maxRetries = isRunningInCI() ? 3 : 1;
        IOException lastException = null;

        for (int retry = 0; retry < maxRetries; retry++) {
            try {
                if (!Files.exists(logFile)) {
                    LOGGER.warn("Log file does not exist yet, waiting... (attempt {}/{})", retry + 1, maxRetries);
                    if (retry < maxRetries - 1) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new IOException("Interrupted while waiting for log file", e);
                        }
                        continue;
                    }
                    throw new IOException("Log file does not exist: " + logFile);
                }

                List<ProcessInstanceLogAnalyzer.LogEntry> entries = parseAndValidateLogs(logFile);

                if (entries.isEmpty() && retry < maxRetries - 1) {
                    LOGGER.warn("No log entries found, retrying... (attempt {}/{})", retry + 1, maxRetries);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted while waiting for log entries", e);
                    }
                    continue;
                }

                return entries;

            } catch (IOException e) {
                lastException = e;
                LOGGER.warn("Failed to parse log file (attempt {}/{}): {}", retry + 1, maxRetries, e.getMessage());
                if (retry < maxRetries - 1) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry", ie);
                    }
                }
            }
        }

        throw new IOException("Failed to parse log file after " + maxRetries + " attempts", lastException);
    }

    /**
     * Parse all log files with retry logic and rotation handling for CI environments.
     * This is the robust method that should be used for comprehensive log analysis.
     *
     * @return Parsed log entries from all available log files
     * @throws IOException if file operations fail after retries
     */
    protected List<ProcessInstanceLogAnalyzer.LogEntry> parseAllLogFilesWithRetry() throws IOException {
        int maxRetries = isRunningInCI() ? 3 : 1;
        IOException lastException = null;

        for (int retry = 0; retry < maxRetries; retry++) {
            try {
                List<ProcessInstanceLogAnalyzer.LogEntry> entries = parseAllLogFiles();

                if (entries.isEmpty() && retry < maxRetries - 1) {
                    LOGGER.warn("No log entries found in any files, retrying... (attempt {}/{})", retry + 1, maxRetries);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted while waiting for log entries", e);
                    }
                    continue;
                }

                return entries;

            } catch (IOException e) {
                lastException = e;
                LOGGER.warn("Failed to parse log files (attempt {}/{}): {}", retry + 1, maxRetries, e.getMessage());
                if (retry < maxRetries - 1) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry", ie);
                    }
                }
            }
        }

        throw new IOException("Failed to parse log files after " + maxRetries + " attempts", lastException);
    }

    /**
     * Wait for workflow completion using polling.
     *
     * @param workflowPath REST path for the workflow
     * @param processInstanceId Process instance ID
     * @param timeout Maximum time to wait
     */
    protected void waitForWorkflowCompletion(String workflowPath, String processInstanceId, Duration timeout) {
        // Extend timeout for CI environments
        Duration actualTimeout = isRunningInCI() ? timeout.multipliedBy(2) : timeout;

        await()
                .atMost(actualTimeout)
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
                        LOGGER.debug("Exception while checking workflow completion: {}", e.getMessage());
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
