/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.services.jobs.impl;

import java.util.concurrent.atomic.AtomicInteger;

import org.kie.api.runtime.process.ProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessRuntime;
import org.kie.kogito.jobs.ProcessInstanceJobDescription;
import org.kie.kogito.jobs.ProcessJobDescription;
import org.kie.kogito.services.uow.UnitOfWorkExecutor;
import org.kie.kogito.timer.TimerInstance;
import org.kie.kogito.uow.UnitOfWorkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.kogito.services.jobs.impl.TriggerJobCommand.SIGNAL;

public class LegacyInMemoryJobService extends InMemoryJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyInMemoryJobService.class);
    private KogitoProcessRuntime processRuntime;

    public LegacyInMemoryJobService(KogitoProcessRuntime processRuntime, UnitOfWorkManager unitOfWorkManager) {
        super(null, unitOfWorkManager);
        this.processRuntime = processRuntime;
    }

    @Override
    public Runnable getSignalProcessInstanceCommand(String jobId, ProcessInstanceJobDescription description, boolean remove, int limit) {
        AtomicInteger counter = new AtomicInteger(limit);
        return () -> {
            try {
                UnitOfWorkExecutor.executeInUnitOfWork(unitOfWorkManager, () -> {
                    ProcessInstance pi = processRuntime.getProcessInstance(description.processInstanceId());
                    if (pi != null) {
                        pi.signalEvent(SIGNAL, TimerInstance.with(jobId, description.timerId(), counter.decrementAndGet()));
                        if (counter.get() == 0) {
                            cancelJob(jobId, false);
                        }
                    } else {
                        // since owning process instance does not exist cancel timers
                        cancelJob(jobId, false);
                    }
                    return null;
                });
                LOGGER.debug("Job {} completed", jobId);
            } finally {
                if (remove) {
                    cancelJob(jobId);
                }
            }
        };
    }

    @Override
    protected Runnable processJobByDescription(String jobId, ProcessJobDescription description) {
        return processCommand(jobId, description, true);
    }

    private Runnable processCommand(String jobId, ProcessJobDescription description, boolean remove) {
        AtomicInteger counter = new AtomicInteger(description.expirationTime().repeatLimit());
        String processId = description.processId();
        return () -> {
            try {
                LOGGER.debug("Job {} started", jobId);
                UnitOfWorkExecutor.executeInUnitOfWork(unitOfWorkManager, () -> {
                    KogitoProcessInstance pi = processRuntime.createProcessInstance(processId, null);
                    if (pi != null) {
                        processRuntime.startProcessInstance(pi.getStringId(), TRIGGER);
                    }
                    return null;
                });
                if (counter.decrementAndGet() == 0) {
                    cancelJob(jobId, false);
                }
                LOGGER.debug("Job {} completed", jobId);
            } finally {
                if (remove) {
                    cancelJob(jobId);
                }
            }
        };
    }

    @Override
    protected Runnable repeatableProcessJobByDescription(String jobId, ProcessJobDescription description) {
        return processCommand(jobId, description, false);
    }
}
