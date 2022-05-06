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
package org.kogito.workitem.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jbpm.process.core.Process;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;
import org.kogito.workitem.rest.auth.ApiKeyAuthDecorator;
import org.kogito.workitem.rest.auth.AuthDecorator;
import org.kogito.workitem.rest.auth.BasicAuthDecorator;
import org.kogito.workitem.rest.auth.BearerTokenAuthDecorator;
import org.kogito.workitem.rest.bodybuilders.DefaultWorkItemHandlerBodyBuilder;
import org.kogito.workitem.rest.bodybuilders.RestWorkItemHandlerBodyBuilder;
import org.kogito.workitem.rest.decorators.ParamsDecorator;
import org.kogito.workitem.rest.decorators.PrefixParamsDecorator;
import org.kogito.workitem.rest.decorators.RequestDecorator;
import org.kogito.workitem.rest.pathresolvers.DefaultPathParamResolver;
import org.kogito.workitem.rest.pathresolvers.PathParamResolver;
import org.kogito.workitem.rest.resulthandlers.DefaultRestWorkItemHandlerResult;
import org.kogito.workitem.rest.resulthandlers.RestWorkItemHandlerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;

import static org.kogito.workitem.rest.RestWorkItemHandlerUtils.getClassListParam;
import static org.kogito.workitem.rest.RestWorkItemHandlerUtils.getClassParam;
import static org.kogito.workitem.rest.RestWorkItemHandlerUtils.getParam;

public class RestWorkItemHandler implements KogitoWorkItemHandler {

    public static final String REST_TASK_TYPE = "Rest";
    public static final String URL = "Url";
    public static final String METHOD = "Method";
    public static final String CONTENT_DATA = "ContentData";
    public static final String RESULT = "Result";
    public static final String USER = "Username";
    public static final String PASSWORD = "Password";
    public static final String HOST = "Host";
    public static final String PORT = "Port";
    public static final String RESULT_HANDLER = "ResultHandler";
    public static final String BODY_BUILDER = "BodyBuilder";
    public static final String PARAMS_DECORATOR = "ParamsDecorator";
    public static final String PATH_PARAM_RESOLVER = "PathParamResolver";
    public static final String AUTH_METHOD = "AuthMethod";

    private static final Logger logger = LoggerFactory.getLogger(RestWorkItemHandler.class);
    private static final RestWorkItemHandlerResult DEFAULT_RESULT_HANDLER = new DefaultRestWorkItemHandlerResult();
    private static final RestWorkItemHandlerBodyBuilder DEFAULT_BODY_BUILDER = new DefaultWorkItemHandlerBodyBuilder();
    private static final ParamsDecorator DEFAULT_PARAMS_DECORATOR = new PrefixParamsDecorator();
    private static final PathParamResolver DEFAULT_PATH_PARAM_RESOLVER = new DefaultPathParamResolver();
    private static final Map<String, RestWorkItemHandlerResult> resultHandlers = new ConcurrentHashMap<>();
    private static final Map<String, RestWorkItemHandlerBodyBuilder> bodyBuilders = new ConcurrentHashMap<>();
    private static final Map<String, ParamsDecorator> paramsDecorators = new ConcurrentHashMap<>();
    private static final Map<String, PathParamResolver> pathParamsResolvers = new ConcurrentHashMap<>();
    private static final Map<String, AuthDecorator> authDecoratorsMap = new ConcurrentHashMap<>();
    private static final Collection<AuthDecorator> DEFAULT_AUTH_DECORATORS = Arrays.asList(new ApiKeyAuthDecorator(), new BasicAuthDecorator(), new BearerTokenAuthDecorator());

    private WebClient client;
    private Collection<RequestDecorator> requestDecorators;

