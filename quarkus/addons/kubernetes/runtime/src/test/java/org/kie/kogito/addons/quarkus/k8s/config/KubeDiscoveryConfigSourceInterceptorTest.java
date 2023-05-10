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
package org.kie.kogito.addons.quarkus.k8s.config;

import java.text.MessageFormat;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kie.kogito.addons.k8s.resource.catalog.KubernetesServiceCatalog;
import org.kie.kogito.addons.k8s.resource.catalog.KubernetesServiceCatalogProvider;
import org.kie.kogito.addons.quarkus.k8s.resource.catalog.DefaultKubernetesServiceCatalogProvider;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.kie.kogito.addons.quarkus.k8s.config.KubeDiscoveryConfigSourceInterceptor.MULTIPLE_PROVIDERS_FOUND_MSG;

class KubeDiscoveryConfigSourceInterceptorTest {

    @Test
    void shouldNotThrowExceptionWhenOneProviderIsPresent() {
        assertThatCode(() -> KubeDiscoveryConfigSourceInterceptor.createKubernetesServiceCatalog(List.of(new DefaultKubernetesServiceCatalogProvider())))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldNotThrowExceptionWhenNoneProviderIsPresent() {
        assertThatCode(() -> KubeDiscoveryConfigSourceInterceptor.createKubernetesServiceCatalog(List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenMoreThanOneProviderIsPresent() {
        List<KubernetesServiceCatalogProvider> providers = List.of(
                new DefaultKubernetesServiceCatalogProvider(), new AnotherKubernetesServiceCatalogProvider());

        String providersName = providers.stream()
                .map(provider -> provider.getClass().getName())
                .collect(joining(", ", "[", "]"));

        String expectedMessage = MessageFormat.format(MULTIPLE_PROVIDERS_FOUND_MSG, KubernetesServiceCatalogProvider.class,
                providersName);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> KubeDiscoveryConfigSourceInterceptor.createKubernetesServiceCatalog(providers))
                .withMessage(expectedMessage);
    }

    private static class AnotherKubernetesServiceCatalogProvider implements KubernetesServiceCatalogProvider {

        @Override
        public KubernetesServiceCatalog create() {
            throw new UnsupportedOperationException("This method is not supposed to be called.");
        }
    }
}