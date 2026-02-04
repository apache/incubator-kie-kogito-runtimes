/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.addons.quarkus.k8s.test.utils;

import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.fabric8.mockwebserver.MockWebServer;
import io.fabric8.openshift.client.OpenShiftClient;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * Quarkus 3.27.2 / Fabric8 7.3.1 upgrade:
 * - The openshift-server-mock artifact and OpenShiftMockServer class were removed in Fabric8 7.x.
 * - Replaced with KubernetesMockServer + client.adapt(OpenShiftClient.class).
 * - Uses KubernetesCrudDispatcher for CRUD mode (auto-handles POST/GET/PUT/DELETE).
 * - The single-boolean constructor KubernetesMockServer(boolean) sets useHttps, NOT crudMode.
 * - Uses io.fabric8.mockwebserver.Context (fully qualified to avoid clash with
 * QuarkusTestResourceLifecycleManager.Context).
 */
public class OpenShiftMockServerTestResource implements QuarkusTestResourceLifecycleManager {

    private KubernetesMockServer server;
    private KubernetesClient kubernetesClient;
    private OpenShiftClient openShiftClient;

    @Override
    public Map<String, String> start() {
        // Fabric8 7.3.1: Create mock server with CRUD mode via KubernetesCrudDispatcher.
        // Context is fully qualified to avoid clash with QuarkusTestResourceLifecycleManager.Context.
        // useHttps=false to avoid SSL handshake overhead in tests.
        server = new KubernetesMockServer(
                new io.fabric8.mockwebserver.Context(),
                new MockWebServer(),
                new HashMap<>(),
                new KubernetesCrudDispatcher(),
                false);
        server.init();

        // Fabric8 7.x: createClient() replaces getClient(), adapt() replaces createOpenShiftClient()
        kubernetesClient = server.createClient();
        openShiftClient = kubernetesClient.adapt(OpenShiftClient.class);

        return Map.of(
                "quarkus.kubernetes-client.master-url", kubernetesClient.getConfiguration().getMasterUrl(),
                "quarkus.kubernetes-client.trust-certs", "true");
    }

    @Override
    public void stop() {
        if (openShiftClient != null) {
            openShiftClient.close();
        }
        if (kubernetesClient != null) {
            kubernetesClient.close();
        }
        if (server != null) {
            server.destroy();
        }
    }

    public KubernetesMockServer getServer() {
        return server;
    }

    public OpenShiftClient getClient() {
        return openShiftClient;
    }
}
