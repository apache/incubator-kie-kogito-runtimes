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
package org.kie.kogito.serverless.workflow.utils;

import org.junit.jupiter.api.Test;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;
import org.kie.kogito.serverless.workflow.test.MockBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;

public class KogitoProcessContextResolverTest {

    KogitoProcessContext context = MockBuilder.kogitoProcessContext()
            .withProcessInstanceMock(it -> {
                when(it.getId()).thenReturn("value-id");
                when(it.getProcessId()).thenReturn("value-process-id");
                when(it.getProcessName()).thenReturn("value-name");
            }).build();

    @Test
    void testGetInstanceId() {
        assertThat(KogitoProcessContextResolver.get().readKey(context, "instanceId")).isEqualTo("value-id");
    }

    @Test
    void testGetId() {
        assertThat(KogitoProcessContextResolver.get().readKey(context, "id")).isEqualTo("value-process-id");
    }

    @Test
    void testGetName() {
        assertThat(KogitoProcessContextResolver.get().readKey(context, "name")).isEqualTo("value-name");
    }

    @Test
    void testGetNonExistentKey() {
        assertThatIllegalArgumentException().isThrownBy(() -> KogitoProcessContextResolver.get().readKey(context, "nonexistent"));
    }
}
