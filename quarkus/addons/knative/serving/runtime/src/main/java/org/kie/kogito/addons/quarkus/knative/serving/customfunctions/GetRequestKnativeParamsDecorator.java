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

import java.text.MessageFormat;
import java.util.Map;

import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kogito.workitem.rest.decorators.ParamsDecorator;

import io.vertx.mutiny.ext.web.client.HttpRequest;

public final class GetRequestKnativeParamsDecorator implements ParamsDecorator {

    @Override
    public void decorate(KogitoWorkItem workItem, Map<String, Object> parameters, HttpRequest<?> request) {
        KnativeFunctionPayloadSupplier.getPayload(parameters).forEach((key, value) -> {
            if (value instanceof String) {
                request.addQueryParam(key, (String) value);
            } else if (value instanceof Number || value instanceof Boolean) {
                request.addQueryParam(key, String.valueOf(value));
            } else {
                String message = "Knative functions support only GET requests with String, Number or Boolean attributes. {0} has a {1} value.";
                throw new IllegalArgumentException(MessageFormat.format(message, key, value.getClass()));
            }
        });
    }
}
