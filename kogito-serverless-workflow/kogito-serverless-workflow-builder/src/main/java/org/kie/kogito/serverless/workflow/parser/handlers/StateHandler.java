/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.serverless.workflow.parser.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.jbpm.compiler.canonical.descriptors.ExpressionReturnValueSupplier;
import org.jbpm.process.core.context.exception.CompensationScope;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.ruleflow.core.Metadata;
import org.jbpm.ruleflow.core.RuleFlowNodeContainerFactory;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.ruleflow.core.factory.AbstractCompositeNodeFactory;
import org.jbpm.ruleflow.core.factory.ActionNodeFactory;
import org.jbpm.ruleflow.core.factory.BoundaryEventNodeFactory;
import org.jbpm.ruleflow.core.factory.CompositeContextNodeFactory;
import org.jbpm.ruleflow.core.factory.EndNodeFactory;
import org.jbpm.ruleflow.core.factory.JoinFactory;
import org.jbpm.ruleflow.core.factory.NodeFactory;
import org.jbpm.ruleflow.core.factory.SplitFactory;
import org.jbpm.ruleflow.core.factory.SupportsAction;
import org.jbpm.ruleflow.core.factory.TimerNodeFactory;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.kie.kogito.serverless.workflow.parser.ParserContext;
import org.kie.kogito.serverless.workflow.parser.ServerlessWorkflowParser;
import org.kie.kogito.serverless.workflow.suppliers.CollectorActionSupplier;
import org.kie.kogito.serverless.workflow.suppliers.CompensationActionSupplier;
import org.kie.kogito.serverless.workflow.suppliers.ExpressionActionSupplier;
import org.kie.kogito.serverless.workflow.suppliers.MergeActionSupplier;
import org.kie.kogito.serverless.workflow.suppliers.ProduceEventActionSupplier;
import org.kie.kogito.serverless.workflow.utils.ExpressionHandlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.end.End;
import io.serverlessworkflow.api.error.Error;
import io.serverlessworkflow.api.error.ErrorDefinition;
import io.serverlessworkflow.api.events.EventDefinition;
import io.serverlessworkflow.api.filters.EventDataFilter;
import io.serverlessworkflow.api.filters.StateDataFilter;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.api.produce.ProduceEvent;
import io.serverlessworkflow.api.transitions.Transition;
import io.serverlessworkflow.api.workflow.Errors;

import static org.kie.kogito.serverless.workflow.parser.ServerlessWorkflowParser.DEFAULT_WORKFLOW_VAR;
import static org.kie.kogito.serverless.workflow.parser.handlers.NodeFactoryUtils.eventBasedSplitNode;
import static org.kie.kogito.serverless.workflow.parser.handlers.NodeFactoryUtils.joinExclusiveNode;
import static org.kie.kogito.serverless.workflow.parser.handlers.NodeFactoryUtils.timerNode;
import static org.kie.kogito.serverless.workflow.utils.TimeoutsConfigResolver.resolveEventTimeout;

public abstract class StateHandler<S extends State> {

    private static Logger logger = LoggerFactory.getLogger(StateHandler.class);

    protected static final String XORSPLITDEFAULT = "Default";

    protected final S state;
    protected final Workflow workflow;
    protected final ParserContext parserContext;

    private NodeFactory<?, ?> startNodeFactory;
    private NodeFactory<?, ?> endNodeFactory;

    private NodeFactory<?, ?> node;
    private NodeFactory<?, ?> outgoingNode;

    protected final boolean isStartState;

    private JoinFactory<?> join;
    private Collection<Long> incomingConnections = new ArrayList<>();

    protected StateHandler(S state, Workflow workflow, ParserContext parserContext) {
        this.workflow = workflow;
        this.state = state;
        this.parserContext = parserContext;
        this.isStartState = state.getName().equals(workflow.getStart().getStateName());
    }

    public boolean usedForCompensation() {
        return false;
    }

    public void handleStart() {
        if (isStartState) {
            startNodeFactory = parserContext.factory().startNode(parserContext.newId()).name(ServerlessWorkflowParser.NODE_START_NAME);
            startNodeFactory.done();
        }
    }

