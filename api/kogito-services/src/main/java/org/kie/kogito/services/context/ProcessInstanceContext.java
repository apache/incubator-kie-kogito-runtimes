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
import java.util.concurrent.ConcurrentHashMap;
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
 * Extension Support: Extensions can register via {@link #registerContextExtension(String, ContextExtension)}
 * to participate in context preservation during async operations.
 */
public final class ProcessInstanceContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInstanceContext.class);

    private static final Map<String, ContextExtension> EXTENSIONS = new ConcurrentHashMap<>();

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
     * Registers a context extension that will participate in context preservation.
     * Extensions with the same ID will replace previously registered extensions.
     *
     * @param extensionId the unique identifier for the extension
     * @param extension the extension to register
     * @throws IllegalArgumentException if extensionId is null or empty, or if extension is null
     */
    public static void registerContextExtension(String extensionId, ContextExtension extension) {
        if (extensionId == null || extensionId.isEmpty()) {
            throw new IllegalArgumentException("Extension ID must not be null or empty");
        }
        if (extension == null) {
            throw new IllegalArgumentException("Extension must not be null");
        }
        EXTENSIONS.put(extensionId, extension);
        LOGGER.debug("Registered context extension: {}", extensionId);
    }

    /**
     * Registers a context extension using its own extension ID.
     * Extensions with the same ID will replace previously registered extensions.
     *
     * @param extension the extension to register
     * @throws IllegalArgumentException if extension is null or its ID is null/empty
     */
    public static void registerContextExtension(ContextExtension extension) {
        if (extension == null) {
            throw new IllegalArgumentException("Extension must not be null");
        }
        registerContextExtension(extension.getExtensionId(), extension);
    }

    /**
     * Retrieves a registered context extension by its ID.
     *
     * @param extensionId the extension ID to retrieve
     * @return the registered extension, or null if not found
     */
    public static ContextExtension getExtension(String extensionId) {
        return EXTENSIONS.get(extensionId);
    }

    /**
     * Clears all registered extensions.
     * This is primarily for testing purposes to ensure test isolation.
     */
    static void clearUserExtensions() {
        EXTENSIONS.clear();
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
     * This method implements a 3-phase lifecycle for registered extensions:
     * 1. beforeContextRestore - extensions inspect the context map (current MDC if null)
     * 2. Core context restoration - MDC is updated with filtered keys
     * 3. afterContextRestore - extensions restore their keys
     *
     * Only core keys and registered extension keys are restored from the context map.
     *
     * @param contextMap the context map to restore, or null to reset to general context
     */
    public static void setContextFromAsync(Map<String, String> contextMap) {
        Map<String, String> currentMdc = MDC.getCopyOfContextMap();
        Map<String, String> contextForExtensions = (contextMap == null || contextMap.isEmpty()) ? currentMdc : contextMap;

        notifyExtensionsBeforeRestore(contextForExtensions);

        if (contextMap != null) {
            if (hasUserRegisteredExtensions()) {
                Map<String, String> filteredContext = filterContextMap(contextMap);
                MDC.setContextMap(filteredContext);
            } else {
                MDC.setContextMap(contextMap);
            }
        } else {
            MDC.clear();
            MDC.put(MDC_PROCESS_INSTANCE_KEY, GENERAL_CONTEXT);
        }

        notifyExtensionsAfterRestore();
    }

    /**
     * Filters the context map to include only core keys and registered extension keys.
     *
     * @param contextMap the context map to filter
     * @return filtered context map containing only allowed keys
     */
    private static Map<String, String> filterContextMap(Map<String, String> contextMap) {
        Map<String, String> filtered = new HashMap<>();

        for (Map.Entry<String, String> entry : contextMap.entrySet()) {
            String key = entry.getKey();

            if (isCoreKey(key) || isExtensionKey(key)) {
                filtered.put(key, entry.getValue());
            }
        }

        return filtered;
    }

    /**
     * Checks if a key is a core ProcessInstanceContext key.
     *
     * @param key the MDC key to check
     * @return true if the key is a core key
     */
    private static boolean isCoreKey(String key) {
        return MDC_PROCESS_INSTANCE_KEY.equals(key) ||
                MDC_TRACE_ID_KEY.equals(key) ||
                MDC_SPAN_ID_KEY.equals(key);
    }

    /**
     * Checks if a key belongs to any registered extension.
     *
     * @param key the MDC key to check
     * @return true if the key belongs to a registered extension
     */
    private static boolean isExtensionKey(String key) {
        for (ContextExtension extension : EXTENSIONS.values()) {
            if (key.startsWith(extension.getMdcKeyPrefix())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there are any registered extensions.
     *
     * @return true if extensions are registered
     */
    private static boolean hasUserRegisteredExtensions() {
        return !EXTENSIONS.isEmpty();
    }

    /**
     * Notifies all registered extensions before context restoration.
     * Extensions can inspect the incoming context map during this phase.
     *
     * @param contextMap the incoming context map that will be restored
     */
    private static void notifyExtensionsBeforeRestore(Map<String, String> contextMap) {
        for (ContextExtension extension : EXTENSIONS.values()) {
            try {
                extension.beforeContextRestore(contextMap);
            } catch (Exception e) {
                LOGGER.warn("Extension {} failed during beforeContextRestore: {}",
                        extension.getExtensionId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Notifies all registered extensions after context restoration.
     * Extensions can restore their state and update MDC during this phase.
     */
    private static void notifyExtensionsAfterRestore() {
        for (ContextExtension extension : EXTENSIONS.values()) {
            try {
                extension.afterContextRestore();
            } catch (Exception e) {
                LOGGER.warn("Extension {} failed during afterContextRestore: {}",
                        extension.getExtensionId(), e.getMessage(), e);
            }
        }
    }

}