    public RestWorkItemHandler(WebClient client) {
        this.client = client;
        this.requestDecorators = StreamSupport.stream(ServiceLoader.load(RequestDecorator.class).spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        Class<?> targetInfo = getTargetInfo(workItem);
        logger.debug("Using target {}", targetInfo);
        //retrieving parameters
        Map<String, Object> parameters = new HashMap<>(workItem.getParameters());
        //removing unnecessary parameter
        parameters.remove("TaskName");
        String endPoint = getParam(parameters, URL, String.class, null);
        if (endPoint == null) {
            throw new IllegalArgumentException("Missing required parameter " + URL);
        }
        HttpMethod method = getParam(parameters, METHOD, HttpMethod.class, HttpMethod.GET);
        String hostProp = getParam(parameters, HOST, String.class, "localhost");
        int portProp = getParam(parameters, PORT, Integer.class, 80);
        RestWorkItemHandlerResult resultHandler = getClassParam(parameters, RESULT_HANDLER, RestWorkItemHandlerResult.class, DEFAULT_RESULT_HANDLER, resultHandlers);
        RestWorkItemHandlerBodyBuilder bodyBuilder = getClassParam(parameters, BODY_BUILDER, RestWorkItemHandlerBodyBuilder.class, DEFAULT_BODY_BUILDER, bodyBuilders);
        ParamsDecorator paramsDecorator = getClassParam(parameters, PARAMS_DECORATOR, ParamsDecorator.class, DEFAULT_PARAMS_DECORATOR, paramsDecorators);
        PathParamResolver pathParamResolver = getClassParam(parameters, PATH_PARAM_RESOLVER, PathParamResolver.class, DEFAULT_PATH_PARAM_RESOLVER, pathParamsResolvers);
        Collection<? extends AuthDecorator> authDecorators = getClassListParam(parameters, AUTH_METHOD, AuthDecorator.class, DEFAULT_AUTH_DECORATORS, authDecoratorsMap);

        logger.debug("Filtered parameters are {}", parameters);
        // create request
        endPoint = pathParamResolver.apply(endPoint, parameters);
        Optional<URL> url = getUrl(endPoint);
        String host = url.map(java.net.URL::getHost).orElse(hostProp);
        if (host == null) {
            host = hostProp;
        }
        int port = url.map(java.net.URL::getPort).orElse(portProp);
        if (port < 0) {
            port = portProp;
        }
        String path = url.map(java.net.URL::getPath).orElse(endPoint).replace(" ", "%20");//fix issue with spaces in the path
        HttpRequest<Buffer> request = client.request(method, port, host, path);
        requestDecorators.forEach(d -> d.decorate(workItem, parameters, request));
        authDecorators.forEach(d -> d.decorate(workItem, parameters, request));
        paramsDecorator.decorate(workItem, parameters, request);
        HttpResponse<Buffer> response = method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT) ? request.sendJsonAndAwait(bodyBuilder.apply(parameters)) : request.sendAndAwait();
        manager.completeWorkItem(workItem.getStringId(), Collections.singletonMap(RESULT, resultHandler.apply(response, targetInfo)));
    }

    private Optional<URL> getUrl(String endPoint) {
        return Optional.ofNullable(endPoint)
                .map(spec -> {
                    try {
                        return new URL(spec);
                    } catch (MalformedURLException e) {
                        return null;
                    }
                });
    }

    private Class<?> getTargetInfo(KogitoWorkItem workItem) {
        String varName = ((WorkItemNode) ((WorkItemNodeInstance) workItem.getNodeInstance()).getNode()).getIoSpecification().getOutputMappingBySources().get(RESULT);
        if (varName != null) {
            return getType(workItem.getProcessInstance(), varName);
        }
        logger.warn("no out mapping for {}", RESULT);
        return null;
    }

    private Class<?> getType(KogitoProcessInstance pi, String varName) {
        VariableScope variableScope = (VariableScope) ((Process) pi.getProcess()).getDefaultContext(
                VariableScope.VARIABLE_SCOPE);
        Variable variable = variableScope.findVariable(varName);
        if (variable != null) {
            return variable.getType().getObjectClass();
        } else {
            logger.warn("Cannot find definition for variable {}", varName);
            return null;
        }
    }

    @Override
    public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        // rest item handler does not support abort
    }

}
