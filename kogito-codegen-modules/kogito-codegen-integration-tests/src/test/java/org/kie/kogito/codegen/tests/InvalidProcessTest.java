/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen.tests;

import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.AbstractCodegenTest;
import org.kie.kogito.codegen.process.ProcessCodegenException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class InvalidProcessTest extends AbstractCodegenTest {

    @Test
    public void testBasicUserTaskProcess() {
        assertThrows(IllegalArgumentException.class,
                     () -> generateCodeProcessesOnly("invalid/invalid-process-id.bpmn2"),
                     "Process id '_7063C749-BCA8-4B6D-BC31-ACEE6FDF5512' is not valid");
    }

    @Test
    public void testDuplicatedProcessId() {
        assertThrows(ProcessCodegenException.class,
                     () -> generateCodeProcessesOnly("invalid/duplicated-process-id-1.bpmn2", "invalid/duplicated-process-id-2.bpmn2"),
                     "Duplicated process with id duplicated found in the project, please review .bpmn files");
    }
}
