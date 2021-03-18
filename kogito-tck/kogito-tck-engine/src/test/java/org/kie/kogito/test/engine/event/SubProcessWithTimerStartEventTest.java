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
package org.kie.kogito.test.engine.event;

import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.listeners.CompletedProcessEventListener;
import org.kie.kogito.tck.junit.listeners.FlowProcessEventListenerTracker;

import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

@KogitoUnitTestExtension
public class SubProcessWithTimerStartEventTest {

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = "org/kie/kogito/test/engine/event/SubProcessWithTimerStartEvent.bpmn2")},
        listeners = {FlowProcessEventListenerTracker.class, CompletedProcessEventListener.class}
    )
    public void simpleSupportProcessTest(KogitoUnitTestContext context) throws Exception {
        ProcessInstance<? extends Model> instance = startProcess(context, "SubProcessWithTimerStartEvent");

        FlowProcessEventListenerTracker tracker = context.find(FlowProcessEventListenerTracker.class);

        context.find(CompletedProcessEventListener.class).waitForCompletion();

        assertThat(instance).isActive();
        assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .notStep("ScriptTask");


    }
}
