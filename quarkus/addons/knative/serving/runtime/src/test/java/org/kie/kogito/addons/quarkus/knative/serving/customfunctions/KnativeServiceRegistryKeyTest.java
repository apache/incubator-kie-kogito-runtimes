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

import org.junit.jupiter.api.Test;
import org.kie.kogito.addons.quarkus.k8s.discovery.GVK;
import org.kie.kogito.addons.quarkus.k8s.discovery.VanillaKubernetesResourceUri;

import static org.assertj.core.api.Assertions.assertThat;

class KnativeServiceRegistryKeyTest {

    @Test
    void toVanillaKubernetesUriWithNamespace() {
        String gvk = GVK.KNATIVE_SERVICE.getValue();
        String service = "service";
        String namespace = "namespace";

        assertThat(new KnativeServiceRegistryKey(namespace, gvk + '/' + service).toVanillaKubernetesUri())
                .isEqualTo(VanillaKubernetesResourceUri.parse(gvk + '/' + namespace + '/' + service));
    }

    @Test
    void toVanillaKubernetesUriWithoutNamespace() {
        String gvk = GVK.KNATIVE_SERVICE.getValue();
        String service = "service";

        assertThat(new KnativeServiceRegistryKey(null, gvk + '/' + service).toVanillaKubernetesUri())
                .isEqualTo(VanillaKubernetesResourceUri.parse(gvk + '/' + service));
    }

    @Test
    void testEquals() {
        KnativeServiceRegistryKey key1 = new KnativeServiceRegistryKey("namespace", "serviceName");

        KnativeServiceRegistryKey equal = new KnativeServiceRegistryKey("namespace", "serviceName");

        KnativeServiceRegistryKey notEqual1 = new KnativeServiceRegistryKey(null, "serviceName");
        KnativeServiceRegistryKey notEqual2 = new KnativeServiceRegistryKey(null, "otherServiceName");
        KnativeServiceRegistryKey notEqual3 = new KnativeServiceRegistryKey("namespace", "otherServiceName");
        KnativeServiceRegistryKey notEqual4 = new KnativeServiceRegistryKey("otherNamespace", "otherServiceName");

        assertThat(key1)
                .isEqualTo(equal)
                .isNotEqualTo(notEqual1)
                .isNotEqualTo(notEqual2)
                .isNotEqualTo(notEqual3)
                .isNotEqualTo(notEqual4);
    }
}
