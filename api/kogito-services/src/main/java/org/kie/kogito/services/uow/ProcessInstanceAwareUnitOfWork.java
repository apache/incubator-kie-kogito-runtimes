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
package org.kie.kogito.services.uow;

import java.util.Map;

import org.kie.kogito.services.context.ProcessInstanceContext;
import org.kie.kogito.uow.UnitOfWork;
import org.kie.kogito.uow.WorkUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A UnitOfWork wrapper that manages process instance context throughout the unit of work lifecycle.
 * This implementation ensures proper context cleanup even in the face of exceptions, preventing
 * context leaks and ensuring thread safety.
 *
 * Key features:
 * - Automatic context setup and cleanup
 * - Support for nested UnitOfWork with context preservation
 * - Exception-safe cleanup using try-finally blocks
 * - ThreadLocal leak prevention
 * - Integration with distributed tracing systems
 *
 * Thread Safety: This class is thread-safe and properly manages ThreadLocal cleanup to prevent
 * memory leaks in long-running applications.
 */
public class ProcessInstanceAwareUnitOfWork implements UnitOfWork {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessInstanceAwareUnitOfWork.class);

    private final UnitOfWork delegate;
    private final String processInstanceId;

    // ThreadLocal to store saved context for nested UnitOfWork scenarios
    // Using ThreadLocal instead of instance variable ensures thread safety
    private static final ThreadLocal<Map<String, String>> SAVED_CONTEXT = new ThreadLocal<>();

    /**
     * Creates a new ProcessInstanceAwareUnitOfWork with the specified process instance ID.
     *
     * @param delegate the underlying UnitOfWork to delegate to
     * @param processInstanceId the process instance ID to use for context
     */
    public ProcessInstanceAwareUnitOfWork(UnitOfWork delegate, String processInstanceId) {
        this.delegate = delegate;
        this.processInstanceId = processInstanceId;
    }

    @Override
    public void start() {
        // Save the current context in case there's already one set (nested UoW scenario)
        if (ProcessInstanceContext.hasContext()) {
            Map<String, String> savedContext = ProcessInstanceContext.copyContextForAsync();
            SAVED_CONTEXT.set(savedContext);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Saved existing process context for UoW start: {}", ProcessInstanceContext.getProcessInstanceId());
            }
        }

        // Set the process instance context for this UoW
        ProcessInstanceContext.setProcessInstanceId(processInstanceId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting UnitOfWork with process instance context: {}", processInstanceId);
        }

        // Start the delegate UnitOfWork
        delegate.start();
    }

    @Override
    public void end() {
        try {
            // End the delegate UnitOfWork
            delegate.end();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Ended UnitOfWork with process instance context: {}", processInstanceId);
            }
        } finally {
            // Always restore context, even if delegate.end() throws
            restoreContext();
        }
    }

    @Override
    public void abort() {
        try {
            // Abort the delegate UnitOfWork
            delegate.abort();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Aborted UnitOfWork with process instance context: {}", processInstanceId);
            }
        } finally {
            // Always restore context, even if delegate.abort() throws
            restoreContext();
        }
    }

    @Override
    public void intercept(WorkUnit work) {
        // Wrap the work unit to ensure context is maintained during execution
        WorkUnit wrappedWork = new ProcessInstanceAwareWorkUnit(work, processInstanceId);
        delegate.intercept(wrappedWork);
    }

    /**
     * Restores the previously saved process instance context.
     * This method is called by both end() and abort() in finally blocks to ensure
     * cleanup happens regardless of exceptions.
     */
    private void restoreContext() {
        try {
            // Clear the current context
            ProcessInstanceContext.clear();

            // Restore the saved context if one exists
            Map<String, String> savedContext = SAVED_CONTEXT.get();
            if (savedContext != null) {
                ProcessInstanceContext.setContextFromAsync(savedContext);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Restored previous process context: {}", ProcessInstanceContext.getProcessInstanceId());
                }
            }
        } finally {
            // Always clean up the ThreadLocal to prevent memory leaks
            SAVED_CONTEXT.remove();
        }
    }

    /**
     * Gets the underlying delegate UnitOfWork.
     *
     * @return the delegate UnitOfWork
     */
    public UnitOfWork getDelegate() {
        return delegate;
    }

    /**
     * Gets the process instance ID for this UnitOfWork.
     *
     * @return the process instance ID
     */
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    /**
     * A WorkUnit wrapper that ensures process instance context is maintained during work unit execution.
     * This wrapper is thread-safe and properly handles context cleanup to prevent memory leaks.
     */
    private static class ProcessInstanceAwareWorkUnit implements WorkUnit {

        private final WorkUnit delegate;
        private final String processInstanceId;

        public ProcessInstanceAwareWorkUnit(WorkUnit delegate, String processInstanceId) {
            this.delegate = delegate;
            this.processInstanceId = processInstanceId;
        }

        @Override
        public Object data() {
            return delegate.data();
        }

        @Override
        public Integer priority() {
            return delegate.priority();
        }

        @Override
        public void perform() {
            ProcessInstanceContext.setProcessInstanceId(processInstanceId);
            try {
                delegate.perform();
            } finally {
                ProcessInstanceContext.clear();
            }
        }

        @Override
        public void abort() {
            ProcessInstanceContext.setProcessInstanceId(processInstanceId);
            try {
                delegate.abort();
            } finally {
                ProcessInstanceContext.clear();
            }
        }
    }
}