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

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstances;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestEnvironment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestEnvironmentProperty;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.api.KogitoUnitTestWorkItemHandler;
import org.kie.kogito.tck.junit.wih.WorkItemHandlerTracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.firstProcessInstance;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

@KogitoUnitTestExtension
@KogitoUnitTestEnvironment(entries =
    @KogitoUnitTestEnvironmentProperty(name = "jbpm.enable.multi.con", value = "true")
)
public class MultipleTasksLoopSubprocessTest {

    private static final String DYNAMIC_PARENT_PROCESS = "org/kie/kogito/test/engine/subprocess/MultipleTasksLoopParentProcess.bpmn";
    private static final String DYNAMIC_CHILD_SUBPROCESS = "org/kie/kogito/test/engine/subprocess/MultipleTasksLoopSubProcess.bpmn";
    private static final String DYNAMIC_PARENT_PROCESS_ID = "MultipleTasksLoopParentProcess";


    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = DYNAMIC_PARENT_PROCESS),
                     @KogitoUnitTestResource(path = DYNAMIC_CHILD_SUBPROCESS)},
        workItemsHandlers = {@KogitoUnitTestWorkItemHandler(name="Human Task", handler = WorkItemHandlerTracker.class)}
    )
    public void testDuplicateTasksAfterSubProcess(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, DYNAMIC_PARENT_PROCESS_ID);

        assertThat(instance).isActive();

        ProcessInstances<? extends Model> instances = context.instances("MultipleTasksLoopSubProcess");
        assertEquals(1, instances.size());

        ProcessInstance<? extends Model> pi = firstProcessInstance(instances);
        WorkItemHandlerTracker tracker = context.find(WorkItemHandlerTracker.class);
        List<KogitoWorkItem> workItems = tracker.getWorkItemsFor(pi.id());

        assertEquals(1, workItems.size());
        workItems.forEach(wih -> pi.completeWorkItem(wih.getStringId(), Collections.emptyMap()));

        assertThat(pi).isCompleted();
        assertThat(instance).isActive();

        List<KogitoWorkItem> workItemsParent = tracker.getWorkItemsFor(instance.id());
        assertEquals(1, workItemsParent.size()); // only 1 task should be available at this point

        instance.abort();
        assertThat(instance).isAborted();
    }
}
