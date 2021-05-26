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
package org.kie.kogito.process.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jbpm.process.core.timer.DateTimeUtils;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.process.instance.*;
import org.jbpm.workflow.core.impl.WorkflowProcessImpl;
import org.jbpm.workflow.core.node.StartNode;
import org.kie.api.runtime.process.EventListener;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessRuntime;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;
import org.kie.kogito.jobs.DurationExpirationTime;
import org.kie.kogito.jobs.ExactExpirationTime;
import org.kie.kogito.jobs.ExpirationTime;
import org.kie.kogito.jobs.ProcessJobDescription;
import org.kie.kogito.process.*;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;

@SuppressWarnings("unchecked")
public abstract class AbstractProcess implements Process {

    protected final ProcessRuntimeServiceProvider services;
    protected ProcessInstancesFactory processInstancesFactory;
    protected MutableProcessInstances instances;
    protected CompletionEventListener completionEventListener = new CompletionEventListener();

    protected Application app;

    protected boolean activated;
    protected List<String> startTimerInstances = new ArrayList<>();
    protected KogitoProcessRuntime processRuntime;

    protected AbstractProcess() {
        this(new LightProcessRuntimeServiceProvider());
    }

    protected AbstractProcess(ProcessConfig config) {
        this(new ConfiguredProcessServices(config));
    }

    protected AbstractProcess(ProcessRuntimeServiceProvider services) {
        this(services, Collections.emptyList(), null);
    }

    protected AbstractProcess(Application app, Collection<KogitoWorkItemHandler> handlers) {
        this(app, handlers, null);
    }

    protected AbstractProcess(Application app, Collection<KogitoWorkItemHandler> handlers, ProcessInstancesFactory factory) {
        this(new ConfiguredProcessServices(app.config().get(ProcessConfig.class)), handlers, factory);
        this.app = app;
    }

    protected AbstractProcess(ProcessRuntimeServiceProvider services, Collection<KogitoWorkItemHandler> handlers, ProcessInstancesFactory factory) {
        this.services = services;
        this.instances = new MapProcessInstances();
        this.processInstancesFactory = factory;
        KogitoWorkItemManager workItemManager = services.getKogitoWorkItemManager();
        for (KogitoWorkItemHandler handler : handlers) {
            workItemManager.registerWorkItemHandler(handler.getName(), handler);
        }
    }

    @Override
    public String id() {
        return process().getId();
    }

    public String name() {
        return process().getName();
    }

    @Override
    public Model createModel() {
        return null;
    }

    public abstract ProcessInstance createInstance(WorkflowProcessInstance wpi);

    public abstract ProcessInstance createReadOnlyInstance(WorkflowProcessInstance wpi);

    @Override
    public ProcessInstances instances() {
        return instances;
    }

    @Override
    public <S> void send(Signal<S> signal) {
        instances().values(ProcessInstanceReadMode.MUTABLE).forEach(pi -> pi.send(signal));
    }

    public Process configure() {

        registerListeners();
        if (isProcessFactorySet()) {
            this.instances = processInstancesFactory.createProcessInstances(this);
        }

        return this;
    }

    protected void registerListeners() {

    }

    @Override
    public void activate() {
        if (this.activated) {
            return;
        }

        configure();
        WorkflowProcessImpl p = (WorkflowProcessImpl) process();
        List<StartNode> startNodes = p.getTimerStart();
        if (startNodes != null && !startNodes.isEmpty()) {
            this.processRuntime = createProcessRuntime().getKogitoProcessRuntime();
            for (StartNode startNode : startNodes) {
                if (startNode != null && startNode.getTimer() != null) {
                    String timerId = processRuntime.getJobsService().scheduleProcessJob(ProcessJobDescription.of(configureTimerInstance(startNode.getTimer()), this));
                    startTimerInstances.add(timerId);
                }
            }
        }
        this.activated = true;
    }

    @Override
    public void deactivate() {
        for (String startTimerId : startTimerInstances) {
            this.processRuntime.getJobsService().cancelJob(startTimerId);
        }
        this.activated = false;
    }

    protected ExpirationTime configureTimerInstance(Timer timer) {
        switch (timer.getTimeType()) {
            case Timer.TIME_CYCLE:
                // when using ISO date/time period is not set
                long[] repeatValues = DateTimeUtils.parseRepeatableDateTime(timer.getDelay());
                if (repeatValues.length == 3) {
                    int parsedReapedCount = (int) repeatValues[0];
                    if (parsedReapedCount <= -1) {
                        parsedReapedCount = Integer.MAX_VALUE;
                    }
                    return DurationExpirationTime.repeat(repeatValues[1], repeatValues[2], parsedReapedCount);
                } else if (repeatValues.length == 2) {
                    return DurationExpirationTime.repeat(repeatValues[0], repeatValues[1], Integer.MAX_VALUE);
                } else {
                    return DurationExpirationTime.repeat(repeatValues[0], repeatValues[0], Integer.MAX_VALUE);
                }

            case Timer.TIME_DURATION:
                long duration = DateTimeUtils.parseDuration(timer.getDelay());
                return DurationExpirationTime.repeat(duration);

            case Timer.TIME_DATE:

                return ExactExpirationTime.of(timer.getDate());

            default:
                throw new UnsupportedOperationException("Not supported timer definition");
        }
    }

    public abstract org.kie.api.definition.process.Process process();

    protected InternalProcessRuntime createProcessRuntime() {
        return new LightProcessRuntime(new LightProcessRuntimeContext(Collections.singletonList(process())), services);
    }

    protected boolean isProcessFactorySet() {
        return processInstancesFactory != null;
    }

    public void setProcessInstancesFactory(ProcessInstancesFactory processInstancesFactory) {
        this.processInstancesFactory = processInstancesFactory;
    }

    public EventListener eventListener() {
        return completionEventListener;
    }

    protected class CompletionEventListener implements EventListener {

        public CompletionEventListener() {
            //Do nothing
        }

        @Override
        public void signalEvent(String type, Object event) {
            if (type.startsWith("processInstanceCompleted:")) {
                KogitoProcessInstance pi = (KogitoProcessInstance) event;
                if (!id().equals(pi.getProcessId()) && pi.getParentProcessInstanceStringId() != null) {
                    instances().findById(pi.getParentProcessInstanceStringId()).ifPresent(p -> p.send(Sig.of(type, event)));
                }
            }
        }

        @Override
        public String[] getEventTypes() {
            return new String[0];
        }
    }
}
