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
package org.kie.kogito.addons.quarkus.knative.serving.customfunctions;

import java.util.Map;
import java.util.Objects;

import org.kie.kogito.jackson.utils.ObjectMapperFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import static org.kie.kogito.addons.quarkus.knative.serving.customfunctions.KnativeWorkItemHandlerConstants.FUNCTION_METADATA_PROPERTY_NAME;
import static org.kie.kogito.addons.quarkus.knative.serving.customfunctions.KnativeWorkItemHandlerConstants.OPERATION_PROPERTY_NAME;

final class KnativeWorkItemHandlerMetadata {

    static final String MISSING_OPERATION_ERROR_MSG = OPERATION_PROPERTY_NAME + " is required.";

    private final String operation;

    private final KnativeCustomFunctionMetadata functionMetadata;

    KnativeWorkItemHandlerMetadata(String operation, KnativeCustomFunctionMetadata functionMetadata) {
        this.operation = operation;
        this.functionMetadata = functionMetadata;
    }

    KnativeCustomFunctionMetadata getFunctionMetadata() {
        return functionMetadata;
    }

    String getOperation() {
        return operation;
    }

    static KnativeWorkItemHandlerMetadata from(Map<String, Object> workItemMetadataMap) {
        String operation = (String) Objects.requireNonNull(workItemMetadataMap.get(OPERATION_PROPERTY_NAME), MISSING_OPERATION_ERROR_MSG);

        KnativeCustomFunctionMetadata functionMetadata;

        try {
            Object functionMetadataPropertyValue = workItemMetadataMap.get(FUNCTION_METADATA_PROPERTY_NAME);

            if (functionMetadataPropertyValue == null || "null".equals(functionMetadataPropertyValue)) {
                functionMetadata = new KnativeCustomFunctionMetadata();
            } else {
                functionMetadata = ObjectMapperFactory.get().readValue(
                        (String) functionMetadataPropertyValue,
                        KnativeCustomFunctionMetadata.class);
            }
        } catch (JsonProcessingException e) {
            throw new ServiceDiscoveryException("Error while reading the function metadata.", e);
        }

        return new KnativeWorkItemHandlerMetadata(operation, functionMetadata);
    }
}
