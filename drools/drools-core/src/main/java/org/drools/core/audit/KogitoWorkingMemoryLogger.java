/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.audit;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.core.WorkingMemory;
import org.drools.core.WorkingMemoryEventManager;
import org.drools.core.audit.event.ActivationLogEvent;
import org.drools.core.audit.event.ILogEventFilter;
import org.drools.core.audit.event.KogitoRuleFlowLogEvent;
import org.drools.core.audit.event.KogitoRuleFlowNodeLogEvent;
import org.drools.core.audit.event.KogitoRuleFlowVariableLogEvent;
import org.drools.core.audit.event.LogEvent;
import org.drools.core.audit.event.ObjectLogEvent;
import org.drools.core.audit.event.RuleBaseLogEvent;
import org.drools.core.audit.event.RuleFlowGroupLogEvent;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.common.AgendaItem;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.impl.StatelessKnowledgeSessionImpl;
import org.drools.core.rule.Declaration;
import org.drools.core.runtime.process.InternalProcessRuntime;
import org.drools.core.spi.Activation;
import org.drools.core.spi.Tuple;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.NodeContainer;
import org.kie.api.event.KieRuntimeEventManager;
import org.kie.api.event.kiebase.AfterFunctionRemovedEvent;
import org.kie.api.event.kiebase.AfterKieBaseLockedEvent;
import org.kie.api.event.kiebase.AfterKieBaseUnlockedEvent;
import org.kie.api.event.kiebase.AfterKiePackageAddedEvent;
import org.kie.api.event.kiebase.AfterKiePackageRemovedEvent;
import org.kie.api.event.kiebase.AfterProcessAddedEvent;
import org.kie.api.event.kiebase.AfterProcessRemovedEvent;
import org.kie.api.event.kiebase.AfterRuleAddedEvent;
import org.kie.api.event.kiebase.AfterRuleRemovedEvent;
import org.kie.api.event.kiebase.BeforeFunctionRemovedEvent;
import org.kie.api.event.kiebase.BeforeKieBaseLockedEvent;
import org.kie.api.event.kiebase.BeforeKieBaseUnlockedEvent;
import org.kie.api.event.kiebase.BeforeKiePackageAddedEvent;
import org.kie.api.event.kiebase.BeforeKiePackageRemovedEvent;
import org.kie.api.event.kiebase.BeforeProcessAddedEvent;
import org.kie.api.event.kiebase.BeforeProcessRemovedEvent;
import org.kie.api.event.kiebase.BeforeRuleAddedEvent;
import org.kie.api.event.kiebase.BeforeRuleRemovedEvent;
import org.kie.api.event.kiebase.KieBaseEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.NodeInstanceContainer;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.Match;
import org.kie.internal.command.RegistryContext;
import org.kie.kogito.internal.process.runtime.KogitoNode;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;

/**
 * A logger of events generated by a working memory.
 * It listens to the events generated by the working memory and
 * creates associated log event (containing a snapshot of the
 * state of the working event at that time).
 * 
 * Filters can be used to filter out unwanted events.
 * 
 * Subclasses of this class should implement the logEventCreated(LogEvent)
 * method and store this information, like for example log to file
 * or database.
 */
public abstract class KogitoWorkingMemoryLogger extends WorkingMemoryLogger {

    private List<ILogEventFilter> filters = new ArrayList<ILogEventFilter>();

    public KogitoWorkingMemoryLogger() {
    }

    /**
     * Creates a new working memory logger for the given working memory.
     *
     * @param workingMemory
     */
    public KogitoWorkingMemoryLogger(final WorkingMemory workingMemory) {
        workingMemory.addEventListener((RuleRuntimeEventListener) this);
        workingMemory.addEventListener((AgendaEventListener) this);
        setProcessRuntimeEventListener((InternalWorkingMemory) workingMemory);
        workingMemory.addEventListener((KieBaseEventListener) this);
    }

    private void setProcessRuntimeEventListener(InternalWorkingMemory workingMemory) {
        try {
            InternalProcessRuntime processRuntime = workingMemory.getProcessRuntime();
            if (processRuntime != null) {
                processRuntime.addEventListener(this);
            }
        } catch (Exception e) {
            /* ignore */
        }
    }

