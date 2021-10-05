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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.knative.serving.v1.Route;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs the discovery operations for Knative Routes
 *
 * @see <a href="https://rohaan.medium.com/accessing-knative-rest-api-using-fabric8-knative-client-443a16ac43f7">Accessing Knative REST API using Fabric8 Knative Client</a>
 * @see <a href="https://github.com/knative/specs/blob/main/specs/serving/knative-api-specification-1.0.md#route-2">Knative Service</a>
 */
@Singleton
public class KnativeRouteEndpointDiscovery implements EndpointDiscovery {

    @Inject
    KubernetesClient kubernetesClient;

    KnativeClient knativeClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(KnativeRouteEndpointDiscovery.class);

    @PostConstruct
    public void configureKnativeClient() {
        if (kubernetesClient.isAdaptable(KnativeClient.class)) {
            knativeClient = kubernetesClient.adapt(KnativeClient.class);
        } else {
            LOGGER.warn("Impossible to adapt Fabric8 Kubernetes Client to Knative Client. Discovery operations for Knative won't work");
        }
    }

    public void setKnativeClient(KnativeClient knativeClient) {
        this.knativeClient = knativeClient;
    }

    @Override
    public Optional<Endpoint> findEndpoint(String namespace, String name) {
        if (knativeClient == null) {
            LOGGER.debug("Knative Client unavailable, skipping Knative Endpoints discovery");
            return Optional.empty();
        }
        final Route route = knativeClient.routes().inNamespace(namespace).withName(name).get();
        return Optional.of(new Endpoint(route.getStatus().getUrl()));
    }

    @Override
    public List<Endpoint> findEndpoint(String namespace, Map<String, String> labels) {
        if (knativeClient == null) {
            LOGGER.debug("Knative Client unavailable, skipping Knative Endpoints discovery");
            return Collections.emptyList();
        }
        final List<Route> routes = knativeClient.routes().inNamespace(namespace).withLabels(labels).list().getItems();
        final List<Endpoint> endpoints = new ArrayList<>();
        routes.forEach(r -> endpoints.add(new Endpoint(r.getStatus().getUrl())));
        return endpoints;
    }
}
