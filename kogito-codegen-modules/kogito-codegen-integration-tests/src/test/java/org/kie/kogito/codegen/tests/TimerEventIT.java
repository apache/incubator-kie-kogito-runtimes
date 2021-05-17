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
package org.kie.kogito.codegen.tests;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.test.util.NodeLeftCountDownProcessEventListener;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.codegen.AbstractCodegenIT;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceReadMode;
import org.kie.kogito.process.Processes;

import static org.assertj.core.api.Assertions.assertThat;

public class TimerEventIT extends AbstractCodegenIT {

    @Test
    void testIntermediateCycleTimerEvent() throws Exception {

        Application app = generateCodeProcessesOnly("timer/IntermediateCatchEventTimerCycleISO.bpmn2");
        assertThat(app).isNotNull();

        NodeLeftCountDownProcessEventListener listener = new NodeLeftCountDownProcessEventListener("timer", 3);
        app.config().get(ProcessConfig.class).processEventListeners().listeners().add(listener);

        Process<? extends Model> p = app.get(Processes.class).processById("IntermediateCatchEvent");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        m.fromMap(parameters);

        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();

        boolean completed = listener.waitTillCompleted(5000);
        assertThat(completed).isTrue();

        assertThat(processInstance.status()).isEqualTo(KogitoProcessInstance.STATE_ACTIVE);
        processInstance.abort();

        assertThat(processInstance.status()).isEqualTo(KogitoProcessInstance.STATE_ABORTED);
    }

    @Test
    void testIntermediateDurationTimerEvent() throws Exception {

        Application app = generateCodeProcessesOnly("timer/IntermediateCatchEventTimerDurationISO.bpmn2");
        assertThat(app).isNotNull();

        NodeLeftCountDownProcessEventListener listener = new NodeLeftCountDownProcessEventListener("timer", 1);
        app.config().get(ProcessConfig.class).processEventListeners().listeners().add(listener);

        Process<? extends Model> p = app.get(Processes.class).processById("IntermediateCatchEvent");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        m.fromMap(parameters);

        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();

        boolean completed = listener.waitTillCompleted(5000);
        assertThat(completed).isTrue();

        assertThat(processInstance.status()).isEqualTo(KogitoProcessInstance.STATE_COMPLETED);
    }

    @Test
    void testIntermediateDateTimerEvent() throws Exception {

        Application app = generateCodeProcessesOnly("timer/IntermediateCatchEventTimerDateISO.bpmn2");
        assertThat(app).isNotNull();

        NodeLeftCountDownProcessEventListener listener = new NodeLeftCountDownProcessEventListener("timer", 1);
        app.config().get(ProcessConfig.class).processEventListeners().listeners().add(listener);

        Process<? extends Model> p = app.get(Processes.class).processById("IntermediateCatchEvent");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        OffsetDateTime plusTwoSeconds = OffsetDateTime.now().plusSeconds(2);
        parameters.put("date", plusTwoSeconds.toString());
        m.fromMap(parameters);

        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();

        boolean completed = listener.waitTillCompleted(5000);
        assertThat(completed).isTrue();

        assertThat(processInstance.status()).isEqualTo(KogitoProcessInstance.STATE_COMPLETED);
    }

    @Test
    void testBoundaryDurationTimerEventOnTask() throws Exception {

        Application app = generateCodeProcessesOnly("timer/TimerBoundaryEventDurationISOOnTask.bpmn2");
        assertThat(app).isNotNull();

        NodeLeftCountDownProcessEventListener listener = new NodeLeftCountDownProcessEventListener("TimerEvent", 1);
        app.config().get(ProcessConfig.class).processEventListeners().listeners().add(listener);

        Process<? extends Model> p = app.get(Processes.class).processById("TimerBoundaryEvent");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        m.fromMap(parameters);

        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();

        boolean completed = listener.waitTillCompleted(5000);
        assertThat(completed).isTrue();

        assertThat(processInstance.status()).isEqualTo(KogitoProcessInstance.STATE_COMPLETED);
    }

    @Test
    void testBoundaryCycleTimerEventOnTask() throws Exception {

        Application app = generateCodeProcessesOnly("timer/TimerBoundaryEventCycleISOOnTask.bpmn2");
        assertThat(app).isNotNull();

        NodeLeftCountDownProcessEventListener listener = new NodeLeftCountDownProcessEventListener("TimerEvent", 1);
        app.config().get(ProcessConfig.class).processEventListeners().listeners().add(listener);

        Process<? extends Model> p = app.get(Processes.class).processById("TimerBoundaryEvent");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        m.fromMap(parameters);

        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();

        boolean completed = listener.waitTillCompleted(5000);
        assertThat(completed).isTrue();

        assertThat(processInstance.status()).isEqualTo(KogitoProcessInstance.STATE_COMPLETED);
    }

