/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.process.instance;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.WorkingMemoryAction;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.event.ProcessEventSupport;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.kogito.jobs.InMemoryJobService;
import org.drools.core.marshalling.impl.MarshallerReaderContext;
import org.drools.core.marshalling.impl.MarshallerWriteContext;
import org.drools.core.marshalling.impl.ProtobufMessages.ActionQueue.Action;
import org.drools.core.phreak.PropagationEntry;
import org.drools.core.time.TimeUtils;
import org.drools.core.time.TimerService;
import org.drools.core.time.impl.CommandServiceTimerJobFactoryManager;
import org.drools.core.time.impl.ThreadSafeTrackableTimeJobFactoryManager;
import org.jbpm.process.core.event.EventFilter;
import org.jbpm.process.core.event.EventTransformer;
import org.jbpm.process.core.event.EventTypeFilter;
import org.jbpm.process.core.timer.BusinessCalendar;
import org.jbpm.process.core.timer.DateTimeUtils;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.node.EventTrigger;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.Trigger;
import org.kie.api.KieBase;
import org.kie.api.command.ExecutableCommand;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.EventListener;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.command.RegistryContext;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.internal.utils.CompositeClassLoader;
import org.kie.kogito.jobs.DurationExpirationTime;
import org.kie.kogito.jobs.ExactExpirationTime;
import org.kie.kogito.jobs.ExpirationTime;
import org.kie.kogito.jobs.JobsService;
import org.kie.kogito.jobs.ProcessJobDescription;
import org.kie.kogito.services.uow.CollectingUnitOfWorkFactory;
import org.kie.kogito.services.uow.DefaultUnitOfWorkManager;
import org.kie.kogito.signal.SignalManager;
import org.kie.kogito.uow.UnitOfWorkManager;

public class ProcessRuntimeImpl implements InternalProcessRuntime {

    private InternalKnowledgeRuntime kruntime;
    private ProcessInstanceManager processInstanceManager;
    private SignalManager signalManager;
    private JobsService jobService;
    private ProcessEventSupport processEventSupport;
    private UnitOfWorkManager unitOfWorkManager;

    public ProcessRuntimeImpl(InternalKnowledgeRuntime kruntime) {
        this.kruntime = kruntime;
        TimerService timerService = kruntime.getTimerService();
        if (!(timerService.getTimerJobFactoryManager() instanceof CommandServiceTimerJobFactoryManager)) {
            timerService.setTimerJobFactoryManager(new ThreadSafeTrackableTimeJobFactoryManager());
        }

        ((CompositeClassLoader) getRootClassLoader()).addClassLoader(getClass().getClassLoader());
        initProcessInstanceManager();
        initSignalManager();
        unitOfWorkManager = new DefaultUnitOfWorkManager(new CollectingUnitOfWorkFactory());
        jobService = new InMemoryJobService(this, unitOfWorkManager);
        processEventSupport = new ProcessEventSupport(unitOfWorkManager);
        if (isActive()) {
            initProcessEventListeners();
            initStartTimers();
        }
        initProcessActivationListener();
    }

    public ProcessRuntimeImpl(InternalWorkingMemory workingMemory) {
        TimerService timerService = workingMemory.getTimerService();
        if (!(timerService.getTimerJobFactoryManager() instanceof CommandServiceTimerJobFactoryManager)) {
            timerService.setTimerJobFactoryManager(new ThreadSafeTrackableTimeJobFactoryManager());
        }

        this.kruntime = (InternalKnowledgeRuntime) workingMemory.getKnowledgeRuntime();
        initProcessInstanceManager();
        initSignalManager();
        unitOfWorkManager = new DefaultUnitOfWorkManager(new CollectingUnitOfWorkFactory());
        jobService = new InMemoryJobService(this, unitOfWorkManager);
        processEventSupport = new ProcessEventSupport(unitOfWorkManager);
        if (isActive()) {
            initProcessEventListeners();
            initStartTimers();
        }
        initProcessActivationListener();
    }

