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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kie.kogito.test.engine.flow;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.process.WorkItem;
import org.kie.kogito.Model;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.junit.api.KogitoUnitTestContext;
import org.kie.kogito.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.junit.api.KogitoUnitTestResource;
import org.kie.kogito.junit.api.KogitoUnitTestWorkItemHandler;
import org.kie.kogito.junit.listeners.FlowProcessEventListenerTracker;
import org.kie.kogito.junit.util.ProcessUtil;
import org.kie.kogito.junit.wih.WorkItemHandlerTracker;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.test.engine.domain.Person;

import static org.kie.kogito.junit.asserts.ProcessAssertions.assertThat;

/**
 * Testing data object and association.
 */

public class DataObjectTest {

    public static final String PROCESS_ID = "org.jbpm.test.functional.DataObject";

    /**
     * DataObject is linked via association with Human Task. Work item should
     * obtain this DataObject in parameters.
     */
    @Test
    @Disabled
    @KogitoUnitTestDeployment(
            namespace = "org.kie.kogito.test.engine.flow",
            resources = { @KogitoUnitTestResource(path = "org/kie/kogito/test/engine/flow/DataObject.bpmn2") },
            workItemsHandlers = { @KogitoUnitTestWorkItemHandler(name = "Human Task", handler = WorkItemHandlerTracker.class) },
            listeners = { FlowProcessEventListenerTracker.class })
    public void testDataObject(KogitoUnitTestContext context) {
        Map<String, Object> params = new HashMap<String, Object>();
        Person mojmir = new Person("Mojmir");
        params.put("person", mojmir);

        ProcessInstance<? extends Model> instance = ProcessUtil.startProcess(context, PROCESS_ID, params);
        FlowProcessEventListenerTracker listener = context.find(FlowProcessEventListenerTracker.class);

        WorkItemHandlerTracker tracker = context.find(WorkItemHandlerTracker.class);

        Collection<KogitoWorkItem> items = tracker.getWorkItems();
        Assertions.assertTrue(items.size() == 1);

        WorkItem wi = items.iterator().next();
        Assertions.assertTrue(wi.getParameters().containsKey("PersonInput"));
        Object param = wi.getParameter("PersonInput");
        Assertions.assertTrue(param instanceof Person);
        Person userTaskInput = (Person) param;
        Assertions.assertEquals("Mojmir", userTaskInput.getName());

        items.forEach(item -> instance.completeWorkItem(item.getStringId(), Collections.emptyMap()));

        assertThat(listener)
                .checkStepsForProcessInstance(instance.id())
                .varAssert("person", (p) -> p.equals(mojmir))
                .started()
                .step("start")
                .step("userTask")
                .step("end")
                .completed();

    }

}
