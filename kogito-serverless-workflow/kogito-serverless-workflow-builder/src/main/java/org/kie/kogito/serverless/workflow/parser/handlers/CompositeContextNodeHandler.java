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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.drools.mvel.java.JavaDialect;
import org.jbpm.compiler.canonical.descriptors.TaskDescriptor;
import org.jbpm.process.core.datatype.DataTypeResolver;
import org.jbpm.ruleflow.core.RuleFlowNodeContainerFactory;
import org.jbpm.ruleflow.core.factory.AbstractCompositeNodeFactory;
import org.jbpm.ruleflow.core.factory.CompositeContextNodeFactory;
import org.jbpm.ruleflow.core.factory.NodeFactory;
import org.jbpm.ruleflow.core.factory.WorkItemNodeFactory;
import org.kie.kogito.codegen.api.SourceFileCodegenBindEvent;
import org.kie.kogito.jackson.utils.JsonNodeVisitor;
import org.kie.kogito.jackson.utils.JsonObjectUtils;
import org.kie.kogito.process.expr.ExpressionHandlerFactory;
import org.kie.kogito.serverless.workflow.SWFConstants;
import org.kie.kogito.serverless.workflow.operationid.WorkflowOperationId;
import org.kie.kogito.serverless.workflow.parser.ParserContext;
import org.kie.kogito.serverless.workflow.parser.ServerlessWorkflowParser;
import org.kie.kogito.serverless.workflow.parser.rest.RestOperationHandlerFactory;
import org.kie.kogito.serverless.workflow.rpc.RPCWorkItemHandler;
import org.kie.kogito.serverless.workflow.suppliers.ExpressionActionSupplier;
import org.kie.kogito.serverless.workflow.suppliers.ExpressionParametersFactorySupplier;
import org.kie.kogito.serverless.workflow.suppliers.ObjectResolverSupplier;
import org.kie.kogito.serverless.workflow.suppliers.ParamsRestBodyBuilderSupplier;
import org.kie.kogito.serverless.workflow.suppliers.SysoutActionSupplier;
import org.kie.kogito.serverless.workflow.utils.ExpressionHandlerUtils;
import org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils;
import org.kogito.workitem.rest.RestWorkItemHandler;
import org.kogito.workitem.rest.auth.ApiKeyAuthDecorator;
import org.kogito.workitem.rest.auth.BearerTokenAuthDecorator;

import com.fasterxml.jackson.databind.JsonNode;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.actions.Action;
import io.serverlessworkflow.api.events.EventRef;
import io.serverlessworkflow.api.filters.ActionDataFilter;
import io.serverlessworkflow.api.functions.FunctionDefinition;
import io.serverlessworkflow.api.functions.FunctionRef;
import io.serverlessworkflow.api.functions.SubFlowRef;
import io.serverlessworkflow.api.interfaces.State;

import static org.kie.kogito.serverless.workflow.parser.handlers.NodeFactoryUtils.fillRest;
import static org.kie.kogito.serverless.workflow.parser.handlers.NodeFactoryUtils.subprocessNode;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.ACCESS_TOKEN;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.API_KEY;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.API_KEY_PREFIX;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.PASSWORD_PROP;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.USER_PROP;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.resolveFunctionMetadata;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.runtimeRestApi;

public abstract class CompositeContextNodeHandler<S extends State> extends StateHandler<S> {

    private static final String SCRIPT_TYPE_PARAM = "script";
    private static final String SYSOUT_TYPE_PARAM = "message";
    private static final String SERVICE_TASK_TYPE = "Service Task";
    private static final String WORKITEM_INTERFACE = "Interface";
    private static final String WORKITEM_OPERATION = "Operation";
    private static final String WORKITEM_INTERFACE_IMPL = "interfaceImplementationRef";
    private static final String WORKITEM_OPERATION_IMPL = "operationImplementationRef";
    private static final String WORKITEM_PARAM_TYPE = "ParameterType";
    private static final String WORKITEM_PARAM = "Parameter";
    private static final String SERVICE_INTERFACE_KEY = "interface";
    private static final String SERVICE_OPERATION_KEY = "operation";
    private static final String SERVICE_IMPL_KEY = "implementation";
    private static final String LANG_SEPARATOR = ":";
    private static final String METHOD_SEPARATOR = ":";
    private static final String INTFC_SEPARATOR = "::";