    public void initStartTimers() {
        KieBase kbase = kruntime.getKieBase();
        Collection<Process> processes = kbase.getProcesses();
        for (Process process : processes) {
            RuleFlowProcess p = (RuleFlowProcess) process;
            List<StartNode> startNodes = p.getTimerStart();
            if (startNodes != null && !startNodes.isEmpty()) {

                for (StartNode startNode : startNodes) {
                    if (startNode != null && startNode.getTimer() != null) {
                        jobService.scheduleProcessJob(ProcessJobDescription.of(createTimerInstance(startNode.getTimer(), kruntime), p.getId()));
                    }
                }
            }
        }
    }

    private void initProcessInstanceManager() {
        processInstanceManager = new DefaultProcessInstanceManagerFactory().createProcessInstanceManager(kruntime);
    }

    private void initSignalManager() {
        signalManager = new DefaultSignalManagerFactory().createSignalManager(kruntime);
    }

    private ClassLoader getRootClassLoader() {
        KieBase kbase = ((InternalKnowledgeBase) kruntime.getKieBase());
        if (kbase != null) {
            return ((InternalKnowledgeBase) kbase).getRootClassLoader();
        }
        CompositeClassLoader result = new CompositeClassLoader();
        result.addClassLoader(Thread.currentThread().getContextClassLoader());
        return result;
    }

    public ProcessInstance startProcess(final String processId) {
        return startProcess(processId, null);
    }

    public ProcessInstance startProcess(String processId,
                                        Map<String, Object> parameters) {
        return startProcess(processId, parameters, null);
    }

    public ProcessInstance startProcess(String processId,
                                        Map<String, Object> parameters, String trigger) {
        ProcessInstance processInstance = createProcessInstance(processId, parameters);
        if (processInstance != null) {
            // start process instance
            return startProcessInstance(processInstance.getId(), trigger);
        }
        return null;
    }

    public ProcessInstance createProcessInstance(String processId,
                                                 Map<String, Object> parameters) {
        return createProcessInstance(processId, null, parameters);
    }

    public ProcessInstance startProcessInstance(String processInstanceId, String trigger) {
        try {
            kruntime.startOperation();

            ProcessInstance processInstance = getProcessInstance(processInstanceId);
            ((org.jbpm.process.instance.ProcessInstance) processInstance).configureSLA();
            getProcessEventSupport().fireBeforeProcessStarted(processInstance, kruntime);
            ((org.jbpm.process.instance.ProcessInstance) processInstance).start(trigger);
            getProcessEventSupport().fireAfterProcessStarted(processInstance, kruntime);
            return processInstance;
        } finally {
            kruntime.endOperation();
        }
    }

    public ProcessInstance startProcessInstance(String processInstanceId) {
        return startProcessInstance(processInstanceId, null);
    }

    @Override
    public ProcessInstance startProcess(String processId, CorrelationKey correlationKey, Map<String, Object> parameters) {
        ProcessInstance processInstance = createProcessInstance(processId, correlationKey, parameters);
        if (processInstance != null) {
            return startProcessInstance(processInstance.getId());
        }
        return null;
    }

    @Override
    public ProcessInstance createProcessInstance(String processId, CorrelationKey correlationKey, Map<String, Object> parameters) {
        try {
            kruntime.startOperation();

            final Process process = kruntime.getKieBase().getProcess(processId);
            if (process == null) {
                throw new IllegalArgumentException("Unknown process ID: " + processId);
            }
            return startProcess(process, correlationKey, parameters);
        } finally {
            kruntime.endOperation();
        }
    }

    @Override
    public ProcessInstance getProcessInstance(CorrelationKey correlationKey) {
        return processInstanceManager.getProcessInstance(correlationKey);
    }

    private org.jbpm.process.instance.ProcessInstance startProcess(Process process, CorrelationKey correlationKey, Map<String, Object> parameters) {
        ProcessInstanceFactory conf = ProcessInstanceFactoryRegistry.INSTANCE.getProcessInstanceFactory(process);
        if (conf == null) {
            throw new IllegalArgumentException("Illegal process type: " + process.getClass());
        }
        return conf.createProcessInstance(process,
                                          correlationKey,
                                          kruntime,
                                          parameters);
    }

