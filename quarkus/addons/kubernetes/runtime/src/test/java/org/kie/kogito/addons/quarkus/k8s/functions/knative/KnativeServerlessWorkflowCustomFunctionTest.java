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
package org.kie.kogito.addons.quarkus.k8s.functions.knative;

import java.util.Map;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.process.workitem.WorkItemExecutionException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.knative.serving.v1.Service;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@QuarkusTest
@WithKubernetesTestServer
@QuarkusTestResource(KnativeServerlessWorkflowCustomFunctionMockServer.class)
class KnativeServerlessWorkflowCustomFunctionTest {

    static final String REMOTE_SERVICE_URL_PROPERTY_NAME = "knative-serverless-workflow-custom-function-test.remote.service.url";

    @ConfigProperty(name = REMOTE_SERVICE_URL_PROPERTY_NAME)
    String remoteServiceUrl;

    @KubernetesTestServer
    KubernetesServer mockK8sServer;

    @Inject
    KnativeServerlessWorkflowCustomFunction knativeServerlessWorkflowCustomFunction;

    @Inject
    KnativeServiceRegistry knativeServiceRegistry;

    static KnativeClient knativeClient;

    @BeforeEach
    void createServiceIfNotExists() {
        String namespace = "test";

        if (mockK8sServer.getClient().services().inNamespace("test").withName("serverless-workflow-greeting-quarkus").get() != null) {
            return;
        }

        knativeClient = mockK8sServer.getClient().adapt(KnativeClient.class);
        Service kService = knativeClient.services()
                .inNamespace(namespace)
                .load(getClass().getClassLoader().getResourceAsStream("knative/quarkus-greeting.yaml"))
                .get();

        kService.getStatus().setUrl(remoteServiceUrl);

        knativeClient.services().inNamespace(namespace).resource(kService).create();

        knativeServiceRegistry.addService("serverless-workflow-greeting-quarkus");
    }

    @AfterAll
    static void cleanup() {
        knativeClient.close();
    }

    @Test
    void executeWithEmptyParameters() {
        JsonNode output = knativeServerlessWorkflowCustomFunction.execute("serverless-workflow-greeting-quarkus", "/", Map.of());

        JsonNode expected = JsonNodeFactory.instance.objectNode()
                .put("org", "Acme")
                .put("project", "Kogito");

        assertThat(output).isEqualTo(expected);
    }

    @Test
    void executeWithParameters() {
        Map<String, Object> parameters = Map.of(
                "org", "Acme",
                "project", "Kogito");

        JsonNode output = knativeServerlessWorkflowCustomFunction.execute("serverless-workflow-greeting-quarkus", "/", parameters);

        JsonNode expected = JsonNodeFactory.instance.objectNode()
                .put("message", "Kogito is awesome!");

        assertThat(output).isEqualTo(expected);
    }

    @Test
    void executeWithQueryParameters() {
        Map<String, Object> parameters = Map.of();

        JsonNode output = knativeServerlessWorkflowCustomFunction.execute("serverless-workflow-greeting-quarkus", "/hello", parameters);

        JsonNode expected = JsonNodeFactory.instance.objectNode()
                .put("message", "Hello Kogito");

        assertThat(output).isEqualTo(expected);
    }

    @Test
    void execute404() {
        Map<String, Object> parameters = Map.of();
        assertThatCode(() -> knativeServerlessWorkflowCustomFunction.execute("serverless-workflow-greeting-quarkus", "/non_existing_path", parameters))
                .isInstanceOf(WorkItemExecutionException.class)
                .extracting("errorCode")
                .isEqualTo("404");
    }
}