    public void handleEnd() {
        End endState = state.getEnd();
        if (endState != null) {
            endNodeFactory = endNodeFactory(parserContext.factory(), endState).name(ServerlessWorkflowParser.NODE_END_NAME);
            endNodeFactory.done();
        }
    }

    protected final <T extends NodeFactory<?, ?> & SupportsAction<?, ?>>
            NodeFactory<?, ?> sendEventNode(T actionNode, ProduceEvent event) {
        return sendEventNode(actionNode, eventDefinition(event.getEventRef()), event.getData(), DEFAULT_WORKFLOW_VAR);
    }

    protected final <T extends NodeFactory<?, ?> & SupportsAction<?, ?>> NodeFactory<?, ?> sendEventNode(T actionNode,
            EventDefinition eventDefinition,
            String data,
            String defaultWorkflowVar) {
        return NodeFactoryUtils.sendEventNode(
                actionNode.action(new ProduceEventActionSupplier(workflow, eventDefinition.getType(), defaultWorkflowVar, data)),
                eventDefinition,
                defaultWorkflowVar);
    }

    private void handleCompensation(RuleFlowNodeContainerFactory<?, ?> factory) {
        StateHandler<?> compensation = parserContext.getStateHandler(state.getCompensatedBy());
        if (compensation == null) {
            throw new IllegalArgumentException("State " + getState().getName() + " refers to a compensation " + state.getCompensatedBy() + " which cannot be found");
        }
        parserContext.setCompensation();
        long eventCompensationId = parserContext.newId();
        long subprocessCompensationId = parserContext.newId();
        long startCompensationId = parserContext.newId();
        String uniqueId = (String) outgoingNode.getNode().getMetaData().get(Metadata.UNIQUE_ID);
        factory.boundaryEventNode(eventCompensationId).addCompensationHandler(uniqueId).attachedTo(uniqueId).eventType("Compensation").metaData(Metadata.EVENT_TYPE, "compensation");
        CompositeContextNodeFactory<?> embeddedSubProcess =
                factory.compositeContextNode(subprocessCompensationId).autoComplete(true).metaData("isForCompensation", true).startNode(startCompensationId).interrupting(true).done();
        factory.association(eventCompensationId, subprocessCompensationId, null);
        long lastNodeId = handleCompensation(embeddedSubProcess, compensation);
        embeddedSubProcess.connection(startCompensationId, lastNodeId);
        compensation = parserContext.getStateHandler(compensation);
        while (compensation != null) {
            if (!compensation.usedForCompensation()) {
                throw new IllegalArgumentException(
                        "Compensation state can only have transition to other compensation state. State " + compensation.getState().getName() + " is not used for compensation");
            }
            lastNodeId = handleCompensation(embeddedSubProcess, compensation);
            compensation = parserContext.getStateHandler(compensation);
        }
        long endCompensationId = parserContext.newId();
        embeddedSubProcess.endNode(endCompensationId).terminate(false).done().connection(lastNodeId, endCompensationId);
    }

    private long handleCompensation(RuleFlowNodeContainerFactory<?, ?> embeddedSubProcess,
            StateHandler<?> compensation) {
        if (compensation.getState().getCompensatedBy() != null) {
            throw new IllegalArgumentException("Serverless workflow specification forbids nested compensations, hence state " + compensation.getState().getName() + " is not valid");
        }
        compensation.handleState(embeddedSubProcess);
        Transition transition = compensation.getState().getTransition();
        compensation.handleTransitions(embeddedSubProcess, transition, compensation.getNode());
        compensation.handleConnections(embeddedSubProcess);
        return compensation.getNode().getNode().getId();
    }

    public void handleState() {
        handleState(parserContext.factory());
    }

    protected void handleState(RuleFlowNodeContainerFactory<?, ?> factory) {
        MakeNodeResult result = makeNode(factory);
        node = result.getIncomingNode();
        outgoingNode = result.getOutgoingNode();
        if (state.getCompensatedBy() != null) {
            handleCompensation(factory);
        }
        node.done();
        outgoingNode.done();
        StateDataFilter stateFilter = state.getStateDataFilter();
        if (stateFilter != null) {
            String input = stateFilter.getInput();
            if (input != null) {
                ActionNodeFactory<?> actionNode = handleStateFilter(factory, input);
                factory.connection(actionNode.getNode().getId(), node.getNode().getId());
                node = actionNode;
            }
            String output = stateFilter.getOutput();
            if (output != null) {
                ActionNodeFactory<?> actionNode = handleStateFilter(factory, output);
                factory.connection(outgoingNode.getNode().getId(), actionNode.getNode().getId());
                outgoingNode = actionNode;
            }
        }
        connectStart(factory);
        connectEnd(factory);
    }

