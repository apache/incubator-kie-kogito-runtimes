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

import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.listeners.CompletedProcessEventListener;

import static java.util.Collections.singletonMap;
import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

@KogitoUnitTestExtension
public class IntermediateEventSubprocessCatchTest  {


    private static final String INTERMEDIATE_CATCH_EVENT_SUBPROCESS_PROCESS = "org/kie/kogito/test/engine/subprocess/IntermediateEventSubprocessCatch.bpmn2";
    private static final String INTERMEDIATE_CATCH_EVENT_SUBPROCESS_PROCESS_ID = "IntermediateEventSubprocessCatch";

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = INTERMEDIATE_CATCH_EVENT_SUBPROCESS_PROCESS)},
        listeners = {CompletedProcessEventListener.class}
    )
    public void testIntermediateCatchEventSubprocess(KogitoUnitTestContext context) throws Exception {
        ProcessInstance<? extends Model> instance = startProcess(context, INTERMEDIATE_CATCH_EVENT_SUBPROCESS_PROCESS_ID, singletonMap("identifier", "1"));
        CompletedProcessEventListener listener = context.find(CompletedProcessEventListener.class);
        listener.waitForCompletion(1);
        assertThat(instance).isCompleted();
    }

}