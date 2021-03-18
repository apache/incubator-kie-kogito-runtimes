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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.jupiter.api.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.listeners.FlowProcessEventListenerTracker;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

@KogitoUnitTestExtension
public class MainProcessExceptionHandlerTest {

    private static final String MAIN_PROCESS = "org/kie/kogito/test/engine/subprocess/MainProcessExceptionHandler.bpmn2";

    private static final String MAIN_SUBPROCESS = "org/kie/kogito/test/engine/subprocess/MainSubprocessExceptionHandler.bpmn2";

    private static final String MAIN_PROCESS_ID = "com.DealWithException";

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = MAIN_PROCESS),
                     @KogitoUnitTestResource(path = MAIN_SUBPROCESS)},
        listeners = {FlowProcessEventListenerTracker.class}
    )
    public void testMainSubprocessExceptionHandler(KogitoUnitTestContext context) {
        MutableBoolean error = new MutableBoolean(false);
        
        context.registerEventListener(new DefaultProcessEventListener() {
            @Override
            public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
                if(!"StartProcess".equals(event.getNodeInstance().getNodeName())) {
                    return;
                }
                if(!"com.DealWithException".equals(event.getProcessInstance().getProcessId())) {
                    return;
                }

                Exception e = (Exception) ((WorkflowProcessInstanceImpl) event.getProcessInstance()).getVariable("event");
                error.setValue(e != null);
            }
        });

        ProcessInstance<? extends Model> instance = startProcess(context, MAIN_PROCESS_ID, Collections.singletonMap("launchOnParent", Boolean.FALSE));

        assertThat(instance).isAborted();
        assertTrue(error.getValue());
    }

}
