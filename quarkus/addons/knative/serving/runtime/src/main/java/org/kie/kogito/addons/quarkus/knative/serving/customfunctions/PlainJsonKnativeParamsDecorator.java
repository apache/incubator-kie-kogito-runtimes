/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.addons.quarkus.knative.serving.customfunctions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kie.kogito.event.cloudevents.utils.CloudEventUtils;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kogito.workitem.rest.decorators.PrefixParamsDecorator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.vertx.mutiny.ext.web.client.HttpRequest;

import static org.kie.kogito.addons.quarkus.knative.serving.customfunctions.KnativeWorkItemHandler.CLOUDEVENT_SENT_AS_PLAIN_JSON_ERROR_MESSAGE;
import static org.kie.kogito.addons.quarkus.knative.serving.customfunctions.KnativeWorkItemHandler.ID;
import static org.kie.kogito.serverless.workflow.SWFConstants.MODEL_WORKFLOW_VAR;

public final class PlainJsonKnativeParamsDecorator extends PrefixParamsDecorator {

    @Override
    public void decorate(KogitoWorkItem workItem, Map<String, Object> parameters, HttpRequest<?> request) {
        if (isCloudEvent(KnativeFunctionPayloadSupplier.getPayload(parameters))) {
            throw new IllegalArgumentException(CLOUDEVENT_SENT_AS_PLAIN_JSON_ERROR_MESSAGE);
        }
        buildFromParams(workItem, parameters, request);
    }

    private static boolean isCloudEvent(Map<String, Object> payload) {
        List<String> cloudEventMissingAttributes = CloudEventUtils.getMissingAttributes(payload);
        return !payload.isEmpty() && (cloudEventMissingAttributes.isEmpty() || (cloudEventMissingAttributes.size() == 1 && cloudEventMissingAttributes.contains(ID)));
    }

    private void buildFromParams(KogitoWorkItem workItem, Map<String, Object> parameters, HttpRequest<?> request) {
        Object modelVar = parameters.get(MODEL_WORKFLOW_VAR);

        if (!(modelVar instanceof ObjectNode objectNode)) {
            super.decorate(workItem, parameters, request);
            return;
        }

        // Flatten the ObjectNode into a plain map so extractHeadersQueries
        // can remove header/query keys from it without touching the original
        Map<String, Object> flatModel = flattenObjectNode(objectNode);

        // Extract headers/queries from the flat map (mutates it and the request)
        Set<String> extractedKeys = super.extractHeadersQueries(workItem, flatModel, request);

        // Apply the same removals to a copy of the original ObjectNode,
        // then put the sanitized copy back so downstream sees a clean model
        ObjectNode sanitizedModel = objectNode.deepCopy();
        sanitizedModel.remove(extractedKeys);
        parameters.put(MODEL_WORKFLOW_VAR, sanitizedModel);
    }

    private Map<String, Object> flattenObjectNode(ObjectNode objectNode) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        objectNode.properties().forEach(entry ->
            result.put(entry.getKey(), mapper.convertValue(entry.getValue(), Object.class))
        );
        return result;
    }
}
