package org.kie.kogito.addons.quarkus.k8s;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 *
 * @see <a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.22/#servicespec-v1-core">Kubernetes ServiceSpec API</a>
 */
@ApplicationScoped
public class KubernetesServiceEndpoint implements EndpointDiscovery {

    @Inject
    KubernetesClient kubernetesClient;

    @Override
    public Optional<Endpoint> findEndpoint(String namespace, String name) {
        final Service service = kubernetesClient.services().inNamespace(namespace).withName(name).get();
        if (service == null) {
            return Optional.empty();
        }

        return null;
    }

    @Override
    public Set<Endpoint> findEndpoint(Map<String, String> labels) {
        return null;
    }
}
