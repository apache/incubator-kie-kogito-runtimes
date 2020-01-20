/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.serverless.workflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.JsonNode;
import org.drools.compiler.rule.builder.dialect.java.JavaDialect;
import org.drools.core.util.StringUtils;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.process.core.event.EventTypeFilter;
import org.jbpm.process.core.validation.ProcessValidationError;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.validation.RuleFlowProcessValidator;
import org.jbpm.serverless.workflow.api.end.End;
import org.jbpm.serverless.workflow.api.events.EventDefinition;
import org.jbpm.serverless.workflow.api.events.OnEvent;
import org.jbpm.serverless.workflow.api.functions.Function;
import org.jbpm.serverless.workflow.api.interfaces.State;
import org.jbpm.serverless.workflow.api.mapper.JsonObjectMapper;
import org.jbpm.serverless.workflow.api.states.EventState;
import org.jbpm.serverless.workflow.api.transitions.Transition;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.EventTrigger;
import org.jbpm.workflow.core.node.StartNode;
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.jbpm.serverless.workflow.api.Workflow;
import org.jbpm.serverless.workflow.api.actions.Action;
import org.jbpm.serverless.workflow.api.states.DefaultState.Type;
import org.jbpm.serverless.workflow.api.states.OperationState;
import sun.font.Script;

/**
 * Serverless Workflow specification parser
 */
public class ServerlessWorkflowParser {

    protected final static String EOL = System.getProperty( "line.separator" );

    private AtomicLong idCounter = new AtomicLong(1);

