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
package org.kie.kogito.addons.quarkus.camel.runtime;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.serverless.workflow.WorkflowWorkItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.kie.kogito.addons.quarkus.camel.runtime.CamelFunctionArgs.BODY;
import static org.kie.kogito.addons.quarkus.camel.runtime.CamelFunctionArgs.HEADERS;

@ApplicationScoped
public class CamelCustomWorkItemHandler extends WorkflowWorkItemHandler {

    public static final String OPERATION = "operation";
    public static final String NAME = "CamelCustomWorkItemHandler";

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelCustomWorkItemHandler.class);

    @Inject
    CamelContext context;

    @Inject
    ObjectMapper objectMapper;

    @Override
    protected Object internalExecute(KogitoWorkItem workItem, Map<String, Object> parameters) {
        final Map<String, Object> metadata = workItem.getNodeInstance().getNode().getMetaData();
        final String camelEndpoint = (String) metadata.get(OPERATION);

        checkEndpointExists(camelEndpoint);

        if (parameters.isEmpty()) {
            LOGGER.debug("Invoking Camel Endpoint '{}' with no body or headers", camelEndpoint);
            return context.createProducerTemplate().requestBody(camelEndpoint, "");
        } else {
            Object body = null;
            Map<String, Object> headers = null;
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                if (HEADERS.equalsIgnoreCase(entry.getKey())) {
                    headers = objectMapper.convertValue(entry.getValue(), new TypeReference<>() {
                    });
                }
                if (BODY.equalsIgnoreCase(entry.getKey())) {
                    body = entry.getValue();
                }
            }
            if (body == null) {
                body = parameters.values().iterator().next();
            }

            if (headers == null) {
                LOGGER.debug("Invoking Camel Endpoint '{}' with body '{}'", camelEndpoint, body);
                return context.createProducerTemplate().requestBody(camelEndpoint, body);
            }
            LOGGER.debug("Invoking Camel Endpoint '{}' with body '{}' and headers '{}'", camelEndpoint, body, headers);
            return context.createProducerTemplate().requestBodyAndHeaders(camelEndpoint, body, headers);
        }
    }

    private void checkEndpointExists(final String endpoint) {
        if (endpoint == null) {
            throw new IllegalArgumentException("Operation (the Camel Endpoint Identifier) is a mandatory parameter");
        }
        if (context.hasEndpoint(endpoint) == null) {
            throw new IllegalArgumentException("Endpoint '" + endpoint + "' doesn't exist. Make sure that the Camel Route is within the project's context.");
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