    protected CompositeContextNodeHandler(S state, Workflow workflow, ParserContext parserContext) {
        super(state, workflow, parserContext);
    }

    protected final CompositeContextNodeFactory<?> makeCompositeNode(RuleFlowNodeContainerFactory<?, ?> factory) {
        return factory.compositeContextNode(parserContext.newId()).name(state.getName()).autoComplete(true);
    }

    protected final <T extends AbstractCompositeNodeFactory<?, ?>> T handleActions(T embeddedSubProcess, List<Action> actions) {
        return handleActions(embeddedSubProcess, actions, null);
    }

    protected final <T extends AbstractCompositeNodeFactory<?, ?>> T handleActions(T embeddedSubProcess, List<Action> actions, String outputVar, String... extraVariables) {
        if (actions != null && !actions.isEmpty()) {
            NodeFactory<?, ?> startNode = embeddedSubProcess.startNode(parserContext.newId()).name("EmbeddedStart");
            NodeFactory<?, ?> currentNode = startNode;
            for (Action action : actions) {
                currentNode = connect(currentNode, getActionNode(embeddedSubProcess, action, outputVar, extraVariables));
            }
            connect(currentNode, embeddedSubProcess.endNode(parserContext.newId()).name("EmbeddedEnd").terminate(true)).done();
        } else {
            connect(embeddedSubProcess.startNode(parserContext.newId()).name("EmbeddedStart"), embeddedSubProcess.endNode(parserContext.newId()).name("EmbeddedEnd").terminate(true)).done();
        }
        handleErrors(parserContext.factory(), embeddedSubProcess);
        return embeddedSubProcess;
    }

    protected final MakeNodeResult getActionNode(RuleFlowNodeContainerFactory<?, ?> embeddedSubProcess,
            Action action) {
        return getActionNode(embeddedSubProcess, action, null);
    }

    public MakeNodeResult getActionNode(RuleFlowNodeContainerFactory<?, ?> embeddedSubProcess,
            Action action, String collectVar, String... extraVariables) {
        ActionDataFilter actionFilter = action.getActionDataFilter();
        String fromExpr = null;
        String resultExpr = null;
        String toExpr = null;
        boolean useData = true;
        if (actionFilter != null) {
            fromExpr = actionFilter.getFromStateData();
            resultExpr = actionFilter.getResults();
            toExpr = actionFilter.getToStateData();
            useData = actionFilter.isUseResults();
        }
        if (action.getFunctionRef() != null) {
            return filterAndMergeNode(embeddedSubProcess, fromExpr, resultExpr, toExpr, useData,
                    (factory, inputVar, outputVar) -> getActionNode(factory, action.getFunctionRef(), inputVar, outputVar, collectVar, extraVariables));
        } else if (action.getEventRef() != null) {
            return filterAndMergeNode(embeddedSubProcess, fromExpr, resultExpr, toExpr, useData,
                    (factory, inputVar, outputVar) -> getActionNode(factory, action.getEventRef(), inputVar));
        } else if (action.getSubFlowRef() != null) {
            return filterAndMergeNode(embeddedSubProcess, fromExpr, resultExpr, toExpr, useData,
                    (factory, inputVar, outputVar) -> getActionNode(factory, action.getSubFlowRef(), inputVar, outputVar));
        } else {
            throw new IllegalArgumentException("Action node " + action.getName() + " of state " + state.getName() + " does not have function or event defined");
        }
    }

