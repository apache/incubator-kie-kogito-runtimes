/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jbpm.bpmn2;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.bpmn2.event.ConditionalStartModel;
import org.jbpm.bpmn2.event.ConditionalStartProcess;
import org.jbpm.bpmn2.intermediate.IntermediateThrowEventSignalModel;
import org.jbpm.bpmn2.intermediate.IntermediateThrowEventSignalProcess;
import org.jbpm.bpmn2.objects.NotAvailableGoodsReport;
import org.jbpm.bpmn2.objects.Person;
import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.bpmn2.start.MessageStartModel;
import org.jbpm.bpmn2.start.MessageStartProcess;
import org.jbpm.bpmn2.start.MultipleEventBasedStartEventProcessProcess;
import org.jbpm.bpmn2.start.SignalStartModel;
import org.jbpm.bpmn2.start.SignalStartProcess;
import org.jbpm.bpmn2.start.SignalStartWithTransformationModel;
import org.jbpm.bpmn2.start.SignalStartWithTransformationProcess;
import org.jbpm.bpmn2.start.StartTimerCycleProcess;
import org.jbpm.bpmn2.start.StartTimerDurationProcess;
import org.jbpm.bpmn2.start.TimerStartCycleLegacyModel;
import org.jbpm.bpmn2.start.TimerStartCycleLegacyProcess;
import org.jbpm.bpmn2.start.TimerStartDurationModel;
import org.jbpm.bpmn2.start.TimerStartDurationProcess;
import org.jbpm.bpmn2.start.TimerStartISOModel;
import org.jbpm.bpmn2.start.TimerStartISOProcess;
import org.jbpm.bpmn2.start.TimerStartModel;
import org.jbpm.bpmn2.start.TimerStartProcess;
import org.jbpm.process.workitem.builtin.SystemOutWorkItemHandler;
import org.jbpm.test.util.NodeLeftCountDownProcessEventListener;
import org.jbpm.test.utils.EventTrackerProcessListener;
import org.jbpm.test.utils.ProcessTestHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.io.Resource;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.io.ResourceFactory;
import org.kie.kogito.Application;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.impl.Sig;
import org.kie.kogito.process.workitems.InternalKogitoWorkItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class StartEventTest extends JbpmBpmn2TestCase {

    @Test
    @Disabled
    public void testConditionalStart() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        List<String> startedInstances = new ArrayList<>();
        EventTrackerProcessListener listener = new EventTrackerProcessListener();
        ProcessTestHelper.registerProcessEventListener(app, listener);
        ProcessTestHelper.registerProcessEventListener(app, new DefaultKogitoProcessEventListener() {

            @Override
            public void afterProcessStarted(ProcessStartedEvent event) {
                startedInstances.add(((KogitoProcessInstance) event.getProcessInstance()).getStringId());
            }

        });
        ProcessTestHelper.registerHandler(app, "Email", new SystemOutWorkItemHandler());
        org.kie.kogito.process.Process<ConditionalStartModel> definition = ConditionalStartProcess.newProcess(app);

        Person person = new Person();
        person.setName("jack");

        definition.send(Sig.of("Conditional", person));

        assertThat(startedInstances).hasSize(1);

        assertThat(listener.tracked())
                .anyMatch(ProcessTestHelper.triggered("StartProcess"))
                .anyMatch(ProcessTestHelper.triggered("Hello"))
                .anyMatch(ProcessTestHelper.triggered("EndProcess"));
    }

    @Test
    public void testTimerStartCycleLegacy() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("StartProcess", 2);
        ProcessTestHelper.registerProcessEventListener(app, countDownListener);
        final List<String> startedInstances = new ArrayList<>();
        ProcessTestHelper.registerProcessEventListener(app, new DefaultKogitoProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                startedInstances.add(((KogitoProcessInstance) event.getProcessInstance()).getStringId());
            }
        });
        org.kie.kogito.process.Process<TimerStartCycleLegacyModel> definition = TimerStartCycleLegacyProcess.newProcess(app);

        logger.debug("About to start ###### {}", new Date());
        assertThat(startedInstances).isEmpty();
        countDownListener.waitTillCompleted();
        assertThat(startedInstances).hasSize(2);
    }

    @Test
    public void testTimerStart() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("StartProcess", 5);
        ProcessTestHelper.registerProcessEventListener(app, countDownListener);
        final List<String> startedInstances = new ArrayList<>();
        ProcessTestHelper.registerProcessEventListener(app, new DefaultKogitoProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                startedInstances.add(((KogitoProcessInstance) event.getProcessInstance()).getStringId());
            }
        });
        org.kie.kogito.process.Process<TimerStartModel> definition = TimerStartProcess.newProcess(app);
        assertThat(startedInstances).isEmpty();
        countDownListener.waitTillCompleted();
        assertThat(startedInstances).hasSize(5);
    }

    @Test
    public void testTimerStartDateISO() throws Exception {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("StartProcess", 1);
        byte[] content = Files.readAllBytes(Paths.get(this.getClass().getResource("/org/jbpm/bpmn2/start/BPMN2-TimerStartDate.bpmn2").getPath()));
        String processContent = new String(content, "UTF-8");

        OffsetDateTime plusTwoSeconds = OffsetDateTime.now().plusSeconds(2);

        processContent = processContent.replaceFirst("#\\{date\\}", plusTwoSeconds.toString());
        Resource resource = ResourceFactory.newReaderResource(new StringReader(processContent));
        resource.setSourcePath("org/jbpm/bpmn2/start/BPMN2-TimerStartDate.bpmn2");
        resource.setTargetPath("org/jbpm/bpmn2/start/BPMN2-TimerStartDate.bpmn2");
        kruntime = createKogitoProcessRuntime(resource);

        kruntime.getProcessEventManager().addEventListener(countDownListener);
        final List<String> list = new ArrayList<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list.add(((KogitoProcessInstance) event.getProcessInstance()).getStringId());
            }
        });
        assertThat(list).isEmpty();
        countDownListener.waitTillCompleted();
        assertThat(list).hasSize(1);

    }

    @Test
    public void testTimerStartCycleISO() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("StartProcess", 6);
        ProcessTestHelper.registerProcessEventListener(app, countDownListener);
        final List<String> startedInstances = new ArrayList<>();
        ProcessTestHelper.registerProcessEventListener(app, new DefaultKogitoProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                startedInstances.add(((KogitoProcessInstance) event.getProcessInstance()).getStringId());
            }
        });
        org.kie.kogito.process.Process<TimerStartISOModel> definition = TimerStartISOProcess.newProcess(app);
        assertThat(startedInstances).isEmpty();
        countDownListener.waitTillCompleted();
        assertThat(startedInstances).hasSize(6);
    }

    @Test
    public void testTimerStartDuration() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("StartProcess", 1);
        ProcessTestHelper.registerProcessEventListener(app, countDownListener);
        final List<String> startedInstances = new ArrayList<>();
        ProcessTestHelper.registerProcessEventListener(app, new DefaultKogitoProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                startedInstances.add(((KogitoProcessInstance) event.getProcessInstance()).getStringId());
            }
        });
        org.kie.kogito.process.Process<TimerStartDurationModel> definition = TimerStartDurationProcess.newProcess(app);
        assertThat(startedInstances).isEmpty();
        countDownListener.waitTillCompleted();
        assertThat(startedInstances).hasSize(1);
    }

    @Test
    public void testSignalToStartProcess() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ProcessTestHelper.registerHandler(app, "Human Task", handler);
        final List<String> startedProcesses = new ArrayList<>();
        ProcessTestHelper.registerProcessEventListener(app, new DefaultKogitoProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                startedProcesses.add(event.getProcessInstance().getProcessId());
            }
        });

        Process<SignalStartModel> signalStartProcess = SignalStartProcess.newProcess(app);
        org.kie.kogito.process.Process<IntermediateThrowEventSignalModel> throwEventProcess = IntermediateThrowEventSignalProcess.newProcess(app);
        org.kie.kogito.process.ProcessInstance<IntermediateThrowEventSignalModel> processInstance = throwEventProcess.createInstance(throwEventProcess.createModel());
        processInstance.start();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        assertThat(startedProcesses).hasSize(2);
        assertThat(startedProcesses).containsExactly("IntermediateThrowEventSignal", "SignalStart");

    }

    @Test
    public void testSignalStart() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        final List<ProcessInstance> startedProcesses = new ArrayList<>();
        ProcessTestHelper.registerProcessEventListener(app, new DefaultKogitoProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                startedProcesses.add(event.getProcessInstance());
            }
        });
        org.kie.kogito.process.Process<SignalStartModel> definition = SignalStartProcess.newProcess(app);
        definition.send(Sig.of("MySignal", "NewValue"));
        assertThat(startedProcesses).hasSize(1);
        assertThat(startedProcesses).extracting(ProcessInstance::getProcessId).containsExactly("SignalStart");
    }

    @Test
    public void testSignalStartDynamic() throws Exception {
        Application app = ProcessTestHelper.newApplication();

        final List<ProcessInstance> startedProcesses = new ArrayList<>();
        ProcessTestHelper.registerProcessEventListener(app, new DefaultKogitoProcessEventListener() {
            @Override
            public void afterProcessStarted(ProcessStartedEvent event) {
                logger.info("{}", event.getProcessInstance().getId());
                startedProcesses.add(event.getProcessInstance());
            }
        });
        org.kie.kogito.process.Process<SignalStartModel> definition = SignalStartProcess.newProcess(app);
        definition.send(Sig.of("MySignal", "NewValue"));
        assertThat(startedProcesses).hasSize(1);
        assertThat(startedProcesses).extracting(ProcessInstance::getProcessId).containsExactly("SignalStart");
        definition.deactivate();
        definition.send(Sig.of("MySignal", "NewValue"));
        assertThat(startedProcesses).hasSize(2);
        assertThat(startedProcesses).extracting(ProcessInstance::getProcessId).containsExactly("SignalStart", "SignalStart");
    }

    @Test
    public void testMessageStart() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        final List<ProcessInstance> startedProcesses = new ArrayList<>();
        ProcessTestHelper.registerProcessEventListener(app, new DefaultKogitoProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                startedProcesses.add(event.getProcessInstance());
            }
        });
        org.kie.kogito.process.Process<MessageStartModel> definition = MessageStartProcess.newProcess(app);
        definition.send(Sig.of("HelloMessage", "NewValue"));
        assertThat(startedProcesses).hasSize(1);
    }

    @Test
    public void testMultipleStartEventsRegularStart() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/start/BPMN2-MultipleStartEventProcessLongInterval.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        KogitoProcessInstance processInstance = kruntime
                .startProcess("MultipleStartEventProcessLongInterval");
        assertProcessInstanceActive(processInstance);
        KogitoWorkItem workItem = workItemHandler.getWorkItem();
        assertThat(workItem).isNotNull();
        assertThat(workItem.getParameter("ActorId")).isEqualTo("john");
        kruntime.getKogitoWorkItemManager().completeWorkItem(workItem.getStringId(), null);
        assertProcessInstanceFinished(processInstance, kruntime);

    }

    @Test
    public void testMultipleStartEventsStartOnTimer() throws Exception {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("StartTimer", 2);
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/start/BPMN2-MultipleStartEventProcess.bpmn2");

        kruntime.getProcessEventManager().addEventListener(countDownListener);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        final List<String> list = new ArrayList<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list.add(((KogitoProcessInstance) event.getProcessInstance()).getStringId());
            }
        });
        assertThat(list).isEmpty();
        // Timer in the process takes 500ms, so after 1 second, there should be 2 process IDs in the list.
        countDownListener.waitTillCompleted();
        assertThat(getNumberOfProcessInstances("MultipleStartEventProcess")).isEqualTo(2);

    }

    @Test
    public void testMultipleEventBasedStartEventsSignalStart() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/start/BPMN2-MultipleEventBasedStartEventProcess.bpmn2");

        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);

        final List<String> list = new ArrayList<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {
            @Override
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(((KogitoProcessInstance) event.getProcessInstance()).getStringId());
            }
        });

        kruntime.signalEvent("startSignal", null);

        assertThat(list).hasSize(1);
        KogitoWorkItem workItem = workItemHandler.getWorkItem();
        String processInstanceId = ((InternalKogitoWorkItem) workItem)
                .getProcessInstanceStringId();

        KogitoProcessInstance processInstance = kruntime
                .getProcessInstance(processInstanceId);

        assertThat(workItem).isNotNull();
        assertThat(workItem.getParameter("ActorId")).isEqualTo("john");
        kruntime.getKogitoWorkItemManager().completeWorkItem(workItem.getStringId(), null);
        assertProcessInstanceFinished(processInstance, kruntime);

    }

    @Test
    public void testMultipleEventBasedStartEventsDifferentPaths() throws Exception {
        kruntime = createKogitoProcessRuntime("BPMN2-MultipleStartEventProcessDifferentPaths.bpmn2");

        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);

        final List<String> list = new ArrayList<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {
            @Override
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(((KogitoProcessInstance) event.getProcessInstance()).getStringId());
            }
        });

        kruntime.startProcess("muliplestartevents");

        assertThat(list).hasSize(1);
        KogitoWorkItem workItem = workItemHandler.getWorkItem();
        String processInstanceId = workItem.getProcessInstanceStringId();

        KogitoProcessInstance processInstance = kruntime
                .getProcessInstance(processInstanceId);

        assertThat(workItem).isNotNull();
        assertThat(workItem.getParameter("ActorId")).isEqualTo("john");
        kruntime.getKogitoWorkItemManager().completeWorkItem(workItem.getStringId(), null);
        assertProcessInstanceFinished(processInstance, kruntime);
        assertNodeTriggered(processInstanceId, "Start", "Script 1", "User task", "End");
    }

    @Test
    public void testMultipleEventBasedStartEventsTimerDifferentPaths() throws Exception {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("StartTimer", 2);
        kruntime = createKogitoProcessRuntime("BPMN2-MultipleStartEventProcessDifferentPaths.bpmn2");
        kruntime.getProcessEventManager().addEventListener(countDownListener);

        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);

        final List<String> list = new ArrayList<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list.add(((KogitoProcessInstance) event.getProcessInstance()).getStringId());
            }
        });

        assertThat(list).isEmpty();
        // Timer in the process takes 1000ms, so after 2 seconds, there should be 2 process IDs in the list.
        countDownListener.waitTillCompleted();

        assertThat(list).hasSize(2);
        List<KogitoWorkItem> workItems = workItemHandler.getWorkItems();

        for (KogitoWorkItem workItem : workItems) {
            String processInstanceId = workItem.getProcessInstanceStringId();

            KogitoProcessInstance processInstance = kruntime
                    .getProcessInstance(processInstanceId);

            assertThat(workItem).isNotNull();
            assertThat(workItem.getParameter("ActorId")).isEqualTo("john");
            kruntime.getKogitoWorkItemManager().completeWorkItem(workItem.getStringId(), null);
            assertProcessInstanceFinished(processInstance, kruntime);
            assertNodeTriggered(processInstanceId, "StartTimer", "Script 2", "User task", "End");
        }
    }

    @Test
    public void testMultipleEventBasedStartEventsSignalDifferentPaths() throws Exception {
        kruntime = createKogitoProcessRuntime("BPMN2-MultipleStartEventProcessDifferentPaths.bpmn2");

        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);

        final List<String> list = new ArrayList<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {
            @Override
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(((KogitoProcessInstance) event.getProcessInstance()).getStringId());
            }
        });

        kruntime.signalEvent("startSignal", null);

        assertThat(list).hasSize(1);
        KogitoWorkItem workItem = workItemHandler.getWorkItem();
        String processInstanceId = ((InternalKogitoWorkItem) workItem)
                .getProcessInstanceStringId();

        KogitoProcessInstance processInstance = kruntime
                .getProcessInstance(processInstanceId);

        assertThat(workItem).isNotNull();
        assertThat(workItem.getParameter("ActorId")).isEqualTo("john");
        kruntime.getKogitoWorkItemManager().completeWorkItem(workItem.getStringId(), null);
        assertProcessInstanceFinished(processInstance, kruntime);
        assertNodeTriggered(processInstanceId, "StartSignal", "Script 3", "User task", "End");
    }

    @Test
    public void testMultipleEventBasedStartEventsStartOnTimer() {
        Application app = ProcessTestHelper.newApplication();
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("StartTimer", 2);
        ProcessTestHelper.registerProcessEventListener(app, countDownListener);
        final List<ProcessInstance> startedProcesses = new ArrayList<>();
        ProcessTestHelper.registerProcessEventListener(app, new DefaultKogitoProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                startedProcesses.add(event.getProcessInstance());
            }
        });
        MultipleEventBasedStartEventProcessProcess.newProcess(app);
        assertThat(startedProcesses).isEmpty();
        countDownListener.waitTillCompleted();
        assertThat(startedProcesses).hasSize(2);

    }

    @Test
    public void testTimerCycle() {
        Application app = ProcessTestHelper.newApplication();
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("start", 5);
        ProcessTestHelper.registerProcessEventListener(app, countDownListener);
        StartCountingListener listener = new StartCountingListener();
        ProcessTestHelper.registerProcessEventListener(app, listener);
        StartTimerCycleProcess.newProcess(app);
        countDownListener.waitTillCompleted();
        assertThat(listener.getCount("StartTimerCycle")).isEqualTo(5);
    }

    @Test
    public void testSignalStartWithTransformation() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("StartProcess", 1);
        ProcessTestHelper.registerProcessEventListener(app, countDownListener);
        final List<ProcessInstance> list = new ArrayList<>();
        ProcessTestHelper.registerProcessEventListener(app, new DefaultKogitoProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance());
            }
        });
        org.kie.kogito.process.Process<SignalStartWithTransformationModel> definition = SignalStartWithTransformationProcess.newProcess(app);
        definition.send(Sig.of("MySignal", "NewValue"));
        countDownListener.waitTillCompleted();
        assertThat(list).extracting(e -> e.getProcessId()).containsExactly("SignalStartWithTransformation");
        assertThat(list).extracting(e -> ((KogitoProcessInstance) e).getVariables()).containsExactly(Collections.singletonMap("x", "NEWVALUE"));
    }

    /**
     * This is how I would expect the start event to work (same as the recurring event)
     */

    @Test
    public void testTimerDelay() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("start", 1);
        ProcessTestHelper.registerProcessEventListener(app, countDownListener);
        StartCountingListener listener = new StartCountingListener();
        ProcessTestHelper.registerProcessEventListener(app, listener);
        StartTimerDurationProcess.newProcess(app);
        countDownListener.waitTillCompleted();
        assertThat(listener.getCount("StartTimerDuration")).isEqualTo(1);
    }

    @Test
    public void testSignalStartWithCustomEvent() throws Exception {
        kruntime = createKogitoProcessRuntime("BPMN2-SingalStartWithCustomEvent.bpmn2");
        final List<KogitoProcessInstance> list = new ArrayList<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list.add((KogitoProcessInstance) event.getProcessInstance());
            }
        });
        NotAvailableGoodsReport report = new NotAvailableGoodsReport("test");
        kruntime.signalEvent("SignalNotAvailableGoods", report);
        assertThat(getNumberOfProcessInstances("org.jbpm.example.SignalObjectProcess")).isEqualTo(1);
        assertThat(list).hasSize(1);
        assertProcessVarValue(list.get(0), "report", "NotAvailableGoodsReport{type:test}");

    }

    /**
     * Should fail as timer expression is not valid
     *
     * @throws Exception
     */
    @Test
    public void testInvalidDateTimerStart() throws Exception {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
            createKogitoProcessRuntime("timer/BPMN2-StartTimerDateInvalid.bpmn2");
        })
                .withMessageContaining("Could not parse date 'abcdef'");
    }

    /**
     * Should fail as timer expression is not valid
     *
     * @throws Exception
     */
    @Test
    public void testInvalidDurationTimerStart() throws Exception {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            createKogitoProcessRuntime("timer/BPMN2-StartTimerDurationInvalid.bpmn2");
        })
                .withMessageContaining("Could not parse delay 'abcdef'");
    }

    /**
     * Should fail as timer expression is not valid
     *
     * @throws Exception
     */
    @Test
    public void testInvalidCycleTimerStart() throws Exception {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            createKogitoProcessRuntime("timer/BPMN2-StartTimerCycleInvalid.bpmn2");
        })
                .withMessageContaining("Could not parse delay 'abcdef'");
    }

    @Test
    public void testStartithMultipleOutgoingFlows() throws Exception {
        System.setProperty("jbpm.enable.multi.con", "true");
        try {
            kruntime = createKogitoProcessRuntime("BPMN2-StartEventWithMultipleOutgoingFlows.bpmn2");

            KogitoProcessInstance pi = kruntime.startProcess("starteventwithmutlipleflows");
            assertProcessInstanceCompleted(pi);

            assertNodeTriggered(pi.getStringId(), "Script 1", "Script 2");
        } finally {
            System.clearProperty("jbpm.enable.multi.con");
        }
    }

    private static class StartCountingListener extends DefaultKogitoProcessEventListener {
        private Map<String, Integer> map = new HashMap<>();

        @Override
        public void beforeProcessStarted(ProcessStartedEvent event) {
            String processId = event.getProcessInstance().getProcessId();
            Integer count = map.get(processId);

            if (count == null) {
                map.put(processId, 1);
            } else {
                map.put(processId, count + 1);
            }
        }

        public int getCount(String processId) {
            Integer count = map.get(processId);
            return (count == null) ? 0 : count;
        }
    }

}
