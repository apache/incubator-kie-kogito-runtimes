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


import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.Sig;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;

import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

@KogitoUnitTestExtension
public class MessageEventTest  {

    private static final String MULTIPLE_SIMPLE =
            "org/kie/kogito/test/engine/event/MessageEvent-multipleSimple.bpmn2";
    private static final String MULTIPLE_SIMPLE_ID =
            "org.jbpm.test.regression.event.MessageEvent-multipleSimple";

    private static final String MULTIPLE_SUBPROCESS =
            "org/kie/kogito/test/engine/event/MessageEvent-multipleSubprocess.bpmn2";
    private static final String MULTIPLE_SUBPROCESS_ID =
            "org.jbpm.test.regression.event.MessageEvent-multipleSubprocess";

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = MULTIPLE_SIMPLE)}
    )
    public void testMultipleIntermediateMessageEventsSimpleProcess(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, MULTIPLE_SIMPLE_ID);
        instance.send(Sig.of("Message-continue", null));
        assertThat(instance).isActive();
    }

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = MULTIPLE_SUBPROCESS)}
    )
    public void testMultipleIntermediateMessageEventsEmbeddedSubProcess(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, MULTIPLE_SUBPROCESS_ID);
        instance.send(Sig.of("Message-continue", null));
        assertThat(instance).isActive();
    }

}
