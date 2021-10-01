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
package org.kie.services.jobs.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.kie.kogito.Model;
import org.kie.kogito.jobs.JobDescription;
import org.kie.kogito.jobs.JobsService;
import org.kie.kogito.jobs.ProcessInstanceJobDescription;
import org.kie.kogito.jobs.ProcessJobDescription;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.Signal;
import org.kie.kogito.services.uow.UnitOfWorkExecutor;
import org.kie.kogito.timer.TimerInstance;
import org.kie.kogito.uow.UnitOfWorkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryJobService implements JobsService, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryJobService.class);
    protected static final String TRIGGER = "timer";

    protected final ScheduledThreadPoolExecutor scheduler;
    protected final UnitOfWorkManager unitOfWorkManager;

    protected ConcurrentHashMap<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();
    private final Processes processes;

    private static ConcurrentHashMap<Processes, InMemoryJobService> INSTANCE = new ConcurrentHashMap<>();

    protected InMemoryJobService(Processes processes, UnitOfWorkManager unitOfWorkManager) {
        this.processes = processes;
        this.unitOfWorkManager = unitOfWorkManager;
        this.scheduler = new ScheduledThreadPoolExecutor(10);
    }

    public static InMemoryJobService get(final Processes processes, final UnitOfWorkManager unitOfWorkManager) {
        Objects.requireNonNull(processes);
        Objects.requireNonNull(unitOfWorkManager);
        INSTANCE.putIfAbsent(processes, new InMemoryJobService(processes, unitOfWorkManager));
        return INSTANCE.get(processes);
    }

    @Override
    public String scheduleProcessJob(ProcessJobDescription description) {
        LOGGER.debug("ScheduleProcessJob: {}", description);
        ScheduledFuture<?> future;
        if (description.expirationTime().repeatInterval() != null) {
            future = scheduler.scheduleAtFixedRate(repeatableProcessJobByDescription(description), calculateDelay(description), description.expirationTime().repeatInterval(), TimeUnit.MILLISECONDS);
        } else {
            future = scheduler.schedule(processJobByDescription(description), calculateDelay(description), TimeUnit.MILLISECONDS);
        }
        scheduledJobs.put(description.id(), future);
        return description.id();
    }

    @Override
    public String scheduleProcessInstanceJob(ProcessInstanceJobDescription description) {
        ScheduledFuture<?> future;
        if (description.expirationTime().repeatInterval() != null) {
            future = scheduler.scheduleAtFixedRate(
                    getSignalProcessInstanceCommand(description, false, description.expirationTime().repeatLimit()),
                    calculateDelay(description), description.expirationTime().repeatInterval(), TimeUnit.MILLISECONDS);
        } else {
            future = scheduler.schedule(getSignalProcessInstanceCommand(description, true, 1), calculateDelay(description),
                    TimeUnit.MILLISECONDS);
        }
        scheduledJobs.put(description.id(), future);
        return description.id();
    }

    public Runnable getSignalProcessInstanceCommand(ProcessInstanceJobDescription description, boolean remove, int limit) {
        return new SignalProcessInstanceOnExpiredTimer(description.id(), description
                .processInstanceId(), description.processId(), remove, limit);
    }

    @Override
    public boolean cancelJob(String id) {
        return cancelJob(id, true);
    }

    public boolean cancelJob(String id, boolean force) {
        LOGGER.debug("Cancel Job: {}", id);
        if (scheduledJobs.containsKey(id)) {
            return scheduledJobs.remove(id).cancel(force);
        }
        return false;
    }

    @Override
    public ZonedDateTime getScheduledTime(String id) {
        if (scheduledJobs.containsKey(id)) {
            ScheduledFuture<?> scheduled = scheduledJobs.get(id);
            long remainingTime = scheduled.getDelay(TimeUnit.MILLISECONDS);
            if (remainingTime > 0) {
                return ZonedDateTime.from(Instant.ofEpochMilli(System.currentTimeMillis() + remainingTime));
            }
        }
        return null;
    }

    protected long calculateDelay(JobDescription description) {
        long delay = Duration.between(ZonedDateTime.now(), description.expirationTime().get()).toMillis();
        if (delay <= 0) {
            return 1;
        }
        return delay;
    }

    protected Runnable processJobByDescription(ProcessJobDescription description) {
        return new StartProcessOnExpiredTimer(description.id(), description.process(), true, -1);
    }

    protected Runnable repeatableProcessJobByDescription(ProcessJobDescription description) {
        return new StartProcessOnExpiredTimer(description.id(), description.process(), false, description.expirationTime().repeatLimit());
    }

    private class JobSignal implements Signal<TimerInstance> {

        String signal;
        TimerInstance payload;

        public JobSignal(String signal, TimerInstance payload) {
            this.signal = signal;
            this.payload = payload;
        }

        @Override
        public String channel() {
            return this.signal;
        }

        @Override
        public TimerInstance payload() {
            return payload;
        }

        @Override
        public String referenceId() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof JobSignal)) {
                return false;
            }
            JobSignal jobSignal = (JobSignal) o;
            return Objects.equals(signal, jobSignal.signal) &&
                    Objects.equals(payload, jobSignal.payload);
        }

        @Override
        public int hashCode() {
            return Objects.hash(signal, payload);
        }
    }

    private class SignalProcessInstanceOnExpiredTimer implements Runnable {

        private final String id;
        private boolean removeAtExecution;
        private String processInstanceId;
        private Integer limit;
        private String processId;

        private SignalProcessInstanceOnExpiredTimer(String id, String processInstanceId, String processId, boolean removeAtExecution, Integer limit) {
            this.id = id;
            this.processInstanceId = processInstanceId;
            this.removeAtExecution = removeAtExecution;
            this.limit = limit;
            this.processId = processId;
        }

        @Override
        public void run() {
            try {
                LOGGER.info("Job {} started", id);
                UnitOfWorkExecutor.executeInUnitOfWork(unitOfWorkManager, () -> {
                    Process<? extends Model> process = processes.processById(processId);
                    Optional<? extends ProcessInstance<?>> pi = process.instances().findById(processInstanceId);
                    if (pi.isPresent()) {
                        String[] ids = id.split("_");
                        try {
                            long timerId = Long.parseLong(ids[1]);
                            pi.get().send(new JobSignal("timerTriggered", TimerInstance.with(timerId, id, --limit)));
                        } catch (NumberFormatException e) {
                            //todo check id != long
                            pi.get().send(new JobSignal("timerTriggered:" + ids[1], null));
                        }
                        if (limit == 0) {
                            cancelJob(id, false);
                        }
                    } else {
                        // since owning process instance does not exist cancel timers
                        cancelJob(id, false);
                    }
                    return null;
                });
                LOGGER.debug("Job {} completed", id);
            } finally {
                if (removeAtExecution) {
                    cancelJob(id, true);
                }
            }
        }
    }

    private class StartProcessOnExpiredTimer implements Runnable {

        private final String id;

        private boolean removeAtExecution;
        @SuppressWarnings("rawtypes")
        private org.kie.kogito.process.Process process;

        private Integer limit;

        private StartProcessOnExpiredTimer(String id, org.kie.kogito.process.Process<?> process, boolean removeAtExecution, Integer limit) {
            this.id = id;
            this.process = process;
            this.removeAtExecution = removeAtExecution;
            this.limit = limit;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            try {
                LOGGER.debug("Job {} started", id);
                UnitOfWorkExecutor.executeInUnitOfWork(unitOfWorkManager, () -> {
                    org.kie.kogito.process.ProcessInstance<?> pi = process.createInstance(process.createModel());
                    if (pi != null) {
                        pi.start(TRIGGER, null);
                    }

                    return null;
                });
                limit--;
                if (limit == 0) {
                    cancelJob(id, false);
                }
                LOGGER.debug("Job {} completed", id);
            } finally {
                if (removeAtExecution) {
                    cancelJob(id, true);
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        scheduledJobs.clear();
        scheduler.shutdown();
    }
}
