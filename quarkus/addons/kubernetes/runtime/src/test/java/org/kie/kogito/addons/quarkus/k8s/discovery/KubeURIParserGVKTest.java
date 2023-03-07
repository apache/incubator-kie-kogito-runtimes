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
package org.kie.kogito.addons.quarkus.k8s.discovery;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KubeURIParserGVKTest {

    @Test
    public void testValidGVK() {
        VanillaKubernetesResourceUri address = VanillaKubernetesResourceUri.parse("apps/v1/deployment/default/kogito-app-1");
        Assertions.assertEquals("apps/v1/deployment", address.getGvk().getGVK());
        Assertions.assertEquals("kogito-app-1", address.getResourceName());

        OpenShiftResourceUri url1 = OpenShiftResourceUri.parse("apps.openshift.io/v1/deploymentconfig/default/kogito-app-1");
        Assertions.assertEquals("apps.openshift.io/v1/deploymentconfig", url1.getGvk().getGVK());
        Assertions.assertEquals("kogito-app-1", url1.getResourceName());

        VanillaKubernetesResourceUri address2 = VanillaKubernetesResourceUri.parse("apps/v1/statefulset/namespace2/kogito-app-1");
        Assertions.assertEquals("apps/v1/statefulset", address2.getGvk().getGVK());
        Assertions.assertEquals("kogito-app-1", address2.getResourceName());

        VanillaKubernetesResourceUri address3 = VanillaKubernetesResourceUri.parse("apps/v1/statefulset/namespace2/kogito-app-1");
        Assertions.assertEquals("apps/v1/statefulset", address3.getGvk().getGVK());
        Assertions.assertEquals("kogito-app-1", address3.getResourceName());

        VanillaKubernetesResourceUri address4 = VanillaKubernetesResourceUri.parse("v1/Service/namespace2/kogito-app-1");
        Assertions.assertEquals("v1/service", address4.getGvk().getGVK());
        Assertions.assertEquals("kogito-app-1", address4.getResourceName());

        VanillaKubernetesResourceUri address5 = VanillaKubernetesResourceUri.parse("v1/service/namespace2/kogito-app-1");
        Assertions.assertEquals("v1/service", address5.getGvk().getGVK());
        Assertions.assertEquals("kogito-app-1", address5.getResourceName());

        VanillaKubernetesResourceUri address6 = VanillaKubernetesResourceUri.parse("route.openshift.io/v1/route/namespace10/kogito-app-1");
        Assertions.assertEquals("route.openshift.io/v1/route", address6.getGvk().getGVK());
        Assertions.assertEquals("kogito-app-1", address6.getResourceName());

        VanillaKubernetesResourceUri address7 = VanillaKubernetesResourceUri.parse("networking.k8s.io/v1/ingress/namespace9/kogito-app-1");
        Assertions.assertEquals("networking.k8s.io/v1/ingress", address7.getGvk().getGVK());
        Assertions.assertEquals("kogito-app-1", address7.getResourceName());

        VanillaKubernetesResourceUri address8 = VanillaKubernetesResourceUri.parse("v1/pod/namespace9/kogito-app-1");
        Assertions.assertEquals("v1/pod", address8.getGvk().getGVK());
        Assertions.assertEquals("kogito-app-1", address8.getResourceName());

        OpenShiftResourceUri url9 = OpenShiftResourceUri.parse("apps.openshift.io/v1/deploymentConfig/default/kogito-app-1");
        Assertions.assertEquals("apps.openshift.io/v1/deploymentconfig", url9.getGvk().getGVK());
        Assertions.assertEquals("kogito-app-1", url9.getResourceName());

        OpenShiftResourceUri url10 = OpenShiftResourceUri.parse("serving.knative.dev/v1/service/default/knative-app-1");
        Assertions.assertEquals("serving.knative.dev/v1/service", url10.getGvk().getGVK());
        Assertions.assertEquals("knative-app-1", url10.getResourceName());
    }

    @Test
    public void testInvalidGVK() {
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            VanillaKubernetesResourceUri.parse("authorization.openshift.io/v1/roleBinding/default/kogito-app-3");
        });

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            VanillaKubernetesResourceUri.parse("v40/invalid/deployment/default/kogito-app-3");
        });

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            VanillaKubernetesResourceUri.parse("authorization.openshift.io/v1/roleBinding/kogito-app-3");
        });

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            OpenShiftResourceUri.parse("apps.openshift.io/v1/deploymentconfigs/default/kogito-app2");
        });
    }

    @Test
    public void testValidGVKWithNoNamespace() {
        VanillaKubernetesResourceUri url = VanillaKubernetesResourceUri.parse("apps/v1/deployment/kogito-app-1");
        Assertions.assertEquals("apps/v1/deployment", url.getGvk().getGVK());
        Assertions.assertEquals("kogito-app-1", url.getResourceName());

        VanillaKubernetesResourceUri url1 = VanillaKubernetesResourceUri.parse("v1/Service/kogito-app-2");
        Assertions.assertEquals("v1/service", url1.getGvk().getGVK());
        Assertions.assertEquals("kogito-app-2", url1.getResourceName());

        OpenShiftResourceUri url2 = OpenShiftResourceUri.parse("v1/Service/kogito-app-2");
        Assertions.assertEquals("v1/service", url2.getGvk().getGVK());
        Assertions.assertEquals("kogito-app-2", url2.getResourceName());

        OpenShiftResourceUri url3 = OpenShiftResourceUri.parse("apps.openshift.io/v1/deploymentconfig/kogito-app-3");
        Assertions.assertEquals("apps.openshift.io/v1/deploymentconfig", url3.getGvk().getGVK());
        Assertions.assertEquals("kogito-app-3", url3.getResourceName());

    }

    @Test
    public void testEmptyGVKValues() {
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            VanillaKubernetesResourceUri.parse("kubernetes:roleBinding/kogito-app-3");
        });

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            OpenShiftResourceUri.parse("apps.openshift.io/deploymentconfig/kogito-app-3");
        });
    }
}
