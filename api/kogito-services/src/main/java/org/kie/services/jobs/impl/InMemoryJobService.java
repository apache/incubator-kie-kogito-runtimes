/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.services.jobs.impl;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.ProcessRuntime;
import org.kie.kogito.jobs.JobDescription;
import org.kie.kogito.jobs.JobsService;
import org.kie.kogito.jobs.ProcessInstanceJobDescription;
import org.kie.kogito.jobs.ProcessJobDescription;
import org.kie.services.time.TimerInstance;


public class InMemoryJobService implements JobsService {

    protected final ScheduledThreadPoolExecutor scheduler;
    protected final ProcessRuntime processRuntime;
    
    protected ConcurrentHashMap<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();
    
    public InMemoryJobService(ProcessRuntime processRuntime) {
        this.scheduler = new ScheduledThreadPoolExecutor(1);
        this.processRuntime = processRuntime;
    }
    
    public InMemoryJobService(int threadPoolSize, ProcessRuntime processRuntime) {
        this.scheduler = new ScheduledThreadPoolExecutor(threadPoolSize);
        this.processRuntime = processRuntime;
    }
    

    @Override
    public String scheduleProcessJob(ProcessJobDescription description) {
        ScheduledFuture<?> future = null;
        if (description.expirationTime().repeatInterval() != null) {
            future = scheduler.scheduleAtFixedRate(new StartProcessOnExpiredTimer(description.id(), description.processId(), false, description.expirationTime().repeatLimit()), calculateDelay(description), description.expirationTime().repeatInterval(), TimeUnit.MILLISECONDS);
        } else {
        
            future = scheduler.schedule(new StartProcessOnExpiredTimer(description.id(), description.processId(), true, -1), calculateDelay(description), TimeUnit.MILLISECONDS);
        }
        scheduledJobs.put(description.id(), future);
        return description.id();
    }
    
    @Override
    public String scheduleProcessInstanceJob(ProcessInstanceJobDescription description) {
        ScheduledFuture<?> future = null;
        if (description.expirationTime().repeatInterval() != null) {
            future = scheduler.scheduleAtFixedRate(new SignalProcessInstanceOnExpiredTimer(description.id(), description.processInstanceId(), false, description.expirationTime().repeatLimit()), calculateDelay(description), description.expirationTime().repeatInterval(), TimeUnit.MILLISECONDS);
        } else {
        
            future = scheduler.schedule(new SignalProcessInstanceOnExpiredTimer(description.id(), description.processInstanceId(), true, -1), calculateDelay(description), TimeUnit.MILLISECONDS);
        }
        scheduledJobs.put(description.id(), future);
        return description.id();
    }

    @Override
    public boolean cancelJob(String id) {
        if (scheduledJobs.containsKey(id)) {
            return scheduledJobs.remove(id).cancel(true);
        }
        
        return false;
    }
    
    protected long calculateDelay(JobDescription description) {
        return Duration.between(ZonedDateTime.now(), description.expirationTime().get()).toMillis();
        
    }

    private class SignalProcessInstanceOnExpiredTimer implements Runnable {
        private final String id;
        
        private boolean removeAtExecution;
        private String processInstanceId;
        
        private Integer limit;
        
        private SignalProcessInstanceOnExpiredTimer(String id, String processInstanceId, boolean removeAtExecution, Integer limit) {
            this.id = id;
            this.processInstanceId = processInstanceId;
            this.removeAtExecution = removeAtExecution;
            this.limit = limit;
        }
        @Override
        public void run() {
            try {
                ProcessInstance pi = processRuntime.getProcessInstance(processInstanceId);
                if (pi != null) {
                    String[] ids = id.split("_");
                    limit--;
                    pi.signalEvent("timerTriggered", TimerInstance.with(Long.valueOf(ids[1]), id, limit));
                    
                    if (limit == 0) {
                        scheduledJobs.remove(id).cancel(false);
                    }
                } else {
                    // since owning process instance does not exist cancel timers
                    scheduledJobs.remove(id).cancel(false);
                }
            } finally {
                if (removeAtExecution) {
                    scheduledJobs.remove(id);
                }
            }
        }
    }

    private class StartProcessOnExpiredTimer implements Runnable {
        private final String id;
        
        private boolean removeAtExecution;
        private String processId;

        private Integer limit;
        
        private StartProcessOnExpiredTimer(String id, String processId, boolean removeAtExecution, Integer limit) {
            this.id = id;
            this.processId = processId;
            this.removeAtExecution = removeAtExecution;
            this.limit = limit;
        }
        @Override
        public void run() {
            try {
                ProcessInstance pi = processRuntime.createProcessInstance(processId, null);
                if (pi != null) {
                    processRuntime.startProcessInstance(pi.getId(), "timer");
                }
                
                limit--;
                if (limit == 0) {
                    scheduledJobs.remove(id).cancel(false);
                }
            } finally {
                if (removeAtExecution) {
                    scheduledJobs.remove(id);
                }
            }
        }
    }
}
