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
package org.kie.kogito.services.context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Utility class for managing process instance context in logging operations.
 * This class uses SLF4J's Mapped Diagnostic Context (MDC) to ensure process instance IDs
 * are automatically included in log messages.
 *
 * When no process instance is available, an empty string is used as the default value
 * to provide cleaner formatting and easier searching in log aggregation systems.
 *
 * Thread Safety: This class is thread-safe and properly manages context isolation
 * between different threads using MDC's inherent ThreadLocal-based storage.
 *
 * Note: This class preserves OpenTelemetry MDC keys (otel.*) during context
 * restoration to maintain distributed tracing consistency. The actual OpenTelemetry
 * integration is provided by the kogito-quarkus-serverless-workflow-otel extension.
 */
public final class ProcessInstanceContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInstanceContext.class);

    /**
     * MDC key used to store the process instance ID.
     * This key should be referenced in logging configurations.
     */
    public static final String MDC_PROCESS_INSTANCE_KEY = "processInstanceId";

    /**
     * MDC key for trace ID (for distributed tracing correlation).
     */
    public static final String MDC_TRACE_ID_KEY = "traceId";

    /**
     * MDC key for span ID (for distributed tracing correlation).
     */
    public static final String MDC_SPAN_ID_KEY = "spanId";

    /**
     * Span attribute key for process instance ID (OpenTelemetry).
     */
    public static final String SPAN_ATTRIBUTE_PROCESS_INSTANCE_ID = "kogito.process.instance.id";

    /**
     * Default value used when no process instance ID is available.
     * Empty string for cleaner log formatting and easier searching.
     */
    public static final String GENERAL_CONTEXT = "";

    // Private constructor to prevent instantiation
    private ProcessInstanceContext() {
        // Utility class
    }

    /**
     * Sets the process instance ID for the current thread context.
     * This method updates SLF4J MDC and only performs the update if the value changes.
     *
     * @param processInstanceId the process instance ID to set, or null to use general context
     */
    public static void setProcessInstanceId(String processInstanceId) {
        String effectiveId = processInstanceId != null ? processInstanceId : GENERAL_CONTEXT;

        // Only update MDC if the value is changing (optimization)
        String currentId = MDC.get(MDC_PROCESS_INSTANCE_KEY);
        if (!effectiveId.equals(currentId)) {
            MDC.put(MDC_PROCESS_INSTANCE_KEY, effectiveId);
        }
    }

    /**
     * Gets the current process instance ID from MDC.
     *
     * @return the current process instance ID, or empty string if no context is set
     */
    public static String getProcessInstanceId() {
        String id = MDC.get(MDC_PROCESS_INSTANCE_KEY);
        return id != null ? id : GENERAL_CONTEXT;
    }

    /**
     * Clears the process instance context for the current thread.
     * This resets the MDC to the general context (empty string).
     */
    public static void clear() {
        MDC.put(MDC_PROCESS_INSTANCE_KEY, GENERAL_CONTEXT);
    }

    /**
     * Checks if a process instance context is currently set.
     *
     * @return true if a process instance context is set, false otherwise
     */
    public static boolean hasContext() {
        String id = MDC.get(MDC_PROCESS_INSTANCE_KEY);
        return id != null && !GENERAL_CONTEXT.equals(id);
    }

    /**
     * Executes the given operation within the specified process instance context.
     * This method ensures proper context setup and cleanup even if exceptions occur.
     *
     * @param processInstanceId the process instance ID to set for the operation
     * @param operation the operation to execute
     * @param <T> the return type of the operation
     * @return the result of the operation
     */
    public static <T> T withProcessInstanceContext(String processInstanceId, Supplier<T> operation) {
        setProcessInstanceId(processInstanceId);
        try {
            return operation.get();
        } finally {
            clear();
        }
    }

    /**
     * Executes the given runnable within the specified process instance context.
     * This method ensures proper context setup and cleanup even if exceptions occur.
     *
     * @param processInstanceId the process instance ID to set for the operation
     * @param operation the operation to execute
     */
    public static void withProcessInstanceContext(String processInstanceId, Runnable operation) {
        setProcessInstanceId(processInstanceId);
        try {
            operation.run();
        } finally {
            clear();
        }
    }

    /**
     * Gets a copy of the current MDC context map for propagation to other threads.
     * This is useful for async operations that need to maintain the same logging context.
     *
     * @return a copy of the current MDC context map, or null if no context is set
     */
    public static Map<String, String> copyContextForAsync() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * Sets the MDC context map from a previously copied context.
     * This is useful for async operations that need to restore logging context.
     *
     * This method preserves OpenTelemetry MDC keys (otel.*) to maintain distributed
     * tracing consistency across ProcessInstanceAwareUnitOfWork boundaries.
     *
     * @param contextMap the context map to restore, or null to reset to general context
     */
    public static void setContextFromAsync(Map<String, String> contextMap) {
        // Preserve OpenTelemetry MDC keys before restoration
        Map<String, String> otelContext = preserveOtelMdcKeys();

        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        } else {
            MDC.put(MDC_PROCESS_INSTANCE_KEY, GENERAL_CONTEXT);
        }

        // Restore OpenTelemetry MDC keys
        restoreOtelMdcKeys(otelContext);
    }

    /**
     * Preserves OpenTelemetry MDC keys that start with "otel." prefix.
     * These keys contain transaction IDs and tracker attributes that need to be
     * maintained across context switches.
     *
     * @return a map containing the preserved OpenTelemetry MDC keys
     */
    private static Map<String, String> preserveOtelMdcKeys() {
        Map<String, String> otelKeys = new HashMap<>();
        Map<String, String> currentMdc = MDC.getCopyOfContextMap();

        if (currentMdc != null) {
            currentMdc.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("otel."))
                    .forEach(entry -> otelKeys.put(entry.getKey(), entry.getValue()));
        }

        return otelKeys;
    }

    /**
     * Restores OpenTelemetry MDC keys that were preserved before context restoration.
     *
     * @param otelKeys the OpenTelemetry MDC keys to restore
     */
    private static void restoreOtelMdcKeys(Map<String, String> otelKeys) {
        if (otelKeys != null && !otelKeys.isEmpty()) {
            otelKeys.forEach(MDC::put);
        }
    }

}
