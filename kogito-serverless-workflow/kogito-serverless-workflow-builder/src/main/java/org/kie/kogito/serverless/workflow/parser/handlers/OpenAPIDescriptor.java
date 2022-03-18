/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

import org.kie.kogito.serverless.workflow.suppliers.ParamsRestBodyBuilderSupplier;
import org.kie.kogito.serverless.workflow.suppliers.SingletonRestBodyBuilderSupplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.javaparser.ast.expr.Expression;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.servers.Server;
import io.vertx.core.http.HttpMethod;

import static org.kie.kogito.internal.utils.ConversionUtils.toCamelCase;

class OpenAPIDescriptor {

    private static final String REGEX_NO_EXT = "[.][^.]+$";
    private static final String ONLY_CHARS = "[^a-z]";

    public static OpenAPIDescriptor of(OpenAPI openAPI, String method, JsonNode functionArgs) {
        // path to operation map
        Map<String, List<OperationInfo>> operations = collectOperations(openAPI.getPaths(), method);
        if (operations.isEmpty()) {
            throw new IllegalArgumentException("Cannot find operation for " + method);
        }
        if (operations.size() > 1) {
            // TODO improvement try to infer the right method from arguments
            throw new IllegalArgumentException("There is more than one operation " + operations + " in different paths with name " + method);
        }
        Map.Entry<String, List<OperationInfo>> operEntry = operations.entrySet().iterator().next();

        if (operEntry.getValue().size() > 1) {
            // TODO improvement try to infer the right method from arguments
            throw new IllegalArgumentException("There is more than one operation " + operations + " in different methods with name " + method + " for path " + operEntry.getKey());
        }
        OperationInfo operation = operEntry.getValue().get(0);
        return new OpenAPIDescriptor(operation.getMethod(), operEntry.getKey(), getRequestBuilder(openAPI.getComponents(), operation.getOperation(), functionArgs));
    }

    private static Supplier<Expression> getRequestBuilder(Components components, Operation operation, JsonNode functionArgs) {
        return getSchema(components, operation).filter(s -> isSingleton(s, functionArgs)).isEmpty() ? new ParamsRestBodyBuilderSupplier() : new SingletonRestBodyBuilderSupplier();
    }

    private static boolean isSingleton(Schema<?> schema, JsonNode functionArgs) {
        return functionArgs != null && functionArgs.get(schema.getName()) != null;
    }

    private static Optional<Schema<?>> getSchema(Components components, Operation operation) {

        RequestBody requestBody = operation.getRequestBody();
        Schema<?> schema = null;
        if (requestBody != null) {
            String ref = requestBody.get$ref();
            if (ref != null) {
                if (components != null && components.getSchemas() != null) {
                    schema = components.getSchemas().get(ref);
                }
            } else if (requestBody.getContent() != null) {
                for (MediaType type : requestBody.getContent().values()) {
                    schema = type.getSchema();
                    if (schema != null) {
                        break;
                    }
                }
            }
        }
        return Optional.ofNullable(schema);
    }

    private static void checkOperation(String path, String operationId, HttpMethod method, Operation operation, Map<String, List<OperationInfo>> map) {
        if (operation != null && (operationId.equals(operation.getOperationId()) || operationId.equals(toCamelCase(operation.getOperationId())))) {
            map.computeIfAbsent(path, k -> new ArrayList<>()).add(new OperationInfo(method, operation));
        }
    }

    private static Map<String, List<OperationInfo>> collectOperations(Paths paths, String operationId) {
        Map<String, List<OperationInfo>> result = new HashMap<>();
        for (Entry<String, PathItem> path : paths.entrySet()) {
            checkOperation(path.getKey(), operationId, HttpMethod.GET, path.getValue().getGet(), result);
            checkOperation(path.getKey(), operationId, HttpMethod.HEAD, path.getValue().getHead(), result);
            checkOperation(path.getKey(), operationId, HttpMethod.DELETE, path.getValue().getDelete(), result);
            checkOperation(path.getKey(), operationId, HttpMethod.PATCH, path.getValue().getPatch(), result);
            checkOperation(path.getKey(), operationId, HttpMethod.POST, path.getValue().getPost(), result);
            checkOperation(path.getKey(), operationId, HttpMethod.PUT, path.getValue().getPut(), result);
            checkOperation(path.getKey(), operationId, HttpMethod.OPTIONS, path.getValue().getOptions(), result);
            checkOperation(path.getKey(), operationId, HttpMethod.TRACE, path.getValue().getTrace(), result);
        }
        return result;
    }

    private final HttpMethod method;
    private final String path;
    private final Supplier<Expression> requestBuilderSupplier;

    private OpenAPIDescriptor(HttpMethod method, String path, Supplier<Expression> requestBuilderSupplier) {
        this.method = method;
        this.path = path;
        this.requestBuilderSupplier = requestBuilderSupplier;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Supplier<Expression> getRequestBuilderSupplier() {
        return requestBuilderSupplier;
    }

    private static class OperationInfo {
        private final HttpMethod method;
        private final Operation operation;

        public OperationInfo(HttpMethod method, Operation operation) {
            this.method = method;
            this.operation = operation;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public Operation getOperation() {
            return operation;
        }
    }

    public static String getServiceName(String uri) {
        return uri.substring(uri.lastIndexOf('/') + 1).toLowerCase().replaceFirst(REGEX_NO_EXT, "").replaceAll(ONLY_CHARS, "");
    }

    public static String getDefaultURL(OpenAPI openAPI, String defaultBase) {
        List<Server> servers = openAPI.getServers();
        if (servers != null) {
            for (Server server : servers) {
                return server.getUrl();
            }
        }
        return defaultBase;
    }
}
