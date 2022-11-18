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
package org.jbpm.process.instance;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.common.ReteEvaluator;
import org.drools.core.common.WorkingMemoryAction;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.phreak.PropagationEntry;
import org.drools.core.time.TimeUtils;
import org.jbpm.process.core.event.EventFilter;
import org.jbpm.process.core.event.EventTypeFilter;
import org.jbpm.process.core.timer.BusinessCalendar;
import org.jbpm.process.core.timer.DateTimeUtils;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.impl.DataAssociation;
import org.jbpm.workflow.core.impl.NodeIoHelper;
import org.jbpm.workflow.core.node.EventTrigger;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.Trigger;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.kie.api.runtime.process.EventListener;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.kogito.Application;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;
import org.kie.kogito.jobs.DurationExpirationTime;
import org.kie.kogito.jobs.ExactExpirationTime;
import org.kie.kogito.jobs.ExpirationTime;
import org.kie.kogito.jobs.JobsService;
import org.kie.kogito.jobs.ProcessJobDescription;
import org.kie.kogito.process.Processes;
import org.kie.kogito.services.jobs.impl.InMemoryJobService;
import org.kie.kogito.signal.SignalManager;
import org.kie.kogito.uow.UnitOfWorkManager;

import static org.jbpm.ruleflow.core.Metadata.TRIGGER_MAPPING_INPUT;

public class LightProcessRuntime extends AbstractProcessRuntime {
    private ProcessRuntimeContext runtimeContext;
    private final InternalKnowledgeRuntime knowledgeRuntime;

    private ProcessInstanceManager processInstanceManager;
    private SignalManager signalManager;
    private JobsService jobService;
    private final KogitoWorkItemManager workItemManager;
    private UnitOfWorkManager unitOfWorkManager;

    public static LightProcessRuntime of(Application app, Collection<Process> process, ProcessRuntimeServiceProvider services) {
        return new LightProcessRuntime(new LightProcessRuntimeContext(process), services, app);
    }

    protected LightProcessRuntime(ProcessRuntimeContext runtimeContext, ProcessRuntimeServiceProvider services) {
        this(runtimeContext, services, null);
    }

    protected LightProcessRuntime(ProcessRuntimeContext runtimeContext, ProcessRuntimeServiceProvider services, Application application) {
        super(application);
        this.unitOfWorkManager = services.getUnitOfWorkManager();
        this.knowledgeRuntime = new DummyKnowledgeRuntime(this);
        this.runtimeContext = runtimeContext;
        this.processInstanceManager = services.getProcessInstanceManager();
        this.signalManager = services.getSignalManager();
        this.jobService = services.getJobsService() == null ? InMemoryJobService.get(application.get(Processes.class), this.unitOfWorkManager) : services.getJobsService();
        this.processEventSupport = services.getEventSupport();
        this.workItemManager = services.getKogitoWorkItemManager();
        if (isActive()) {
            initProcessEventListeners();
            initStartTimers();
        }
        initProcessActivationListener();
    }

    public void initStartTimers() {
        Collection<Process> processes = runtimeContext.getProcesses();
        for (Process process : processes) {
            RuleFlowProcess p = (RuleFlowProcess) process;
            List<StartNode> startNodes = p.getTimerStart();
            if (startNodes != null && !startNodes.isEmpty()) {
                for (StartNode startNode : startNodes) {
                    if (startNode != null && startNode.getTimer() != null) {

                        jobService.scheduleProcessJob(ProcessJobDescription.of(createTimerInstance(startNode.getTimer(), knowledgeRuntime), p.getId()));

                    }
                }
            }
        }
    }

    @Override
    public ProcessInstance startProcess(String processId) {
        return startProcess(processId, null, null, null);
    }

    @Override
    public ProcessInstance startProcess(String processId, Map<String, Object> parameters) {
        return startProcess(processId, parameters, null, null);
    }

    public ProcessInstance startProcess(String processId, Map<String, Object> parameters, String trigger) {
        return startProcess(processId, parameters, trigger, null);
    }

    @Override
    public ProcessInstance startProcess(String processId, AgendaFilter agendaFilter) {
        return startProcess(processId, null, null, agendaFilter);
    }

    @Override
    public ProcessInstance startProcess(String processId, Map<String, Object> parameters, AgendaFilter agendaFilter) {
        return startProcess(processId, parameters, null, agendaFilter);
    }

