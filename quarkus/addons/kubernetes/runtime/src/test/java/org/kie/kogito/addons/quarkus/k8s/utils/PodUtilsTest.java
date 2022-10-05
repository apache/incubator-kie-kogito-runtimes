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
package org.kie.kogito.addons.quarkus.k8s.utils;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.kie.kogito.addons.quarkus.k8s.KubeResourceDiscovery;
import org.kie.kogito.addons.quarkus.k8s.parser.KubeURI;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This tests also covers the queryServiceByLabelOrSelector method from {@link ServiceUtils}
 */
@QuarkusTest
@WithKubernetesTestServer
public class PodUtilsTest {

    @KubernetesTestServer
    KubernetesServer mockServer;
    KubeResourceDiscovery kubeResourceDiscovery;
    private final String namespace = "serverless-workflow-greeting-quarkus";

    @Test
    public void testPodNotFound() {
        kubeResourceDiscovery = new KubeResourceDiscovery(mockServer.getClient());
        Pod pod = mockServer.getClient().pods().inNamespace(namespace)
                .load(this.getClass().getClassLoader().getResourceAsStream("pod/pod-no-service.yaml")).get();
        pod.getMetadata().setName("test-pod");
        mockServer.getClient().resource(pod).inNamespace(namespace).createOrReplace();
        assertEquals(Optional.empty(),
                kubeResourceDiscovery.query(new KubeURI("kubernetes:v1/pod/" + namespace + "/hello")));
    }

    @Test
    public void testPodWithNoService() {
        kubeResourceDiscovery = new KubeResourceDiscovery(mockServer.getClient());
        KubeURI kubeURI = new KubeURI("kubernetes:v1/pod/" + namespace + "/process-quarkus-example-pod-no-service");

        Pod pod = mockServer.getClient().pods().inNamespace(namespace)
                .load(this.getClass().getClassLoader().getResourceAsStream("pod/pod-no-service.yaml")).get();
        mockServer.getClient().resource(pod).inNamespace(namespace).createOrReplace();

        Optional<String> url = kubeResourceDiscovery.query(kubeURI);
        assertEquals("http://172.17.0.21:8080", url.get());
    }

    @Test
    public void testPodWithNoServiceCustomPortName() {
        kubeResourceDiscovery = new KubeResourceDiscovery(mockServer.getClient());
        KubeURI kubeURI = new KubeURI("kubernetes:v1/pod/" + namespace + "/pod-no-service-custom-port?port-name=my-custom-port");

        Pod pod = mockServer.getClient().pods().inNamespace(namespace)
                .load(this.getClass().getClassLoader().getResourceAsStream("pod/pod-no-service-custom-port-name.yaml")).get();
        mockServer.getClient().resource(pod).inNamespace(namespace).createOrReplace();

        Optional<String> url = kubeResourceDiscovery.query(kubeURI);
        assertEquals("http://172.17.0.22:52485", url.get());
    }

    @Test
    public void testPodWithService() {
        kubeResourceDiscovery = new KubeResourceDiscovery(mockServer.getClient());
        KubeURI kubeURI = new KubeURI("kubernetes:v1/pod/" + namespace + "/test-pod-with-service");

        Pod pod = mockServer.getClient().pods().inNamespace(namespace)
                .load(this.getClass().getClassLoader().getResourceAsStream("pod/pod-no-service.yaml")).get();
        pod.getMetadata().setName("test-pod-with-service");
        mockServer.getClient().resource(pod).inNamespace(namespace).createOrReplace();

        Service service = mockServer.getClient().services().inNamespace(namespace)
                .load(this.getClass().getClassLoader().getResourceAsStream("service/service-clusterip.yaml")).get();

        mockServer.getClient().resource(service).inNamespace(namespace).createOrReplace();

        Optional<String> url = kubeResourceDiscovery.query(kubeURI);
        assertEquals("http://10.10.10.10:80", url.get());
    }

    @Test
    public void testPodWithServiceWithCustomLabel() {
        kubeResourceDiscovery = new KubeResourceDiscovery(mockServer.getClient());
        KubeURI kubeURI = new KubeURI("kubernetes:v1/pod/" + namespace + "/test-pod-with-service-custom-label?labels=label-name=test-label;other-label=other-value");

        Pod pod = mockServer.getClient().pods().inNamespace(namespace)
                .load(this.getClass().getClassLoader().getResourceAsStream("pod/pod-no-service.yaml")).get();
        pod.getMetadata().setName("test-pod-with-service-custom-label");
        mockServer.getClient().resource(pod).inNamespace(namespace).createOrReplace();

        Service service = mockServer.getClient().services().inNamespace(namespace)
                .load(this.getClass().getClassLoader().getResourceAsStream("service/service-clusterip.yaml")).get();
        service.getMetadata().setName(" process-quarkus-example-pod-clusterip-svc-custom-label");
        mockServer.getClient().resource(service).inNamespace(namespace).createOrReplace();

        Service service1 = mockServer.getClient().services().inNamespace(namespace)
                .load(this.getClass().getClassLoader().getResourceAsStream("service/service-clusterip.yaml")).get();
        Map<String, String> labels = service1.getMetadata().getLabels();
        labels.put("label-name", "test-label");
        service1.getMetadata().setLabels(labels);
        service1.getMetadata().setName("second-service");
        service1.getSpec().setClusterIP("20.20.20.20");
        mockServer.getClient().resource(service1).inNamespace(namespace).createOrReplace();

        Optional<String> url = kubeResourceDiscovery.query(kubeURI);
        assertEquals("http://20.20.20.20:80", url.get());
    }
}
