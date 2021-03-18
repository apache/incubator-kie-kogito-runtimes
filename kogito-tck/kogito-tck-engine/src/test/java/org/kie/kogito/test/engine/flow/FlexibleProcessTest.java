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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kie.kogito.test.engine.flow;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.Sig;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.api.KogitoUnitTestWorkItemHandler;
import org.kie.kogito.tck.junit.listeners.FlowProcessEventListenerTracker;
import org.kie.kogito.tck.junit.wih.WorkItemHandlerTracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

@KogitoUnitTestExtension
@KogitoUnitTestDeployment(
    resources = {@KogitoUnitTestResource(path = FlexibleProcessTest.PROCESS)},
    listeners = {FlowProcessEventListenerTracker.class},
    workItemsHandlers = {@KogitoUnitTestWorkItemHandler(name="Human Task", handler=WorkItemHandlerTracker.class),
                         @KogitoUnitTestWorkItemHandler(name="addedWorkItem", handler=WorkItemHandlerTracker.class)}
)
public class FlexibleProcessTest  {

    public static final String PROCESS = "org/kie/kogito/test/engine/flow/FlexibleProcess.bpmn2";
    public static final String PROCESS_ID = "org.jbpm.test.functional.FlexibleProcess";

    /**
     * Flexible process with four fragments. -default - with start node, without
     * end event -two fragments which will be signaled -one fragment which won't
     * be signaled - it should not be executed
     */
    @Test
    public void testFlexibleProcess(KogitoUnitTestContext context) throws Exception {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);

        FlowProcessEventListenerTracker tracker = context.find(FlowProcessEventListenerTracker.class);
        WorkItemHandlerTracker handler = context.find(WorkItemHandlerTracker.class);

        assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .step("start")
            .step("task1");


        instance.send(Sig.of("userTask", null));

         assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .entered("userTask");


        assertThat(tracker.eventsForProcess(instance.id())).isNotEmpty();

        KogitoWorkItem item = handler.getWorkItems().iterator().next();

        instance.completeWorkItem(item.getStringId(), null);

        assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .entered("userTask2");

        instance.send(Sig.of("task21", null));

        assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .step("task21")
            .step("task22")
            .step("end1")
            .completed()
            .notEntered("task3")
            .notEntered("end2");
    }

    /**
     * Tests dynamic insertion of work item node into adhoc top-level process.
     * DynamicUtils does not support adhoc processes yet, but there is improved
     * version on jbpm master branch.
     */
    @Test
    @Disabled
    public void testFlexibleProcessAddWorkItem(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);


        FlowProcessEventListenerTracker tracker = context.find(FlowProcessEventListenerTracker.class);
        WorkItemHandlerTracker handler = context.find(WorkItemHandlerTracker.class);


        assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .started();
// this is no where to find supported ?
//        DynamicUtils.addDynamicWorkItem(pi, instance., "addedWorkItem", Collections.<String, Object>emptyMap());

        KogitoWorkItem item = handler.getWorkItems().iterator().next();
        instance.completeWorkItem(item.getStringId(), null);

        assertThat(item.getName()).isEqualTo("addedWorkItem");
    }

}