    private ActionNodeFactory<?> handleStateFilter(RuleFlowNodeContainerFactory<?, ?> factory, String filter) {
        ActionNodeFactory<?> result =
                factory.actionNode(parserContext.newId())
                        .action(ExpressionActionSupplier.of(workflow, filter).build());
        result.done();
        return result;
    }

    protected void connectSource(NodeFactory<?, ?> sourceNode) {
        long id = sourceNode.getNode().getId();
        if (sourceNode instanceof SplitFactory || !incomingConnections.contains(id)) {
            incomingConnections.add(id);
        }
    }

    public void handleConnections() {
        handleConnections(parserContext.factory());
    }

    protected void handleConnections(RuleFlowNodeContainerFactory<?, ?> factory) {
        NodeFactory<?, ?> incoming = getIncomingNode(factory);
        for (long sourceId : incomingConnections) {
            factory.connection(sourceId, incoming.getNode().getId());
        }
    }

    private boolean hasCode(ErrorDefinition errorDef) {
        if (errorDef.getCode() == null) {
            logger.error("Kogito requires code error to be set. Ignoring {}", errorDef.getName());
            return false;
        }
        return true;
    }

    protected final Collection<ErrorDefinition> getErrorDefinitions(Error error) {
        Errors errors = workflow.getErrors();
        if (errors == null) {
            throw new IllegalArgumentException("workflow should contain errors property");
        }
        List<ErrorDefinition> errorDefs = errors.getErrorDefs();
        if (errorDefs == null) {
            throw new IllegalArgumentException("workflow errors property must contain errorDefs property");
        }

        if (error.getErrorRef() != null) {
            return getErrorsDefinitions(errorDefs, Arrays.asList(error.getErrorRef()));
        } else if (error.getErrorRefs() != null) {
            return getErrorsDefinitions(errorDefs, error.getErrorRefs());
        } else {
            throw new IllegalArgumentException("state errors should contain either errorRef or errorRefs property");
        }
    }

