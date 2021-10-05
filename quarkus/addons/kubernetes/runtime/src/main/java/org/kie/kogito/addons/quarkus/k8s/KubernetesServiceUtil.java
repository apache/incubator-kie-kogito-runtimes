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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;

/**
 * Collection of utilities for Kubernetes Service API integration
 */
public class KubernetesServiceUtil {

    public static final String SECURE_HTTP_PROTOCOL = "https";
    public static final String NONSECURE_HTTP_PROTOCOL = "http";

    private static final Map<Integer, String> STANDARD_PORTS = new HashMap<>() {{
        put(80, NONSECURE_HTTP_PROTOCOL);
        put(8080, NONSECURE_HTTP_PROTOCOL);
        put(8443, SECURE_HTTP_PROTOCOL);
        put(443, SECURE_HTTP_PROTOCOL);
    }};
    /**
     * Protocol, Host, Port
     */
    private static final String FORMAT_SVC_URL = "%s://%s:%s";
    private static final String CLUSTER_TYPE_NONE = "None";

    /**
     * Builds a collection of URLs based on the given {@link Service}.
     * <p/>
     * This method is biased by standard ports, meaning that if the service has exposed one of those (80, 443, and etc.), this is the URL that will be generated.
     * Ports on unconventional ports such as 8775 will have its URL generated only if standards ports are not found.
     * <p/>
     * This is done to facilitate the service call later in the future.
     * <p/>
     * TODO: users should be able to define the target port or at least the behaviour of this use case.
     *
     * @param service the given service to generate the URL
     * @return the collection of URLs created for standard ports
     * @see #STANDARD_PORTS
     */
    public static List<String> buildURLsForStandardPorts(final Service service) {
        final List<String> urls = new ArrayList<>();
        if (service == null || isClusterIPEmpty(service.getSpec())) {
            return urls;
        }
        // prefer the standard ports
        urls.addAll(service.getSpec().getPorts()
                            .stream()
                            .filter(p -> STANDARD_PORTS.containsKey(p.getPort()))
                            .map(p -> String.format(FORMAT_SVC_URL, NONSECURE_HTTP_PROTOCOL, service.getSpec().getClusterIP(), p.getPort()))
                            .collect(Collectors.toList()));
        if (urls.isEmpty()) {
            urls.addAll(
                    service.getSpec().getPorts()
                            .stream()
                            .map(p -> String.format(FORMAT_SVC_URL, NONSECURE_HTTP_PROTOCOL, service.getSpec().getClusterIP(), p.getPort()))
                            .collect(Collectors.toList()));
        }
        return urls;
    }

    /**
     * Verifies if ClusterIP is valid.
     *
     * @see <a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.22/#servicespec-v1-core">Service Spec ClusterIP</a>
     */
    private static boolean isClusterIPEmpty(final ServiceSpec serviceSpec) {
        return serviceSpec.getClusterIP() == null ||
                serviceSpec.getClusterIP().isEmpty() ||
                CLUSTER_TYPE_NONE.equals(serviceSpec.getClusterIP());
    }
}