    @Test
    void testBoundaryDateTimerEventOnTask() throws Exception {

        Application app = generateCodeProcessesOnly("timer/TimerBoundaryEventDateISOOnTask.bpmn2");
        assertThat(app).isNotNull();

        NodeLeftCountDownProcessEventListener listener = new NodeLeftCountDownProcessEventListener("TimerEvent", 1);
        app.config().get(ProcessConfig.class).processEventListeners().listeners().add(listener);

        Process<? extends Model> p = app.get(Processes.class).processById("TimerBoundaryEvent");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        OffsetDateTime plusTwoSeconds = OffsetDateTime.now().plusSeconds(2);
        parameters.put("date", plusTwoSeconds.toString());
        m.fromMap(parameters);

        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();

        boolean completed = listener.waitTillCompleted(5000);
        assertThat(completed).isTrue();

        assertThat(processInstance.status()).isEqualTo(KogitoProcessInstance.STATE_COMPLETED);
    }

    @Test
    void testBoundaryDurationTimerEventOnSubProcess() throws Exception {

        Application app = generateCodeProcessesOnly("timer/TimerBoundaryEventDurationISO.bpmn2");
        assertThat(app).isNotNull();

        NodeLeftCountDownProcessEventListener listener = new NodeLeftCountDownProcessEventListener("TimerEvent", 1);
        app.config().get(ProcessConfig.class).processEventListeners().listeners().add(listener);

        Process<? extends Model> p = app.get(Processes.class).processById("TimerBoundaryEvent");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        m.fromMap(parameters);

        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();

        boolean completed = listener.waitTillCompleted(5000);
        assertThat(completed).isTrue();

        assertThat(processInstance.status()).isEqualTo(KogitoProcessInstance.STATE_COMPLETED);
    }

    @Test
    void testStartTimerEvent() throws Exception {

        Application app = generateCodeProcessesOnly("timer/StartTimerDuration.bpmn2");
        assertThat(app).isNotNull();

        NodeLeftCountDownProcessEventListener listener = new NodeLeftCountDownProcessEventListener("timer fired", 1);
        app.config().get(ProcessConfig.class).processEventListeners().listeners().add(listener);

        Process<? extends Model> p = app.get(Processes.class).processById("defaultPackage.TimerProcess");
        // activate to schedule timers
        p.activate();

        boolean completed = listener.waitTillCompleted(5000);
        assertThat(completed).isTrue();

        Collection<?> instances = p.instances().values(ProcessInstanceReadMode.MUTABLE);
        assertThat(instances).hasSize(1);

        ProcessInstance<?> processInstance = (ProcessInstance<?>) instances.iterator().next();
        assertThat(processInstance).isNotNull();

        assertThat(processInstance.status()).isEqualTo(KogitoProcessInstance.STATE_ACTIVE);

        processInstance.abort();
        assertThat(processInstance.status()).isEqualTo(KogitoProcessInstance.STATE_ABORTED);

        assertThat(p.instances().size()).isZero();
    }

    @Test
    void testStartTimerEventTimeCycle() throws Exception {
        Application app = generateCodeProcessesOnly("timer/StartTimerCycle.bpmn2");
        assertThat(app).isNotNull();

        NodeLeftCountDownProcessEventListener listener = new NodeLeftCountDownProcessEventListener("timer fired", 2);
        app.config().get(ProcessConfig.class).processEventListeners().listeners().add(listener);

        Process<? extends Model> p = app.get(Processes.class).processById("defaultPackage.TimerProcess");
        // activate to schedule timers
        p.activate();

        boolean completed = listener.waitTillCompleted(5000);
        assertThat(completed).isTrue();

        Collection<?> instances = p.instances().values(ProcessInstanceReadMode.MUTABLE);
        assertThat(instances).hasSize(2);

        ProcessInstance<?> processInstance = (ProcessInstance<?>) instances.iterator().next();
        assertThat(processInstance).isNotNull();

        assertThat(processInstance.status()).isEqualTo(KogitoProcessInstance.STATE_ACTIVE);
        // deactivate to cancel timer, so there should be no more timers fired
        p.deactivate();

        // reset the listener to make sure nothing more is triggered
        listener.reset(1);
        completed = listener.waitTillCompleted(3000);
        assertThat(completed).isFalse();
        // same amount of instances should be active as before deactivation
        instances = p.instances().values(ProcessInstanceReadMode.MUTABLE);
        assertThat(instances).hasSize(2);
        // clean up by aborting all instances
        instances.forEach(i -> ((ProcessInstance<?>) i).abort());
        assertThat(p.instances().size()).isZero();
    }
}
