package org.kie.kogito.addons.quarkus.k8s;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Entry point interface for the {@link Endpoint} discovery engine.
 */
public interface EndpointDiscovery {

    /**
     * Finds an endpoint by Kubernetes Namespace and Name
     *
     * @param namespace k8s namespace
     * @param name k8s name
     * @return an {@link Optional} endpoint
     */
    Optional<Endpoint> findEndpoint(String namespace, String name);

    /**
     * Finds an endpoint by its labels. Implementations should define the target object. For example a Service or a Knative Service.
     *
     * @param labels map containing the labels of the object.
     * @return a {@link Set} of discovered endpoints. Kubernetes objects can have the same label. The caller should know how to distinguish the endpoint.
     */
    Set<Endpoint> findEndpoint(Map<String, String> labels);

}
