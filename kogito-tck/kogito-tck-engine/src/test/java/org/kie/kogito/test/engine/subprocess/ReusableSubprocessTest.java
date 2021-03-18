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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.asserts.ProcessAssertions;
import org.kie.kogito.tck.junit.listeners.FlowProcessEventListenerTracker;
import org.kie.kogito.tck.junit.listeners.ProcessEventListenerTracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

@KogitoUnitTestExtension
public class ReusableSubprocessTest {

    private static final String WAIT_FOR_COMPLETION_FALSE_PARENT =
            "org/kie/kogito/test/engine/subprocess/ReusableSubprocess-waitForCompletionFalse-parent.bpmn2";
    private static final String WAIT_FOR_COMPLETION_FALSE_SUBPROCESS =
            "org/kie/kogito/test/engine/subprocess/ReusableSubprocess-waitForCompletionFalse-subprocess.bpmn2";
    private static final String DEPENDENT_SUBPROCESS_ABORT_PARENT =
            "org/kie/kogito/test/engine/subprocess/ReusableSubprocess-dependentSubprocessAbort-parent.bpmn2";
    private static final String DEPENDENT_SUBPROCESS_ABORT_SUBPROCESS =
            "org/kie/kogito/test/engine/subprocess/ReusableSubprocess-dependentSubprocessAbort-subprocess.bpmn2";
    
    private static final String DEPENDENT_SUBPROCESS_ABORT_PARENT_ID =
            "org.jbpm.test.regression.subprocess.ReusableSubprocess-dependentSubprocessAbort-parent";
    private static final String WAIT_FOR_COMPLETION_FALSE_PARENT_ID =
            "org.jbpm.test.regression.subprocess.ReusableSubprocessWaitForCompletionFalseParent";
    private static final String DEPENDENT_SUBPROCESS_ABORT_SUBPROCESS_ID =
            "org.jbpm.test.regression.subprocess.ReusableSubprocessDependentSubprocessAbortSubprocess";

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = WAIT_FOR_COMPLETION_FALSE_PARENT),
                     @KogitoUnitTestResource(path = WAIT_FOR_COMPLETION_FALSE_SUBPROCESS)},
        listeners = {ProcessEventListenerTracker.class}
    )
    public void testWaitForCompletionFalse(KogitoUnitTestContext context) throws Exception {
        ProcessInstance<? extends Model> instance = startProcess(context, WAIT_FOR_COMPLETION_FALSE_PARENT_ID);
        ProcessAssertions.assertThat(instance).isCompleted();
    }

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = DEPENDENT_SUBPROCESS_ABORT_PARENT),
                     @KogitoUnitTestResource(path = DEPENDENT_SUBPROCESS_ABORT_SUBPROCESS)},
        listeners = {FlowProcessEventListenerTracker.class}
    )
    public void testDependentSubprocessAbort(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, DEPENDENT_SUBPROCESS_ABORT_PARENT_ID);

        assertThat(instance).isActive();
        instance.abort();
        assertThat(instance).isAborted();
        
        // everything is 0
        assertEquals(context.instances(DEPENDENT_SUBPROCESS_ABORT_SUBPROCESS_ID).size(), 0);
        assertEquals(context.instances(DEPENDENT_SUBPROCESS_ABORT_PARENT_ID).size(), 0);

        
        ProcessEventListenerTracker tracker = context.find(ProcessEventListenerTracker.class);
        assertEquals(tracker.countForProcessIdStarted(DEPENDENT_SUBPROCESS_ABORT_PARENT_ID), 1);
        assertEquals(tracker.countForProcessIdStarted(DEPENDENT_SUBPROCESS_ABORT_SUBPROCESS_ID), 1);

    }

}