    private Collection<ErrorDefinition> getErrorsDefinitions(List<ErrorDefinition> errorDefs, List<String> errorRefs) {
        Collection<ErrorDefinition> result = new ArrayList<>();
        for (String errorRef : errorRefs) {
            result.add(errorDefs.stream().filter(errorDef -> errorDef.getName().equals(errorRef) && hasCode(errorDef)).findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find any error definition for errorRef" + errorRef)));
        }
        return result;
    }

    protected final void handleErrors(RuleFlowNodeContainerFactory<?, ?> factory, RuleFlowNodeContainerFactory<?, ?> targetNode) {
        for (Error error : state.getOnErrors()) {
            getErrorDefinitions(error).forEach(errorDef -> {
                String errorPrefix = RuleFlowProcessFactory.ERROR_TYPE_PREFIX + targetNode.getNode().getMetaData().get("UniqueId") + '-';
                BoundaryEventNodeFactory<?> boundaryNode = factory.boundaryEventNode(parserContext.newId()).attachedTo(targetNode.getNode().getId())
                        .metaData(Metadata.EVENT_TYPE, Metadata.EVENT_TYPE_ERROR).metaData("HasErrorEvent", true).metaData(Metadata.ERROR_EVENT, errorDef.getCode())
                        .eventType(errorPrefix + errorDef.getCode())
                        .name(RuleFlowProcessFactory.ERROR_TYPE_PREFIX + targetNode.getNode().getName() + '-' + errorDef.getCode());
                targetNode.exceptionHandler(errorDef.getCode(), errorDef.getCode());
                if (error.getEnd() != null) {
                    connect(boundaryNode, endNodeFactory(factory, error.getEnd()));
                } else {
                    handleTransitions(factory, error.getTransition(), boundaryNode);
                }
            });
        }
    }

    public void handleTransitions() {
        handleTransitions(parserContext.factory(), state.getTransition(), outgoingNode);
    }

    protected void handleTransitions(RuleFlowNodeContainerFactory<?, ?> factory,
            Transition transition,
            NodeFactory<?, ?> sourceNode) {
        handleTransition(factory, transition, sourceNode, Optional.empty());
    }

    private void connectStart(RuleFlowNodeContainerFactory<?, ?> factory) {
        if (startNodeFactory != null) {
            factory.connection(startNodeFactory.getNode().getId(), node.getNode().getId());
        }
    }

    private void connectEnd(RuleFlowNodeContainerFactory<?, ?> factory) {
        if (endNodeFactory != null) {
            if (state.getEnd().isCompensate()) {
                endNodeFactory.done().connection(compensationEvent(factory, outgoingNode).getNode().getId(), endNodeFactory.getNode().getId());
            } else {
                factory.connection(outgoingNode.getNode().getId(), endNodeFactory.getNode().getId());
            }
        }
    }

    public final NodeFactory<?, ?> getNode() {
        return node;
    }

    public S getState() {
        return state;
    }

    public NodeFactory<?, ?> getIncomingNode(RuleFlowNodeContainerFactory<?, ?> factory) {
        if (join != null) {
            return join;
        } else if (incomingConnections.size() > 1) {
            join = factory.joinNode(parserContext.newId()).type(Join.TYPE_OR).name("Join-" + node.getNode()
                    .getName());
            join.done().connection(join.getNode().getId(), node.getNode().getId());
            return join;
        } else {
            return getNode();
        }
    }

    protected abstract MakeNodeResult makeNode(RuleFlowNodeContainerFactory<?, ?> factory);

    protected final void handleTransition(RuleFlowNodeContainerFactory<?, ?> factory,
            Transition transition,
            NodeFactory<?, ?> sourceFactory,
            Optional<HandleTransitionCallBack> callback) {
        StateHandler<?> targetState = parserContext.getStateHandler(transition);
        if (targetState != null) {
            List<ProduceEvent> produceEvents = transition.getProduceEvents();
            if (produceEvents.isEmpty()) {
                if (transition.isCompensate()) {
                    NodeFactory<?, ?> compensationNode = compensationEvent(factory, sourceFactory);
                    targetState.connectSource(compensationNode);
                    callback.ifPresent(c -> c.onIdTarget(compensationNode.getNode().getId()));
                } else {
                    targetState.connectSource(sourceFactory);
                    callback.ifPresent(c -> c.onStateTarget(targetState));
                }
            } else {
                ActionNodeFactory<?> actionNode = factory.actionNode(parserContext.newId());
                NodeFactory<?, ?> startNode = handleProduceEvents(factory, actionNode, produceEvents);
                factory.connection(sourceFactory.getNode().getId(), startNode.getNode().getId());
                if (transition.isCompensate()) {
                    long eventId = compensationEvent(factory, sourceFactory).getNode().getId();
                    callback.ifPresent(c -> c.onIdTarget(eventId));
                } else {
                    callback.ifPresent(c -> c.onIdTarget(startNode.getNode().getId()));
                }
                targetState.connectSource(actionNode);
            }
        } else {
            callback.ifPresent(HandleTransitionCallBack::onEmptyTarget);
        }
    }

    private <T extends NodeFactory<?, ?> & SupportsAction<?, ?>> NodeFactory<?, ?> handleProduceEvents(RuleFlowNodeContainerFactory<?, ?> factory, T endNode, List<ProduceEvent> produceEvents) {
        NodeFactory<?, ?> startNode = endNode;
        NodeFactory<?, ?> currentNode;
        sendEventNode(endNode, produceEvents.get(0));
        if (produceEvents.size() > 1) {
            ListIterator<ProduceEvent> iter = produceEvents.listIterator(1);
            while (iter.hasNext()) {
                currentNode = startNode;
                startNode = sendEventNode(factory.actionNode(parserContext.newId()), iter.next());
                connect(startNode, currentNode);
            }
        }
        return startNode;
    }

    @FunctionalInterface
    protected interface FilterableNodeSupplier {
        NodeFactory<?, ?> apply(RuleFlowNodeContainerFactory<?, ?> factory, String inputVar, String outputVar);
    }

    protected final String getVarName() {
        return state.getName() + "_" + parserContext.newId();
    }

    protected final MakeNodeResult filterAndMergeNode(RuleFlowNodeContainerFactory<?, ?> embeddedSubProcess, EventDataFilter eventFilter, FilterableNodeSupplier nodeSupplier) {
        return filterAndMergeNode(embeddedSubProcess, eventFilter, getVarName(), nodeSupplier);
    }

    protected final MakeNodeResult filterAndMergeNode(RuleFlowNodeContainerFactory<?, ?> embeddedSubProcess, EventDataFilter eventFilter, String varName,
            FilterableNodeSupplier nodeSupplier) {
        String dataExpr = null;
        String toExpr = null;
        boolean useData = true;
        if (eventFilter != null) {
            dataExpr = eventFilter.getData();
            toExpr = eventFilter.getToStateData();
            useData = eventFilter.isUseData();
        }
        return filterAndMergeNode(embeddedSubProcess, varName, null, dataExpr, toExpr, useData, true, nodeSupplier);
    }

    protected boolean isTempVariable(String varName) {
        return !varName.equals(ServerlessWorkflowParser.DEFAULT_WORKFLOW_VAR);
    }

    protected final MakeNodeResult filterAndMergeNode(RuleFlowNodeContainerFactory<?, ?> embeddedSubProcess, String actionVarName, String fromStateExpr, String resultExpr, String toStateExpr,
            boolean useData,
            boolean shouldMerge, FilterableNodeSupplier nodeSupplier) {

        if (isTempVariable(actionVarName)) {
            embeddedSubProcess.variable(actionVarName, new ObjectDataType(JsonNode.class.getCanonicalName()), Variable.VARIABLE_TAGS, Variable.INTERNAL_TAG);
        }
        NodeFactory<?, ?> startNode, currentNode;
        if (fromStateExpr != null) {
            startNode = embeddedSubProcess.actionNode(parserContext.newId()).action(ExpressionActionSupplier.of(workflow, fromStateExpr)
                    .withVarNames(DEFAULT_WORKFLOW_VAR, actionVarName).build());
            currentNode = connect(startNode, nodeSupplier.apply(embeddedSubProcess, actionVarName, actionVarName));

        } else {
            startNode = currentNode = nodeSupplier.apply(embeddedSubProcess, DEFAULT_WORKFLOW_VAR, actionVarName);
        }

        if (useData && resultExpr != null) {
            currentNode = connect(currentNode, embeddedSubProcess.actionNode(parserContext.newId()).action(ExpressionActionSupplier.of(workflow, resultExpr)
                    .withVarNames(actionVarName, actionVarName).build()));
        }

        if (useData) {
            if (toStateExpr != null) {
                currentNode = connect(currentNode, embeddedSubProcess.actionNode(parserContext.newId())
                        .action(new CollectorActionSupplier(workflow.getExpressionLang(), toStateExpr, DEFAULT_WORKFLOW_VAR, actionVarName)));
            } else if (shouldMerge) {
                currentNode = connect(currentNode, embeddedSubProcess.actionNode(parserContext.newId()).action(new MergeActionSupplier(actionVarName, DEFAULT_WORKFLOW_VAR)));
            }
        }
        currentNode.done();
        return new MakeNodeResult(startNode, currentNode);
    }

    protected final NodeFactory<?, ?> connect(NodeFactory<?, ?> currentNode, NodeFactory<?, ?> nodeFactory) {
        currentNode.done().connection(currentNode.getNode().getId(), nodeFactory.getNode().getId());
        return nodeFactory;
    }

    protected final NodeFactory<?, ?> connect(NodeFactory<?, ?> currentNode, MakeNodeResult twoNodes) {
        connect(currentNode, twoNodes.getIncomingNode()).done();
        return twoNodes.getOutgoingNode();
    }

    protected final NodeFactory<?, ?> consumeEventNode(RuleFlowNodeContainerFactory<?, ?> factory, String eventRef, String inputVar, String outputVar) {
        EventDefinition eventDefinition = eventDefinition(eventRef);
        return NodeFactoryUtils.consumeMessageNode(factory.eventNode(parserContext.newId()), eventDefinition, inputVar, outputVar);
    }

    protected final EventDefinition eventDefinition(String eventName) {
        return workflow.getEvents().getEventDefs().stream()
                .filter(wt -> wt.getName().equals(eventName))
                .findFirst().orElseThrow(() -> new NoSuchElementException("No event for " + eventName));
    }

    protected final MakeNodeResult makeTimeoutNode(RuleFlowNodeContainerFactory<?, ?> factory, MakeNodeResult notTimerBranch) {
        String eventTimeout = resolveEventTimeout(state, workflow);
        if (eventTimeout != null) {
            if (notTimerBranch.getIncomingNode() == notTimerBranch.getOutgoingNode() && notTimerBranch.getIncomingNode() instanceof AbstractCompositeNodeFactory) {
                // reusing composite
                ((AbstractCompositeNodeFactory<?, ?>) notTimerBranch.getIncomingNode()).timeout(eventTimeout);
                return notTimerBranch;
            } else {
                // creating a split-join branch for the timer
                SplitFactory<?> splitNode = eventBasedSplitNode(factory.splitNode(parserContext.newId()), Split.TYPE_XAND);
                JoinFactory<?> joinNode = joinExclusiveNode(factory.joinNode(parserContext.newId()));
                connect(connect(splitNode, notTimerBranch), joinNode);
                createTimerNode(factory, splitNode, joinNode, eventTimeout);
                return new MakeNodeResult(splitNode, joinNode);
            }
        } else {
            // No timeouts, returning the existing branch.
            return notTimerBranch;
        }
    }

    private void createTimerNode(RuleFlowNodeContainerFactory<?, ?> factory, SplitFactory<?> splitNode, JoinFactory<?> joinNode, String eventTimeout) {
        TimerNodeFactory<?> eventTimeoutTimerNode = timerNode(factory.timerNode(parserContext.newId()), eventTimeout);
        connect(splitNode, eventTimeoutTimerNode);
        connect(eventTimeoutTimerNode, joinNode);
    }

    protected final NodeFactory<?, ?> endNodeFactory(RuleFlowNodeContainerFactory<?, ?> factory, End end) {
        EndNodeFactory<?> nodeFactory = factory.endNode(parserContext.newId());
        NodeFactory<?, ?> startNode = nodeFactory;
        List<ProduceEvent> produceEvents = end.getProduceEvents();
        if (produceEvents != null && !produceEvents.isEmpty()) {
            startNode = handleProduceEvents(factory, nodeFactory, produceEvents);
        }
        nodeFactory.terminate(end.isTerminate());
        return startNode;
    }

    private NodeFactory<?, ?> compensationEvent(RuleFlowNodeContainerFactory<?, ?> factory, NodeFactory<?, ?> sourceFactory) {
        long eventId = parserContext.newId();
        NodeFactory<?, ?> compensationNode =
                factory.actionNode(eventId).name(state.getName() + "-" + eventId).action(new CompensationActionSupplier(CompensationScope.IMPLICIT_COMPENSATION_PREFIX + workflow.getId()));
        compensationNode.done().connection(sourceFactory.getNode().getId(), eventId);
        return compensationNode;
    }

    protected interface HandleTransitionCallBack {
        default void onStateTarget(StateHandler<?> targetState) {
        }

        default void onIdTarget(long targetId) {
        }

        default void onEmptyTarget() {
        }
    }

    protected final <T extends RuleFlowNodeContainerFactory<T, ?>> SplitFactory<T> addCondition(SplitFactory<T> splitNode, NodeFactory<?, ?> targetNode, String condition, boolean isDefault) {
        return addCondition(splitNode, targetNode.getNode().getId(), condition, isDefault);
    }

    protected final <T extends RuleFlowNodeContainerFactory<T, ?>> SplitFactory<T> addCondition(SplitFactory<T> splitNode, long targetId, String condition, boolean isDefault) {
        return splitNode.constraint(targetId, concatId(splitNode.getNode().getId(), targetId),
                "DROOLS_DEFAULT", workflow.getExpressionLang(),
                new ExpressionReturnValueSupplier(workflow.getExpressionLang(), ExpressionHandlerUtils.replaceExpr(workflow, condition), DEFAULT_WORKFLOW_VAR), 0, isDefault)
                .metaData(Metadata.VARIABLE, DEFAULT_WORKFLOW_VAR);
    }

    protected static String concatId(long start, long end) {
        return start + "_" + end;
    }
}
