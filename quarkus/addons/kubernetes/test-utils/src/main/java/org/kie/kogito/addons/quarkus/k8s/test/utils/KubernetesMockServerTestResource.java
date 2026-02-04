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

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.fabric8.mockwebserver.MockWebServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * Quarkus test resource that provides a Fabric8 Kubernetes mock server with CRUD support.
 *
 * Quarkus 3.27.2 / Fabric8 7.3.1 upgrade:
 * - Replaced KubernetesServer (Fabric8 6.x) with KubernetesMockServer (Fabric8 7.x).
 * - before()/after() lifecycle replaced with init()/destroy().
 * - Client creation changed from server.getClient() to server.createClient().
 * - CRUD mode now requires explicit KubernetesCrudDispatcher. The single-boolean
 * constructor KubernetesMockServer(boolean) sets useHttps, NOT crudMode.
 * - The responses map must be a mutable HashMap (not Collections.emptyMap()).
 * - Uses io.fabric8.mockwebserver.Context (fully qualified to avoid clash with
 * QuarkusTestResourceLifecycleManager.Context).
 */
public class KubernetesMockServerTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String TEST_NAMESPACE = "serverless-workflow-greeting-quarkus";
    private static KubernetesMockServer server;
    private KubernetesClient client;

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

        // Fabric8 7.x: createClient() replaces getClient() from Fabric8 6.x
        client = server.createClient();
        String mockServerUrl = client.getConfiguration().getMasterUrl();

        System.setProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY, mockServerUrl);

        Map<String, String> config = new HashMap<>();
        config.put("quarkus.kubernetes-client.master-url", mockServerUrl);
        config.put("quarkus.kubernetes-client.namespace", TEST_NAMESPACE);
        config.put("quarkus.kubernetes-client.trust-certs", "true");
        return config;
    }

    @Override
    public void stop() {
        if (client != null) {
            client.close();
        }
        if (server != null) {
            server.destroy();
            server = null;
        }
    }

    public static KubernetesMockServer getServer() {
        return server;
    }

    public KubernetesClient getClient() {
        return client;
    }
}
