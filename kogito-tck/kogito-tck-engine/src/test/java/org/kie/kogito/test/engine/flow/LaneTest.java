/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kie.kogito.test.engine.flow;

import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.asserts.ProcessAssertions;
import org.kie.kogito.tck.junit.listeners.FlowProcessEventListenerTracker;

import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

/**
 * Simple testing of lanes - there is nothing special to test.
 */
@KogitoUnitTestExtension
public class LaneTest  {

    public static final String PROCESS = "org/kie/kogito/test/engine/flow/Lane.bpmn2";
    public static final String PROCESS_ID = "org.jbpm.test.functional.Lane";


    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = PROCESS)},
        listeners = {FlowProcessEventListenerTracker.class}
    )
    public void testLane(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);

        FlowProcessEventListenerTracker tracker = context.find(FlowProcessEventListenerTracker.class);
        ProcessAssertions.assertThat(tracker)
            .checkStepsForProcessInstance(instance.id())
            .started()
            .step("start")
            .entered("fork")
            .exited("fork")
            .step("scriptTask2")
            .step("end2")
            .exited("fork")
            .step("scriptTask1")
            .step("end1")
            .completed();
    }

}
