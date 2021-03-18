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
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.api.KogitoUnitTestWorkItemHandler;
import org.kie.kogito.tck.junit.listeners.FlowProcessEventListenerTracker;
import org.kie.kogito.tck.junit.wih.WorkItemHandlerTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.singletonMap;
import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

/**
 * This is a sample file to launch a process.
 */
@KogitoUnitTestExtension
@KogitoUnitTestDeployment(
    resources = {@KogitoUnitTestResource(path = "org/kie/kogito/test/engine/flow/ParalellLoopTest.bpmn2")},
    listeners = {FlowProcessEventListenerTracker.class},
    workItemsHandlers = {@KogitoUnitTestWorkItemHandler(name="Human Task", handler=WorkItemHandlerTracker.class)}
)
public class ParalellLoopTest {

    private static final Logger logger = LoggerFactory.getLogger(ParalellLoopTest.class);
    public static final String PROCESS_ID = "hu.tsm.ParalellLoopTest";



    
    @Test
    public void testProcessBothReject(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);
        FlowProcessEventListenerTracker tracker = context.find(FlowProcessEventListenerTracker.class);
        WorkItemHandlerTracker handler = context.find(WorkItemHandlerTracker.class);

        assertThat(instance).isActive();

        assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .entered("ApproveMary")
            .entered("ApproveJohn");

        for (int i = 0; i < 20; i++) {
            logger.debug(">>> Loop: {}", i);
            instance.completeWorkItem(handler.getIdForNodeName("ApproveJohn"), singletonMap("OUTCOME", "Reject"));
            instance.completeWorkItem(handler.getIdForNodeName("ApproveMary"), singletonMap("OUTCOME", "Reject"));
        }

        instance.completeWorkItem(handler.getIdForNodeName("ApproveMary"), singletonMap("OUTCOME", "Approve"));
        instance.completeWorkItem(handler.getIdForNodeName("ApproveJohn"), singletonMap("OUTCOME", "Approve"));

        assertThat(instance).isCompleted();
    }

    @Test
    public void testProcessMaryApproveJohnReject(KogitoUnitTestContext context) {

        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);
        FlowProcessEventListenerTracker tracker = context.find(FlowProcessEventListenerTracker.class);
        WorkItemHandlerTracker handler = context.find(WorkItemHandlerTracker.class);

        assertThat(instance).isActive();

        assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .entered("ApproveMary")
            .entered("ApproveJohn");

        for (int i = 0; i < 20; i++) {
            logger.debug(">>> Loop: {}", i);
            instance.completeWorkItem(handler.getIdForNodeName("ApproveJohn"), singletonMap("OUTCOME", "Approve"));
            instance.completeWorkItem(handler.getIdForNodeName("ApproveMary"), singletonMap("OUTCOME", "Reject"));
        }

        instance.completeWorkItem(handler.getIdForNodeName("ApproveMary"), singletonMap("OUTCOME", "Approve"));
        instance.completeWorkItem(handler.getIdForNodeName("ApproveJohn"), singletonMap("OUTCOME", "Approve"));

        assertThat(instance).isCompleted();

    }

    @Test
    public void testProcessJohnApproveMaryReject(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);
        FlowProcessEventListenerTracker tracker = context.find(FlowProcessEventListenerTracker.class);
        WorkItemHandlerTracker handler = context.find(WorkItemHandlerTracker.class);

        assertThat(instance).isActive();

        assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .entered("ApproveMary")
            .entered("ApproveJohn");

        for (int i = 0; i < 20; i++) {
            logger.debug(">>> Loop: {}", i);
            instance.completeWorkItem(handler.getIdForNodeName("ApproveJohn"), singletonMap("OUTCOME", "Reject"));
            instance.completeWorkItem(handler.getIdForNodeName("ApproveMary"), singletonMap("OUTCOME", "Approve"));
        }

        instance.completeWorkItem(handler.getIdForNodeName("ApproveMary"), singletonMap("OUTCOME", "Approve"));
        instance.completeWorkItem(handler.getIdForNodeName("ApproveJohn"), singletonMap("OUTCOME", "Approve"));

        assertThat(instance).isCompleted();
    }

    @Test
    public void testProcessAlternateReject(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);
        FlowProcessEventListenerTracker tracker = context.find(FlowProcessEventListenerTracker.class);
        WorkItemHandlerTracker handler = context.find(WorkItemHandlerTracker.class);

        assertThat(instance).isActive();

        assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .entered("ApproveMary")
            .entered("ApproveJohn");

        for (int i = 0; i < 20; i++) {
            logger.debug(">>> Loop: {}", i);
            instance.completeWorkItem(handler.getIdForNodeName(i % 2 == 0 ? "ApproveMary" : "ApproveJohn"), singletonMap("OUTCOME", "Approve"));
            instance.completeWorkItem(handler.getIdForNodeName(i % 2 == 0 ? "ApproveJohn" : "ApproveMary"), singletonMap("OUTCOME", "Reject"));
 
        }

        instance.completeWorkItem(handler.getIdForNodeName("ApproveMary"), singletonMap("OUTCOME", "Approve"));
        instance.completeWorkItem(handler.getIdForNodeName("ApproveJohn"), singletonMap("OUTCOME", "Approve"));

        assertThat(instance).isCompleted();

    }

    @Test
    public void testProcessMaryApproveJohnApprove(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);
        FlowProcessEventListenerTracker tracker = context.find(FlowProcessEventListenerTracker.class);
        WorkItemHandlerTracker handler = context.find(WorkItemHandlerTracker.class);

        assertThat(instance).isActive();

        assertThat(tracker)
            .checkEventsProcessInstanceThat(instance.id())
            .entered("ApproveMary")
            .entered("ApproveJohn");

        instance.completeWorkItem(handler.getIdForNodeName("ApproveMary"), singletonMap("OUTCOME", "Approve"));
        instance.completeWorkItem(handler.getIdForNodeName("ApproveJohn"), singletonMap("OUTCOME", "Approve"));

        assertThat(instance).isCompleted();
    }



}
