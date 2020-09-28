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
package org.jbpm.serverless.workflow.parser;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.serverless.workflow.api.Workflow;
import org.jbpm.serverless.workflow.api.actions.Action;
import org.jbpm.serverless.workflow.api.branches.Branch;
import org.jbpm.serverless.workflow.api.end.End;
import org.jbpm.serverless.workflow.api.events.EventDefinition;
import org.jbpm.serverless.workflow.api.functions.Function;
import org.jbpm.serverless.workflow.api.interfaces.State;
import org.jbpm.serverless.workflow.api.mapper.BaseObjectMapper;
import org.jbpm.serverless.workflow.api.states.DefaultState.Type;
import org.jbpm.serverless.workflow.api.states.DelayState;
import org.jbpm.serverless.workflow.api.states.EventState;
import org.jbpm.serverless.workflow.api.states.InjectState;
import org.jbpm.serverless.workflow.api.states.OperationState;
import org.jbpm.serverless.workflow.api.states.ParallelState;
import org.jbpm.serverless.workflow.api.states.SubflowState;
import org.jbpm.serverless.workflow.api.states.SwitchState;
import org.jbpm.serverless.workflow.api.switchconditions.DataCondition;
import org.jbpm.serverless.workflow.api.switchconditions.EventCondition;
import org.jbpm.serverless.workflow.api.transitions.Transition;
import org.jbpm.serverless.workflow.parser.core.ServerlessWorkflowFactory;
import org.jbpm.serverless.workflow.parser.util.ServerlessWorkflowUtils;
import org.jbpm.serverless.workflow.parser.util.WorkflowAppContext;
import org.jbpm.workflow.core.impl.ConnectionRef;
import org.jbpm.workflow.core.impl.ConstraintImpl;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.EventNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.jbpm.workflow.core.node.TimerNode;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerlessWorkflowParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerlessWorkflowParser.class);

    private static final String SCRIPT_TYPE = "script";
    private static final String REST_TYPE = "rest";
    private static final String SCRIPT_TYPE_PARAM = "script";
    private static final String SYSOUT_TYPE = "sysout";
    private static final String SYSOUT_TYPE_PARAM = "message";
    private static final String SERVICE_TYPE = "service";
    private static final String DECISION_TYPE = "decision";
    private static final String INTEGRATION_TYPE = "integration";
    private static final String RULE_TYPE = "rule";
    private static final String NODE_START_NAME = "Start";
    private static final String NODE_END_NAME = "End";
    private static final String NODETOID_START = "start";
    private static final String NODETOID_END = "end";

    private AtomicLong idCounter = new AtomicLong(1);
    private ServerlessWorkflowFactory factory;
    private BaseObjectMapper objectMapper;

    public ServerlessWorkflowParser(String workflowFormat) {
        this.objectMapper = ServerlessWorkflowUtils.getObjectMapper(workflowFormat);
        this.factory = new ServerlessWorkflowFactory(WorkflowAppContext.ofAppResources());
    }

    public ServerlessWorkflowParser(String workflowFormat, WorkflowAppContext workflowAppContext) {
        this.objectMapper = ServerlessWorkflowUtils.getObjectMapper(workflowFormat);
        this.factory = new ServerlessWorkflowFactory(workflowAppContext);
    }

    public Process parseWorkFlow(Reader workflowFile) throws JsonProcessingException {
        Workflow workflow = objectMapper.readValue(ServerlessWorkflowUtils.readWorkflowFile(workflowFile), Workflow.class);
        RuleFlowProcess process = factory.createProcess(workflow);
        Map<String, Map<String, Long>> nameToNodeId = new HashMap<>();

        if (!ServerlessWorkflowUtils.includesSupportedStates(workflow)) {
            LOGGER.warn("workflow includes currently unsupported states.");
            LOGGER.warn("default process is generated.");

            StartNode startNode = factory.startNode(idCounter.getAndIncrement(), NODE_START_NAME, process);
            EndNode endNode = factory.endNode(idCounter.getAndIncrement(), NODE_END_NAME, true, process);
            factory.connect(startNode.getId(), endNode.getId(), startNode.getId() + "_" + endNode.getId(), process);

            factory.validate(process);
            return process;
        }

        List<State> workflowStates = workflow.getStates();
        List<Function> workflowFunctions = workflow.getFunctions();

        StartNode workflowStartNode;
        Map<String, EndNode> workflowEndNodes = new HashMap<>();

        State workflowStartState = ServerlessWorkflowUtils.getWorkflowStartState(workflow);

        if (workflowStartState.getType().equals(Type.EVENT)) {
            EventState startEventState = (EventState) workflowStartState;
            workflowStartNode = factory.messageStartNode(idCounter.getAndIncrement(), ServerlessWorkflowUtils.getWorkflowEventFor(workflow, startEventState.getEventsActions().get(0).getEventRefs().get(0)), process);
        } else {
            workflowStartNode = factory.startNode(idCounter.getAndIncrement(), NODE_START_NAME, process);
        }

        List<State> endStates = ServerlessWorkflowUtils.getWorkflowEndStates(workflow);

        for (State endState : endStates) {
            if (endState.getEnd().getKind() == End.Kind.EVENT) {
                workflowEndNodes.put(endState.getName(), factory.messageEndNode(idCounter.getAndIncrement(), NODE_END_NAME, workflow, endState.getEnd(), process));
            } else {
                workflowEndNodes.put(endState.getName(), factory.endNode(idCounter.getAndIncrement(), NODE_END_NAME, true, process));
            }
        }

        for (State state : workflowStates) {
            if (state.getType().equals(Type.EVENT)) {
                EventState eventState = (EventState) state;
                if (eventState.getStart() == null) {
                    throw new IllegalArgumentException("currently support only event start states");
                }

                CompositeContextNode embeddedSubProcess = factory.subProcessNode(idCounter.getAndIncrement(), state.getName(), process);
                handleActions(workflowFunctions, eventState.getEventsActions().get(0).getActions(), process, embeddedSubProcess);

                factory.connect(workflowStartNode.getId(), embeddedSubProcess.getId(), workflowStartNode.getId() + "_" + embeddedSubProcess.getId(), process);

                if (state.getEnd() != null) {
                    factory.connect(embeddedSubProcess.getId(), workflowEndNodes.get(state.getName()).getId(), embeddedSubProcess.getId() + "_" + workflowEndNodes.get(state.getName()).getId(), process);
                }

                Map<String, Long> startEndMap = new HashMap<>();
                startEndMap.put(NODETOID_START, embeddedSubProcess.getId());
                startEndMap.put(NODETOID_END, embeddedSubProcess.getId());
                nameToNodeId.put(state.getName(), startEndMap);
            }

            if (state.getType().equals(Type.OPERATION)) {
                OperationState operationState = (OperationState) state;
                CompositeContextNode embeddedSubProcess = factory.subProcessNode(idCounter.getAndIncrement(), state.getName(), process);
                handleActions(workflowFunctions, operationState.getActions(), process, embeddedSubProcess);

                if (state.getStart() != null) {
                    factory.connect(workflowStartNode.getId(), embeddedSubProcess.getId(), workflowStartNode.getId() + "_" + embeddedSubProcess.getId(), process);
                }

                if (state.getEnd() != null) {
                    factory.connect(embeddedSubProcess.getId(), workflowEndNodes.get(state.getName()).getId(), embeddedSubProcess.getId() + "_" + workflowEndNodes.get(state.getName()).getId(), process);
                }

                Map<String, Long> startEndMap = new HashMap<>();
                startEndMap.put(NODETOID_START, embeddedSubProcess.getId());
                startEndMap.put(NODETOID_END, embeddedSubProcess.getId());
                nameToNodeId.put(state.getName(), startEndMap);
            }

            if (state.getType().equals(Type.DELAY)) {
                DelayState delayState = (DelayState) state;

                TimerNode timerNode = factory.timerNode(idCounter.getAndIncrement(), delayState.getName(), delayState.getTimeDelay(), process);

                if (state.getStart() != null) {
                    factory.connect(workflowStartNode.getId(), timerNode.getId(), workflowStartNode.getId() + "_" + timerNode.getId(), process);
                }

                if (state.getEnd() != null) {
                    factory.connect(timerNode.getId(), workflowEndNodes.get(state.getName()).getId(), timerNode.getId() + "_" + workflowEndNodes.get(state.getName()).getId(), process);
                }

                Map<String, Long> startEndMap = new HashMap<>();
                startEndMap.put(NODETOID_START, timerNode.getId());
                startEndMap.put(NODETOID_END, timerNode.getId());
                nameToNodeId.put(state.getName(), startEndMap);

            }

            if (state.getType().equals(Type.INJECT)) {
                InjectState injectState = (InjectState) state;

                ActionNode actionNode;

                JsonNode toInjectNode = injectState.getData();

                if (toInjectNode != null) {
                    actionNode = factory.scriptNode(idCounter.getAndIncrement(), injectState.getName(), ServerlessWorkflowUtils.getInjectScript(toInjectNode), process);
                } else {
                    //no-op script
                    actionNode = factory.scriptNode(idCounter.getAndIncrement(), injectState.getName(), "", process);
                }

                if (state.getStart() != null) {
                    factory.connect(workflowStartNode.getId(), actionNode.getId(), workflowStartNode.getId() + "_" + actionNode.getId(), process);
                }

                if (state.getEnd() != null) {
                    factory.connect(actionNode.getId(), workflowEndNodes.get(state.getName()).getId(), actionNode.getId() + "_" + workflowEndNodes.get(state.getName()).getId(), process);
                }

                Map<String, Long> startEndMap = new HashMap<>();
                startEndMap.put(NODETOID_START, actionNode.getId());
                startEndMap.put(NODETOID_END, actionNode.getId());
                nameToNodeId.put(state.getName(), startEndMap);
            }

            if (state.getType().equals(Type.SUBFLOW)) {
                SubflowState subflowState = (SubflowState) state;

                SubProcessNode callActivityNode = factory.callActivity(idCounter.getAndIncrement(), subflowState.getName(), subflowState.getWorkflowId(), subflowState.isWaitForCompletion(), process);

                if (state.getStart() != null) {
                    factory.connect(workflowStartNode.getId(), callActivityNode.getId(), workflowStartNode.getId() + "_" + callActivityNode.getId(), process);
                }

                if (state.getEnd() != null) {
                    factory.connect(callActivityNode.getId(), workflowEndNodes.get(state.getName()).getId(), callActivityNode.getId() + "_" + workflowEndNodes.get(state.getName()).getId(), process);
                }

                Map<String, Long> startEndMap = new HashMap<>();
                startEndMap.put(NODETOID_START, callActivityNode.getId());
                startEndMap.put(NODETOID_END, callActivityNode.getId());
                nameToNodeId.put(state.getName(), startEndMap);
            }

            if (state.getType().equals(Type.SWITCH)) {
                SwitchState switchState = (SwitchState) state;

                // check if data-based or event-based switch state
                if (switchState.getDataConditions() != null && !switchState.getDataConditions().isEmpty()) {
                    // data-based switch state
                    Split splitNode = factory.splitNode(idCounter.getAndIncrement(), switchState.getName(), Split.TYPE_XOR, process);

                    if (state.getStart() != null) {
                        factory.connect(workflowStartNode.getId(), splitNode.getId(), workflowStartNode.getId() + "_" + splitNode.getId(), process);
                    }
                    // switch states cannot be end states

                    Map<String, Long> startEndMap = new HashMap<>();
                    startEndMap.put(NODETOID_START, splitNode.getId());
                    startEndMap.put(NODETOID_END, splitNode.getId());
                    nameToNodeId.put(state.getName(), startEndMap);
                } else if (switchState.getEventConditions() != null && !switchState.getEventConditions().isEmpty()) {
                    // event-based switch state
                    Split splitNode = factory.eventBasedSplit(idCounter.getAndIncrement(), switchState.getName(), process);
                    if (state.getStart() != null) {
                        factory.connect(workflowStartNode.getId(), splitNode.getId(), workflowStartNode.getId() + "_" + splitNode.getId(), process);
                    }
                    // switch states cannot be end states

                    Map<String, Long> startEndMap = new HashMap<>();
                    startEndMap.put(NODETOID_START, splitNode.getId());
                    startEndMap.put(NODETOID_END, splitNode.getId());
                    nameToNodeId.put(state.getName(), startEndMap);
                } else {
                    LOGGER.warn("unable to determine switch state type (data or event based): {}", switchState.getName());
                }
            }

            if (state.getType().equals(Type.PARALLEL)) {
                ParallelState parallelState = (ParallelState) state;

                Split parallelSplit = factory.splitNode(idCounter.getAndIncrement(), parallelState.getName() + NODE_START_NAME, Split.TYPE_AND, process);
                Join parallelJoin = factory.joinNode(idCounter.getAndIncrement(), parallelState.getName() + NODE_END_NAME, Join.TYPE_AND, process);

                for (Branch branch : parallelState.getBranches()) {
                    SubflowState subflowState = (SubflowState) branch.getStates().get(0);
                    SubProcessNode callActivityNode = factory.callActivity(idCounter.getAndIncrement(), subflowState.getName(), subflowState.getWorkflowId(), subflowState.isWaitForCompletion(), process);

                    factory.connect(parallelSplit.getId(), callActivityNode.getId(), parallelSplit.getId() + "_" + callActivityNode.getId(), process);
                    factory.connect(callActivityNode.getId(), parallelJoin.getId(), callActivityNode.getId() + "_" + parallelJoin.getId(), process);

                }

                if (state.getStart() != null) {
                    factory.connect(workflowStartNode.getId(), parallelSplit.getId(), workflowStartNode.getId() + "_" + parallelSplit.getId(), process);
                }

                if (state.getEnd() != null) {
                    factory.connect(parallelJoin.getId(), workflowEndNodes.get(state.getName()).getId(), parallelJoin.getId() + "_" + workflowEndNodes.get(state.getName()).getId(), process);
                }

                Map<String, Long> startEndMap = new HashMap<>();
                startEndMap.put(NODETOID_START, parallelSplit.getId());
                startEndMap.put(NODETOID_END, parallelJoin.getId());
                nameToNodeId.put(state.getName(), startEndMap);
            }
        }

        workflow.getStates().stream().filter(state -> (state instanceof State)).forEach(state -> {
            Transition transition = state.getTransition();

            if (transition != null && transition.getNextState() != null) {
                Long sourceId = nameToNodeId.get(state.getName()).get(NODETOID_END);
                Long targetId = nameToNodeId.get(state.getTransition().getNextState()).get(NODETOID_START);

                if (transition.getProduceEvent() != null && transition.getProduceEvent().getEventRef() != null) {
                    ActionNode sendEventNode = factory.sendEventNode(idCounter.getAndIncrement(),
                            ServerlessWorkflowUtils.getWorkflowEventFor(workflow, transition.getProduceEvent().getEventRef()), process);
                    factory.connect(sourceId, sendEventNode.getId(), sourceId + "_" + sendEventNode.getId(), process);
                    factory.connect(sendEventNode.getId(), targetId, sendEventNode + "_" + targetId, process);
                } else {
                    factory.connect(sourceId, targetId, sourceId + "_" + targetId, process);
                }
            }
        });

        // after all nodes initialized add constraints, finish switch nodes
        List<State> switchStates = ServerlessWorkflowUtils.getStatesByType(workflow, Type.SWITCH);
        if (switchStates != null && switchStates.size() > 0) {
            for (State state : switchStates) {
                SwitchState switchState = (SwitchState) state;

                if (switchState.getDataConditions() != null && !switchState.getDataConditions().isEmpty()) {
                    finalizeDataBasedSwitchState(switchState, nameToNodeId, process);
                } else {
                    finalizeEventBasedSwitchState(switchState, nameToNodeId, process, workflow);
                }
            }
        }

        factory.validate(process);
        return process;
    }

    protected void finalizeEventBasedSwitchState(SwitchState switchState, Map<String, Map<String, Long>> nameToNodeId, RuleFlowProcess process, Workflow workflow) {
        long eventSplitNodeId = nameToNodeId.get(switchState.getName()).get(NODETOID_START);
        Split eventSplit = (Split) process.getNode(eventSplitNodeId);

        if (eventSplit != null) {

            List<EventCondition> conditions = switchState.getEventConditions();

            if (conditions != null && !conditions.isEmpty()) {
                for (EventCondition eventCondition : conditions) {
                    EventDefinition eventDefinition = ServerlessWorkflowUtils.getWorkflowEventFor(workflow, eventCondition.getEventRef());
                    long targetId = nameToNodeId.get(eventCondition.getTransition().getNextState()).get(NODETOID_START);

                    EventNode eventNode = factory.consumeEventNode(idCounter.getAndIncrement(), eventDefinition, process);

                    factory.connect(eventSplit.getId(), eventNode.getId(), eventSplit.getId() + "_" + eventNode, process);
                    factory.connect(eventNode.getId(), targetId, eventNode.getId() + "_" + targetId, process);

                }
            } else {
                LOGGER.warn("switch state has no event conditions: {}", switchState.getName());
            }

        } else {
            LOGGER.error("unable to get event split node for switch state: {}", switchState.getName());
        }
    }

    protected void finalizeDataBasedSwitchState(SwitchState switchState, Map<String, Map<String, Long>> nameToNodeId, RuleFlowProcess process) {
        long splitNodeId = nameToNodeId.get(switchState.getName()).get(NODETOID_START);
        Split xorSplit = (Split) process.getNode(splitNodeId);

        if (xorSplit != null) {
            // set default connection
            if (switchState.getDefault() != null && switchState.getDefault().getNextState() != null) {
                long targetId = nameToNodeId.get(switchState.getDefault().getNextState()).get(NODETOID_START);
                xorSplit.getMetaData().put("Default", xorSplit.getId() + "_" + targetId);
            }

            List<DataCondition> conditions = switchState.getDataConditions();

            if (conditions != null && !conditions.isEmpty()) {
                for (DataCondition condition : conditions) {
                    // connect
                    long targetId = nameToNodeId.get(condition.getTransition().getNextState()).get(NODETOID_START);
                    factory.connect(xorSplit.getId(), targetId, xorSplit.getId() + "_" + targetId, process);

                    // set constraint
                    boolean isDefaultConstraint = false;
                    if (switchState.getDefault().getNextState() != null && condition.getTransition().getNextState().equals(switchState.getDefault().getNextState())) {
                        isDefaultConstraint = true;
                    }

                    ConstraintImpl constraintImpl = factory.splitConstraint(xorSplit.getId() + "_" + targetId,
                            "DROOLS_DEFAULT", "java", ServerlessWorkflowUtils.conditionScript(condition.getPath(), condition.getOperator(), condition.getValue()), 0, isDefaultConstraint);
                    xorSplit.addConstraint(new ConnectionRef(xorSplit.getId() + "_" + targetId, targetId, org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE), constraintImpl);

                }
            } else {
                LOGGER.warn("switch state has no conditions: {}", switchState.getName());
            }
        } else {
            LOGGER.error("unable to get split node for switch state: {}", switchState.getName());
        }
    }

    protected void handleActions(List<Function> workflowFunctions,
                                 List<Action> actions,
                                 RuleFlowProcess process,
                                 CompositeContextNode embeddedSubProcess) {
        if (actions != null && !actions.isEmpty()) {
            StartNode embeddedStartNode =
                    factory.startNode(idCounter.getAndIncrement(), "EmbeddedStart", embeddedSubProcess);
            Node start = embeddedStartNode;
            Node current = null;

            for (Action action : actions) {
                Function actionFunction = workflowFunctions
                    .stream()
                    .filter(wf -> wf.getName().equals(action.getFunctionRef().getRefName()))
                    .findFirst()
                    .orElseThrow(
                                 () -> new IllegalArgumentException(
                                     "cannot find function " + action.getFunctionRef().getRefName()));
                if (actionFunction.getType() != null) {
                    switch (actionFunction.getType().toLowerCase()) {
                        case SCRIPT_TYPE:
                            current = factory
                                .scriptNode(
                                            idCounter.getAndIncrement(),
                                            action.getFunctionRef().getRefName(),
                                            ServerlessWorkflowUtils
                                                .scriptFunctionScript(
                                                                      action
                                                                          .getFunctionRef()
                                                                          .getParameters()
                                                                          .get(SCRIPT_TYPE_PARAM)),
                                            embeddedSubProcess);
                            break;
                        case SYSOUT_TYPE:
                            current = factory
                                .scriptNode(
                                            idCounter.getAndIncrement(),
                                            action.getFunctionRef().getRefName(),
                                            ServerlessWorkflowUtils
                                                .sysOutFunctionScript(
                                                                      action
                                                                          .getFunctionRef()
                                                                          .getParameters()
                                                                          .get(SYSOUT_TYPE_PARAM)),
                                            embeddedSubProcess);
                            break;
                        case SERVICE_TYPE:
                            current = factory
                                .serviceNode(
                                             idCounter.getAndIncrement(),
                                             action.getFunctionRef().getRefName(),
                                             actionFunction,
                                             embeddedSubProcess);
                            break;
                        case DECISION_TYPE:
                            current = factory
                                .humanTaskNode(
                                               idCounter.getAndIncrement(),
                                               action.getFunctionRef().getRefName(),
                                               actionFunction,
                                               process,
                                               embeddedSubProcess);
                            break;
                        case RULE_TYPE:
                            current = factory
                                .ruleSetNode(
                                             idCounter.getAndIncrement(),
                                             action.getFunctionRef().getRefName(),
                                             actionFunction,
                                             embeddedSubProcess);
                            break;
                        case INTEGRATION_TYPE:
                            current = factory
                                .camelRouteServiceNode(
                                                       idCounter.getAndIncrement(),
                                                       action.getFunctionRef().getRefName(),
                                                       actionFunction,
                                                       embeddedSubProcess);
                            break;
                        case REST_TYPE:
                            current = factory
                                .restServiceNode(
                                                 idCounter.getAndIncrement(),
                                                 action.getFunctionRef(),
                                                 actionFunction,
                                                 embeddedSubProcess);
                            break;
                        default:
                            current = unsupportedNode(action, embeddedSubProcess);
                    }
                } else {
                    current = unsupportedNode(action, embeddedSubProcess);
                }
                factory.connect(start.getId(), current.getId(), start.getId() + "_" + current.getId(), embeddedSubProcess);
                start = current;
            }
            EndNode embeddedEndNode = factory.endNode(idCounter.getAndIncrement(), "EmbeddedEnd", true, embeddedSubProcess);
            try {
                factory.connect(current.getId(), embeddedEndNode.getId(), current.getId() + "_" + embeddedEndNode.getId(), embeddedSubProcess);
            } catch (NullPointerException e) {
                LOGGER.warn("unable to connect current node to embedded end node");
            }
        }
    }

    private Node unsupportedNode(Action action, CompositeContextNode embeddedSubProcess) {
        LOGGER
            .warn(
                  "currently unsupported function type, supported types are 'script', 'sysout', 'service', 'decision', 'rule', 'integration', 'rest'");
        LOGGER.warn("defaulting to script type");
        return factory.scriptNode(
                        idCounter.getAndIncrement(),
                        action.getFunctionRef().getRefName(),
                        ServerlessWorkflowUtils.scriptFunctionScript(""),
                        embeddedSubProcess);
    }

}