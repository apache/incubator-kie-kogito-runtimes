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
import org.kie.kogito.test.engine.payload.SignalObjectReport;

import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

@KogitoUnitTestExtension
public class StartEventTest {

    private static final String ERROR_EXCEPTION_HANDLER = "org/kie/kogito/test/engine/event/StartEvent-errorExceptionHandler.bpmn2";
    private static final String ERROR_EXCEPTION_MAPPING = "org/kie/kogito/test/engine/event/StartEvent-errorExceptionMapping.bpmn2";
    private static final String SIGNAL_DATA_MAPPING = "org/kie/kogito/test/engine/event/StartEvent-signalDataMapping.bpmn2";
    private static final String SIGNAL_OUTPUT_TYPE = "org/kie/kogito/test/engine/event/StartEvent-signalOutputType.bpmn2";
    
    private static final String ERROR_EXCEPTION_HANDLER_ID = "org.jbpm.test.regression.event.StartEventErrorExceptionHandler";
    private static final String ERROR_EXCEPTION_MAPPING_ID = "org.jbpm.test.regression.event.StartEventErrorExceptionMapping";



    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = ERROR_EXCEPTION_HANDLER)}
    )
    public void testErrorStartEventDefaultExceptionHandler(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, ERROR_EXCEPTION_HANDLER_ID);
        assertThat(instance.variables()).hasSize(2);
        assertThat(instance.variables()).output("capturedException").isEqualTo("java.lang.RuntimeException: XXX");

    }

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = ERROR_EXCEPTION_MAPPING)}
    )
    public void testErrorStartEventDataOutputMapping(KogitoUnitTestContext context) {
        ProcessInstance<? extends Model> instance = startProcess(context, ERROR_EXCEPTION_MAPPING_ID);
        assertThat(instance.variables()).hasSize(2);
        assertThat(instance.variables()).output("capturedException").isEqualTo("java.lang.RuntimeException: XXX");
    }

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = SIGNAL_DATA_MAPPING)}
    )
    public void testSignalStartEventDataMapping(KogitoUnitTestContext context) throws Exception {
        ProcessInstance<? extends Model> instance = startProcess(context, "org.jbpm.test.regression.event.StartEventSignalDataMapping");
        
        SignalObjectReport report = new SignalObjectReport("Type of signal object report");
        instance.send(Sig.of("SignalObjectReport", report));

        assertThat(instance).isCompleted();
        assertThat(instance.variables()).hasSize(1);
        assertThat(instance.variables()).output("x").isEqualTo(5);

    }

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = SIGNAL_OUTPUT_TYPE)}
    )
    public void testSignalOutputType(KogitoUnitTestContext context) throws Exception {
        ProcessInstance<? extends Model> instance = startProcess(context, "org.jbpm.test.regression.event.StartEventSignalOutputType");
        SignalObjectReport report = new SignalObjectReport("Type of signal object report");
        instance.send(Sig.of("SignalObjectReport", report));

        assertThat(instance).isCompleted();
        assertThat(instance.variables()).hasSize(1);
        assertThat(instance.variables()).output("x").isEqualTo(report.toString());
    }

}
