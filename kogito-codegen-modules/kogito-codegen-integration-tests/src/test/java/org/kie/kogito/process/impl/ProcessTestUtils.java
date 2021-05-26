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

import org.kie.kogito.process.ProcessInstance;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessTestUtils {

    public static void assertState(ProcessInstance processInstance, int state) {
        assertThat(processInstance).isInstanceOf(AbstractProcessInstance.class);
        AbstractProcessInstance abstractProcessInstance = (AbstractProcessInstance) processInstance;
        assertThat(abstractProcessInstance.status)
                .withFailMessage("ProcessInstance [%s] Status - Expected: %s - Got: %s", processInstance.id(), state, processInstance.status())
                .isEqualTo(state);
        assertThat(abstractProcessInstance.processInstance.getState())
                .withFailMessage(
                        "LegacyProcessInstance [%s] Status - Expected: %s - Got: %s",
                        processInstance.id(), state, ((AbstractProcessInstance) processInstance).processInstance.getState())
                .isEqualTo(state);
    }

}
