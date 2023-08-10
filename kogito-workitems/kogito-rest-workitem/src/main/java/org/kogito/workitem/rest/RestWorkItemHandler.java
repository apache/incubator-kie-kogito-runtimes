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
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jbpm.process.core.ContextResolver;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;
import org.kie.kogito.process.workitem.WorkItemExecutionException;
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

import static org.kie.kogito.internal.utils.ConversionUtils.isEmpty;
import static org.kogito.workitem.rest.RestWorkItemHandlerUtils.getClassListParam;
import static org.kogito.workitem.rest.RestWorkItemHandlerUtils.getClassParam;
import static org.kogito.workitem.rest.RestWorkItemHandlerUtils.getParam;

public class RestWorkItemHandler implements KogitoWorkItemHandler {

    public static final String REST_TASK_TYPE = "Rest";
    public static final String PROTOCOL = "Protocol";
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

    public static final String REQUEST_TIMEOUT_IN_MILLIS = "RequestTimeout";

    public static final int DEFAULT_PORT = 80;
    public static final int DEFAULT_SSL_PORT = 443;

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

    protected final WebClient httpClient;
    protected final WebClient httpsClient;
    private Collection<RequestDecorator> requestDecorators;

    public RestWorkItemHandler(WebClient httpClient, WebClient httpsClient) {
        this.httpClient = httpClient;
        this.httpsClient = httpsClient;
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
        RestWorkItemHandlerResult resultHandler = getClassParam(parameters, RESULT_HANDLER, RestWorkItemHandlerResult.class, DEFAULT_RESULT_HANDLER, resultHandlers);
        RestWorkItemHandlerBodyBuilder bodyBuilder = getClassParam(parameters, BODY_BUILDER, RestWorkItemHandlerBodyBuilder.class, DEFAULT_BODY_BUILDER, bodyBuilders);
        ParamsDecorator paramsDecorator = getClassParam(parameters, PARAMS_DECORATOR, ParamsDecorator.class, DEFAULT_PARAMS_DECORATOR, paramsDecorators);
        PathParamResolver pathParamResolver = getClassParam(parameters, PATH_PARAM_RESOLVER, PathParamResolver.class, DEFAULT_PATH_PARAM_RESOLVER, pathParamsResolvers);
        Collection<? extends AuthDecorator> authDecorators = getClassListParam(parameters, AUTH_METHOD, AuthDecorator.class, DEFAULT_AUTH_DECORATORS, authDecoratorsMap);

        logger.debug("Filtered parameters are {}", parameters);
        // create request
        endPoint = pathParamResolver.apply(endPoint, parameters);

        String protocol = null;
        String host = null;
        int port = -1;
        String path = null;
        try {
            URL uri = new URL(endPoint);
            protocol = uri.getProtocol();
            host = uri.getHost();
            port = uri.getPort();
            path = uri.getPath();
            String query = uri.getQuery();
            if (!isEmpty(path) && !isEmpty(query)) {
                path += "?" + query;
            }
        } catch (MalformedURLException ex) {
            logger.info("Parameter endpoint {} is not valid uri {}", endPoint, ex.getMessage());
        }

        if (isEmpty(protocol)) {
            protocol = getParam(parameters, PROTOCOL, String.class, "http");
            logger.info("Protocol not specified, using {}", protocol);
        }

        boolean isSsl = protocol.equalsIgnoreCase("https");

        if (isEmpty(host)) {
            host = getParam(parameters, HOST, String.class, "localhost");
            logger.info("Host not specified, using {}", host);
        }
        if (port == -1) {
            port = getParam(parameters, PORT, Integer.class, isSsl ? DEFAULT_SSL_PORT : DEFAULT_PORT);
            logger.info("Port not specified, using {}", port);
        }
        if (isEmpty(path)) {
            path = endPoint;
            logger.info("Path is empty, using whole endpoint {}", endPoint);
        }
        logger.debug("Invoking request with protocol {} host {} port {} and endpoint {}", protocol, host, port, path);
        WebClient client = isSsl ? httpsClient : httpClient;
        HttpRequest<Buffer> request = client.request(method, port, host, path);
        requestDecorators.forEach(d -> d.decorate(workItem, parameters, request));
        authDecorators.forEach(d -> d.decorate(workItem, parameters, request));
        paramsDecorator.decorate(workItem, parameters, request);
        Duration requestTimeout = getRequestTimeout(parameters);
        HttpResponse<Buffer> response = method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)
                ? sendJson(request, bodyBuilder.apply(parameters), requestTimeout)
                : send(request, requestTimeout);
        int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new WorkItemExecutionException(Integer.toString(statusCode), "Request for endpoint " + endPoint + " failed with message: " + response.statusMessage());
        }
        manager.completeWorkItem(workItem.getStringId(), Collections.singletonMap(RESULT, resultHandler.apply(response, targetInfo)));
    }

    private static HttpResponse<Buffer> sendJson(HttpRequest<Buffer> request, Object body, Duration requestTimeout) {
        if (requestTimeout == null) {
            return request.sendJsonAndAwait(body);
        } else {
            return request.sendJson(body).await().atMost(requestTimeout);
        }
    }

    private static HttpResponse<Buffer> send(HttpRequest<Buffer> request, Duration requestTimeout) {
        if (requestTimeout == null) {
            return request.sendAndAwait();
        } else {
            return request.send().await().atMost(requestTimeout);
        }
    }

    private static Duration getRequestTimeout(Map<String, Object> parameters) {
        Long requestTimeoutInMillis = getParam(parameters, REQUEST_TIMEOUT_IN_MILLIS, Long.class, null);
        return requestTimeoutInMillis == null ? null : Duration.ofMillis(requestTimeoutInMillis);
    }

    private Class<?> getTargetInfo(KogitoWorkItem workItem) {
        String varName = ((WorkItemNode) ((WorkItemNodeInstance) workItem.getNodeInstance()).getNode()).getIoSpecification().getOutputMappingBySources().get(RESULT);
        if (varName != null) {
            return getType(workItem, varName);
        }
        logger.warn("no out mapping for {}", RESULT);
        return null;
    }

    private Class<?> getType(KogitoWorkItem workItem, String varName) {
        VariableScope variableScope = (VariableScope) ((ContextResolver) ((NodeInstance) workItem.getNodeInstance()).getNode()).resolveContext(VariableScope.VARIABLE_SCOPE, varName);
        if (variableScope != null) {
            Variable variable = variableScope.findVariable(varName);
            if (variable != null) {
                return variable.getType().getObjectClass();
            }
        }
        logger.info("Cannot find definition for variable {}", varName);
        return null;
    }

    @Override
    public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        // rest item handler does not support abort
    }
}
