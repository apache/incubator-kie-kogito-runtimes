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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.listeners.CompletedProcessEventListener;
import org.kie.kogito.tck.junit.listeners.FlowProcessEventListenerTracker;
import org.kie.kogito.tck.junit.listeners.ProcessEventListenerTracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

@KogitoUnitTestExtension
public class MultipleInstancesSubprocessTest {

    private static final String TIMER_EVENT_PARENT =
            "org/kie/kogito/test/engine/subprocess/MultipleInstancesSubprocess-timerEvent-parent.bpmn2";
    private static final String TIMER_EVENT_SUBPROCESS1 =
            "org/kie/kogito/test/engine/subprocess/MultipleInstancesSubprocess-timerEvent-subprocess1.bpmn2";
    private static final String TIMER_EVENT_SUBPROCESS2 =
            "org/kie/kogito/test/engine/subprocess/MultipleInstancesSubprocess-timerEvent-subprocess2.bpmn2";
    private static final String ENTRY_AND_EXIT_SCRIPT_SUBPROCESS =
            "org/kie/kogito/test/engine/subprocess/MultipleInstancesSubprocess-entryAndExitScript-subprocess.bpmn2";
    private static final String ENTRY_AND_EXIT_SCRIPT_PARENT =
            "org/kie/kogito/test/engine/subprocess/MultipleInstancesSubprocess-entryAndExitScript-parent.bpmn2";

    
    private static final String TIMER_EVENT_PARENT_ID =
            "org.jbpm.test.regression.subprocess.MultipleInstancesSubprocess-timerEvent-parent";
    private static final String ENTRY_AND_EXIT_SCRIPT_PARENT_ID =
            "org.jbpm.test.regression.subprocess.MultipleInstancesSubprocess-entryAndExitScript-parent";
    private static final String ENTRY_AND_EXIT_SCRIPT_SUBPROCESS_ID =
            "org.jbpm.test.regression.subprocess.MultipleInstancesSubprocess-entryAndExitScript-subprocess";

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = TIMER_EVENT_PARENT),
                     @KogitoUnitTestResource(path = TIMER_EVENT_SUBPROCESS1),
                     @KogitoUnitTestResource(path = TIMER_EVENT_SUBPROCESS2)},
        listeners = {FlowProcessEventListenerTracker.class, CompletedProcessEventListener.class}
    )
    public void testTimerEvent(KogitoUnitTestContext context) throws Exception {

        Map<String, Object> params = new HashMap<String, Object>();
        List<String> clients = new ArrayList<String>();
        clients.add("A");
        clients.add("B");
        params.put("clients", clients);
        
        ProcessInstance<? extends Model> instance = startProcess(context, TIMER_EVENT_PARENT_ID, params);

        context.find(CompletedProcessEventListener.class).waitForCompletion(5);

        FlowProcessEventListenerTracker processEvents = context.find(FlowProcessEventListenerTracker.class);
        assertThat(processEvents)
            .checkEventsProcessInstanceThat(instance.id())
            .step("main-script1")
            .step("main-multiinstance1")
            .step("main-script2")
            .step("main-multiinstance2")
            .completed();

    }

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = ENTRY_AND_EXIT_SCRIPT_PARENT),
                     @KogitoUnitTestResource(path = ENTRY_AND_EXIT_SCRIPT_SUBPROCESS)},
        listeners = {FlowProcessEventListenerTracker.class, ProcessEventListenerTracker.class}
    )
    public void testEntryAndExitScript(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, ENTRY_AND_EXIT_SCRIPT_PARENT_ID);

        assertThat(instance.variables()).output("onEntryScriptTriggered").isEqualTo(Boolean.TRUE);
        assertThat(instance.variables()).output("onExitScriptTriggered").isEqualTo(Boolean.TRUE);

        ProcessEventListenerTracker tracker = context.find(ProcessEventListenerTracker.class);

        assertEquals(tracker.countForProcessIdCompleted(ENTRY_AND_EXIT_SCRIPT_PARENT_ID), 1);
        assertEquals(tracker.countForProcessIdCompleted(ENTRY_AND_EXIT_SCRIPT_SUBPROCESS_ID), 2);

    }

}
