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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Task;
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.Processes;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.api.KogitoUnitTestWorkItemHandler;
import org.kie.kogito.tck.junit.listeners.ProcessEventListenerTracker;
import org.kie.kogito.tck.junit.listeners.FlowProcessEventListenerTracker;
import org.kie.kogito.tck.junit.wih.WorkItemHandlerTracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

@KogitoUnitTestExtension
public class TimerEventTest {

    private static final String EXCEPTION_AFTER_TIMER = "org/kie/kogito/test/engine/event/TimerEvent-exceptionAfter.bpmn2";
    public static final String BOUNDARY_MULTIPLE_INSTANCES = "org/kie/kogito/test/engine/event/TimerEvent-boundaryMultipleInstances.bpmn2";
    private static final String START_TIMER_CYCLE = "org/kie/kogito/test/engine/event/TimerEvent-startTimerCycle.bpmn2";
    private static final String CANCELLED_TIMER = "org/kie/kogito/test/engine//event/TimerEvent-cancelledTimer.bpmn";
    private static final String TIMER_AND_GATEWAY = "org/kie/kogito/test/engine//event/TimerEvent-timerAndGateway.bpmn";
    
    private static final String EXCEPTION_AFTER_TIMER_ID = "org.jbpm.test.regression.event.TimerEvent-exceptionAfter";
    private static final String START_TIMER_CYCLE_ID = "org.jbpm.test.regression.event.TimerEvent-startTimerCycle";
    private static final String CANCELLED_TIMER_ID = "org.jbpm.test.regression.event.TimerEvent-cancelledTimer";
    private static final String TIMER_AND_GATEWAY_ID = "org.jbpm.test.regression.event.TimerEvent-timerAndGateway";
    public static final String BOUNDARY_MULTIPLE_INSTANCES_ID = "org.jbpm.test.regression.event.TimerEvent-boundaryMultipleInstances";

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = EXCEPTION_AFTER_TIMER)}
    )
    public void testRuntimeExceptionAfterTimer(KogitoUnitTestContext context) throws InterruptedException {
        ProcessInstance<? extends Model> instance = startProcess(context, EXCEPTION_AFTER_TIMER_ID);
        Thread.sleep(5000);
        assertThat(instance).isActive();
        instance.abort();
        assertThat(instance).isAborted();

    }

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = START_TIMER_CYCLE)},
        listeners = {ProcessEventListenerTracker.class}
    )
    public void testStartTimerCycle(KogitoUnitTestContext context) throws InterruptedException {

        ProcessEventListenerTracker listener = context.find(ProcessEventListenerTracker.class);
        Thread.sleep(5000);
        assertEquals(listener.countForProcessIdStarted(START_TIMER_CYCLE_ID), 1);

        Thread.sleep(5000);
        assertEquals(listener.countForProcessIdStarted(START_TIMER_CYCLE_ID), 2);

        Thread.sleep(5000);
        assertEquals(listener.countForProcessIdStarted(START_TIMER_CYCLE_ID), 3);
    }

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = CANCELLED_TIMER)},
        listeners = {ProcessEventListenerTracker.class},
        workItemsHandlers = {@KogitoUnitTestWorkItemHandler(name="Human Task", handler = WorkItemHandlerTracker.class)}
    )
    public void testCancelledTimerNotScheduled(KogitoUnitTestContext context) {
//        for (int i = 0; i < 5; i++) {
//            ProcessInstance<? extends Model> instance = startProcess(context, EXCEPTION_AFTER_TIMER_ID);
//           
//
//            List<Long> list = taskService.getTasksByProcessInstanceId(pi.getId());
//            for (long taskId : list) {
//                Task task = taskService.getTaskById(taskId);
//                System.out.println("taskId = " + task.getId() + ", status = " + task.getTaskData().getStatus());
//            }
//
//            Date before = new Date();
//
//            // Check if engine did not waited for timer
//            Date after = new Date();
//            long seconds = (after.getTime() - before.getTime()) / 1000;
//            Assertions.assertThat(seconds).as("Cancelled timer has been scheduled").isLessThan(5);
//        }
    }

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = TIMER_AND_GATEWAY)},
        listeners = {ProcessEventListenerTracker.class},
        workItemsHandlers = {@KogitoUnitTestWorkItemHandler(name="task1", handler = WorkItemHandlerTracker.class),
                             @KogitoUnitTestWorkItemHandler(name="task2", handler = WorkItemHandlerTracker.class)}
    )
    public void testTimerAndGateway(KogitoUnitTestContext context) throws Exception {
        ProcessInstance<? extends Model> instance = startProcess(context, TIMER_AND_GATEWAY_ID);

        // Both sides of the join are completed. But on the process instance, there are two JoinInstance for the same
        // Join, and since it is an AND join, it never reaches task2. It fails after the next assertion
//        Assertions.assertThat(workItem2).isNotNull();
//        Assertions.assertThat(handler1.getWorkItem()).isNull();
    }

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = BOUNDARY_MULTIPLE_INSTANCES)},
        listeners = {ProcessEventListenerTracker.class},
        workItemsHandlers = {@KogitoUnitTestWorkItemHandler(name="task1", handler = WorkItemHandlerTracker.class),
                             @KogitoUnitTestWorkItemHandler(name="task2", handler = WorkItemHandlerTracker.class)}
    )
    public void testBoundaryTimerInMultipleInstancesSubprocess(KogitoUnitTestContext context) throws InterruptedException {
        
        Set<Integer> runList = new HashSet<Integer>();
        runList.add(1);
        runList.add(2);
        runList.add(3);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("runList", runList);

        ProcessInstance<? extends Model> instance = startProcess(context, BOUNDARY_MULTIPLE_INSTANCES, parameters);

        // wait for 3x 50ms timers to be triggered
        Thread.sleep(1000);

//        Integer counter = (Integer) ksession.getGlobal("counter");
//        Assertions.assertThat(counter).isEqualTo(3);
    }


}
