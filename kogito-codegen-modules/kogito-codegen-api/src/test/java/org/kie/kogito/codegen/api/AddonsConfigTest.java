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
package org.kie.kogito.codegen.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.codegen.api.AddonsConfig.DEFAULT;
import static org.kie.kogito.codegen.api.AddonsConfig.builder;

public class AddonsConfigTest {

    @Test
    public void allAddonsAreDisabledInDefaultConfiguration() {
        AddonsConfig addonsConfig = DEFAULT;
        assertThat(addonsConfig.useMonitoring()).isFalse();
        assertThat(addonsConfig.useTracing()).isFalse();
        assertThat(addonsConfig.usePersistence()).isFalse();
        assertThat(addonsConfig.useCloudEvents()).isFalse();
        assertThat(addonsConfig.usePersistence()).isFalse();
        assertThat(addonsConfig.useCloudEvents()).isFalse();
    }

    @Test
    public void addonsAreProperlyActivated() {
        ;
        assertThat(DEFAULT.useMonitoring()).isFalse();
        assertThat(builder().withMonitoring(true).build().useMonitoring()).isTrue();

        assertThat(DEFAULT.usePrometheusMonitoring()).isFalse();
        assertThat(builder().withPrometheusMonitoring(true).build().usePrometheusMonitoring()).isTrue();

        assertThat(DEFAULT.useTracing()).isFalse();
        assertThat(builder().withTracing(true).build().useTracing()).isTrue();

        assertThat(DEFAULT.usePersistence()).isFalse();
        assertThat(builder().withPersistence(true).build().usePersistence()).isTrue();

        assertThat(DEFAULT.useKnativeEventing()).isFalse();
        assertThat(builder().withKnativeEventing(true).build().useKnativeEventing()).isTrue();

        assertThat(DEFAULT.useCloudEvents()).isFalse();
        assertThat(builder().withCloudEvents(true).build().useCloudEvents()).isTrue();

        assertThat(DEFAULT.useExplainability()).isFalse();
        assertThat(builder().withExplainability(true).build().useExplainability()).isTrue();
    }
}