    public KogitoWorkingMemoryLogger(final KieRuntimeEventManager session) {
        if (session instanceof StatefulKnowledgeSessionImpl) {
            StatefulKnowledgeSessionImpl statefulSession = ((StatefulKnowledgeSessionImpl) session);
            WorkingMemoryEventManager eventManager = statefulSession;
            eventManager.addEventListener((RuleRuntimeEventListener) this);
            eventManager.addEventListener((AgendaEventListener) this);
            eventManager.addEventListener((KieBaseEventListener) this);
            setProcessRuntimeEventListener((InternalWorkingMemory) session);
        } else if (session instanceof StatelessKnowledgeSessionImpl) {
            StatelessKnowledgeSessionImpl statelessSession = ((StatelessKnowledgeSessionImpl) session);
            statelessSession.addEventListener((RuleRuntimeEventListener) this);
            statelessSession.addEventListener((AgendaEventListener) this);
            statelessSession.getKnowledgeBase().addEventListener((KieBaseEventListener) this);
        } else if (session instanceof CommandBasedStatefulKnowledgeSession) {
            StatefulKnowledgeSessionImpl statefulSession =
                    ((StatefulKnowledgeSessionImpl) ((RegistryContext) ((CommandBasedStatefulKnowledgeSession) session).getRunner().createContext()).lookup(KieSession.class));
            InternalWorkingMemory eventManager = statefulSession;
            eventManager.addEventListener((RuleRuntimeEventListener) this);
            eventManager.addEventListener((AgendaEventListener) this);
            InternalProcessRuntime processRuntime = eventManager.getProcessRuntime();
            eventManager.addEventListener((KieBaseEventListener) this);
            if (processRuntime != null) {
                processRuntime.addEventListener(this);
            }
        } else {
            throw new IllegalArgumentException("Not supported session in logger: " + session.getClass());
        }
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        filters = (List<ILogEventFilter>) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(filters);
    }

    /**
     * This method is invoked every time a new log event is created.
     * Subclasses should implement this method and store the event,
     * like for example log to a file or database.
     * 
     * @param logEvent
     */
    public abstract void logEventCreated(LogEvent logEvent);

    /**
     * This method is invoked every time a new log event is created.
     * It filters out unwanted events.
     * 
     * @param logEvent
     */
    private void filterLogEvent(final LogEvent logEvent) {
        for (ILogEventFilter filter : this.filters) {
            // do nothing if one of the filters doesn't accept the event
            if (!filter.acceptEvent(logEvent)) {
                return;
            }
        }
        // if all the filters accepted the event, signal the creation
        // of the event
        logEventCreated(logEvent);
    }

    /**
     * Adds the given filter to the list of filters for this event log.
     * A log event must be accepted by all the filters to be entered in
     * the event log.
     *
     * @param filter The filter that should be added.
     */
    public void addFilter(final ILogEventFilter filter) {
        if (filter == null) {
            throw new NullPointerException();
        }
        this.filters.add(filter);
    }

    /**
     * Removes the given filter from the list of filters for this event log.
     * If the given filter was not a filter of this event log, nothing
     * happens.
     *
     * @param filter The filter that should be removed.
     */
    public void removeFilter(final ILogEventFilter filter) {
        this.filters.remove(filter);
    }

    /**
     * Clears all filters of this event log.
     */
    public void clearFilters() {
        this.filters.clear();
    }

    /**
     * @see RuleRuntimeEventListener
     */
    public void objectInserted(final ObjectInsertedEvent event) {
        filterLogEvent(new ObjectLogEvent(LogEvent.INSERTED,
                ((InternalFactHandle) event.getFactHandle()).getId(),
                event.getObject().toString()));
    }

    /**
     * @see RuleRuntimeEventListener
     */
    public void objectUpdated(final ObjectUpdatedEvent event) {
        filterLogEvent(new ObjectLogEvent(LogEvent.UPDATED,
                ((InternalFactHandle) event.getFactHandle()).getId(),
                event.getObject().toString()));
    }

    /**
     * @see RuleRuntimeEventListener
     */
    public void objectDeleted(final ObjectDeletedEvent event) {
        filterLogEvent(new ObjectLogEvent(LogEvent.RETRACTED,
                ((InternalFactHandle) event.getFactHandle()).getId(),
                event.getOldObject().toString()));
    }

    /**
     * @see AgendaEventListener
     */
    public void matchCreated(MatchCreatedEvent event) {
        filterLogEvent(new ActivationLogEvent(LogEvent.ACTIVATION_CREATED,
                getActivationId(event.getMatch()),
                event.getMatch().getRule().getName(),
                extractDeclarations(event.getMatch()),
                ((RuleImpl) event.getMatch().getRule()).getRuleFlowGroup(),
                extractFactHandleIds((Activation) event.getMatch())));
    }

