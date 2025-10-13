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
 * This ensures that all work units executed within this UoW have the correct process instance context
 * for logging purposes.
 */
public class ProcessInstanceAwareUnitOfWork implements UnitOfWork {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessInstanceAwareUnitOfWork.class);

    private final UnitOfWork delegate;
    private final String processInstanceId;
    private Map<String, String> savedContext;

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
        // Save the current context in case there's already one set
        if (ProcessInstanceContext.hasContext()) {
            savedContext = ProcessInstanceContext.copyContextForAsync();
            LOG.debug("Saved existing process context for UoW start: {}", ProcessInstanceContext.getProcessInstanceId());
        }

        // Set the process instance context for this UoW
        ProcessInstanceContext.setProcessInstanceId(processInstanceId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting UnitOfWork with process instance context: {}", processInstanceId);
        }

        delegate.start();
    }

    @Override
    public void end() {
        try {
            delegate.end();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Ended UnitOfWork with process instance context: {}", processInstanceId);
            }
        } finally {
            // Clear the current context and restore the saved one if needed
            ProcessInstanceContext.clear();

            if (savedContext != null) {
                ProcessInstanceContext.setContextFromAsync(savedContext);
                LOG.debug("Restored previous process context after UoW end: {}", ProcessInstanceContext.getProcessInstanceId());
                savedContext = null;
            }
        }
    }

    @Override
    public void abort() {
        try {
            delegate.abort();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Aborted UnitOfWork with process instance context: {}", processInstanceId);
            }
        } finally {
            // Clear the current context and restore the saved one if needed
            ProcessInstanceContext.clear();

            if (savedContext != null) {
                ProcessInstanceContext.setContextFromAsync(savedContext);
                LOG.debug("Restored previous process context after UoW abort: {}", ProcessInstanceContext.getProcessInstanceId());
                savedContext = null;
            }
        }
    }

    @Override
    public void intercept(WorkUnit work) {
        // Wrap the work unit to ensure context is maintained during execution
        WorkUnit wrappedWork = new ProcessInstanceAwareWorkUnit(work, processInstanceId);
        delegate.intercept(wrappedWork);
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