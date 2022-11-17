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
package org.kie.kogito.addons.quarkus.k8s.functions.knative;

import java.util.Map;
import java.util.Objects;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.kogito.process.workitem.WorkItemExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;

@ApplicationScoped
final class KnativeServerlessWorkflowCustomFunction {

    private static final Logger logger = LoggerFactory.getLogger(KnativeServerlessWorkflowCustomFunction.class);

    private final WebClient webClient;

    private final KnativeServiceRegistry knativeServiceRegistry;

    @Inject
    KnativeServerlessWorkflowCustomFunction(Vertx vertx, KnativeServiceRegistry knativeServiceRegistry) {
        this.webClient = WebClient.create(vertx);
        this.knativeServiceRegistry = knativeServiceRegistry;
    }

    @PreDestroy
    void preDestroy() {
        webClient.close();
    }

    JsonNode execute(String knativeServiceName, String path, Map<String, Object> parameters) {
        Objects.requireNonNull(knativeServiceName, "knativeServiceName is a mandatory parameter");
        Objects.requireNonNull(path, "path is a mandatory parameter");

        Server server = getServer(knativeServiceName);

        return sendRequest(server, path, parameters);
    }

    private JsonNode sendRequest(Server server, String path, Map<String, Object> parameters) {
        HttpRequest<Buffer> request = webClient.request(HttpMethod.POST, server.getPort(), server.getHost(), path);

        HttpResponse<Buffer> response;

        if (parameters.isEmpty()) {
            logger.debug("Sending request with empty body - host: {}, port: {}, path: {}", server.getHost(), server.getPort(), path);
            response = request.sendAndAwait();
        } else {
            JsonObject body = new JsonObject(parameters);
            logger.debug("Sending request with body - host: {}, port: {}, path: {}, body: {}", server.getHost(), server.getPort(), path, body);
            response = request.sendJsonObjectAndAwait(body);
        }

        JsonObject responseBody = response.bodyAsJsonObject();

        logger.debug("Response - status code: {}, body: {}", response.statusCode(), responseBody);

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new WorkItemExecutionException(Integer.toString(response.statusCode()), response.statusMessage());
        } else {
            ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
            responseBody.fieldNames().forEach(fieldName -> jsonNode.put(fieldName, responseBody.getString(fieldName)));
            return jsonNode;
        }
    }

    private Server getServer(String knativeServiceName) {
        return knativeServiceRegistry.getServer(knativeServiceName)
                .orElseThrow(() -> new WorkItemExecutionException("The Knative service '" + knativeServiceName
                        + "' could not be found."));
    }
}
