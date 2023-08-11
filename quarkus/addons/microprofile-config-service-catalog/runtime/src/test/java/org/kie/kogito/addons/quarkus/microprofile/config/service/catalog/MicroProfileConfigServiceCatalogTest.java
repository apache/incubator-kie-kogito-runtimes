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
package org.kie.kogito.addons.quarkus.microprofile.config.service.catalog;

import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Test;
import org.kie.kogito.addons.k8s.resource.catalog.KubernetesProtocol;
import org.kie.kogito.addons.k8s.resource.catalog.KubernetesServiceCatalogKey;
import org.kie.kogito.addons.k8s.resource.catalog.KubernetesServiceCatalogTest;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class MicroProfileConfigServiceCatalogTest extends KubernetesServiceCatalogTest {

    private static final String KUBERNETES_SERVICENAME = "serverless-workflow-greeting-quarkus-kubernetes";

    private static final String NAMESPACE = "test";

    private static final String NAMESPACE_KUBERNETES_SERVICENAME = NAMESPACE + '/' + KUBERNETES_SERVICENAME;

    @Inject
    MicroProfileConfigServiceCatalog configServiceCatalog;

    @Inject
    Config config;

    @Inject
    MicroProfileConfigServiceCatalogTest(MicroProfileConfigServiceCatalog configServiceCatalog) {
        super(configServiceCatalog);
    }

    @Test
    public void testGetServiceAddressWithInvalidURI() {
        KubernetesServiceCatalogKey key = new KubernetesServiceCatalogKey(KubernetesProtocol.KUBERNETES, NAMESPACE_KUBERNETES_SERVICENAME);

        assertThrows(RuntimeException.class, () -> configServiceCatalog.getServiceAddress(key));
    }

}
