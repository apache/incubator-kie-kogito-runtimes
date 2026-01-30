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

import java.util.Map;

import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * Quarkus 3.27.2 upgrade: Updated for Fabric8 Kubernetes Client 7.x API changes:
 * - OpenShiftServer replaced with OpenShiftMockServer
 * - before()/after() replaced with init()/destroy()
 * - Client creation via createOpenShiftClient() and exposed via getClient() for test injection
 * - Tests now use @QuarkusTestResource instead of @WithKubernetesTestServer/@KubernetesTestServer
 */
public class OpenShiftMockServerTestResource implements QuarkusTestResourceLifecycleManager {

    private OpenShiftMockServer server;
    private OpenShiftClient client;

    @Override
    public Map<String, String> start() {
        // Create and start the OpenShift mock server (no-arg constructor for Fabric8 7.x)
        server = new OpenShiftMockServer();
        server.init();

        // Create client from the mock server
        client = server.createOpenShiftClient();

        return Map.of(
                "quarkus.kubernetes-client.master-url", client.getMasterUrl().toString(),
                "quarkus.kubernetes-client.trust-certs", "true");
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

    public OpenShiftMockServer getServer() {
        return server;
    }

    public OpenShiftClient getClient() {
        return client;
    }
}
