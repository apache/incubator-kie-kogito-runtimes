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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.fabric8.kubernetes.client.KubernetesClient;

@ApplicationScoped
public class EndpointDiscoveryProducer {

    @Inject
    KubernetesClient kubernetesClient;

    @Produces
    @Singleton
    @Default
    @Named("default")
    public DefaultQuarkusEndpointDiscovery endpointDiscovery() {
        final QuarkusKubernetesServiceEndpointDiscovery kubernetesServiceEndpointDiscovery = new QuarkusKubernetesServiceEndpointDiscovery(kubernetesClient);
        final QuarkusKnativeRouteEndpointDiscovery knativeRouteEndpointDiscovery = new QuarkusKnativeRouteEndpointDiscovery(kubernetesClient);
        final DefaultQuarkusEndpointDiscovery defaultQuarkusEndpointDiscovery = new DefaultQuarkusEndpointDiscovery(kubernetesServiceEndpointDiscovery, knativeRouteEndpointDiscovery);
        return defaultQuarkusEndpointDiscovery;
    }
}
