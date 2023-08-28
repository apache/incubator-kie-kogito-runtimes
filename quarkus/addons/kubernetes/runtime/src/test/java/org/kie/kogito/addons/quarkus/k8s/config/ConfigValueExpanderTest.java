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
package org.kie.kogito.addons.quarkus.k8s.config;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.smallrye.config.ConfigValue;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigValueExpanderTest {

    @ParameterizedTest
    @ValueSource(strings = { "${kubernetes:pods.v1/kie/kogito}/path", "${openshift:pods.v1/kie/kogito}/path", "${knative:kie/kogito}/path" })
    void expandable(String expandableValues) {
        String expectedUrl = "https://localhost/8080";
        String expectedValue = expectedUrl + "/path";

        ConfigValueExpander expander = new ConfigValueExpander(new FakeKubeDiscoveryConfigCache(expectedUrl));

        ConfigValue configValue = ConfigValue.builder()
                .withRawValue(expandableValues)
                .build();

        assertThat(expander.expand(configValue).getValue())
                .isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://localhost/8080", "kubernetes:pods.v1/kie/kogito", "openshift:pods.v1/kie/kogito", "knative:kie/kogito" })
    void nonExpandable(String nonExpandableValue) {
        ConfigValueExpander expander = new ConfigValueExpander(new FakeKubeDiscoveryConfigCache("should not be returned"));

        ConfigValue configValue = ConfigValue.builder()
                .withRawValue(nonExpandableValue)
                .build();

        assertThat(expander.expand(configValue).getRawValue())
                .isEqualTo(nonExpandableValue);
    }

    @Test
    void nullShouldBeNonExpandable() {
        ConfigValueExpander expander = new ConfigValueExpander(new FakeKubeDiscoveryConfigCache("should not be returned"));

        ConfigValue configValue = ConfigValue.builder()
                .withRawValue(null)
                .build();

        assertThat(expander.expand(configValue).getValue())
                .isNull();
    }

    private static class FakeKubeDiscoveryConfigCache extends KubeDiscoveryConfigCache {

        private final String value;

        private FakeKubeDiscoveryConfigCache(String value) {
            super(null);
            this.value = value;
        }

        @Override
        Optional<String> get(String configName, String configValue) {
            return Optional.of(value);
        }
    }
}