    private NodeFactory<?, ?> getActionNode(RuleFlowNodeContainerFactory<?, ?> factory,
            SubFlowRef subFlowRef,
            String inputVar,
            String outputVar) {
        return subprocessNode(
                factory.subProcessNode(parserContext.newId()).name(subFlowRef.getWorkflowId()).processId(subFlowRef.getWorkflowId()).waitForCompletion(true),
                inputVar,
                outputVar);
    }

    private NodeFactory<?, ?> getActionNode(RuleFlowNodeContainerFactory<?, ?> embeddedSubProcess,
            EventRef eventRef, String inputVar) {
        return sendEventNode(embeddedSubProcess.actionNode(parserContext.newId()), eventDefinition(eventRef.getTriggerEventRef()), eventRef.getData(), inputVar);
    }

    private NodeFactory<?, ?> getActionNode(RuleFlowNodeContainerFactory<?, ?> embeddedSubProcess,
            FunctionRef functionRef, String inputVar, String outputVar, String collectVar, String... extraVariables) {
        String actionName = functionRef.getRefName();
        FunctionDefinition actionFunction = workflow.getFunctions().getFunctionDefs()
                .stream()
                .filter(wf -> wf.getName().equals(actionName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("cannot find function " + actionName));

        ActionType actionType = ActionType.from(actionFunction);
        switch (actionType) {
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
                        .action(ExpressionActionSupplier.of(workflow, actionType.getOperation(actionFunction))
                                .withVarNames(inputVar, outputVar).withCollectVar(collectVar)
                                .withAddInputVars(extraVariables).build());
            case SYSOUT:
                return embeddedSubProcess
                        .actionNode(parserContext.newId())
                        .name(actionName)
                        .action(new SysoutActionSupplier(workflow.getExpressionLang(), functionRef.getArguments().get(SYSOUT_TYPE_PARAM).asText(), inputVar, extraVariables));
            case SERVICE:
                return addServiceParameters(embeddedSubProcess
                        .workItemNode(parserContext.newId())
                        .name(actionName)
                        .metaData(TaskDescriptor.KEY_WORKITEM_TYPE, SERVICE_TASK_TYPE)
                        .workName(SERVICE_TASK_TYPE)
                        .inMapping(inputVar, WORKITEM_PARAM)
                        .outMapping(WORKITEM_PARAM, outputVar), actionFunction, actionType.getOperation(actionFunction), functionRef.getArguments());
            case REST:
                return addFunctionArgs(addRestParameters(buildWorkItem(embeddedSubProcess, actionFunction, inputVar, outputVar), actionFunction, actionType.getOperation(actionFunction)), functionRef);
            case RPC:
                return addFunctionArgs(addRPCParameters(buildWorkItem(embeddedSubProcess, actionFunction, inputVar, outputVar), actionFunction), functionRef);
            case OPENAPI:
                WorkflowOperationId operationId = parserContext.operationIdFactory().from(workflow, actionFunction, Optional.of(parserContext));
                notifySourceFileCodegenBindListeners(operationId.getUri().toString());
                return addFunctionArgs(RestOperationHandlerFactory.get(parserContext, operationId).fillWorkItemHandler(buildWorkItem(embeddedSubProcess, actionFunction, inputVar, outputVar), workflow,
                        actionFunction), functionRef);
            default:
                return emptyNode(embeddedSubProcess, actionName);
        }
    }

    private WorkItemNodeFactory<?> buildWorkItem(RuleFlowNodeContainerFactory<?, ?> embeddedSubProcess,
            FunctionDefinition actionFunction,
            String inputVar,
            String outputVar) {
        return embeddedSubProcess.workItemNode(parserContext.newId())
                .inMapping(inputVar, SWFConstants.MODEL_WORKFLOW_VAR)
                .outMapping(RestWorkItemHandler.RESULT, outputVar).name(actionFunction.getName());
    }

    private void notifySourceFileCodegenBindListeners(String uri) {
        parserContext.getContext()
                .getSourceFileCodegenBindNotifier()
                .ifPresent(notifier -> notifier.notify(new SourceFileCodegenBindEvent(workflow.getId(), uri)));
    }

    private <T extends RuleFlowNodeContainerFactory<T, ?>> WorkItemNodeFactory<T> addFunctionArgs(WorkItemNodeFactory<T> node, FunctionRef functionRef) {
        JsonNode functionArgs = functionRef.getArguments();
        if (functionArgs != null) {
            processArgs(node, functionArgs, SWFConstants.MODEL_WORKFLOW_VAR);
        }
        return node;
    }

    private <T extends RuleFlowNodeContainerFactory<T, ?>> WorkItemNodeFactory<T> addServiceParameters(WorkItemNodeFactory<T> node,
            FunctionDefinition actionFunction,
            String operation, JsonNode functionArgs) {
        String intfc = null;
        String method = null;
        String lang = null;
        // try extracting from operation (format language:interface::method)
        if (operation != null) {
            int indexOf = operation.indexOf(INTFC_SEPARATOR);
            if (indexOf != -1) {
                method = operation.substring(indexOf + INTFC_SEPARATOR.length());
                operation = operation.substring(0, indexOf);
                indexOf = operation.indexOf(LANG_SEPARATOR);
                if (indexOf != -1) {
                    intfc = operation.substring(indexOf + LANG_SEPARATOR.length());
                    lang = operation.substring(0, indexOf);
                } else {
                    intfc = operation;
                }
            }
        }
        if (lang == null) {
            lang = resolveFunctionMetadata(
                    actionFunction, SERVICE_IMPL_KEY, parserContext.getContext(), String.class, "Java");
        }
        // fallback to metadata for backward compatibility
        if (intfc == null) {
            intfc = resolveFunctionMetadata(
                    actionFunction, SERVICE_INTERFACE_KEY, parserContext.getContext());
        }
        if (method == null) {
            method = resolveFunctionMetadata(
                    actionFunction, SERVICE_OPERATION_KEY, parserContext.getContext());
        }

        if (functionArgs == null || functionArgs.isEmpty()) {
            node.workParameter(WORKITEM_PARAM_TYPE, ServerlessWorkflowParser.JSON_NODE);
        } else {
            processArgs(node, functionArgs, WORKITEM_PARAM);
        }

        return node.workParameter(WORKITEM_INTERFACE, intfc)
                .workParameter(WORKITEM_OPERATION, method)
                .workParameter(WORKITEM_INTERFACE_IMPL, intfc)
                .workParameter(WORKITEM_OPERATION_IMPL, method)
                .workParameter(SERVICE_IMPL_KEY, lang);
    }

    private <T extends RuleFlowNodeContainerFactory<T, ?>> WorkItemNodeFactory<T> addRestParameters(WorkItemNodeFactory<T> node,
            FunctionDefinition actionFunction,
            String operation) {
        String url = null;
        String method = null;
        // try extracting from operation (format method:url)
        if (operation != null) {
            int indexOf = operation.indexOf(METHOD_SEPARATOR);
            if (indexOf != -1) {
                method = operation.substring(0, indexOf);
                url = operation.substring(indexOf + METHOD_SEPARATOR.length());
            } else {
                url = operation;
            }
        }
        if (method == null) {
            method = resolveFunctionMetadata(actionFunction, "method", parserContext.getContext());
        }

        return fillRest(node.workParameter(RestWorkItemHandler.URL, url)
                .workParameter(RestWorkItemHandler.METHOD, method)
                .workParameter(RestWorkItemHandler.USER, runtimeRestApi(actionFunction, USER_PROP, parserContext.getContext()))
                .workParameter(RestWorkItemHandler.PASSWORD, runtimeRestApi(actionFunction, PASSWORD_PROP, parserContext.getContext()))
                .workParameter(RestWorkItemHandler.HOST, runtimeRestApi(actionFunction, "host", parserContext.getContext()))
                .workParameter(RestWorkItemHandler.PORT, runtimeRestApi(actionFunction, "port", parserContext.getContext(), Integer.class, 8080))
                .workParameter(RestWorkItemHandler.BODY_BUILDER, new ParamsRestBodyBuilderSupplier())
                .workParameter(BearerTokenAuthDecorator.BEARER_TOKEN, runtimeRestApi(actionFunction, ACCESS_TOKEN, parserContext.getContext()))
                .workParameter(ApiKeyAuthDecorator.KEY_PREFIX, runtimeRestApi(actionFunction, API_KEY_PREFIX, parserContext.getContext()))
                .workParameter(ApiKeyAuthDecorator.KEY, runtimeRestApi(actionFunction, API_KEY, parserContext.getContext())));
    }

    private <T extends RuleFlowNodeContainerFactory<T, ?>> WorkItemNodeFactory<T> addRPCParameters(WorkItemNodeFactory<T> node, FunctionDefinition function) {
        WorkflowOperationId operationId = parserContext.operationIdFactory().from(workflow, function, Optional.of(parserContext));
        return node.workName(ServerlessWorkflowUtils.getRPCClassName(operationId.getService()))
                .metaData(RPCWorkItemHandler.FILE_PROP, operationId.getFileName())
                .metaData(RPCWorkItemHandler.SERVICE_PROP, operationId.getService())
                .metaData(RPCWorkItemHandler.METHOD_PROP, operationId.getOperation());
    }

    private Map<String, Object> functionsToMap(JsonNode jsonNode) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (jsonNode != null) {
            Iterator<Entry<String, JsonNode>> iter = jsonNode.fields();
            while (iter.hasNext()) {
                Entry<String, JsonNode> entry = iter.next();
                map.put(entry.getKey(), functionReference(JsonObjectUtils.simpleToJavaValue(entry.getValue())));
            }
        }
        return map;
    }

