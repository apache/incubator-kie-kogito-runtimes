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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.drools.mvel.java.JavaDialect;
import org.jbpm.compiler.canonical.descriptors.AbstractServiceTaskDescriptor;
import org.jbpm.compiler.canonical.descriptors.OpenApiTaskDescriptor;
import org.jbpm.compiler.canonical.descriptors.TaskDescriptor;
import org.jbpm.process.core.datatype.DataTypeResolver;
import org.jbpm.ruleflow.core.RuleFlowNodeContainerFactory;
import org.jbpm.ruleflow.core.factory.AbstractCompositeNodeFactory;
import org.jbpm.ruleflow.core.factory.NodeFactory;
import org.jbpm.ruleflow.core.factory.WorkItemNodeFactory;
import org.kie.kogito.process.workitems.impl.expr.ExpressionHandler;
import org.kie.kogito.process.workitems.impl.expr.ExpressionHandlerFactory;
import org.kie.kogito.process.workitems.impl.expr.ExpressionWorkItemResolver;
import org.kie.kogito.serverless.workflow.JsonNodeResolver;
import org.kie.kogito.serverless.workflow.ObjectResolver;
import org.kie.kogito.serverless.workflow.parser.ParserContext;
import org.kie.kogito.serverless.workflow.parser.ServerlessWorkflowParser;
import org.kie.kogito.serverless.workflow.parser.util.ServerlessWorkflowUtils;
import org.kie.kogito.serverless.workflow.parser.util.WorkflowAppContext;
import org.kie.kogito.serverless.workflow.suppliers.ExpressionActionSupplier;
import org.kie.kogito.serverless.workflow.suppliers.RestBodyBuilderSupplier;
import org.kie.kogito.serverless.workflow.suppliers.SysoutActionSupplier;
import org.kogito.workitem.openapi.JsonNodeResultHandler;
import org.kogito.workitem.openapi.suppliers.JsonNodeResultHandlerExprSupplier;
import org.kogito.workitem.rest.RestWorkItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.actions.Action;
import io.serverlessworkflow.api.functions.FunctionDefinition;
import io.serverlessworkflow.api.functions.FunctionDefinition.Type;
import io.serverlessworkflow.api.functions.FunctionRef;
import io.serverlessworkflow.api.interfaces.State;

