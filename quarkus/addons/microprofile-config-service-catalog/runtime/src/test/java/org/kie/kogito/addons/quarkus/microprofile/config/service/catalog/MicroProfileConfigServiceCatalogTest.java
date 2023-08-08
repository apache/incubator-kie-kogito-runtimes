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
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.addons.k8s.resource.catalog.KubernetesServiceCatalogTest;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.quarkus.test.junit.QuarkusTest;

import static org.kie.kogito.addons.k8s.resource.catalog.KubernetesProtocol.KNATIVE;
import static org.kie.kogito.addons.k8s.resource.catalog.KubernetesProtocol.KUBERNETES;
import static org.kie.kogito.addons.k8s.resource.catalog.KubernetesProtocol.OPENSHIFT;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@ExtendWith(MockitoExtension.class)
public class MicroProfileConfigServiceCatalogTest extends KubernetesServiceCatalogTest {

    private static final String KNATIVE_URI = "http://serverless-workflow-greeting-quarkus.test.10.99.154.147.sslip.io";
    private static final String KUBERNETES_URI = "http://serverless-workflow-greeting-quarkus-kubernetes.test.10.99.154.147.sslip.io";
    private static final String OPENSHIFT_URI = "http://serverless-workflow-greeting-quarkus-openshift.test.10.99.154.147.sslip.io";

    @Inject
    private Config config;

    @BeforeEach
    void setUp() {
        config = mock(Config.class);
        try (MockedStatic<ConfigProvider> mockConfigProvider = Mockito.mockStatic(ConfigProvider.class)) {
            mockConfigProvider.when(ConfigProvider::getConfig).thenReturn(config);
            when(config.getValue(eq(KNATIVE + ":" + getNamespace() + "/" + getKnativeServiceName()), eq(String.class)))
                    .thenReturn(KNATIVE_URI);
            when(config.getValue(eq(KUBERNETES + ":" + getNamespace() + "/" + getKubernetesServiceName()), eq(String.class)))
                    .thenReturn(KUBERNETES_URI);
            when(config.getValue(eq(OPENSHIFT + ":" + getNamespace() + "/" + getKubernetesServiceName()), eq(String.class)))
                    .thenReturn(OPENSHIFT_URI);

        }
    }

    @Inject
    MicroProfileConfigServiceCatalogTest(MicroProfileConfigServiceCatalog configServiceCatalog) {
        super(configServiceCatalog);
    }

}
