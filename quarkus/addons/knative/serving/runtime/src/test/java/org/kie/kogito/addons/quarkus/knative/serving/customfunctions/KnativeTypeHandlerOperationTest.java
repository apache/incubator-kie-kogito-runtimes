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

class KnativeTypeHandlerOperationTest {

    private static final Map<String, Object> params = Map.of(
            "param1", "value1",
            "param2", "value2",
            "param3", "value3");

    static Stream<Arguments> from() {
        return Stream.of(
                Arguments.of("my_service", defaultPathEmptyParams()),
                Arguments.of("my_service?path=/my_path", customPathEmptyParams()),
                Arguments.of("my_service?param1=value1&param2=value2&param3=value3", defaultPathWithParams()),
                Arguments.of("my_service?path=/my_path&param1=value1&param2=value2&param3=value3", customPathWithParams()),
                Arguments.of("my_service?param1=value1&param2=value2&path=/my_path&param3=value3", customPathWithParams()));
    }

    @ParameterizedTest
    @MethodSource
    void from(String operation, KnativeTypeHandlerOperation expectedMetadata) {
        assertThat(KnativeTypeHandlerOperation.from(operation))
                .isEqualTo(expectedMetadata);
    }

    @Test
    void nullOperationShouldThrowNPE() {
        assertThatNullPointerException()
                .isThrownBy(() -> KnativeTypeHandlerOperation.from(null))
                .withMessage(KnativeTypeHandlerOperation.OPERATION_IS_REQUIRED_MSG);
    }

    private static KnativeTypeHandlerOperation customPathWithParams() {
        return new KnativeTypeHandlerOperation("my_service", "/my_path", params);
    }

    private static KnativeTypeHandlerOperation customPathEmptyParams() {
        return new KnativeTypeHandlerOperation("my_service", "/my_path", Map.of());
    }

    private static KnativeTypeHandlerOperation defaultPathWithParams() {
        return new KnativeTypeHandlerOperation("my_service", "/", params);
    }

    private static KnativeTypeHandlerOperation defaultPathEmptyParams() {
        return new KnativeTypeHandlerOperation("my_service", "/", Map.of());
    }
}
