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
package org.kie.kogito.addons.quarkus.knative.serving.customfunctions;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.kogito.addons.quarkus.k8s.discovery.GVK;
import org.kie.kogito.addons.quarkus.k8s.discovery.KnativeServiceDiscovery;
import org.kie.kogito.addons.quarkus.k8s.discovery.KnativeServiceUri;
import org.kie.kogito.addons.quarkus.k8s.discovery.VanillaKubernetesResourceDiscovery;

@ApplicationScoped
final class KnativeServiceRegistry {

    private final Map<KnativeServiceRegistryKey, URI> services = new ConcurrentHashMap<>();

    private final KnativeServiceDiscovery knativeServiceDiscovery;

    private final VanillaKubernetesResourceDiscovery vanillaKubernetesResourceDiscovery;

    @Inject
    KnativeServiceRegistry(KnativeServiceDiscovery knativeServiceDiscovery,
            VanillaKubernetesResourceDiscovery vanillaKubernetesResourceDiscovery) {
        this.knativeServiceDiscovery = knativeServiceDiscovery;
        this.vanillaKubernetesResourceDiscovery = vanillaKubernetesResourceDiscovery;
    }

    Optional<URI> getServiceAddress(KnativeServiceRegistryKey key) {
        String[] splitServiceName = key.getServiceName().split("/");
        final Function<KnativeServiceRegistryKey, Optional<URI>> function;
        switch (splitServiceName.length) {
            case 1:
                function = k -> knativeServiceDiscovery.query(new KnativeServiceUri(k.getNamespace(), k.getServiceName()));
                break;
            case 2:
                if (GVK.isValid(splitServiceName[0])) {
                    function = k -> vanillaKubernetesResourceDiscovery.query(k.toVanillaKubernetesUri());
                } else {
                    function = k -> knativeServiceDiscovery.query(new KnativeServiceUri(splitServiceName[0], splitServiceName[1]));
                }
                break;
            default:
                function = k -> vanillaKubernetesResourceDiscovery.query(k.toVanillaKubernetesUri());
        }
        return Optional.ofNullable(services.computeIfAbsent(key, k -> function.apply(k).orElse(null)));
    }
}
