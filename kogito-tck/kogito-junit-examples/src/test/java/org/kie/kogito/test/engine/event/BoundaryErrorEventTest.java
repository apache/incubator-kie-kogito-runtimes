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

package org.kie.kogito.test.engine.event;

import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.junit.api.KogitoUnitTestContext;
import org.kie.kogito.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.junit.api.KogitoUnitTestDeploymentException;
import org.kie.kogito.junit.api.KogitoUnitTestListeners;
import org.kie.kogito.junit.api.KogitoUnitTestResource;
import org.kie.kogito.junit.api.KogitoUnitTestWorkItemHandler;
import org.kie.kogito.junit.api.KogitoUnitTestWorkItemHandlerRegistry;
import org.kie.kogito.junit.listeners.FlowProcessEventListenerTracker;
import org.kie.kogito.junit.wih.ExceptionWorkItemHandler;
import org.kie.kogito.process.ProcessInstance;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.kogito.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.junit.util.ProcessUtil.startProcess;

@KogitoUnitTestWorkItemHandlerRegistry(
        entries = { @KogitoUnitTestWorkItemHandler(name = "Human Task", handler = ExceptionWorkItemHandler.class) })
@KogitoUnitTestListeners({ FlowProcessEventListenerTracker.class })
public class BoundaryErrorEventTest {

    private static final String PROCESS_PREFIX = "org/kie/kogito/test/engine/event/BoundaryErrorEvent-";
    private static final String PROCESS_ID = "org.jbpm.test.regression.event.BoundaryErrorEvent";

    @Test
    @KogitoUnitTestDeployment(
            resources = { @KogitoUnitTestResource(path = PROCESS_PREFIX + "WithErrorCodeWithoutStructureRef.bpmn2") })
    public void testBoundaryErrorEventDefaultHandlerWithErrorCodeWithoutStructureRef(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);
        assertThat(context.find(FlowProcessEventListenerTracker.class))
                .checkEventsProcessInstanceThat(instance.id())
                .step("Start")
                .entered("User Task");
    }

    @Test
    @KogitoUnitTestDeployment(
            resources = { @KogitoUnitTestResource(path = PROCESS_PREFIX + "WithErrorCodeMatchWithStructureRef.bpmn2") })
    public void testBoundaryErrorEventDefaultHandlerWithErrorCodeMatchWithStructureRef(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);
        assertThat(context.find(FlowProcessEventListenerTracker.class))
                .checkEventsProcessInstanceThat(instance.id())
                .step("Start")
                .step("User Task")
                .exited("MyBoundaryErrorEvent")
                .step("Script Task 1");
    }

    @Test
    @KogitoUnitTestDeployment(
            resources = { @KogitoUnitTestResource(path = PROCESS_PREFIX + "WithErrorCodeMatchWithoutStructureRef.bpmn2") })
    public void testBoundaryErrorEventDefaultHandlerWithErrorCodeMatchWithoutStructureRef(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, PROCESS_ID);
        assertThat(context.find(FlowProcessEventListenerTracker.class))
                .checkEventsProcessInstanceThat(instance.id())
                .step("Start")
                .entered("User Task")
                .exited("MyBoundaryErrorEvent");
    }

    @Test
    @KogitoUnitTestDeployment(
            resources = { @KogitoUnitTestResource(path = PROCESS_PREFIX + "WithoutErrorCodeWithStructureRef.bpmn2") })
    @KogitoUnitTestDeploymentException
    public void testBoundaryErrorEventDefaultHandlerWithoutErrorCodeWithStructureRef(KogitoUnitTestContext context) {

        assertNotNull(context.find(Throwable.class));
    }

    @Test
    @KogitoUnitTestDeployment(
            resources = { @KogitoUnitTestResource(path = PROCESS_PREFIX + "WithoutErrorCodeWithoutStructureRef.bpmn2") })
    @KogitoUnitTestDeploymentException
    public void testBoundaryErrorEventDefaultHandlerWithoutErrorCodeWithoutStructureRef(KogitoUnitTestContext context) {
        assertNotNull(context.find(Throwable.class));
    }

}