    /**
     * @see AgendaEventListener
     */
    public void matchCancelled(MatchCancelledEvent event) {
        filterLogEvent(new ActivationLogEvent(LogEvent.ACTIVATION_CANCELLED,
                getActivationId(event.getMatch()),
                event.getMatch().getRule().getName(),
                extractDeclarations(event.getMatch()),
                ((RuleImpl) event.getMatch().getRule()).getRuleFlowGroup(),
                extractFactHandleIds((Activation) event.getMatch())));
    }

    /**
     * @see AgendaEventListener
     */
    public void beforeMatchFired(BeforeMatchFiredEvent event) {
        filterLogEvent(new ActivationLogEvent(LogEvent.BEFORE_ACTIVATION_FIRE,
                getActivationId(event.getMatch()),
                event.getMatch().getRule().getName(),
                extractDeclarations(event.getMatch()),
                ((RuleImpl) event.getMatch().getRule()).getRuleFlowGroup(),
                extractFactHandleIds((Activation) event.getMatch())));
    }

    /**
     * @see AgendaEventListener
     */
    public void afterMatchFired(final AfterMatchFiredEvent event) {
        filterLogEvent(new ActivationLogEvent(LogEvent.AFTER_ACTIVATION_FIRE,
                getActivationId(event.getMatch()),
                event.getMatch().getRule().getName(),
                extractDeclarations(event.getMatch()),
                ((RuleImpl) event.getMatch().getRule()).getRuleFlowGroup(),
                extractFactHandleIds((Activation) event.getMatch())));
    }

    /**
     * Creates a string representation of the declarations of an activation.
     * This is a list of name-value-pairs for each of the declarations in the
     * tuple of the activation. The name is the identifier (=name) of the
     * declaration, and the value is a toString of the value of the
     * parameter, followed by the id of the fact between parentheses.
     * 
     * @param match The match from which the declarations should be extracted
     * @return A String represetation of the declarations of the activation.
     */
    private String extractDeclarations(Match match) {
        final StringBuilder result = new StringBuilder();
        List<String> declarations = match.getDeclarationIds();
        Map<String, Declaration> declsMap = ((AgendaItem) match).getTerminalNode().getSubRule().getOuterDeclarations();
        for (int i = 0; i < declarations.size(); i++) {
            String declaration = declarations.get(i);

            Declaration decl = declsMap.get(declaration);
            InternalFactHandle handle = ((Tuple) match).get(decl);
            if (!handle.isValid()) {
                continue;
            }
            Object value = decl.getValue(null, handle.getObject());

            result.append(declaration);
            result.append("=");
            if (value == null) {
                // this should never occur
                result.append("null");
            } else {
                result.append(value);
            }
            if (i < declarations.size() - 1) {
                result.append("; ");
            }
        }
        return result.toString();
    }

    private String extractFactHandleIds(Activation activation) {
        InternalFactHandle activatingFact = activation.getPropagationContext().getFactHandle();
        StringBuilder sb = new StringBuilder();
        if (activatingFact != null) {
            sb.append(activatingFact.getId());
        }
        InternalFactHandle[] factHandles = activation.getTuple().toFactHandles();
        for (int i = 0; i < factHandles.length; i++) {
            if (activatingFact != null) {
                if (activatingFact.getId() == factHandles[i].getId()) {
                    continue;
                }
                sb.append(",");
            } else {
                if (i > 0) {
                    sb.append(",");
                }
            }
            sb.append(factHandles[i].getId());
        }
        return sb.toString();
    }

    /**
     * Returns a String that can be used as unique identifier for an
     * activation. Since the activationId is the same for all assertions
     * that are created during a single insert, update or retract, the
     * key of the tuple of the activation is added too (which is a set
     * of fact handle ids).
     * 
     * @param match The match for which a unique id should be generated
     * @return A unique id for the activation
     */
    private static String getActivationId(Match match) {
        final StringBuilder result = new StringBuilder(match.getRule().getName());
        result.append(" [");
        List<? extends FactHandle> factHandles = match.getFactHandles();
        for (int i = 0; i < factHandles.size(); i++) {
            result.append(((InternalFactHandle) factHandles.get(i)).getId());
            if (i < factHandles.size() - 1) {
                result.append(", ");
            }
        }
        return result.append("]").toString();
    }