    public Process parseWorkFlow(Reader workflowFile) throws Exception {
        JsonObjectMapper mapper = new JsonObjectMapper();

        Workflow workflow = mapper.readValue(readWorkflowSource(workflowFile), Workflow.class);

        Map<String, Long> nameToNodeId = new HashMap<>();

        RuleFlowProcess process = new RuleFlowProcess();
        process.setId(workflow.getId());
        process.setName(workflow.getName());
        process.setAutoComplete(true);
        process.setVersion(workflow.getVersion());

        if(workflow.getMetadata() != null && workflow.getMetadata().containsKey("packagename")) {
            process.setPackageName(workflow.getMetadata().get("packagename"));
        } else {
            process.setPackageName("org.kie.kogito");
        }

        process.setVisibility(RuleFlowProcess.PUBLIC_VISIBILITY);

        StartNode startNode = startNode(process);
        // add "workflowdata" process var
        addJsonNodeVar(process, "workflowdata");

        List<State> workflowStates = workflow.getStates();
        List<Function> workflowFunctions = workflow.getFunctions();
        List<EventDefinition> workflowEventDefinitions = workflow.getEvents();

        for (State state : workflowStates) {

            // TODO - assume single event state with single event definition
            // TODO - will be fixed soon
            if(state.getType().equals(Type.EVENT)) {
                EventState eventState = (EventState) state;

                CompositeContextNode embedded = compositeContextNode(state.getName(), process);

                List<OnEvent> onEventHandlers = eventState.getOnEvent();

                if(onEventHandlers != null && !onEventHandlers.isEmpty()) {

                    OnEvent onEvent = onEventHandlers.get(0);

                    // add process var
                    addJSONNodetVar(process, getWorkflowEventFor(workflowEventDefinitions, onEvent.getEventExpression()));

                    // remove original start node and replace with message start
                    process.removeNode(startNode);
                    startNode = messageStartNode(process, getWorkflowEventFor(workflowEventDefinitions, onEvent.getEventExpression()));

                    List<Action> actions = onEvent.getActions();

                    if(actions != null && !actions.isEmpty()) {
                        StartNode embeddedStartNode = startNode(embedded);
                        Node start = embeddedStartNode;
                        Node current = null;

                        for(Action action : actions) {
                            Function actionFunction = workflowFunctions.stream()
                                    .filter(wf -> wf.getName().equals(action.getFunctionref().getRefname()))
                                    .findFirst()
                                    .get();

                            if ("script".equalsIgnoreCase(actionFunction.getType())) {
                                String script = applySubstitutionsToScript(action.getFunctionref().getParameters().get("script"));
                                current = scriptNode(action.getFunctionref().getRefname(), script, embedded);

                                connection(start.getId(), current.getId(), start.getId() + "_" + current.getId(), embedded);
                                start = current;
                            } else if ("sysout".equalsIgnoreCase(actionFunction.getType())) {
                                String script = applySubstitutionsToScript("System.out.println(" + action.getFunctionref().getParameters().get("message") + ");");
                                current = scriptNode(action.getFunctionref().getRefname(), script, embedded);

                                connection(start.getId(), current.getId(), start.getId() + "_" + current.getId(), embedded);
                                start = current;
                            }
                        }
                        EndNode embeddedEndNode = endNode(true, embedded);
                        connection(current.getId(), embeddedEndNode.getId(), current.getId() + "_" + embeddedEndNode.getId(), embedded);
                    }
                    if (state.getName().equals(workflow.getStartsAt())) {
                        connection(startNode.getId(), embedded.getId(), startNode.getId() + "_" + embedded.getId(), process);
                    }

                    if (state.getEnd() != null) {

                        EndNode endNode = null;
                        if(state.getEnd().getType() == End.Type.TERMINATE) {
                            endNode = endNode(true, process);
                        } else { //TODO assume its otherwise message...need to fix
                            endNode = messageEndNode(process, workflowEventDefinitions, state.getEnd());
                        }

                        connection(embedded.getId(), endNode.getId(), embedded.getId() + "_" + endNode.getId(), process);
                    }

                    nameToNodeId.put(state.getName(), embedded.getId());
                }

            }


            if (state.getType().equals(Type.OPERATION)) {
                OperationState operationState = (OperationState) state;
                List<Action> actions = operationState.getActions();

                CompositeContextNode embedded = compositeContextNode(state.getName(), process);

                if (actions != null && !actions.isEmpty()) {

                    StartNode embeddedStartNode = startNode(embedded);
                    Node start = embeddedStartNode;
                    Node current = null;
                    for (Action action : actions) {

                        Function actionFunction = workflowFunctions.stream()
                                .filter(wf -> wf.getName().equals(action.getFunctionref().getRefname()))
                                .findFirst()
                                .get();

                        if ("script".equalsIgnoreCase(actionFunction.getType())) {
                            String script = applySubstitutionsToScript(action.getFunctionref().getParameters().get("script"));
                            current = scriptNode(action.getFunctionref().getRefname(), script, embedded);

                            connection(start.getId(), current.getId(), start.getId() + "_" + current.getId(), embedded);
                            start = current;
                        } else if ("sysout".equalsIgnoreCase(actionFunction.getType())) {
                            String script = applySubstitutionsToScript("System.out.println(" + action.getFunctionref().getParameters().get("message") + ");");
                            current = scriptNode(action.getFunctionref().getRefname(), script, embedded);

                            connection(start.getId(), current.getId(), start.getId() + "_" + current.getId(), embedded);
                            start = current;
                        }

                    }

                    EndNode embeddedEndNode = endNode(true, embedded);
                    connection(current.getId(), embeddedEndNode.getId(), current.getId() + "_" + embeddedEndNode.getId(), embedded);
                }
                if (state.getName().equals(workflow.getStartsAt())) {
                    connection(startNode.getId(), embedded.getId(), startNode.getId() + "_" + embedded.getId(), process);
                }

                if (state.getEnd() != null) {
                    EndNode endNode = endNode(true, process);

                    connection(embedded.getId(), endNode.getId(), embedded.getId() + "_" + endNode.getId(), process);
                }

                nameToNodeId.put(state.getName(), embedded.getId());
            }
        }

        // link states
        workflow.getStates().stream().filter(state -> (state instanceof State)).forEach(state -> {
            Transition transition = null;
            if(state instanceof EventState) {
                // TODO -- same thing here, assuming a single event
                // TODO within event state
                transition = ((EventState)state).getOnEvent().get(0).getTransition();
            } else if(state instanceof OperationState) {
                transition = ((OperationState)state).getTransition();
            }

            if (transition != null && transition.getNextState() != null) {
                Long sourceId = nameToNodeId.get(state.getName());
                Long targetId = nameToNodeId.get(((OperationState)state).getTransition().getNextState());

                connection(sourceId, targetId, sourceId + "_" + targetId, process);
            }
        });

        validate(process);
        return process;
    }

