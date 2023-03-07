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

import org.junit.jupiter.api.Test;
import org.kie.kogito.addons.quarkus.k8s.KubeConstants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class KubernetesProtocolTest {

    @Test
    void parseVanillaKubernetes() {
        assertThat(KubernetesProtocol.parse("kubernetes:whatever"))
                .isEqualTo(KubernetesProtocol.VANILLA_KUBERNETES);
    }

    @Test
    void parseOpenShift() {
        assertThat(KubernetesProtocol.parse("openshift:whatever"))
                .isEqualTo(KubernetesProtocol.OPENSHIFT);
    }

    @Test
    void parseKnative() {
        assertThat(KubernetesProtocol.parse("knative:whatever"))
                .isEqualTo(KubernetesProtocol.KNATIVE);
    }

    @Test
    void parseInvalidProtocol() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KubernetesProtocol.parse("nonexistent_protocol:whatever"))
                .withMessage("The provided protocol [nonexistent_protocol] is not " +
                        "supported, supported values are " +
                        KubeConstants.SUPPORTED_PROTOCOLS);
    }

    @Test
    void parseInvalidUri() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KubernetesProtocol.parse("invalid_uri"))
                .withMessage("The provided uri [invalid_uri] doesn't have a defined protocol");
    }
}