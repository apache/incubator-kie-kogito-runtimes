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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.kie.kogito.addons.quarkus.knative.serving.customfunctions.KnativeWorkItemHandler.PAYLOAD_FIELDS_DELIMITER;
import static org.kie.kogito.addons.quarkus.knative.serving.customfunctions.KnativeWorkItemHandler.PAYLOAD_FIELDS_PROPERTY_NAME;

@QuarkusTest
class GetRequestKnativeParamsDecoratorTest {

    @Inject
    WebClient webClient;

    final GetRequestKnativeParamsDecorator decorator = new GetRequestKnativeParamsDecorator();

    @Test
    void decorate() {
        Map<String, String> expectedParams = Map.of(
                "key1", "value1",
                "key2", "value2");

        HttpRequest<?> request = createRequest();

        HashMap<String, Object> parameters = new HashMap<>(expectedParams);
        parameters.put(PAYLOAD_FIELDS_PROPERTY_NAME, String.join(PAYLOAD_FIELDS_DELIMITER, expectedParams.keySet()));

        decorator.decorate(null, parameters, request);

        assertThat(request.queryParams()).hasSize(2);
        expectedParams.forEach((k, v) -> assertThat(request.queryParams().get(k)).isEqualTo(v));
    }

    @Test
    void decorateNonStringValuesShouldThrowException() {
        Map<String, Object> expectedParams = Map.of(
                "key1", "value1",
                "key2", new Object());

        HttpRequest<?> request = createRequest();

        HashMap<String, Object> parameters = new HashMap<>(expectedParams);
        parameters.put(PAYLOAD_FIELDS_PROPERTY_NAME, String.join(PAYLOAD_FIELDS_DELIMITER, expectedParams.keySet()));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> decorator.decorate(null, parameters, request));
    }

    private HttpRequest<Buffer> createRequest() {
        return webClient.request(HttpMethod.GET, 8080, "localhost", "/path");
    }
}
