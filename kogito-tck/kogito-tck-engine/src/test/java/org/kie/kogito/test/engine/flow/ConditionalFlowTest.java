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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestEnvironment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestEnvironmentProperty;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestProcessDebug;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.listeners.TrackingProcessEventListener;

import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

/**
 * basic compilation test framework and start process
 */
@KogitoUnitTestExtension
@KogitoUnitTestEnvironment(entries =
    @KogitoUnitTestEnvironmentProperty(name = "jbpm.enable.multi.con", value = "true")
)
public class ConditionalFlowTest {

    private static final String PROCESS_ID = "org.jbpm.test.functional.ConditionalFlow";


    @Test
    @Disabled
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = "org/kie/kogito/test/engine/flow/ConditionalFlow.bpmn2")},
        listeners = {TrackingProcessEventListener.class}
    )
    @KogitoUnitTestProcessDebug
    public void testConditionalFlow(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);

        TrackingProcessEventListener tracker = context.find(TrackingProcessEventListener.class);
        assertThat(instance).isCompleted();
        assertThat(instance.variables()).output("x").isEqualTo(5);

        assertThat(tracker)
            .checkStepsForProcessInstance(instance.id())
            .started()
            .step("start")
            .entered("script")
            .varAssert("x", (x) -> x.equals(5))
            .exited("script")
            .step("end1")
            .completed();

    }



}
