/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.serverless.workflow.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.Metadata;
import org.jbpm.ruleflow.core.RuleFlowNodeContainerFactory;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.ruleflow.core.factory.EventNodeFactory;
import org.jbpm.ruleflow.core.factory.NodeFactory;
import org.jbpm.ruleflow.core.factory.StartNodeFactory;
import org.jbpm.ruleflow.core.factory.SubProcessNodeFactory;
import org.jbpm.serverless.workflow.parser.handlers.StateHandler;
import org.jbpm.serverless.workflow.parser.handlers.StateHandlerFactory;
import org.jbpm.serverless.workflow.parser.util.ServerlessWorkflowUtils;
import org.kie.api.definition.process.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.events.EventDefinition;
import io.serverlessworkflow.api.interfaces.State;

public class ServerlessWorkflowParser {

    private static final Logger logger = LoggerFactory.getLogger(ServerlessWorkflowParser.class);

    public static final String NODE_START_NAME = "Start";
    public static final String NODE_END_NAME = "End";
    private static final String DEFAULT_PACKAGE = "org.kie.kogito.serverless";

    public static final String JSON_NODE = "com.fasterxml.jackson.databind.JsonNode";
    public static final String DEFAULT_WORKFLOW_VAR = "workflowdata";

    private NodeIdGenerator idGenerator = DefaultNodeIdGenerator.get();
    private Workflow workflow;
    private Process process;

    public static ServerlessWorkflowParser of(Reader workflowFile,
            String workflowFormat) throws JsonProcessingException {
        return of(ServerlessWorkflowUtils.getObjectMapper(workflowFormat).readValue(ServerlessWorkflowUtils
                .readWorkflowFile(workflowFile), Workflow.class));
    }

    public static ServerlessWorkflowParser of(Workflow workflow) {
        return new ServerlessWorkflowParser(workflow);
    }

    private ServerlessWorkflowParser(Workflow workflow) {
        this.workflow = workflow;
        this.process = parseProcess();
    }

    private Process parseProcess() {
        RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess(workflow.getId())
                .name(workflow.getName() == null ? "workflow" : workflow.getName())
                .version(workflow.getVersion() == null ? "1.0" : workflow.getVersion())
                .packageName(workflow.getMetadata() != null ? workflow.getMetadata().getOrDefault("package",
                        DEFAULT_PACKAGE) : DEFAULT_PACKAGE)
                .visibility("Public")
                .variable(DEFAULT_WORKFLOW_VAR, JsonNode.class);
        Map<String, ConnectionInfo> stateConnection = new HashMap<>();
        String workflowStartStateName = workflow.getStart().getStateName();
        if (workflowStartStateName == null || workflowStartStateName.trim().isEmpty()) {
            throw new IllegalArgumentException("workflow does not define a starting state");
        }

        List<StateHandler<?, ?, ?>> stateHandlers = new ArrayList<>();
        for (State state : workflow.getStates()) {
            StateHandler<?, ?, ?> stateHandler = StateHandlerFactory.getStateHandler(state, workflow, factory, idGenerator);
            if (stateHandler == null) {
                logger.warn("Unsupported state {}. Ignoring it", state.getName());
            } else {
                stateHandlers.add(stateHandler);
                stateHandler.handleStart(workflowStartStateName);
            }
        }

        for (StateHandler<?, ?, ?> stateHandler : stateHandlers) {
            stateHandler.handleEnd();
        }

        for (StateHandler<?, ?, ?> stateHandler : stateHandlers) {
            stateHandler.handleState();
            stateConnection.put(stateHandler.getState().getName(), new ConnectionInfo(stateHandler.getNode(),
                    stateHandler.getConnectionNode()));
        }

        for (StateHandler<?, ?, ?> stateHandler : stateHandlers) {
            stateHandler.handleTransition(stateConnection);
            stateHandler.done(stateConnection);
        }
        return factory.validate().getProcess();
    }

    public Process getProcess() {
        return process;
    }

    public static <T extends RuleFlowNodeContainerFactory<T, ?>> StartNodeFactory<T> messageStartNode(StartNodeFactory<T> nodeFactory,
            EventDefinition eventDefinition) {
        return nodeFactory
                .name(eventDefinition.getName())
                .metaData(Metadata.TRIGGER_MAPPING, DEFAULT_WORKFLOW_VAR)
                .metaData(Metadata.TRIGGER_TYPE, "ConsumeMessage")
                .metaData(Metadata.TRIGGER_REF, eventDefinition.getSource())
                .metaData(Metadata.MESSAGE_TYPE, JSON_NODE)
                .trigger(JSON_NODE, DEFAULT_WORKFLOW_VAR);
    }

    public static <T extends RuleFlowNodeContainerFactory<T, ?>> SubProcessNodeFactory<T> subprocessNode(SubProcessNodeFactory<T> nodeFactory) {
        Map<String, String> types = Collections.singletonMap(DEFAULT_WORKFLOW_VAR, JSON_NODE);
        VariableScope variableScope = new VariableScope();
        return nodeFactory
                .independent(true)
                .metaData("BPMN.InputTypes", types)
                .metaData("BPMN.OutputTypes", types)
                .inMapping(DEFAULT_WORKFLOW_VAR, DEFAULT_WORKFLOW_VAR)
                .outMapping(DEFAULT_WORKFLOW_VAR, DEFAULT_WORKFLOW_VAR)
                .context(variableScope)
                .defaultContext(variableScope);
    }

    public static <T extends NodeFactory<T, P>, P extends RuleFlowNodeContainerFactory<P, ?>> NodeFactory<T, P> sendEventNode(NodeFactory<T, P> actionNode,
            EventDefinition eventDefinition) {
        return actionNode
                .name(eventDefinition.getName())
                .metaData(Metadata.TRIGGER_TYPE, "ProduceMessage")
                .metaData(Metadata.MAPPING_VARIABLE, DEFAULT_WORKFLOW_VAR)
                .metaData(Metadata.TRIGGER_REF, eventDefinition.getSource())
                .metaData(Metadata.MESSAGE_TYPE, JSON_NODE);
    }

    public static <T extends RuleFlowNodeContainerFactory<T, ?>> EventNodeFactory<T> consumeEventNode(EventNodeFactory<T> eventNode,
            EventDefinition eventDefinition) {
        return eventNode
                .name(eventDefinition.getName())
                .variableName(DEFAULT_WORKFLOW_VAR)
                .metaData(Metadata.EVENT_TYPE, "message")
                .metaData(Metadata.TRIGGER_REF, eventDefinition.getSource())
                .metaData(Metadata.MESSAGE_TYPE, JSON_NODE)
                .metaData(Metadata.TRIGGER_TYPE, "ConsumeMessage")
                .eventType("Message-" + eventDefinition.getSource());
    }

}
