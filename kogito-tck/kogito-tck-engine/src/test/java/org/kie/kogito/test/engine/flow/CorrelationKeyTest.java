/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.test.engine.flow;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.asserts.ProcessAssertions;

import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

@KogitoUnitTestExtension
@KogitoUnitTestDeployment(
    resources = {@KogitoUnitTestResource(path = "org/kie/kogito/test/engine/flow/CorrelationKey.bpmn2")}
)
public class CorrelationKeyTest {

    private static final String PROCESS_ID = "org.jbpm.test.functional.CorrelationKey";
    private static final String SIMPLE_KEY = "mySimpleCorrelationKey";

    @Test
    public void testSimpleKey(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID, SIMPLE_KEY);
        ProcessAssertions.assertThat(instance).isActive();

        Optional<ProcessInstance<? extends Model>> instanceFound = context.findByBusinessKey(PROCESS_ID, SIMPLE_KEY);

        Assertions.assertThat(instanceFound).isPresent();
        Assertions.assertThat(instanceFound.get().id()).isEqualTo(instance.id());
    }

    @Test
    @Disabled
    public void testNotUniqueSimpleKey(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID, SIMPLE_KEY);
        ProcessAssertions.assertThat(instance).isActive();

        try {
            startProcess(context, PROCESS_ID, SIMPLE_KEY);
            Assertions.fail("Not unique correlation key used. Exception should have been thrown.");
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            Assertions.assertThat(ex.getMessage()).contains("already exists");
        }
    }


    @Test
    public void testGetNotExistingSimpleKey(KogitoUnitTestContext context) {
        Optional<ProcessInstance<? extends Model>> instanceFound = context.findByBusinessKey(PROCESS_ID, SIMPLE_KEY);
        Assertions.assertThat(instanceFound).isEmpty();
    }

}
