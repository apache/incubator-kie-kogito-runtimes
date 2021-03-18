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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestEnvironment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestEnvironmentProperty;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResourceType;
import org.kie.kogito.tck.junit.listeners.TrackingAgendaEventListener;
import org.kie.kogito.tck.junit.listeners.FlowProcessEventListenerTracker;
import org.kie.kogito.test.engine.domain.Person;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.tck.junit.asserts.ProcessAssertions.assertThat;
import static org.kie.kogito.tck.junit.util.ProcessUtil.startProcess;

/**
 * Tests fireUntilHalt with jBPM process - this test makes sense only with singleton strategy
 */
@KogitoUnitTestExtension
@KogitoUnitTestEnvironment(
    entries = @KogitoUnitTestEnvironmentProperty(name = "org.jbpm.rule.task.waitstate", value = "true")
)
public class FireUntilHaltTest  {

    private static final String PROCESS = "org/kie/kogito/test/engine/flow/FireUntilHalt.bpmn2";
    private static final String PROCESS_DRL = "org/kie/kogito/test/engine/flow/FireUntilHalt.drl";
    private static final String PROCESS_ID = "org.jbpm.test.functional.FireUntilHalt";


    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = PROCESS),
                     @KogitoUnitTestResource(path = PROCESS_DRL, type = KogitoUnitTestResourceType.RULES)},
        listeners = {FlowProcessEventListenerTracker.class, TrackingAgendaEventListener.class}
    )
    public void testFireUntilHaltWithProcess(KogitoUnitTestContext context) throws Exception {

        List<Person> persons = new ArrayList<>();

        final int wantedPersonsNum = 3;
        final int unwantedPersonsNum = 2;

        // insert 3 wanted persons
        for (int i = 0; i < wantedPersonsNum; i++) {
            Person p = new Person("wanted person");
            p.setId(i);
            persons.add(p);
        }
        // insert 2 unwanted persons
        for (int i = 0; i < unwantedPersonsNum; i++) {
            Person p = new Person("unwanted person");
            p.setId(i);
            persons.add(p);
        }

        ProcessInstance<? extends Model> pi = startProcess(context, PROCESS_ID, singletonMap("persons", persons));

        TrackingAgendaEventListener listener = context.find(TrackingAgendaEventListener.class);

        // wantedPersonsNum + unwantedPersonsNum should be acknowledge
        assertThat(listener.ruleFiredCount("person detector")).isEqualTo(wantedPersonsNum + unwantedPersonsNum);

        // we start defined process
        assertThat(listener.ruleFiredCount("initial actions")).isEqualTo(1);
        assertThat(listener.ruleFiredCount("wanted person detector")).isEqualTo(wantedPersonsNum);
        assertThat(listener.ruleFiredCount("change unwanted person to wanted")).isEqualTo(unwantedPersonsNum);
        assertThat(pi).isCompleted();
    }

}
