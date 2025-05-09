package org.kie.kogito.addons.quarkus.k8s.test.utils;

import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class KubernetesMockServerTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String TEST_NAMESPACE = "serverless-workflow-greeting-quarkus";
    private final KubernetesServer server = new KubernetesServer(false, true); // https enabled, crud mode enabled

    @Override
    public Map<String, String> start() {
        server.before(); // Start mock server

        String mockServerUrl = server.getClient().getConfiguration().getMasterUrl();

        // Make sure system property is also set for Fabric8 clients
        System.setProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY, mockServerUrl);

        Map<String, String> config = new HashMap<>();
        config.put("quarkus.kubernetes-client.master-url", mockServerUrl);
        config.put("quarkus.kubernetes-client.namespace", TEST_NAMESPACE);
        config.put("quarkus.kubernetes-client.trust-certs", "true");
        return config;
    }

    @Override
    public void stop() {
        server.after(); // Stop mock server
    }

    public KubernetesServer getServer() {
        return server;
    }
}
