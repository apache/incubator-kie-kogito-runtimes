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

import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.Sig;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.listeners.FlowProcessEventListenerTracker;

import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;


@KogitoUnitTestExtension
@KogitoUnitTestDeployment(
    resources = {@KogitoUnitTestResource(path = EventListenersTest.PROCESS)},
    listeners = {FlowProcessEventListenerTracker.class}
)
public class EventListenersTest {

    public static final String PROCESS = "org/kie/kogito/test/engine/flow/EventListeners.bpmn2";
    public static final String PROCESS_ID = "org.jbpm.test.functional.EventListeners";


    @Test
    public void testClearExecution(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);

        FlowProcessEventListenerTracker tracker = context.find(FlowProcessEventListenerTracker.class);
        instance.send(Sig.of("other-branch", "hello world!"));
        assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .step("introduction")
            .notStep("script-warning")
            .varChanged("signalData")
            .varNotChanged("stringVariable")
            .completed();

    }

    @Test
    public void testUnfinishedProcess(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);

        FlowProcessEventListenerTracker tracker = context.find(FlowProcessEventListenerTracker.class);

        assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .step("introduction")
            .step("split")
            .notStep("xor-gateway");
        
        assertThat(instance).isActive();

    }

    @Test
    public void testBadSignal(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);

        FlowProcessEventListenerTracker tracker = context.find(FlowProcessEventListenerTracker.class);
        instance.send(Sig.of("bad-signal", "bad signal!"));

        assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .started()
            .step("introduction")
            .notStep("info")
            .step("script-warning")
            .varNotChanged("signalData")
            .varNotChanged("stringVariable")
            .completed();

    }

}