    protected String readWorkflowSource(Reader reader) throws FileNotFoundException {
        return StringUtils.readFileAsString(reader);
    }

    protected String readWorkflowSource(File location) throws FileNotFoundException {
        return StringUtils.readFileAsString(new InputStreamReader(new FileInputStream(location)));
    }

    protected String readWorkflowSource(String location) {
        return StringUtils.readFileAsString(new InputStreamReader(this.getClass().getResourceAsStream(location)));
    }

    protected StartNode startNode(NodeContainer nodeContainer) {
        StartNode startNode = new StartNode();
        startNode.setId(idCounter.getAndIncrement());
        startNode.setName("start node");

        nodeContainer.addNode(startNode);

        return startNode;
    }

    protected StartNode messageStartNode(NodeContainer nodeContainer, EventDefinition eventDefinition) {

        StartNode startNode = new StartNode();
        startNode.setId(idCounter.getAndIncrement());
        startNode.setName(eventDefinition.getName());
        startNode.setMetaData("TriggerMapping", eventDefinition.getSource() + "Var");
        startNode.setMetaData("TriggerType", "ConsumeMessage");
        startNode.setMetaData("TriggerRef", eventDefinition.getSource());
        startNode.setMetaData("MessageType", "com.fasterxml.jackson.databind.JsonNode");
        addTriggerWithInMappings(startNode, "com.fasterxml.jackson.databind.JsonNode");

        nodeContainer.addNode(startNode);
        return startNode;
    }

    protected EndNode endNode(boolean terminate, NodeContainer nodeContainer) {
        EndNode endNode = new EndNode();
        endNode.setId(idCounter.getAndIncrement());
        endNode.setName("end node");

        nodeContainer.addNode(endNode);

        return endNode;
    }

    protected EndNode messageEndNode(NodeContainer nodeContainer, List<EventDefinition> workflowEventDefinitions, End stateEnd) {
        EndNode endNode = new EndNode();
        endNode.setTerminate(false);
        endNode.setId(idCounter.getAndIncrement());
        endNode.setName("end node");

        EventDefinition eventDef = getWorkflowEventFor(workflowEventDefinitions, stateEnd.getProduce().getEventName());

        endNode.setMetaData("TriggerRef", eventDef.getSource());
        endNode.setMetaData("TriggerType", "ProduceMessage");
        endNode.setMetaData("MessageType", "com.fasterxml.jackson.databind.JsonNode");
        endNode.setMetaData("MappingVariable", stateEnd.getProduce().getDataRef());
        addMessageEndNodeAction(endNode, stateEnd.getProduce().getDataRef(), "com.fasterxml.jackson.databind.JsonNode");

        nodeContainer.addNode(endNode);
        return endNode;
    }

    protected ActionNode scriptNode(String name, String script, NodeContainer nodeContainer) {
        ActionNode scriptNode = new ActionNode();
        scriptNode.setId(idCounter.getAndIncrement());
        scriptNode.setName(name);

        scriptNode.setAction(new DroolsConsequenceAction());
        ((DroolsConsequenceAction)scriptNode.getAction()).setConsequence(script);
        ((DroolsConsequenceAction)scriptNode.getAction()).setDialect(JavaDialect.ID);

        nodeContainer.addNode(scriptNode);

        return scriptNode;
    }

    protected CompositeContextNode compositeContextNode(String name, NodeContainer nodeContainer) {
        CompositeContextNode subProcessNode = new CompositeContextNode();
        subProcessNode.setId(idCounter.getAndIncrement());
        subProcessNode.setName(name);
        VariableScope variableScope = new VariableScope();
        subProcessNode.addContext(variableScope);
        subProcessNode.setDefaultContext(variableScope);
        subProcessNode.setAutoComplete(true);

        nodeContainer.addNode(subProcessNode);

        return subProcessNode;
    }

