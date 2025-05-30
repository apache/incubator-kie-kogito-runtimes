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
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.kie.kogito.event.cloudevents.utils.CloudEventUtils;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.jackson.utils.ObjectNodeListenerAware;
import org.kogito.workitem.rest.RestWorkItemHandler;
import org.kogito.workitem.rest.decorators.PrefixParamsDecorator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        Map<String, Object> inputModel = new HashMap<>();

        Object inputModelObject = parameters.get(MODEL_WORKFLOW_VAR);
        if (inputModelObject != null && inputModelObject instanceof ObjectNodeListenerAware) {
            ObjectMapper mapper = new ObjectMapper();
            ((ObjectNodeListenerAware) inputModelObject).fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();
                Object rawValue = mapper.convertValue(value, Object.class);
                inputModel.put(entry.getKey(), rawValue);
            });
        }

        Set<String> keysFilter = Set.of(RestWorkItemHandler.REQUEST_TIMEOUT_IN_MILLIS, MODEL_WORKFLOW_VAR);
        Map<String, Object> filteredParams = parameters.entrySet().stream()
                .filter(entry -> !keysFilter.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (filteredParams.isEmpty()) {
            Set<String> paramsRemove = super.extractHeadersQueries(workItem, inputModel, request);
            if (inputModelObject != null && inputModelObject instanceof ObjectNodeListenerAware) {
                ((ObjectNodeListenerAware) inputModelObject).remove(paramsRemove);
            }
        } else {
            super.decorate(workItem, parameters, request);
        }
    }
}
