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
package org.kie.kogito.process.impl;

import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.codegen.AbstractCodegenIT;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.Processes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.kie.kogito.process.impl.ProcessTestUtils.assertState;

class BusinessKeyIT extends AbstractCodegenIT {

    @Test
    void testBusinessKey() throws Exception {
        String businessKey = "some business key";
        Application app = generateCodeProcessesOnly("cases/ActivationAdHoc.bpmn");
        assertThat(app).isNotNull();

        Process p = app.get(Processes.class).processById("TestCase.ActivationAdHoc");
        ProcessInstance processInstance = p.createInstance(businessKey, p.createModel());
        assertState(processInstance, ProcessInstance.STATE_PENDING);
        assertEquals(businessKey, processInstance.businessKey());
    }
}
