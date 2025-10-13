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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Utility class for analyzing process instance aware logging in integration tests.
 * Supports parsing pipe-delimited log format: timestamp|level|processInstanceId|logger|message
 */
public class ProcessInstanceLogAnalyzer {

    // Pattern matching default pipe-delimited format from kogito-quarkus-common-deployment
    private static final Pattern LOG_LINE_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\|" + // Group 1: timestamp
                    "(\\w+)\\|" + // Group 2: level
                    "([^\\|]*)\\|" + // Group 3: processInstanceId (empty for general)
                    "([^\\|]+)\\|" + // Group 4: logger
                    "(.*)" // Group 5: message (may include newlines)
    );

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Represents a single log entry with all its components.
     */
    public static class LogEntry {
        public final LocalDateTime timestamp;
        public final String level;
        public final String processInstanceId; // Empty string for general context
        public final String logger;
        public final StringBuilder message; // StringBuilder to support multiline messages

        public LogEntry(LocalDateTime timestamp, String level, String processInstanceId, String logger, String message) {
            this.timestamp = timestamp;
            this.level = level;
            this.processInstanceId = processInstanceId != null ? processInstanceId : "";
            this.logger = logger;
            this.message = new StringBuilder(message);
        }

        /**
         * Append additional lines to this log entry (for multiline logs like stack traces).
         */
        public void appendMessage(String line) {
            message.append("\n").append(line);
        }

        /**
         * Check if this log entry has a process instance ID (not general context).
         */
        public boolean hasProcessInstance() {
            return !processInstanceId.isEmpty();
        }

        /**
         * Check if this log entry is general context (empty process instance ID).
         */
        public boolean isGeneralContext() {
            return processInstanceId.isEmpty();
        }

        @Override
        public String toString() {
            return String.format("%s|%s|%s|%s|%s",
                    timestamp.format(TIMESTAMP_FORMATTER), level, processInstanceId, logger, message);
        }
    }

    /**
     * Statistics about log entries for analysis.
     */
    public static class LogStatistics {
        public final long totalLogs;
        public final long processSpecificLogs;
        public final long generalContextLogs;
        public final Map<String, Long> logsByProcessInstance;
        public final Map<String, Long> logsByLevel;

        public LogStatistics(List<LogEntry> entries) {
            this.totalLogs = entries.size();
            this.processSpecificLogs = entries.stream().filter(LogEntry::hasProcessInstance).count();
            this.generalContextLogs = entries.stream().filter(LogEntry::isGeneralContext).count();
            this.logsByProcessInstance = entries.stream()
                    .collect(Collectors.groupingBy(
                            entry -> entry.processInstanceId.isEmpty() ? "" : entry.processInstanceId,
                            Collectors.counting()));
            this.logsByLevel = entries.stream()
                    .collect(Collectors.groupingBy(entry -> entry.level, Collectors.counting()));
        }

        @Override
        public String toString() {
            return String.format(
                    "LogStatistics{total=%d, processSpecific=%d, general=%d, byProcess=%s, byLevel=%s}",
                    totalLogs, processSpecificLogs, generalContextLogs, logsByProcessInstance, logsByLevel);
        }
    }

    /**
     * Parse log file with multiline support for stack traces and exception messages.
     */
    public static List<LogEntry> parseLogFileWithMultilineSupport(Path logFile) throws IOException {
        List<String> lines = Files.readAllLines(logFile);
        List<LogEntry> entries = new ArrayList<>();
        LogEntry currentEntry = null;

        for (String line : lines) {
            Matcher matcher = LOG_LINE_PATTERN.matcher(line);
            if (matcher.matches()) {
                // New log entry - save previous if exists
                if (currentEntry != null) {
                    entries.add(currentEntry);
                }

                // Parse new entry
                LocalDateTime timestamp = LocalDateTime.parse(matcher.group(1), TIMESTAMP_FORMATTER);
                String level = matcher.group(2);
                String processInstanceId = matcher.group(3);
                String logger = matcher.group(4);
                String message = matcher.group(5);

                currentEntry = new LogEntry(timestamp, level, processInstanceId, logger, message);
            } else if (currentEntry != null) {
                // Continuation line (stack trace, multiline message, etc.)
                currentEntry.appendMessage(line);
            }
            // If currentEntry is null and line doesn't match pattern, skip the line
        }

        // Add the last entry
        if (currentEntry != null) {
            entries.add(currentEntry);
        }

        return entries;
    }

    /**
     * Group log entries by process instance ID.
     * Returns a map where key is process instance ID (or "general" for empty process ID).
     */
    public static Map<String, List<LogEntry>> groupByProcessInstance(List<LogEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.processInstanceId.isEmpty() ? "" : entry.processInstanceId));
    }

    /**
     * Filter entries by logger pattern.
     */
    public static List<LogEntry> filterByLogger(List<LogEntry> entries, Pattern loggerPattern) {
        return entries.stream()
                .filter(entry -> loggerPattern.matcher(entry.logger).matches())
                .collect(Collectors.toList());
    }

    /**
     * Filter entries by log level.
     */
    public static List<LogEntry> filterByLevel(List<LogEntry> entries, String level) {
        return entries.stream()
                .filter(entry -> level.equals(entry.level))
                .collect(Collectors.toList());
    }

    /**
     * Calculate statistics for log analysis.
     */
    public static LogStatistics calculateStatistics(List<LogEntry> entries) {
        return new LogStatistics(entries);
    }

    /**
     * Validate that no context leaks occur - completed process IDs should not appear in later general logs.
     */
    public static void validateNoContextLeaks(List<LogEntry> entries, Map<String, LocalDateTime> processCompletionTimes) {
        for (LogEntry entry : entries) {
            if (entry.isGeneralContext()) {
                // Check if this general log message contains any completed process ID
                for (Map.Entry<String, LocalDateTime> completion : processCompletionTimes.entrySet()) {
                    String processId = completion.getKey();
                    LocalDateTime completionTime = completion.getValue();

                    if (entry.timestamp.isAfter(completionTime) &&
                            entry.message.toString().contains(processId)) {

                        fail(String.format(
                                "Context leak detected: Process ID %s appears in general context log after completion. " +
                                        "Log entry at %s: %s",
                                processId, entry.timestamp, entry.message));
                    }
                }
            }
        }
    }

    /**
     * Validate that all entries match the expected log format.
     */
    public static void validateLogFormat(List<LogEntry> entries) {
        for (LogEntry entry : entries) {
            assertThat(entry.timestamp).as("Timestamp should not be null").isNotNull();
            assertThat(entry.level).as("Log level should not be null").isNotNull();
            assertThat(entry.logger).as("Logger should not be null").isNotNull();
            assertThat(entry.message).as("Message should not be null").isNotNull();

            // Validate log level is valid
            assertThat(entry.level)
                    .as("Log level should be valid")
                    .isIn("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL");
        }
    }

    /**
     * Validate that process instances have distinct, non-overlapping log groups.
     */
    public static void validateProcessInstanceSegregation(
            Map<String, List<LogEntry>> entriesByProcess,
            List<String> expectedProcessInstanceIds) {

        // Validate each expected process has logs
        for (String processId : expectedProcessInstanceIds) {
            List<LogEntry> processLogs = entriesByProcess.get(processId);

            assertThat(processLogs)
                    .as("Should have log entries for process instance " + processId)
                    .isNotEmpty();

            // Validate all entries for this process have correct ID
            long correctlyTagged = processLogs.stream()
                    .filter(entry -> processId.equals(entry.processInstanceId))
                    .count();

            assertThat(correctlyTagged)
                    .as("All logs for process " + processId + " should have correct process instance ID")
                    .isEqualTo(processLogs.size());
        }
    }

    /**
     * Validate consistent log patterns across different process instances.
     * Process instances executing the same workflow should have similar log patterns.
     */
    public static void validateConsistentLogPatterns(
            Map<String, List<LogEntry>> entriesByProcess,
            List<String> processInstanceIds,
            double minimumOverlapRatio) {

        if (processInstanceIds.size() < 2) {
            return; // Can't compare patterns with less than 2 processes
        }

        // Extract normalized log patterns for each process
        Map<String, List<String>> patternsByProcess = new HashMap<>();

        for (String processId : processInstanceIds) {
            List<LogEntry> logs = entriesByProcess.get(processId);
            if (logs != null) {
                List<String> patterns = logs.stream()
                        .map(entry -> entry.logger + ":" + normalizeMessage(entry.message.toString()))
                        .collect(Collectors.toList());
                patternsByProcess.put(processId, patterns);
            }
        }

        // Compare each process pattern with the first one
        String firstProcessId = processInstanceIds.get(0);
        List<String> firstProcessPatterns = patternsByProcess.get(firstProcessId);

        if (firstProcessPatterns == null || firstProcessPatterns.isEmpty()) {
            return;
        }

        for (int i = 1; i < processInstanceIds.size(); i++) {
            String processId = processInstanceIds.get(i);
            List<String> patterns = patternsByProcess.get(processId);

            if (patterns == null || patterns.isEmpty()) {
                continue;
            }

            // Calculate pattern overlap
            long matchingPatterns = patterns.stream()
                    .filter(firstProcessPatterns::contains)
                    .count();

            double overlapRatio = (double) matchingPatterns / Math.max(firstProcessPatterns.size(), patterns.size());

            assertThat(overlapRatio)
                    .as("Process %s should have similar log patterns to process %s (overlap: %.2f%%)",
                            processId, firstProcessId, overlapRatio * 100)
                    .isGreaterThanOrEqualTo(minimumOverlapRatio);
        }
    }

    /**
     * Normalize log message by removing process-specific values for pattern comparison.
     */
    private static String normalizeMessage(String message) {
        return message
                // Replace UUIDs with placeholder
                .replaceAll("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}", "<UUID>")
                // Replace timestamps with placeholder
                .replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}", "<TIMESTAMP>")
                // Replace decimal numbers with placeholder
                .replaceAll("\\d+\\.\\d+", "<NUMBER>")
                // Replace integers with placeholder
                .replaceAll("\\b\\d+\\b", "<NUMBER>")
                // Replace common dynamic values
                .replaceAll("\\b(duration|time|elapsed|took)\\s*[:=]?\\s*\\d+", "$1=<TIME>")
                .trim();
    }
}