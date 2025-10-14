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

import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Utility class for managing process instance context in logging operations.
 * This class uses SLF4J's Mapped Diagnostic Context (MDC) to ensure process instance IDs
 * are automatically included in log messages with the format:
 * [date]|[log level]|[process instance id]|[logger]|[log message]
 *
 * When no process instance is available, an empty string is used as the default value
 * to provide cleaner formatting and easier searching in log aggregation systems.
 *
 * Thread Safety: This class is thread-safe and properly manages context isolation
 * between different threads and nested process executions.
 */
public final class ProcessInstanceContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInstanceContext.class);

    /**
     * MDC key used to store the process instance ID.
     * This key should be referenced in logging configurations.
     */
    public static final String MDC_PROCESS_INSTANCE_KEY = "processInstanceId";

    /**
     * Default value used when no process instance ID is available.
     * Empty string for cleaner log formatting and easier searching.
     */
    public static final String GENERAL_CONTEXT = "";

    /**
     * ThreadLocal storage for process instance ID as backup to MDC.
     * This provides faster access and helps detect nested contexts.
     */
    private static final ThreadLocal<String> PROCESS_INSTANCE_ID = new ThreadLocal<>();

    /**
     * ThreadLocal storage for nested context depth to handle nested process calls.
     */
    private static final ThreadLocal<Integer> CONTEXT_DEPTH = new ThreadLocal<>();

    // Static initializer to set default MDC value
    static {
        // Ensure MDC always has a default value for the current thread
        ensureGeneralContext();
    }

    /**
     * Ensures that the MDC has a default "general" value if no process instance ID is set.
     * This method is thread-safe and can be called from any thread.
     */
    private static void ensureGeneralContext() {
        if (MDC.get(MDC_PROCESS_INSTANCE_KEY) == null) {
            MDC.put(MDC_PROCESS_INSTANCE_KEY, GENERAL_CONTEXT);
        }
    }

    // Private constructor to prevent instantiation
    private ProcessInstanceContext() {
        // Utility class
    }

    /**
     * Sets the process instance ID for the current thread context.
     * This method automatically updates both ThreadLocal storage and SLF4J MDC.
     *
     * @param processInstanceId the process instance ID to set, or null to use general context
     */
    public static void setProcessInstanceId(String processInstanceId) {
        try {
            String effectiveId = processInstanceId != null ? processInstanceId : GENERAL_CONTEXT;

            // Check if we're setting the same ID (optimization for nested calls)
            String currentId = PROCESS_INSTANCE_ID.get();
            if (effectiveId.equals(currentId)) {
                // Increment depth for nested calls with same ID
                Integer depth = CONTEXT_DEPTH.get();
                CONTEXT_DEPTH.set(depth != null ? depth + 1 : 1);
                return;
            }

            // Set new context - always set ThreadLocal, even for general context
            if (GENERAL_CONTEXT.equals(effectiveId)) {
                // For general context, don't set ThreadLocal (leave it null) but set MDC
                PROCESS_INSTANCE_ID.remove();
            } else {
                // For actual process instance IDs, set ThreadLocal
                PROCESS_INSTANCE_ID.set(effectiveId);
            }

            // Defensive check: ensure MDC operations are atomic for this thread
            try {
                MDC.put(MDC_PROCESS_INSTANCE_KEY, effectiveId);
            } catch (Exception e) {
                // In case of MDC issues, log but don't fail the operation
                LOGGER.warn("Failed to set MDC context for process instance {}: {}", effectiveId, e.getMessage());
            }

            CONTEXT_DEPTH.set(1);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Set process instance context to: {}", effectiveId);
            }
        } catch (Exception e) {
            // Defensive catch for any unexpected issues in context management
            LOGGER.error("Unexpected error setting process instance context: {}", e.getMessage(), e);
            // Ensure we at least have general context in MDC
            ensureGeneralContext();
        }
    }

    /**
     * Gets the current process instance ID from ThreadLocal storage.
     *
     * @return the current process instance ID, or empty string if no context is set
     */
    public static String getProcessInstanceId() {
        // Ensure MDC is initialized for this thread
        ensureGeneralContext();
        String id = PROCESS_INSTANCE_ID.get();
        return id != null ? id : GENERAL_CONTEXT;
    }

    /**
     * Gets the current process instance ID, returning an empty string if none is set.
     *
     * @return the current process instance ID or empty string if no context is set
     */
    public static String getProcessInstanceIdOrGeneral() {
        // Ensure MDC is initialized for this thread
        ensureGeneralContext();
        String id = PROCESS_INSTANCE_ID.get();
        return id != null ? id : GENERAL_CONTEXT;
    }

    /**
     * Clears the process instance context for the current thread.
     * This method properly handles nested contexts by only clearing when
     * the outermost context is being removed. When clearing, it removes
     * the ThreadLocal context but keeps the general context in MDC for logging.
     */
    public static void clear() {
        try {
            Integer depth = CONTEXT_DEPTH.get();

            if (depth == null || depth <= 1) {
                // Clear ThreadLocal but keep general context in MDC for logging
                String clearedId = PROCESS_INSTANCE_ID.get();

                try {
                    PROCESS_INSTANCE_ID.remove();
                } catch (Exception e) {
                    LOGGER.warn("Error removing ThreadLocal process instance ID: {}", e.getMessage());
                }

                try {
                    MDC.put(MDC_PROCESS_INSTANCE_KEY, GENERAL_CONTEXT);
                } catch (Exception e) {
                    LOGGER.warn("Error resetting MDC to general context: {}", e.getMessage());
                }

                try {
                    CONTEXT_DEPTH.remove();
                } catch (Exception e) {
                    LOGGER.warn("Error removing ThreadLocal context depth: {}", e.getMessage());
                }

                if (LOGGER.isDebugEnabled() && clearedId != null && !GENERAL_CONTEXT.equals(clearedId)) {
                    LOGGER.debug("Cleared process instance context: {}, reset to empty", clearedId);
                }
            } else {
                // Decrement depth for nested calls
                try {
                    CONTEXT_DEPTH.set(depth - 1);
                } catch (Exception e) {
                    LOGGER.warn("Error decrementing context depth: {}", e.getMessage());
                    // Fallback: remove the depth completely
                    CONTEXT_DEPTH.remove();
                }
            }
        } catch (Exception e) {
            // Defensive catch for any unexpected issues
            LOGGER.error("Unexpected error clearing process instance context: {}", e.getMessage(), e);
            try {
                // Try to at least clean up the ThreadLocals
                PROCESS_INSTANCE_ID.remove();
                CONTEXT_DEPTH.remove();
                ensureGeneralContext();
            } catch (Exception cleanupException) {
                LOGGER.error("Failed to cleanup ThreadLocal context: {}", cleanupException.getMessage());
            }
        }
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
     * @param contextMap the context map to restore, or null to reset to general context
     */
    public static void setContextFromAsync(Map<String, String> contextMap) {
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
            // Also update ThreadLocal for consistency
            String processInstanceId = contextMap.get(MDC_PROCESS_INSTANCE_KEY);
            if (processInstanceId != null && !GENERAL_CONTEXT.equals(processInstanceId)) {
                PROCESS_INSTANCE_ID.set(processInstanceId);
                CONTEXT_DEPTH.set(1);
            } else {
                PROCESS_INSTANCE_ID.remove();
                CONTEXT_DEPTH.remove();
            }
        } else {
            // Reset to general context instead of clearing completely
            PROCESS_INSTANCE_ID.remove();
            MDC.put(MDC_PROCESS_INSTANCE_KEY, GENERAL_CONTEXT);
            CONTEXT_DEPTH.remove();
        }
    }

    /**
     * Checks if a process instance context is currently set.
     *
     * @return true if a process instance context is set, false otherwise
     */
    public static boolean hasContext() {
        String id = PROCESS_INSTANCE_ID.get();
        return id != null && !GENERAL_CONTEXT.equals(id);
    }

    /**
     * Checks if the current context is the general context (not process-specific).
     *
     * @return true if the current context is empty (general), false if it's process-specific or no context is set
     */
    public static boolean isGeneralContext() {
        String id = PROCESS_INSTANCE_ID.get();
        return GENERAL_CONTEXT.equals(id);
    }

    /**
     * Gets the current nesting depth of the process context.
     * This is useful for debugging nested process calls.
     *
     * @return the current nesting depth, or 0 if no context is set
     */
    public static int getContextDepth() {
        Integer depth = CONTEXT_DEPTH.get();
        return depth != null ? depth : 0;
    }
}