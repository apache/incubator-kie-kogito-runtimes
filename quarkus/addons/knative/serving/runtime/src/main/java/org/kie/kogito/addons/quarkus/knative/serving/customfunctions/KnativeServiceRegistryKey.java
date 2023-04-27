/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

import java.util.Objects;

import org.kie.kogito.addons.quarkus.k8s.discovery.VanillaKubernetesResourceUri;

final class KnativeServiceRegistryKey {

    private final String namespace;

    private final String serviceName;

    KnativeServiceRegistryKey(String namespace, String serviceName) {
        this.namespace = namespace;
        this.serviceName = serviceName;
    }

    String getNamespace() {
        return namespace;
    }

    String getServiceName() {
        return serviceName;
    }

    VanillaKubernetesResourceUri toVanillaKubernetesUri() {
        if (namespace == null) {
            return VanillaKubernetesResourceUri.parse(serviceName);
        } else {
            String[] splitServiceName = serviceName.split("/");
            return VanillaKubernetesResourceUri.parse(String.join("/", splitServiceName[0], namespace, splitServiceName[1]));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KnativeServiceRegistryKey knativeServiceRegistryKey = (KnativeServiceRegistryKey) o;
        return Objects.equals(namespace, knativeServiceRegistryKey.namespace) && Objects.equals(serviceName, knativeServiceRegistryKey.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, serviceName);
    }

    @Override
    public String toString() {
        return "KnativeServiceRegistryKey{" +
                "namespace='" + namespace + '\'' +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }
}
