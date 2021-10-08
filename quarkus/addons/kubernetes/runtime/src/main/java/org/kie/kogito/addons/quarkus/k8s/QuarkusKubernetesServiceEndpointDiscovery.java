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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.kie.kogito.addons.k8s.KubernetesServiceEndpointDiscovery;

import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * Performs the discovery operations on Kubernetes Core Services.
 *
 * @see <a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.22/#servicespec-v1-core">Kubernetes ServiceSpec API</a>
 */
@Singleton
public class QuarkusKubernetesServiceEndpointDiscovery extends KubernetesServiceEndpointDiscovery {

    @Inject
    KubernetesClient kubernetesClient;

    public QuarkusKubernetesServiceEndpointDiscovery(KubernetesClient kubernetesClient) {
        super(kubernetesClient);
    }
}
