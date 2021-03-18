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

package org.kie.kogito.test.engine.subprocess;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeploymentException;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.listeners.FlowProcessEventListenerTracker;

import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

@KogitoUnitTestExtension
public class EmbeddedSubprocessTest {

    private static final String INVALID_SUBPROCESS =
            "org/kie/kogito/test/engine/subprocess/EmbeddedSubprocess-invalidSubprocess.bpmn2";
    public static final String TASK_COMPENSATION =
            "org/kie/kogito/test/engine/subprocess/EmbeddedSubprocess-taskCompensation.bpmn2";
    private static final String INVALID_SUBPROCESS2 =
            "org/kie/kogito/test/engine/subprocess/EmbeddedSubprocess-invalidSubprocess2.bpmn2";
    public static final String TERMINATING_END_EVENT =
            "org/kie/kogito/test/engine/subprocess/EmbeddedSubprocess-terminatingEndEvent.bpmn2";
    
    
    public static final String TERMINATING_END_EVENT_ID =
            "org.jbpm.test.regression.subprocess.EmbeddedSubprocess-terminatingEndEvent";
    public static final String TASK_COMPENSATION_ID =
            "org.jbpm.test.regression.subprocess.EmbeddedSubprocess-taskCompensation";

    @Test
    @Disabled
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = INVALID_SUBPROCESS)}
    )
    public void testInvalidSubprocess(KogitoUnitTestContext context) {
        Assertions.assertNotNull(context.find(Throwable.class));
    }

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = INVALID_SUBPROCESS2)}
    )
    @KogitoUnitTestDeploymentException
    public void testInvalidSubprocess2(KogitoUnitTestContext context) {
        Assertions.assertNotNull(context.find(Throwable.class));
    }

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = TERMINATING_END_EVENT)},
        listeners = {FlowProcessEventListenerTracker.class}
    )
    public void testTerminatingEndEvent(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, TERMINATING_END_EVENT_ID);

        FlowProcessEventListenerTracker tracker = context.find(FlowProcessEventListenerTracker.class);
        assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .step("main-script")
            .step("main-end");

    }

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = TASK_COMPENSATION)},
        listeners = {FlowProcessEventListenerTracker.class}
    )
    public void testTaskCompensation(KogitoUnitTestContext context) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("compensation", "True");

        ProcessInstance<? extends Model> instance = startProcess(context, TASK_COMPENSATION_ID, params);
        assertThat(instance.variables()).output("compensation").isEqualTo("compensation");

    }

}
