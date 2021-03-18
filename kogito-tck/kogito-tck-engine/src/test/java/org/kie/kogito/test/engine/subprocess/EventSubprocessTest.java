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

import org.assertj.core.api.Assertions;
import org.jbpm.bpmn2.handler.SignallingTaskHandlerDecorator;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.api.KogitoUnitTestWorkItemHandler;
import org.kie.kogito.tck.junit.asserts.ProcessAssertions;
import org.kie.kogito.tck.junit.listeners.FlowProcessEventListenerTracker;
import org.kie.kogito.tck.junit.wih.ExceptionWorkItemHandler;

import static org.junit.jupiter.api.Assertions.fail;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;



@KogitoUnitTestExtension
public class EventSubprocessTest {

    private static final String ERROR_CODE_EXCEPTION =
            "org/kie/kogito/test/engine/subprocess/EventSubprocess-errorCodeException.bpmn2";
    private static final String ERROR_CODE_EXCEPTION_ID =
            "org.jbpm.test.regression.subprocess.EventSubprocessErrorCodeException";

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = ERROR_CODE_EXCEPTION)},
        listeners = {FlowProcessEventListenerTracker.class},
        workItemsHandlers = {@KogitoUnitTestWorkItemHandler(name="Error Handler", handler=SystemOutWorkItemHandler.class)}
    )
    public void testErrorCodeException(KogitoUnitTestContext context) {
        try {
            context.registerWorkItemHandler("Request Handler", new SignallingTaskHandlerDecorator(ExceptionWorkItemHandler.class, "Error-90277"));
            ProcessInstance<? extends Model> instance = startProcess(context, ERROR_CODE_EXCEPTION_ID);
            ProcessAssertions.assertThat(instance).isAborted();
        } catch (WorkflowRuntimeException e) {
            fail("Error code exceptions in subprocess does not work.");
        }
    }



}
