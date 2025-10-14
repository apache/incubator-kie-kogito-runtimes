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

import org.kie.kogito.event.EventManager;
import org.kie.kogito.uow.UnitOfWork;
import org.kie.kogito.uow.UnitOfWorkFactory;

/**
 * A UnitOfWorkFactory that creates process-instance-aware UnitOfWork instances.
 * This factory wraps any existing UnitOfWorkFactory to add process instance context management.
 */
public class ProcessInstanceAwareUnitOfWorkFactory implements UnitOfWorkFactory {

    private final UnitOfWorkFactory delegate;

    /**
     * Creates a new ProcessInstanceAwareUnitOfWorkFactory.
     *
     * @param delegate the underlying UnitOfWorkFactory to delegate to
     */
    public ProcessInstanceAwareUnitOfWorkFactory(UnitOfWorkFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public UnitOfWork create(EventManager eventManager) {
        UnitOfWork baseUow = delegate.create(eventManager);

        // Try to get the current process instance ID from context
        String processInstanceId = getCurrentProcessInstanceId();

        if (processInstanceId != null) {
            return new ProcessInstanceAwareUnitOfWork(baseUow, processInstanceId);
        } else {
            // No process instance context available, return the base UoW
            return baseUow;
        }
    }

    /**
     * Creates a UnitOfWork with an explicit process instance ID.
     *
     * @param eventManager the event manager to pass to the delegate factory
     * @param processInstanceId the process instance ID to use for context
     * @return a process-instance-aware UnitOfWork
     */
    public UnitOfWork create(EventManager eventManager, String processInstanceId) {
        UnitOfWork baseUow = delegate.create(eventManager);
        return new ProcessInstanceAwareUnitOfWork(baseUow, processInstanceId);
    }

    /**
     * Gets the underlying delegate factory.
     *
     * @return the delegate UnitOfWorkFactory
     */
    public UnitOfWorkFactory getDelegate() {
        return delegate;
    }

    /**
     * Gets the current process instance ID from the context.
     *
     * @return the current process instance ID, or null if not available
     */
    private String getCurrentProcessInstanceId() {
        if (!org.kie.kogito.services.context.ProcessInstanceContext.hasContext()) {
            return null;
        }
        return org.kie.kogito.services.context.ProcessInstanceContext.getProcessInstanceId();
    }
}