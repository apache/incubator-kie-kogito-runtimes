/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addons.k8s.workitems;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.kie.api.runtime.process.WorkItem;
import org.kie.kogito.addons.k8s.Endpoint;
import org.kie.kogito.addons.k8s.EndpointDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This service is meant to be used with KogitoWorkItemHandlers to call well-known Kogito services deployed in the very same Kubernetes cluster.
 */
// TODO: review this implementation with the team. This class should be part of kogito-rest-workitem. Then we can inject the discoverability to that use case.
// TODO: see more at . This implementation is inherited from the old "DiscoveredServiceWorkItemHandler".
public abstract class AbstractDiscoveredEndpointCaller {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDiscoveredEndpointCaller.class);
    private static final List<String> INTERNAL_FIELDS = Arrays.asList("TaskName", "ActorId", "GroupId", "Priority", "Comment", "Skippable", "Content", "Model", "Namespace");

    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public AbstractDiscoveredEndpointCaller(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    protected abstract EndpointDiscovery getEndpointDiscovery();

    public Map<String, Object> discoverAndCall(WorkItem workItem, String namespace, String serviceName, String httpMethod) {
        final Map<String, Object> data = new HashMap<>(workItem.getParameters());
        final String service = (String) data.remove(serviceName);

        final Optional<Endpoint> endpoint = this.getEndpointDiscovery().findEndpoint(namespace, service);
        if (endpoint.isEmpty()) {
            throw new IllegalArgumentException("Kubernetes service " + service + " not found in the namespace " + namespace);
        }
        LOGGER.debug("Found endpoint for service {} in namespace {} with URL {}", service, namespace, endpoint.get().getURL());

        INTERNAL_FIELDS.forEach(data::remove);

        final Request request = createRequest(endpoint.get(), createRequestPayload(data), httpMethod);
        try (Response response = this.httpClient.newCall(request).execute()) {
            return createResultsFromResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RequestBody createRequestPayload(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        try {
            final String json = objectMapper.writeValueAsString(data);
            LOGGER.debug("Sending body {}", json);
            return RequestBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_JSON), json);
        } catch (Exception e) {
            throw new EndpointCallerException("Unexpected error when producing request payload", e);
        }
    }

    private Request createRequest(Endpoint endpoint, RequestBody body, String httpMethod) {
        Request.Builder builder = new Request.Builder().url(endpoint.getURL()).get();
        switch (httpMethod) {
            case HttpMethod.DELETE:
                builder = new Request.Builder().url(endpoint.getURL()).delete(body);
                break;
            case HttpMethod.POST:
                builder = new Request.Builder().url(endpoint.getURL()).post(body);
                break;
            case HttpMethod.PUT:
                builder = new Request.Builder().url(endpoint.getURL()).put(body);
                break;
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createResultsFromResponse(Response response) throws IOException {
        String payload = "";
        if (response.body() != null) {
            payload = response.body().string();
        }
        LOGGER.debug("Response code {} and payload {}", response.code(), payload);
        if (!response.isSuccessful()) {
            throw new EndpointCallerException("Unsuccessful response from service " + response.message() + " (code " + response.code() + ")");
        }
        return objectMapper.readValue(payload, Map.class);
    }
}