    protected Connection connection(long fromId, long toId, String uniqueId, NodeContainer nodeContainer) {
        Node from = nodeContainer.getNode(fromId);
        Node to = nodeContainer.getNode(toId);
        ConnectionImpl connection = new ConnectionImpl(
                from, org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE,
                to, org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE);
        connection.setMetaData("UniqueId", uniqueId);
        return connection;
    }

    protected RuleFlowProcess validate(RuleFlowProcess process) {
        ProcessValidationError[] errors = RuleFlowProcessValidator.getInstance().validateProcess(process);
        for (ProcessValidationError error : errors) {
            System.out.println(error.toString());
        }
        if (errors.length > 0) {
            throw new RuntimeException("Process could not be validated !");
        }
        return process;
    }

    protected void addTriggerWithInMappings(StartNode startNode, String triggerEventType) {
        EventTrigger trigger = new EventTrigger();
        EventTypeFilter eventFilter = new EventTypeFilter();
        eventFilter.setType(triggerEventType);
        trigger.addEventFilter(eventFilter);

        String mapping = (String) startNode.getMetaData("TriggerMapping");
        if (mapping != null) {
            trigger.addInMapping(mapping, startNode.getOutMapping(mapping));
        }

        startNode.addTrigger(trigger);
    }

    protected void addMessageEndNodeAction(EndNode endNode, String variable, String messageType){
        List<DroolsAction> actions = new ArrayList<DroolsAction>();

        actions.add(new DroolsConsequenceAction("java",
                                                "org.drools.core.process.instance.impl.WorkItemImpl workItem = new org.drools.core.process.instance.impl.WorkItemImpl();" + EOL +
                                                        "workItem.setName(\"Send Task\");" + EOL +
                                                        "workItem.setNodeInstanceId(kcontext.getNodeInstance().getId());" + EOL +
                                                        "workItem.setProcessInstanceId(kcontext.getProcessInstance().getId());" + EOL +
                                                        "workItem.setNodeId(kcontext.getNodeInstance().getNodeId());" + EOL +
                                                        "workItem.setParameter(\"MessageType\", \"" + messageType + "\");" + EOL +
                                                        (variable == null ? "" : "workItem.setParameter(\"Message\", " + variable + ");" + EOL) +
                                                        "workItem.setDeploymentId((String) kcontext.getKnowledgeRuntime().getEnvironment().get(\"deploymentId\"));" + EOL +
                                                        "((org.drools.core.process.instance.WorkItemManager) kcontext.getKnowledgeRuntime().getWorkItemManager()).internalExecuteWorkItem(workItem);"));
        endNode.setActions(EndNode.EVENT_NODE_ENTER, actions);
    }

    protected void addJSONNodetVar(RuleFlowProcess process, EventDefinition eventDefinition) {

        addJsonNodeVar(process, eventDefinition.getSource() + "Var");
    }

    protected void addJsonNodeVar(RuleFlowProcess process, String varName) {
        List<Variable> variables = new ArrayList<Variable>();
        Variable variable = new Variable();
        variable.setName(varName);
        variable.setType( new ObjectDataType(JsonNode.class.getName()) );
        variables.add( variable );
        process.getVariableScope().setVariables( variables );
    }

    protected EventDefinition getWorkflowEventFor(List<EventDefinition> workflowEvents, String eventName) {
        // TODO we assume to get by name, need to plug in expression evaluator heres
        return workflowEvents.stream()
                .filter(wt -> wt.getName().equals(eventName))
                .findFirst().get();
    }

    protected String applySubstitutionsToScript(String script) {
        script = script.replaceFirst("\\bworkflowdata.([a-z]*)\\b", "workflowdata.get(\"$1\")");
        script = script.replaceAll("\\bworkflowdata\\b", "((com.fasterxml.jackson.databind.JsonNode)kcontext.getVariable(\\\"workflowdata\\\"))");
        script = script.replaceFirst("\\bkcontext.([A-Za-z]+).([A-Za-z]+)\\b", "((com.fasterxml.jackson.databind.JsonNode)kcontext.getVariable(\"$1\")).get(\"$2\")");

        return script;
    }
}