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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.kie.kogito.addons.quarkus.knative.serving.customfunctions.KnativeWorkItemHandlerConstants.FUNCTION_METADATA_PROPERTY_NAME;
import static org.kie.kogito.addons.quarkus.knative.serving.customfunctions.KnativeWorkItemHandlerConstants.OPERATION_PROPERTY_NAME;
import static org.kie.kogito.addons.quarkus.knative.serving.customfunctions.KnativeWorkItemHandlerMetadata.MISSING_OPERATION_ERROR_MSG;

class KnativeWorkItemHandlerMetadataTest {

    private static final String MY_SERVICE = "my_service";

    @Test
    void from() {
        String serviceName = MY_SERVICE;
        String path = "/my_path";

        Map<String, Object> metadataMap = Map.of(
                OPERATION_PROPERTY_NAME, serviceName,
                FUNCTION_METADATA_PROPERTY_NAME, "{ \"path\": \"" + path + "\" }");

        KnativeWorkItemHandlerMetadata knativeWorkItemHandlerMetadata = KnativeWorkItemHandlerMetadata.from(metadataMap);

        assertThat(knativeWorkItemHandlerMetadata.getOperation()).isEqualTo(serviceName);
        assertThat(knativeWorkItemHandlerMetadata.getFunctionMetadata().getPath()).isEqualTo(path);
    }

    @Test
    void missingOperationShouldThrowIllegalArgumentException() {
        String path = "/my_path";

        Map<String, Object> metadataMap = Map.of(
                FUNCTION_METADATA_PROPERTY_NAME, "{ \"path\": \"" + path + "\" }");

        assertThatNullPointerException()
                .isThrownBy(() -> KnativeWorkItemHandlerMetadata.from(metadataMap))
                .withMessage(MISSING_OPERATION_ERROR_MSG);
    }

    @ParameterizedTest
    @MethodSource
    void missingFunctionMetadataShouldReturnNullPath(Map<String, Object> metadataMap) {
        KnativeWorkItemHandlerMetadata knativeWorkItemHandlerMetadata = KnativeWorkItemHandlerMetadata.from(metadataMap);

        assertThat(knativeWorkItemHandlerMetadata.getOperation()).isEqualTo(MY_SERVICE);
        assertThat(knativeWorkItemHandlerMetadata.getFunctionMetadata().getPath()).isNull();
    }

    static Stream<Arguments> missingFunctionMetadataShouldReturnNullPath() {
        return Stream.of(
                Arguments.of(Map.of(OPERATION_PROPERTY_NAME, MY_SERVICE)),
                Arguments.of(Map.of(
                        OPERATION_PROPERTY_NAME, MY_SERVICE,
                        FUNCTION_METADATA_PROPERTY_NAME, "null")));
    }
}