    public ProcessInstanceManager getProcessInstanceManager() {
        return processInstanceManager;
    }

    public JobsService getJobsService() {
        return jobService;
    }

    public SignalManager getSignalManager() {
        return signalManager;
    }

    public Collection<ProcessInstance> getProcessInstances() {
        return processInstanceManager.getProcessInstances();
    }

    public ProcessInstance getProcessInstance(String id) {
        return getProcessInstance(id, false);
    }

    public ProcessInstance getProcessInstance(String id, boolean readOnly) {
        return processInstanceManager.getProcessInstance(id, readOnly);
    }

    public void removeProcessInstance(ProcessInstance processInstance) {
        processInstanceManager.removeProcessInstance(processInstance);
    }

    public void initProcessEventListeners() {
        for (Process process : kruntime.getKieBase().getProcesses()) {
            initProcessEventListener(process);
        }
    }

    public void removeProcessEventListeners() {
        for (Process process : kruntime.getKieBase().getProcesses()) {
            removeProcessEventListener(process);
        }
    }

    private void removeProcessEventListener(Process process) {
        if (process instanceof RuleFlowProcess) {
            String type = (String) ((RuleFlowProcess) process).getRuntimeMetaData().get("StartProcessEventType");
            StartProcessEventListener listener = (StartProcessEventListener) ((RuleFlowProcess) process).getRuntimeMetaData().get("StartProcessEventListener");
            if (type != null && listener != null) {
                signalManager.removeEventListener(type, listener);
            }
        }
    }

