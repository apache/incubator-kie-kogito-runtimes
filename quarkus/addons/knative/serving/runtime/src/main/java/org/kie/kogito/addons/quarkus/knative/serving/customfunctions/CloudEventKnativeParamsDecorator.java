/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addons.quarkus.knative.serving.customfunctions;

import java.util.Map;

import org.kie.kogito.event.cloudevents.utils.CloudEventUtils;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kogito.workitem.rest.decorators.ParamsDecorator;

import io.vertx.mutiny.ext.web.client.HttpRequest;

import static org.kie.kogito.addons.quarkus.knative.serving.customfunctions.KnativeWorkItemHandler.APPLICATION_CLOUDEVENTS_JSON_CHARSET_UTF_8;
import static org.kie.kogito.addons.quarkus.knative.serving.customfunctions.KnativeWorkItemHandler.ID;

public final class CloudEventKnativeParamsDecorator implements ParamsDecorator {

    @Override
    public void decorate(KogitoWorkItem workItem, Map<String, Object> parameters, HttpRequest<?> request) {
        Map<String, Object> cloudEvent = KnativeFunctionPayloadSupplier.getPayload(parameters);

        if (cloudEvent.get(ID) == null) {
            String cloudEventId = generateCloudEventId(cloudEvent.hashCode(), workItem.getProcessInstanceStringId());
            cloudEvent.put(ID, cloudEventId);
            parameters.put(ID, cloudEventId);
        }

        CloudEventUtils.validateCloudEvent(cloudEvent);

        request.putHeader("Content-Type", APPLICATION_CLOUDEVENTS_JSON_CHARSET_UTF_8);
    }

    private static String generateCloudEventId(int uniqueIdentifier, String processInstanceId) {
        return processInstanceId + "_" + uniqueIdentifier;
    }
}
