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
package org.kie.kogito.serverless.workflow.operationid;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowOperationIdFactoryProviderTest {

    @Test
    void testFactories() {
        assertThat(WorkflowOperationIdFactoryProvider.getFactory(Optional.of("full_uri"))).isEqualTo(WorkflowOperationIdFactoryType.FULL_URI.factory());
        assertThat(WorkflowOperationIdFactoryProvider.getFactory(Optional.of("spec_title"))).isEqualTo(WorkflowOperationIdFactoryType.SPEC_TITLE.factory());
        assertThat(WorkflowOperationIdFactoryProvider.getFactory(Optional.of("function_name"))).isEqualTo(WorkflowOperationIdFactoryType.FUNCTION_NAME.factory());
        assertThat(WorkflowOperationIdFactoryProvider.getFactory(Optional.of("file_name"))).isEqualTo(WorkflowOperationIdFactoryType.FILE_NAME.factory());
        assertThat(WorkflowOperationIdFactoryProvider.getFactory(Optional.empty())).isEqualTo(WorkflowOperationIdFactoryType.FILE_NAME.factory());
        assertThat(WorkflowOperationIdFactoryProvider.getFactory(Optional.of("random"))).isEqualTo(WorkflowOperationIdFactoryType.FILE_NAME.factory());
    }
}