public abstract class CompositeContextNodeHandler<S extends State, P extends RuleFlowNodeContainerFactory<P, ?>, N extends AbstractCompositeNodeFactory<N, P>> extends
        StateHandler<S, N, P> {

    private static final Logger logger = LoggerFactory.getLogger(CompositeContextNodeHandler.class);

    private static final String SCRIPT_TYPE = "script";
    private static final String REST_TYPE = "rest";
    private static final String SCRIPT_TYPE_PARAM = "script";
    private static final String SYSOUT_TYPE = "sysout";
    private static final String SYSOUT_TYPE_PARAM = "message";
    private static final String SERVICE_TYPE = "service";
    private static final String SERVICE_TASK_TYPE = "Service Task";
    private static final String WORKITEM_INTERFACE = "Interface";
    private static final String WORKITEM_OPERATION = "Operation";
    private static final String WORKITEM_INTERFACE_IMPL = "interfaceImplementationRef";
    private static final String WORKITEM_OPERATION_IMPL = "operationImplementationRef";
    private static final String WORKITEM_PARAM_TYPE = "ParameterType";
    private static final String WORKITEM_PARAM = "Parameter";
    private static final String WORKITEM_RESULT = "Result";
    private static final String SERVICE_INTERFACE_KEY = "interface";
    private static final String SERVICE_OPERATION_KEY = "operation";
    private static final String SERVICE_IMPL_KEY = "implementation";

    private final WorkflowAppContext workflowAppContext = WorkflowAppContext.ofAppResources();

    protected CompositeContextNodeHandler(S state, Workflow workflow, RuleFlowNodeContainerFactory<P, ?> factory,
            ParserContext parserContext) {
        super(state, workflow, factory, parserContext);
    }

    protected final N handleActions(List<Action> actions) {
        N embeddedSubProcess = (N) factory.compositeContextNode(parserContext.newId()).name(state.getName()).autoComplete(true);

        if (actions != null && !actions.isEmpty()) {
            NodeFactory<?, ?> startNode = embeddedSubProcess.startNode(parserContext.newId()).name("EmbeddedStart");
            NodeFactory<?, ?> currentNode = startNode;

            for (Action action : actions) {
                currentNode = getActionNode(embeddedSubProcess, action);
                embeddedSubProcess.connection(startNode.getNode().getId(), currentNode.getNode().getId());
                startNode = currentNode;
            }
            long endId = parserContext.newId();
            embeddedSubProcess.endNode(endId).name("EmbeddedEnd").terminate(true).done().connection(currentNode
                    .getNode().getId(), endId);
        } else {
            long startId = parserContext.newId();
            long endId = parserContext.newId();
            embeddedSubProcess.startNode(startId).name("EmbeddedStart").done().endNode(endId).name("EmbeddedEnd")
                    .terminate(true).done().connection(startId, endId);
        }
        return embeddedSubProcess;
    }

    protected final NodeFactory<?, ?> getActionNode(N embeddedSubProcess,
            Action action) {
        FunctionRef functionRef = action.getFunctionRef();
        JsonNode functionArgs = functionRef.getArguments();
        String actionName = functionRef.getRefName();
        if (workflow.getFunctions() == null) {
            throw new IllegalArgumentException("cannot find function " + actionName + " because funtions are not defined");
        }
        FunctionDefinition actionFunction = workflow.getFunctions().getFunctionDefs()
                .stream()
                .filter(wf -> wf.getName().equals(actionName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("cannot find function " + actionName));

        switch (getActionType(actionFunction)) {
            case SCRIPT:
                return embeddedSubProcess
                        .actionNode(parserContext.newId())
                        .name(actionName)
                        .action(JavaDialect.ID,
                                functionRef
                                        .getArguments().get(SCRIPT_TYPE_PARAM).asText());
            case EXPRESSION:
                return embeddedSubProcess
                        .actionNode(parserContext.newId())
                        .name(actionName)
                        .action(new ExpressionActionSupplier(workflow.getExpressionLang(), actionFunction.getOperation()));
            case SYSOUT:
                return embeddedSubProcess
                        .actionNode(parserContext.newId())
                        .name(actionName)
                        .action(new SysoutActionSupplier(workflow.getExpressionLang(), functionRef.getArguments().get(SYSOUT_TYPE_PARAM).asText()));
            case SERVICE:
                WorkItemNodeFactory<N> serviceFactory = embeddedSubProcess
                        .workItemNode(parserContext.newId())
                        .name(actionName)
                        .metaData(TaskDescriptor.KEY_WORKITEM_TYPE, SERVICE_TASK_TYPE)
                        .workName(SERVICE_TASK_TYPE)
                        .workParameter(WORKITEM_INTERFACE, ServerlessWorkflowUtils.resolveFunctionMetadata(
                                actionFunction, SERVICE_INTERFACE_KEY, workflowAppContext))
                        .workParameter(WORKITEM_OPERATION, ServerlessWorkflowUtils.resolveFunctionMetadata(
                                actionFunction, SERVICE_OPERATION_KEY, workflowAppContext))
                        .workParameter(WORKITEM_INTERFACE_IMPL, ServerlessWorkflowUtils
                                .resolveFunctionMetadata(actionFunction, SERVICE_INTERFACE_KEY,
                                        workflowAppContext))
                        .workParameter(WORKITEM_OPERATION_IMPL, ServerlessWorkflowUtils
                                .resolveFunctionMetadata(actionFunction, SERVICE_OPERATION_KEY,
                                        workflowAppContext))
                        .workParameter(SERVICE_IMPL_KEY, ServerlessWorkflowUtils.resolveFunctionMetadata(
                                actionFunction, SERVICE_IMPL_KEY, workflowAppContext, "Java"))
                        .inMapping(WORKITEM_PARAM, ServerlessWorkflowParser.DEFAULT_WORKFLOW_VAR);

                if (functionArgs == null || functionArgs.isEmpty()) {
                    serviceFactory.workParameter(WORKITEM_PARAM_TYPE, ServerlessWorkflowParser.JSON_NODE)
                            .outMapping(WORKITEM_PARAM, ServerlessWorkflowParser.DEFAULT_WORKFLOW_VAR);
                } else {
                    processArgs(serviceFactory, functionArgs, WORKITEM_PARAM, ObjectResolver.class);
                }
                return serviceFactory;

            case REST:
                WorkItemNodeFactory<N> workItemFactory = embeddedSubProcess
                        .workItemNode(parserContext.newId())
                        .name(actionFunction.getName())
                        .metaData(TaskDescriptor.KEY_WORKITEM_TYPE, RestWorkItemHandler.REST_TASK_TYPE)
                        .workName(RestWorkItemHandler.REST_TASK_TYPE)
                        .workParameter(RestWorkItemHandler.URL, actionFunction.getOperation())
                        .workParameter(RestWorkItemHandler.METHOD, ServerlessWorkflowUtils
                                .resolveFunctionMetadata(actionFunction, "method",
                                        workflowAppContext))
                        .workParameter(RestWorkItemHandler.USER, ServerlessWorkflowUtils
                                .resolveFunctionMetadata(actionFunction, "user",
                                        workflowAppContext))
                        .workParameter(RestWorkItemHandler.PASSWORD, ServerlessWorkflowUtils
                                .resolveFunctionMetadata(actionFunction, "password",
                                        workflowAppContext))
                        .workParameter(RestWorkItemHandler.HOST, ServerlessWorkflowUtils
                                .resolveFunctionMetadata(actionFunction, "host",
                                        workflowAppContext))
                        .workParameter(RestWorkItemHandler.PORT, ServerlessWorkflowUtils
                                .resolveFunctionMetadataAsInt(actionFunction, "port",
                                        workflowAppContext))
                        .workParameter(RestWorkItemHandler.BODY_BUILDER, new RestBodyBuilderSupplier())
                        .inMapping(RestWorkItemHandler.CONTENT_DATA,
                                ServerlessWorkflowParser.DEFAULT_WORKFLOW_VAR)
                        .outMapping(RestWorkItemHandler.RESULT,
                                ServerlessWorkflowParser.DEFAULT_WORKFLOW_VAR);
                if (functionArgs != null && !functionArgs.isEmpty()) {
                    processArgs(workItemFactory, functionArgs, RestWorkItemHandler.CONTENT_DATA, ObjectResolver.class);
                }
                return workItemFactory;
            case OPENAPI:

                return OpenApiTaskDescriptor.builderFor(ServerlessWorkflowUtils.getOpenApiURI(actionFunction),
                        ServerlessWorkflowUtils.getOpenApiOperationId(actionFunction))
                        .withExprLang(workflow.getExpressionLang())
                        .withModelParameter(WORKITEM_PARAM)
                        .withArgs(functionsToMap(functionArgs), JsonNodeResolver.class, JsonNode.class, s -> true)
                        .withResultHandler(new JsonNodeResultHandlerExprSupplier(), JsonNodeResultHandler.class)
                        .build(embeddedSubProcess.workItemNode(parserContext.newId())).name(functionRef.getRefName())
                        .inMapping(WORKITEM_PARAM,
                                ServerlessWorkflowParser.DEFAULT_WORKFLOW_VAR)
                        .outMapping(
                                WORKITEM_RESULT,
                                ServerlessWorkflowParser.DEFAULT_WORKFLOW_VAR);

            default:
                return emptyNode(embeddedSubProcess, actionName);
        }

    }

    private static Map<String, Object> functionsToMap(JsonNode jsonNode) {

        Map<String, Object> map = new HashMap<>();
        Iterator<Entry<String, JsonNode>> iter = jsonNode.fields();
        while (iter.hasNext()) {
            Entry<String, JsonNode> entry = iter.next();
            map.put(entry.getKey(), processValue(entry.getValue()));
        }
        return map;
    }

    private static Object processValue(JsonNode jsonNode) {
        if (jsonNode.isTextual()) {
            return jsonNode.asText();
        } else if (jsonNode.isBoolean()) {
            return jsonNode.asBoolean();
        } else if (jsonNode.isInt()) {
            return jsonNode.asInt();
        } else if (jsonNode.isDouble()) {
            return jsonNode.asDouble();
        } else {
            /* this code is here for backward compatibility, we probably need to throw exception directly here */
            logger.warn("Suspicious node {}, trying to convert to string", jsonNode);
            return new ObjectMapper().convertValue(jsonNode, String.class);
        }
    }

    private void processArgs(WorkItemNodeFactory<N> workItemFactory,
            JsonNode functionArgs, String paramName, Class<? extends ExpressionWorkItemResolver> clazz) {
        ExpressionHandler expressionHandler = ExpressionHandlerFactory.get(workflow.getExpressionLang());
        Map<String, Object> map = functionsToMap(functionArgs);
        map.entrySet().forEach(
                entry -> workItemFactory
                        .workParameter(entry.getKey(), AbstractServiceTaskDescriptor.processWorkItemValue(workflow.getExpressionLang(), entry.getValue(), paramName, clazz, expressionHandler::isExpr))
                        .workParameterDefinition(entry.getKey(),
                                DataTypeResolver.fromObject(entry.getValue(), expressionHandler::isExpr)));
    }

    private enum ActionType {
        REST,
        SERVICE,
        OPENAPI,
        EXPRESSION,
        SCRIPT,
        SYSOUT,
        EMPTY

    }

    private ActionType getActionType(FunctionDefinition actionFunction) {
        if (ServerlessWorkflowUtils.isOpenApiOperation(actionFunction)) {
            return ActionType.OPENAPI;
        } else if (actionFunction.getType() == Type.EXPRESSION) {
            return ActionType.EXPRESSION;
        } else {

            String type = actionFunction.getMetadata() != null ? actionFunction.getMetadata().get("type") : null;
            if (SERVICE_TYPE.equalsIgnoreCase(type)) {
                return ActionType.SERVICE;
            } else if (SCRIPT_TYPE.equalsIgnoreCase(type)) {
                return ActionType.SCRIPT;
            } else if (SYSOUT_TYPE.equalsIgnoreCase(type)) {
                return ActionType.SYSOUT;
            } else if (REST_TYPE.equalsIgnoreCase(type)) {
                return ActionType.REST;
            } else {
                return ActionType.EMPTY;
            }
        }
    }

    private NodeFactory<?, ?> emptyNode(N embeddedSubProcess, String actionName) {
        return embeddedSubProcess
                .actionNode(parserContext.newId())
                .name(actionName)
                .action(JavaDialect.ID, "");
    }

}
