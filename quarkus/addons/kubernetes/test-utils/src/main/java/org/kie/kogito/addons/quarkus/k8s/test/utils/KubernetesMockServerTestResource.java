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
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * Quarkus test resource that provides a Fabric8 Kubernetes mock server with CRUD support.
 *
 * Quarkus 3.27.2 upgrade: Updated for Fabric8 Kubernetes Client 7.x API changes:
 * - KubernetesServer replaced with KubernetesMockServer
 * - before()/after() replaced with init()/destroy()
 * - Client creation now via createClient() method
 */
public class KubernetesMockServerTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String TEST_NAMESPACE = "serverless-workflow-greeting-quarkus";
    private KubernetesMockServer server;
    private KubernetesClient client;

    @Override
    public Map<String, String> start() {
        // Create and start the mock server (no-arg constructor for Fabric8 7.x)
        server = new KubernetesMockServer();
        server.init();

        // Create client from the mock server
        client = server.createClient();
        String mockServerUrl = client.getConfiguration().getMasterUrl();

        // Ensure the Fabric8 client picks up the mock server
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
        }
    }

    /**
     * Expose the Fabric8 Kubernetes mock server instance for advanced use in tests.
     */
    public KubernetesMockServer getServer() {
        return server;
    }

    /**
     * Get the Kubernetes client connected to the mock server.
     */
    public KubernetesClient getClient() {
        return client;
    }
}
