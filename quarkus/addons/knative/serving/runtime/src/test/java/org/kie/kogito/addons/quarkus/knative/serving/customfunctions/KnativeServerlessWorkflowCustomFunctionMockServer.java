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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.kie.kogito.addons.quarkus.knative.serving.customfunctions.KnativeServerlessWorkflowCustomFunctionTest.REMOTE_SERVICE_URL_PROPERTY_NAME;

public class KnativeServerlessWorkflowCustomFunctionMockServer implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        configureWiremockServer();
        return Map.of(REMOTE_SERVICE_URL_PROPERTY_NAME, wireMockServer.baseUrl());
    }

    private void configureWiremockServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();

        createExecuteWithEmptyParametersStub();
        createExecuteWithParametersStub();
        createExecuteWithQueryParametersStub();
        createExecute404Stub();
    }

    private void createExecute404Stub() {
        wireMockServer.stubFor(post(urlEqualTo("/non_existing_path"))
                .willReturn(aResponse()
                        .withStatus(404)));
    }

    private void createExecuteWithQueryParametersStub() {
        wireMockServer.stubFor(post(urlEqualTo("/hello"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(JsonNodeFactory.instance.objectNode()
                                .put("message", "Hello Kogito"))));
    }

    private void createExecuteWithParametersStub() {
        wireMockServer.stubFor(post(urlEqualTo("/"))
                .withRequestBody(equalToJson(JsonNodeFactory.instance.objectNode()
                        .put("org", "Acme")
                        .put("project", "Kogito")
                        .toString()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(JsonNodeFactory.instance.objectNode()
                                .put("message", "Kogito is awesome!"))));
    }

    private void createExecuteWithEmptyParametersStub() {
        wireMockServer.stubFor(post(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(JsonNodeFactory.instance.objectNode()
                                .put("org", "Acme")
                                .put("project", "Kogito"))));
    }

    @Override
    public void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
