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

import java.util.Optional;

import javax.inject.Inject;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.knative.mock.EnableKnativeMockClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@WithKubernetesTestServer // needed to resolve the mock k8s client in our services
@EnableKnativeMockClient(crud = true)
public class KnativeRouteEndpointDiscoveryTest {

    @Inject
    KnativeRouteEndpointDiscovery endpointDiscovery;

    static KnativeClient knativeClient;

    @BeforeEach
    public void setupMockData() {
        this.endpointDiscovery.setKnativeClient(knativeClient);
    }

    @Test
    public void testSimpleCase() {
        final Optional<Endpoint> endpoint = endpointDiscovery.findEndpoint("test", "ksvc1");
        assertTrue(endpoint.isEmpty());
    }
}
