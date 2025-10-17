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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for propagating process instance context to async work item executions.
 * This class provides helper methods for work item handlers to ensure proper MDC context
 * propagation in async operations.
 */
public final class ProcessInstanceAwareWorkItemHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInstanceAwareWorkItemHandler.class);

    // Private constructor to prevent instantiation
    private ProcessInstanceAwareWorkItemHandler() {
    }

    /**
     * Executes a Runnable within the context of a given process instance.
     * This method can be used by work item handlers to ensure proper context during execution.
     *
     * @param workItem the work item containing the process instance ID
     * @param operation the operation to execute with process context
     */
    public static void executeWithProcessContext(KogitoWorkItem workItem, Runnable operation) {
        String processInstanceId = workItem.getProcessInstanceId();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Executing operation for work item {} with process instance {}", workItem.getId(), processInstanceId);
        }

        ProcessInstanceContext.withProcessInstanceContext(processInstanceId, operation);
    }

    /**
     * Utility method to wrap any CompletableFuture with process instance context propagation.
     * This method can be used by work item handlers that perform async operations.
     *
     * @param processInstanceId the process instance ID
     * @param supplier the supplier that creates the CompletableFuture
     * @param <T> the return type
     * @return a CompletableFuture with process context propagation
     */
    public static <T> CompletableFuture<T> withAsyncProcessContext(String processInstanceId, Supplier<CompletableFuture<T>> supplier) {
        // Capture the current MDC context
        Map<String, String> contextMap = ProcessInstanceContext.copyContextForAsync();

        // Set the process instance context
        ProcessInstanceContext.setProcessInstanceId(processInstanceId);
        try {
            // Create the future
            CompletableFuture<T> future = supplier.get();

            // Wrap the future to restore context on completion
            return future.whenComplete((result, throwable) -> {
                ProcessInstanceContext.setContextFromAsync(contextMap);
            });
        } finally {
            ProcessInstanceContext.clear();
        }
    }

    /**
     * Utility method to wrap any Runnable with process instance context propagation.
     * This method can be used for async operations that don't return a value.
     *
     * @param processInstanceId the process instance ID
     * @param runnable the runnable to execute
     * @return a Runnable with process context propagation
     */
    public static Runnable withAsyncProcessContext(String processInstanceId, Runnable runnable) {
        // Capture the current MDC context
        Map<String, String> contextMap = ProcessInstanceContext.copyContextForAsync();

        return () -> {
            ProcessInstanceContext.setContextFromAsync(contextMap);
            try {
                runnable.run();
            } finally {
                ProcessInstanceContext.clear();
            }
        };
    }

    /**
     * Utility method to create an executor that automatically propagates process instance context.
     * This can be used by work item handlers that submit tasks to executors.
     *
     * @param delegate the underlying executor
     * @param processInstanceId the process instance ID to propagate
     * @return an executor that propagates process context
     */
    public static Executor createContextPropagatingExecutor(Executor delegate, String processInstanceId) {
        return command -> {
            Runnable wrappedCommand = withAsyncProcessContext(processInstanceId, command);
            delegate.execute(wrappedCommand);
        };
    }
}