    private ProcessInstance startProcess(String processId, Map<String, Object> parameters, String trigger, AgendaFilter agendaFilter) {
        KogitoProcessInstance processInstance = createProcessInstance(processId, parameters);
        if (processInstance != null) {
            return kogitoProcessRuntime.startProcessInstance(processInstance.getStringId(), trigger, agendaFilter);
        }
        return null;
    }

    @Override
    public KogitoProcessInstance createProcessInstance(String processId, Map<String, Object> parameters) {
        return createProcessInstance(processId, null, parameters);
    }

    @Override
    public KogitoProcessInstance startProcess(String processId, CorrelationKey correlationKey, Map<String, Object> parameters) {
        KogitoProcessInstance processInstance = createProcessInstance(processId, correlationKey, parameters);
        if (processInstance != null) {
            return kogitoProcessRuntime.startProcessInstance(processInstance.getStringId());
        }
        return null;
    }

    @Override
    public KogitoProcessInstance createProcessInstance(String processId, CorrelationKey correlationKey, Map<String, Object> parameters) {
        return createProcessInstance(
                runtimeContext.findProcess(processId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Unknown process ID: " + processId)),
                correlationKey, parameters);
    }

    private KogitoProcessInstance createProcessInstance(Process process, CorrelationKey correlationKey, Map<String, Object> parameters) {
        try {
            runtimeContext.startOperation();
            org.jbpm.process.instance.ProcessInstance pi = runtimeContext.createProcessInstance(process, correlationKey);
            pi.setKnowledgeRuntime(knowledgeRuntime);
            runtimeContext.setupParameters(pi, parameters);
            processInstanceManager.addProcessInstance(pi);
            return pi;
        } finally {
            runtimeContext.endOperation();
        }

    }

    @Override
    public ProcessInstanceManager getProcessInstanceManager() {
        return processInstanceManager;
    }

    @Override
    public JobsService getJobsService() {
        return jobService;
    }

    @Override
    public SignalManager getSignalManager() {
        return signalManager;
    }

    @Override
    public Collection<ProcessInstance> getProcessInstances() {
        return (Collection<ProcessInstance>) (Object) processInstanceManager.getProcessInstances();
    }

    public KogitoProcessInstance getProcessInstance(String id) {
        return getProcessInstance(id, false);
    }

    public KogitoProcessInstance getProcessInstance(String id, boolean readOnly) {
        return processInstanceManager.getProcessInstance(id, readOnly);
    }

    public void removeProcessInstance(KogitoProcessInstance processInstance) {
        processInstanceManager.removeProcessInstance(processInstance);
    }

    public void initProcessEventListeners() {
        for (Process process : runtimeContext.getProcesses()) {
            initProcessEventListener(process);
        }
    }

    public void removeProcessEventListeners() {
        for (Process process : runtimeContext.getProcesses()) {
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
                                    StartProcessEventListener listener = new StartProcessEventListener(startNode, trigger, process.getId(), filters);
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

    private class StartProcessEventListener implements EventListener {
        private String processId;
        private List<EventFilter> eventFilters;
        private Trigger trigger;
        private StartNode startNode;

        public StartProcessEventListener(StartNode startNode, Trigger trigger, String processId, List<EventFilter> eventFilters) {
            this.startNode = startNode;
            this.trigger = trigger;
            this.processId = processId;
            this.eventFilters = eventFilters;
        }

        @Override
        public String[] getEventTypes() {
            return new String[0];
        }

        @Override
        public void signalEvent(final String type, Object event) {
            for (EventFilter filter : eventFilters) {
                if (!filter.acceptsEvent(type, event, varName -> null)) {
                    return;
                }
            }
            Map<String, Object> outputSet = new HashMap<>();
            for (Map.Entry<String, String> entry : trigger.getInMappings().entrySet()) {
                outputSet.put(entry.getKey(), entry.getKey());
            }
            // data association needs to be corrected as it is not input mapping but output mapping
            boolean eventFound = false;
            for (DataAssociation dataAssociation : trigger.getInAssociations()) {
                if ("event".equals(dataAssociation.getSources().get(0).getLabel())) {
                    eventFound = true;
                }
            }

            if (!eventFound && !trigger.getInAssociations().isEmpty()) {
                String inputLabel = (String) startNode.getMetaData(TRIGGER_MAPPING_INPUT);
                outputSet.put(inputLabel, event);
            } else {
                outputSet.put("event", event);
            }

            Map<String, Object> parameters = NodeIoHelper.processOutputs(trigger.getInAssociations(), key -> outputSet.get(key));
            startProcessWithParamsAndTrigger(processId, parameters, type);
        }
    }

    private void initProcessActivationListener() {
        runtimeContext.addEventListener(new DefaultAgendaEventListener() {
            @Override
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

                        runtimeContext.queueWorkingMemoryAction(new SignalManagerSignalAction(eventType, event));
                    } else if (ruleName.startsWith("RuleFlowStateEventSubProcess-")
                            || ruleName.startsWith("RuleFlowStateEvent-")
                            || ruleName.startsWith("RuleFlow-Milestone-")
                            || ruleName.startsWith("RuleFlow-AdHocComplete-")
                            || ruleName.startsWith("RuleFlow-AdHocActivate-")) {
                        runtimeContext.queueWorkingMemoryAction(new SignalManagerSignalAction(ruleName, event));
                    }
                } else {
                    String ruleName = event.getMatch().getRule().getName();
                    if (ruleName.startsWith("RuleFlow-Start-")) {
                        String processId = ruleName.replace("RuleFlow-Start-", "");

                        startProcessWithParamsAndTrigger(processId, null, "conditional");
                    }
                }
            }
        });

        runtimeContext.addEventListener(new DefaultAgendaEventListener() {
            @Override
            public void afterRuleFlowGroupDeactivated(final RuleFlowGroupDeactivatedEvent event) {
                if (runtimeContext instanceof StatefulKnowledgeSession) {
                    signalManager.signalEvent("RuleFlowGroup_" + event.getRuleFlowGroup().getName() + "_" + ((StatefulKnowledgeSession) runtimeContext).getIdentifier(),
                            null);
                } else {
                    signalManager.signalEvent("RuleFlowGroup_" + event.getRuleFlowGroup().getName(),
                            null);
                }
            }
        });
    }

