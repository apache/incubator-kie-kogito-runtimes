/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addons.quarkus.k8s;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@WithKubernetesTestServer
@QuarkusTest
public class KubernetesServiceEndpointDiscoveryTest {

    @KubernetesTestServer
    KubernetesServer mockServer;

    @Inject
    KubernetesServiceEndpointDiscovery endpointDiscovery;

    @BeforeEach
    public void setupMockData() {
        if (!mockServer.getClient().services().inNamespace("test").list().getItems().isEmpty()) {
            return;
        }

        final Service svc1 = new ServiceBuilder()
                .withNewMetadata()
                .withName("svc1").withNamespace("test")
                .and().withSpec(new ServiceSpec()).build();
        final Service svc2 = new ServiceBuilder()
                .withNewMetadata()
                .withName("svc2").withNamespace("test")
                .withLabels(Collections.singletonMap("app", "test1"))
                .and().withSpec(new ServiceSpec()).build();

        final ServicePort defaultPort = new ServicePort();
        defaultPort.setPort(80);

        final ServicePort randomPort = new ServicePort();
        randomPort.setPort(8778);

        final List<ServicePort> ports = new ArrayList<>();
        ports.add(defaultPort);
        ports.add(randomPort);

        svc1.getSpec().setClusterIP("127.0.0.1");
        svc1.getSpec().setPorts(ports);

        svc2.getSpec().setClusterIP("192.168.32.148");
        svc2.getSpec().setPorts(Collections.singletonList(randomPort));

        mockServer.getClient().services().create(svc1);
        mockServer.getClient().services().create(svc2);
    }

    @Test
    public void testGetURLOnStandardPort() {
        final Optional<Endpoint> endpoint = endpointDiscovery.findEndpoint("test", "svc1");
        assertTrue(endpoint.isPresent());
        assertEquals(1, endpoint.get().getURLs().size());
        try {
            new URL(endpoint.get().getURLs().get(0));
        } catch (MalformedURLException e) {
            fail("The generated URL " + endpoint.get().getURLs().get(0) + " is invalid"); //verbose
        }
    }

    @Test
    public void testGetURLOnRandomPort() {
        final List<Endpoint> endpoints = endpointDiscovery.findEndpoint("test", Collections.singletonMap("app", "test1"));
        assertFalse(endpoints.isEmpty());
        assertEquals(1, endpoints.get(0).getURLs().size());
        try {
            new URL(endpoints.get(0).getURLs().get(0));
        } catch (MalformedURLException e) {
            fail("The generated URL " + endpoints.get(0).getURLs().get(0) + " is invalid"); //verbose
        }
    }
}
