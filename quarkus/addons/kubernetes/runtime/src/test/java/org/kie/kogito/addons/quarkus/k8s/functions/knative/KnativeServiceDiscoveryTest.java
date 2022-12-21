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

import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.knative.serving.v1.Service;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@WithKubernetesTestServer
class KnativeServiceDiscoveryTest {

    @KubernetesTestServer
    KubernetesServer mockServer;

    @Inject
    KnativeServiceDiscovery knativeServiceDiscovery;

    static KnativeClient knativeClient;

    @BeforeEach
    void createServiceIfNotExists() {
        String namespace = "test";

        if (mockServer.getClient().services().inNamespace("test").withName("serverless-workflow-greeting-quarkus").get() != null) {
            return;
        }

        knativeClient = mockServer.getClient().adapt(KnativeClient.class);
        Service kService = knativeClient.services()
                .inNamespace(namespace)
                .load(getClass().getClassLoader().getResourceAsStream("knative/quarkus-greeting.yaml"))
                .get();
        knativeClient.services().inNamespace(namespace).resource(kService).create();
    }

    @AfterAll
    static void cleanup() {
        knativeClient.close();
    }

    @Test
    void discoverSpecificNamespace() {
        Optional<Server> server = knativeServiceDiscovery.discover("test/serverless-workflow-greeting-quarkus");
        assertThat(server).map(Server::getHost).hasValue("serverless-workflow-greeting-quarkus.test.10.99.154.147.sslip.io");
        assertThat(server).map(Server::getPort).hasValue(80);
    }

    @Test
    void discoverCurrentNamespace() {
        Optional<Server> server = knativeServiceDiscovery.discover("serverless-workflow-greeting-quarkus");
        assertThat(server).map(Server::getHost).hasValue("serverless-workflow-greeting-quarkus.test.10.99.154.147.sslip.io");
        assertThat(server).map(Server::getPort).hasValue(80);
    }

    @Test
    void serviceInDifferentNamespaceShouldNotBeFound() {
        Optional<Server> server = knativeServiceDiscovery.discover("different_namespace/serverless-workflow-greeting-quarkus");
        assertThat(server).isEmpty();
    }
}