    private void startProcessWithParamsAndTrigger(String processId, Map<String, Object> params, String type) {

        startProcess(processId, params, type);
    }

    public void abortProcessInstance(String processInstanceId) {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        if (processInstance == null) {
            throw new IllegalArgumentException("Could not find process instance for id " + processInstanceId);
        }
        ((org.jbpm.process.instance.ProcessInstance) processInstance).setState(KogitoProcessInstance.STATE_ABORTED);
    }

    @Override
    public WorkItemManager getWorkItemManager() {
        return (WorkItemManager) getKogitoWorkItemManager();
    }

    public KogitoWorkItemManager getKogitoWorkItemManager() {
        return workItemManager;
    }

    @Override
    public UnitOfWorkManager getUnitOfWorkManager() {
        return this.unitOfWorkManager;
    }

    @Override
    public void signalEvent(String type, Object event) {
        signalManager.signalEvent(type, event);
    }

    public void signalEvent(String type, Object event, String processInstanceId) {
        signalManager.signalEvent(processInstanceId, type, event);
    }

    @Override
    public void dispose() {
        this.processEventSupport.reset();
        runtimeContext = null;
    }

    @Override
    public void clearProcessInstances() {
        this.processInstanceManager.clearProcessInstances();
    }

    @Override
    public void clearProcessInstancesState() {
        this.processInstanceManager.clearProcessInstancesState();
    }

    public boolean isActive() {
        // originally: kruntime.getEnvironment().get("Active")
        return runtimeContext.isActive();
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
                    return DurationExpirationTime.repeat(repeatValues[1], repeatValues[2]);
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
                return DurationExpirationTime.repeat(duration);

            case Timer.TIME_DATE:

                return ExactExpirationTime.of(timer.getDate());

            default:
                throw new UnsupportedOperationException("Not supported timer definition");
        }

    }

    public class SignalManagerSignalAction extends PropagationEntry.AbstractPropagationEntry implements WorkingMemoryAction {

        private String type;
        private Object event;

        public SignalManagerSignalAction(String type, Object event) {
            this.type = type;
            this.event = event;
        }

        @Override
        public void execute(ReteEvaluator reteEvaluator) {
            signalEvent(type, event);
        }
    }

    @Override
    public InternalKnowledgeRuntime getInternalKieRuntime() {
        return this.knowledgeRuntime;
    }

    @Override
    public ProcessInstance startProcessFromNodeIds(String s, Map<String, Object> map, String... strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcessInstance startProcessInstance(String l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcessInstance startProcessFromNodeIds(String s, CorrelationKey correlationKey, Map<String, Object> map, String... strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcessInstance getProcessInstance(CorrelationKey correlationKey) {
        throw new UnsupportedOperationException();
    }
}