    private void initProcessEventListener(Process process) {
        if (process instanceof RuleFlowProcess) {
            for (Node node : ((RuleFlowProcess) process).getNodes()) {
                if (node instanceof StartNode) {
                    StartNode startNode = (StartNode) node;
                    if (startNode != null) {
                        List<Trigger> triggers = startNode.getTriggers();
                        if (triggers != null) {
                            for (Trigger trigger : triggers) {
                                if (trigger instanceof EventTrigger) {
                                    final List<EventFilter> filters = ((EventTrigger) trigger).getEventFilters();
                                    String type = null;
                                    for (EventFilter filter : filters) {
                                        if (filter instanceof EventTypeFilter) {
                                            type = ((EventTypeFilter) filter).getType();
                                        }
                                    }
                                    StartProcessEventListener listener = new StartProcessEventListener(process.getId(),
                                                                                                       filters,
                                                                                                       trigger.getInMappings(),
                                                                                                       startNode.getEventTransformer());
                                    signalManager.addEventListener(type, listener);
                                    ((RuleFlowProcess) process).getRuntimeMetaData().put("StartProcessEventType", type);
                                    ((RuleFlowProcess) process).getRuntimeMetaData().put("StartProcessEventListener", listener);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public ProcessEventSupport getProcessEventSupport() {
        return processEventSupport;
    }

    public void setProcessEventSupport(ProcessEventSupport processEventSupport) {
        this.processEventSupport = processEventSupport;
    }

    public void addEventListener(final ProcessEventListener listener) {
        this.processEventSupport.addEventListener(listener);
    }

    public void removeEventListener(final ProcessEventListener listener) {
        this.processEventSupport.removeEventListener(listener);
    }

    public List<ProcessEventListener> getProcessEventListeners() {
        return processEventSupport.getEventListeners();
    }

    private void initProcessActivationListener() {
        kruntime.addEventListener(new DefaultAgendaEventListener() {
            public void matchCreated(MatchCreatedEvent event) {
                String ruleFlowGroup = ((RuleImpl) event.getMatch().getRule()).getRuleFlowGroup();
                if ("DROOLS_SYSTEM".equals(ruleFlowGroup)) {
                    // new activations of the rule associate with a state node
                    // signal process instances of that state node
                    String ruleName = event.getMatch().getRule().getName();
                    if (ruleName.startsWith("RuleFlowStateNode-")) {
                        int index = ruleName.indexOf('-',
                                                     18);
                        index = ruleName.indexOf('-',
                                                 index + 1);
                        String eventType = ruleName.substring(0,
                                                              index);

                        kruntime.queueWorkingMemoryAction(new SignalManagerSignalAction(eventType, event));
                    } else if (ruleName.startsWith("RuleFlowStateEventSubProcess-")
                            || ruleName.startsWith("RuleFlowStateEvent-")
                            || ruleName.startsWith("RuleFlow-Milestone-")
                            || ruleName.startsWith("RuleFlow-AdHocComplete-")
                            || ruleName.startsWith("RuleFlow-AdHocActivate-")) {
                        kruntime.queueWorkingMemoryAction(new SignalManagerSignalAction(ruleName, event));
                    }
                } else {
                    String ruleName = event.getMatch().getRule().getName();
                    if (ruleName.startsWith("RuleFlow-Start-")) {
                        String processId = ruleName.replace("RuleFlow-Start-", "");

                        startProcessWithParamsAndTrigger(processId, null, "conditional", true);
                    }
                }
            }
        });

        kruntime.addEventListener(new DefaultAgendaEventListener() {
            public void afterRuleFlowGroupDeactivated(final RuleFlowGroupDeactivatedEvent event) {
                if (kruntime instanceof StatefulKnowledgeSession) {
                    signalManager.signalEvent("RuleFlowGroup_" + event.getRuleFlowGroup().getName() + "_" + ((StatefulKnowledgeSession) kruntime).getIdentifier(),
                                              null);
                } else {
                    signalManager.signalEvent("RuleFlowGroup_" + event.getRuleFlowGroup().getName(), null);
                }
            }
        });
    }

    private void startProcessWithParamsAndTrigger(String processId, Map<String, Object> params, String type, boolean dispose) {

        startProcess(processId, params, type);
    }

    public void abortProcessInstance(String processInstanceId) {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        if (processInstance == null) {
            throw new IllegalArgumentException("Could not find process instance for id " + processInstanceId);
        }
        ((org.jbpm.process.instance.ProcessInstance) processInstance).setState(ProcessInstance.STATE_ABORTED);
    }

    public WorkItemManager getWorkItemManager() {
        return kruntime.getWorkItemManager();
    }

    @Override
    public UnitOfWorkManager getUnitOfWorkManager() {
        return unitOfWorkManager;
    }

    public void signalEvent(String type, Object event) {
        signalManager.signalEvent(type, event);
    }

    public void signalEvent(String type, Object event, String processInstanceId) {
        signalManager.signalEvent(processInstanceId, type, event);
    }

    public void dispose() {
        this.processEventSupport.reset();
        kruntime = null;
    }

    public void clearProcessInstances() {
        this.processInstanceManager.clearProcessInstances();
    }

    public void clearProcessInstancesState() {
        this.processInstanceManager.clearProcessInstancesState();
    }

    public boolean isActive() {
        Boolean active = (Boolean) kruntime.getEnvironment().get("Active");
        if (active == null) {
            return true;
        }

        return active.booleanValue();
    }

    protected ExpirationTime createTimerInstance(Timer timer, InternalKnowledgeRuntime kruntime) {
        if (kruntime != null && kruntime.getEnvironment().get("jbpm.business.calendar") != null) {
            BusinessCalendar businessCalendar = (BusinessCalendar) kruntime.getEnvironment().get("jbpm.business.calendar");

            long delay = businessCalendar.calculateBusinessTimeAsDuration(timer.getDelay());

            if (timer.getPeriod() == null) {
                return DurationExpirationTime.repeat(delay);
            } else {
                long period = businessCalendar.calculateBusinessTimeAsDuration(timer.getPeriod());

                return DurationExpirationTime.repeat(delay, period);
            }
        } else {
            return configureTimerInstance(timer);
        }
    }

    private ExpirationTime configureTimerInstance(Timer timer) {
        long duration = -1;
        switch (timer.getTimeType()) {
            case Timer.TIME_CYCLE:
                // when using ISO date/time period is not set
                long[] repeatValues = DateTimeUtils.parseRepeatableDateTime(timer.getDelay());
                if (repeatValues.length == 3) {
                    int parsedReapedCount = (int) repeatValues[0];

                    return DurationExpirationTime.repeat(repeatValues[1], repeatValues[2], parsedReapedCount);
                } else {
                    long delay = repeatValues[0];
                    long period = -1;
                    try {
                        period = TimeUtils.parseTimeString(timer.getPeriod());
                    } catch (RuntimeException e) {
                        period = repeatValues[0];
                    }

                    return DurationExpirationTime.repeat(delay, period);
                }

            case Timer.TIME_DURATION:

                duration = DateTimeUtils.parseDuration(timer.getDelay());
                return DurationExpirationTime.after(duration);

            case Timer.TIME_DATE:

                return ExactExpirationTime.of(timer.getDate());

            default:
                throw new UnsupportedOperationException("Not supported timer definition");
        }
    }

    @Override
    public InternalKnowledgeRuntime getInternalKieRuntime() {
        return this.kruntime;
    }

    private class StartProcessEventListener implements EventListener {

        private String processId;
        private List<EventFilter> eventFilters;
        private Map<String, String> inMappings;
        private EventTransformer eventTransformer;

        public StartProcessEventListener(String processId,
                                         List<EventFilter> eventFilters,
                                         Map<String, String> inMappings,
                                         EventTransformer eventTransformer) {
            this.processId = processId;
            this.eventFilters = eventFilters;
            this.inMappings = inMappings;
            this.eventTransformer = eventTransformer;
        }

        public String[] getEventTypes() {
            return null;
        }

        public void signalEvent(final String type,
                                Object event) {
            for (EventFilter filter : eventFilters) {
                if (!filter.acceptsEvent(type,
                                         event)) {
                    return;
                }
            }
            if (eventTransformer != null) {
                event = eventTransformer.transformEvent(event);
            }
            Map<String, Object> params = null;
            if (inMappings != null && !inMappings.isEmpty()) {
                params = new HashMap<String, Object>();

                if (inMappings.size() == 1) {
                    params.put(inMappings.keySet().iterator().next(), event);
                } else {
                    for (Map.Entry<String, String> entry : inMappings.entrySet()) {
                        if ("event".equals(entry.getValue())) {
                            params.put(entry.getKey(),
                                       event);
                        } else {
                            params.put(entry.getKey(),
                                       entry.getValue());
                        }
                    }
                }
            }
            startProcessWithParamsAndTrigger(processId, params, type, false);
        }
    }

    private class StartProcessWithTypeCommand implements ExecutableCommand<Void> {

        private static final long serialVersionUID = -8890906804846111698L;

        private String processId;
        private Map<String, Object> params;
        private String type;

        private StartProcessWithTypeCommand(String processId, Map<String, Object> params, String type) {
            this.processId = processId;
            this.params = params;
            this.type = type;
        }

        @Override
        public Void execute(Context context) {
            KieSession ksession = ((RegistryContext) context).lookup(KieSession.class);
            ((ProcessRuntimeImpl) ((InternalKnowledgeRuntime) ksession).getProcessRuntime()).startProcess(processId,
                                                                                                          params, type);

            return null;
        }
    }

    public class SignalManagerSignalAction extends PropagationEntry.AbstractPropagationEntry implements WorkingMemoryAction {

        private String type;
        private Object event;

        public SignalManagerSignalAction(String type, Object event) {
            this.type = type;
            this.event = event;
        }

        public SignalManagerSignalAction(MarshallerReaderContext context) throws IOException, ClassNotFoundException {
            type = context.readUTF();
            if (context.readBoolean()) {
                event = context.readObject();
            }
        }

        public void execute(InternalWorkingMemory workingMemory) {

            signalEvent(type, event);
        }

        public void execute(InternalKnowledgeRuntime kruntime) {
            signalEvent(type, event);
        }

        public Action serialize(MarshallerWriteContext context) throws IOException {
            return null;
        }
    }
}
