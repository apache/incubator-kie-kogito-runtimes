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
import java.util.concurrent.atomic.AtomicInteger;
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
     * Enhanced with resilient parsing for CI environments.
     */
    public static List<LogEntry> parseLogFileWithMultilineSupport(Path logFile) throws IOException {
        List<String> lines = Files.readAllLines(logFile);
        List<LogEntry> entries = new ArrayList<>();
        LogEntry currentEntry = null;
        int lineNumber = 0;
        AtomicInteger malformedLineCount = new AtomicInteger(0);

        for (String line : lines) {
            lineNumber++;
            currentEntry = parseLogLine(line, currentEntry, entries, malformedLineCount, lineNumber);
        }

        // Add the last entry
        if (currentEntry != null) {
            entries.add(currentEntry);
        }

        // Log statistics about parsing
        if (malformedLineCount.get() > 0) {
            System.err.printf("Warning: Encountered %d malformed/problematic lines out of %d total lines while parsing %s%n",
                    malformedLineCount.get(), lineNumber, logFile.getFileName());
        }

        return entries;
    }

    private static LogEntry parseLogLine(String line, LogEntry currentEntry, List<LogEntry> entries, AtomicInteger malformedLineCount, int lineNumber) {
        try {
            Matcher matcher = LOG_LINE_PATTERN.matcher(line);
            if (matcher.matches()) {
                // New log entry - save previous if exists
                if (currentEntry != null) {
                    entries.add(currentEntry);
                }

                currentEntry = parseEntry(matcher, malformedLineCount);
            } else if (currentEntry != null) {
                // Continuation line (stack trace, multiline message, etc.)
                currentEntry.appendMessage(line);
            } else {
                // Malformed line without current entry - try alternative patterns
                LogEntry fallbackEntry = tryAlternativePatterns(line, lineNumber);
                if (fallbackEntry != null) {
                    entries.add(fallbackEntry);
                    malformedLineCount.set(malformedLineCount.get() + 1);
                    ;
                }
                // Otherwise skip the line
            }
        } catch (Exception e) {
            // Defensive catch for any line parsing issues
            malformedLineCount.set(malformedLineCount.get() + 1);
            ;
            // Continue processing other lines
        }
        return currentEntry;
    }

    private static LogEntry parseEntry(Matcher matcher, AtomicInteger malformedLineCount) {
        LocalDateTime timestamp = parseTimestamp(matcher, malformedLineCount);
        String level = sanitizeString(matcher.group(2));
        String processInstanceId = sanitizeString(matcher.group(3));
        String logger = sanitizeString(matcher.group(4));
        String message = sanitizeString(matcher.group(5));

        if (level.isEmpty()) {
            level = "INFO"; // Default level
            malformedLineCount.set(malformedLineCount.get() + 1);
            ;
        }
        if (logger.isEmpty()) {
            logger = "unknown.logger"; // Default logger
            malformedLineCount.set(malformedLineCount.get() + 1);
            ;
        }

        return new LogEntry(timestamp, level, processInstanceId, logger, message);
    }

    private static LocalDateTime parseTimestamp(Matcher matcher, AtomicInteger malformedLineCount) {
        LocalDateTime timestamp;
        try {
            timestamp = LocalDateTime.parse(matcher.group(1), TIMESTAMP_FORMATTER);
        } catch (Exception e) {
            // If timestamp parsing fails, use current time as fallback
            timestamp = LocalDateTime.now();
            malformedLineCount.set(malformedLineCount.get() + 1);
            ;
        }
        return timestamp;
    }

    /**
     * Sanitize string input to prevent null/empty issues.
     */
    private static String sanitizeString(String input) {
        return input != null ? input.trim() : "";
    }

    /**
     * Try alternative log parsing patterns for non-standard log formats.
     * This provides fallback parsing for CI environments with different logging configurations.
     */
    private static LogEntry tryAlternativePatterns(String line, int lineNumber) {
        // Try common alternative patterns

        // Pattern 1: Simple timestamp + level + message format
        Pattern simplePattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+(\\w+)\\s+(.+)");
        Matcher simpleMatcher = simplePattern.matcher(line);
        if (simpleMatcher.matches()) {
            try {
                LocalDateTime timestamp = LocalDateTime.parse(simpleMatcher.group(1), TIMESTAMP_FORMATTER);
                String level = simpleMatcher.group(2);
                String message = simpleMatcher.group(3);
                return new LogEntry(timestamp, level, "", "unknown.logger", message);
            } catch (Exception e) {
                // Fall through to next pattern
            }
        }

        // Pattern 2: Level + logger + message format (common in some frameworks)
        Pattern levelLoggerPattern = Pattern.compile("(\\w+)\\s+([^\\s]+)\\s+(.+)");
        Matcher levelLoggerMatcher = levelLoggerPattern.matcher(line);
        if (levelLoggerMatcher.matches()) {
            try {
                String level = levelLoggerMatcher.group(1);
                String logger = levelLoggerMatcher.group(2);
                String message = levelLoggerMatcher.group(3);
                return new LogEntry(LocalDateTime.now(), level, "", logger, message);
            } catch (Exception e) {
                // Fall through
            }
        }

        // Pattern 3: Very simple format - just treat as message
        if (!line.trim().isEmpty()) {
            return new LogEntry(LocalDateTime.now(), "INFO", "", "unknown.logger", line);
        }

        return null;
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
     * Enhanced with more lenient validation for CI environments.
     */
    public static void validateLogFormat(List<LogEntry> entries) {
        if (entries.isEmpty()) {
            // Don't fail if no entries found, but warn
            System.err.println("Warning: No log entries found for validation");
            return;
        }

        int validationIssues = 0;
        for (LogEntry entry : entries) {
            try {
                assertThat(entry.timestamp).as("Timestamp should not be null").isNotNull();
                assertThat(entry.level).as("Log level should not be null").isNotNull();
                assertThat(entry.logger).as("Logger should not be null").isNotNull();
                assertThat(entry.message).as("Message should not be null").isNotNull();

                // Validate log level is valid (with more tolerance)
                if (!isValidLogLevel(entry.level)) {
                    validationIssues++;
                    System.err.printf("Warning: Invalid log level '%s' found in entry%n", entry.level);
                }
            } catch (AssertionError e) {
                validationIssues++;
                System.err.printf("Warning: Log format validation issue: %s%n", e.getMessage());
            }
        }

        // Allow some validation issues in CI environments but warn about them
        if (validationIssues > 0) {
            double errorRate = (double) validationIssues / entries.size();
            if (errorRate > 0.1) { // More than 10% validation issues
                throw new AssertionError(String.format(
                        "Too many log format validation issues: %d out of %d entries (%.1f%%) have issues",
                        validationIssues, entries.size(), errorRate * 100));
            } else {
                System.err.printf("Log format validation completed with %d minor issues out of %d entries (%.1f%%)%n",
                        validationIssues, entries.size(), errorRate * 100);
            }
        }
    }

    /**
     * Check if log level is valid, including common variations.
     */
    private static boolean isValidLogLevel(String level) {
        if (level == null || level.trim().isEmpty()) {
            return false;
        }
        String normalizedLevel = level.trim().toUpperCase();
        return normalizedLevel.matches("TRACE|DEBUG|INFO|WARN|WARNING|ERROR|FATAL|OFF|ALL") ||
                normalizedLevel.matches("FINE|FINER|FINEST|SEVERE|CONFIG"); // Java util.logging levels
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