    private Object functionReference(Object object) {
        if (object instanceof JsonNode) {
            return JsonNodeVisitor.transformTextNode((JsonNode) object, node -> JsonObjectUtils.fromValue(ExpressionHandlerUtils.replaceExpr(workflow, node.asText())));
        } else if (object instanceof CharSequence) {
            return ExpressionHandlerUtils.replaceExpr(workflow, object.toString());
        } else {
            return object;
        }
    }

    private void processArgs(WorkItemNodeFactory<?> workItemFactory,
            JsonNode functionArgs, String paramName) {
        if (functionArgs.isObject()) {
            functionsToMap(functionArgs).entrySet().forEach(entry -> processArg(entry.getKey(), entry.getValue(), workItemFactory, paramName));
        } else {
            Object object = functionReference(JsonObjectUtils.simpleToJavaValue(functionArgs));
            if (isExpression(object)) {
                workItemFactory.workParameterFactory(new ExpressionParametersFactorySupplier(workflow.getExpressionLang(), object, paramName));
            } else {
                workItemFactory.workParameter(RestWorkItemHandler.CONTENT_DATA, object);
            }
        }
    }

    private void processArg(String key, Object value, WorkItemNodeFactory<?> workItemFactory, String paramName) {
        boolean isExpr = isExpression(value);
        workItemFactory
                .workParameter(key,
                        isExpr ? new ObjectResolverSupplier(workflow.getExpressionLang(), value, paramName) : value)
                .workParameterDefinition(key,
                        DataTypeResolver.fromObject(value, isExpr));
    }

    private boolean isExpression(Object value) {
        return value instanceof CharSequence && ExpressionHandlerFactory.get(workflow.getExpressionLang(), value.toString()).isValid() || value instanceof JsonNode;
    }

    private NodeFactory<?, ?> emptyNode(RuleFlowNodeContainerFactory<?, ?> embeddedSubProcess, String actionName) {
        return embeddedSubProcess
                .actionNode(parserContext.newId())
                .name(actionName)
                .action(JavaDialect.ID, "");
    }
}