    public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
        // we don't audit this yet     
    }

    public void agendaGroupPushed(AgendaGroupPushedEvent event) {
        // we don't audit this yet        
    }

    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        filterLogEvent(new RuleFlowGroupLogEvent(
                LogEvent.BEFORE_RULEFLOW_GROUP_ACTIVATED,
                event.getRuleFlowGroup().getName(),
                ((org.drools.core.spi.RuleFlowGroup) event.getRuleFlowGroup()).size()));
    }

    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        filterLogEvent(new RuleFlowGroupLogEvent(
                LogEvent.AFTER_RULEFLOW_GROUP_ACTIVATED,
                event.getRuleFlowGroup().getName(),
                ((org.drools.core.spi.RuleFlowGroup) event.getRuleFlowGroup()).size()));
    }

    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        filterLogEvent(new RuleFlowGroupLogEvent(
                LogEvent.BEFORE_RULEFLOW_GROUP_DEACTIVATED,
                event.getRuleFlowGroup().getName(),
                ((org.drools.core.spi.RuleFlowGroup) event.getRuleFlowGroup()).size()));
    }

    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        filterLogEvent(new RuleFlowGroupLogEvent(
                LogEvent.AFTER_RULEFLOW_GROUP_DEACTIVATED,
                event.getRuleFlowGroup().getName(),
                ((org.drools.core.spi.RuleFlowGroup) event.getRuleFlowGroup()).size()));
    }

    public void beforeProcessStarted(ProcessStartedEvent event) {
        filterLogEvent(new KogitoRuleFlowLogEvent(LogEvent.BEFORE_RULEFLOW_CREATED, event.getProcessInstance()));
    }

    public void afterProcessStarted(ProcessStartedEvent event) {
        filterLogEvent(new KogitoRuleFlowLogEvent(LogEvent.AFTER_RULEFLOW_CREATED, event.getProcessInstance()));
    }

    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        filterLogEvent(new KogitoRuleFlowLogEvent(LogEvent.BEFORE_RULEFLOW_COMPLETED, event.getProcessInstance()));
    }

    public void afterProcessCompleted(ProcessCompletedEvent event) {
        filterLogEvent(new KogitoRuleFlowLogEvent(LogEvent.AFTER_RULEFLOW_COMPLETED, event.getProcessInstance()));
    }

    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        filterLogEvent(new KogitoRuleFlowNodeLogEvent(LogEvent.BEFORE_RULEFLOW_NODE_TRIGGERED,
                createNodeId(event.getNodeInstance()),
                event.getNodeInstance().getNodeName(),
                createNodeInstanceId(event.getNodeInstance()),
                event.getProcessInstance()));
    }

    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        filterLogEvent(new KogitoRuleFlowNodeLogEvent(LogEvent.AFTER_RULEFLOW_NODE_TRIGGERED,
                createNodeId(event.getNodeInstance()),
                event.getNodeInstance().getNodeName(),
                createNodeInstanceId(event.getNodeInstance()),
                event.getProcessInstance()));
    }

    private String createNodeId(NodeInstance nodeInstance) {
        Node node = nodeInstance.getNode();
        if (node == null) {
            return "";
        }
        Object uniqueIdObj = node.getMetaData().get("UniqueId");
        String nodeId;
        if (uniqueIdObj == null) {
            nodeId = "" + node.getId();
        } else {
            nodeId = (String) uniqueIdObj;
        }
        NodeContainer nodeContainer = ((KogitoNode) node).getParentContainer();
        while (nodeContainer != null) {
            if (nodeContainer instanceof Node) {
                node = (Node) nodeContainer;
                nodeContainer = node.getNodeContainer();
                // TODO fix this filter out hidden compositeNode inside ForEach node
                if (!(nodeContainer.getClass().getName().endsWith("ForEachNode"))) {
                    nodeId = node.getId() + ":" + nodeId;
                }
            } else {
                break;
            }
        }
        return nodeId;
    }

    private String createNodeInstanceId(NodeInstance nodeInstance) {
        String nodeInstanceId = "" + ((KogitoNodeInstance) nodeInstance).getStringId();
        NodeInstanceContainer nodeContainer = nodeInstance.getNodeInstanceContainer();
        while (nodeContainer != null) {
            if (nodeContainer instanceof NodeInstance) {
                nodeInstance = (NodeInstance) nodeContainer;
                nodeInstanceId = ((KogitoNodeInstance) nodeInstance).getStringId() + ":" + nodeInstanceId;
                nodeContainer = nodeInstance.getNodeInstanceContainer();
            } else {
                break;
            }
        }
        return nodeInstanceId;
    }

    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
        filterLogEvent(new KogitoRuleFlowNodeLogEvent(LogEvent.BEFORE_RULEFLOW_NODE_EXITED,
                createNodeId(event.getNodeInstance()),
                event.getNodeInstance().getNodeName(),
                createNodeInstanceId(event.getNodeInstance()),
                event.getProcessInstance()));
    }

    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        filterLogEvent(new KogitoRuleFlowNodeLogEvent(LogEvent.AFTER_RULEFLOW_NODE_EXITED,
                createNodeId(event.getNodeInstance()),
                event.getNodeInstance().getNodeName(),
                createNodeInstanceId(event.getNodeInstance()),
                event.getProcessInstance()));
    }

    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
        filterLogEvent(new KogitoRuleFlowVariableLogEvent(LogEvent.BEFORE_VARIABLE_INSTANCE_CHANGED,
                event.getVariableId(),
                event.getVariableInstanceId(),
                event.getProcessInstance(),
                event.getNewValue() == null ? "null" : event.getNewValue().toString()));
    }

    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        filterLogEvent(new KogitoRuleFlowVariableLogEvent(LogEvent.AFTER_VARIABLE_INSTANCE_CHANGED,
                event.getVariableId(),
                event.getVariableInstanceId(),
                event.getProcessInstance(),
                event.getNewValue() == null ? "null" : event.getNewValue().toString()));
    }

    public void afterKiePackageAdded(AfterKiePackageAddedEvent event) {
        filterLogEvent(new RuleBaseLogEvent(LogEvent.AFTER_PACKAGE_ADDED,
                event.getKiePackage().getName(),
                null));
    }

    public void afterKiePackageRemoved(AfterKiePackageRemovedEvent event) {
        filterLogEvent(new RuleBaseLogEvent(LogEvent.AFTER_PACKAGE_REMOVED,
                event.getKiePackage().getName(),
                null));
    }

    public void beforeKieBaseLocked(BeforeKieBaseLockedEvent event) {
    }

    public void afterKieBaseLocked(AfterKieBaseLockedEvent event) {
    }

    public void beforeKieBaseUnlocked(BeforeKieBaseUnlockedEvent event) {
    }

    public void afterKieBaseUnlocked(AfterKieBaseUnlockedEvent event) {
    }

    public void afterRuleAdded(AfterRuleAddedEvent event) {
        filterLogEvent(new RuleBaseLogEvent(LogEvent.AFTER_RULE_ADDED,
                event.getRule().getPackageName(),
                event.getRule().getName()));
    }

    public void afterRuleRemoved(AfterRuleRemovedEvent event) {
        filterLogEvent(new RuleBaseLogEvent(LogEvent.AFTER_RULE_REMOVED,
                event.getRule().getPackageName(),
                event.getRule().getName()));
    }

    public void beforeFunctionRemoved(BeforeFunctionRemovedEvent event) {
    }

    public void beforeKiePackageAdded(BeforeKiePackageAddedEvent event) {
        filterLogEvent(new RuleBaseLogEvent(LogEvent.BEFORE_PACKAGE_ADDED,
                event.getKiePackage().getName(),
                null));
    }

    public void beforeKiePackageRemoved(BeforeKiePackageRemovedEvent event) {
        filterLogEvent(new RuleBaseLogEvent(LogEvent.BEFORE_PACKAGE_REMOVED,
                event.getKiePackage().getName(),
                null));
    }

    public void beforeRuleAdded(BeforeRuleAddedEvent event) {
        filterLogEvent(new RuleBaseLogEvent(LogEvent.BEFORE_RULE_ADDED,
                event.getRule().getPackageName(),
                event.getRule().getName()));
    }

    public void beforeRuleRemoved(BeforeRuleRemovedEvent event) {
        filterLogEvent(new RuleBaseLogEvent(LogEvent.BEFORE_RULE_REMOVED,
                event.getRule().getPackageName(),
                event.getRule().getName()));
    }

    public void afterFunctionRemoved(AfterFunctionRemovedEvent event) {
    }

    public void beforeProcessAdded(BeforeProcessAddedEvent event) {
    }

    public void afterProcessAdded(AfterProcessAddedEvent event) {
    }

    public void beforeProcessRemoved(BeforeProcessRemovedEvent event) {
    }

    public void afterProcessRemoved(AfterProcessRemovedEvent event) {
    }
}
