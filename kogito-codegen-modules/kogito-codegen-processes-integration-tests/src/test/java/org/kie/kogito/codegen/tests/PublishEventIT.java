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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.auth.IdentityProviders;
import org.kie.kogito.auth.SecurityPolicy;
import org.kie.kogito.codegen.AbstractCodegenIT;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.EventPublisher;
import org.kie.kogito.event.process.MilestoneEventBody;
import org.kie.kogito.event.process.NodeInstanceDataEvent;
import org.kie.kogito.event.process.NodeInstanceEventBody;
import org.kie.kogito.event.process.ProcessInstanceDataEvent;
import org.kie.kogito.event.process.ProcessInstanceEventBody;
import org.kie.kogito.event.process.UserTaskInstanceDataEvent;
import org.kie.kogito.event.process.UserTaskInstanceEventBody;
import org.kie.kogito.event.process.VariableInstanceDataEvent;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessError;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.flexible.ItemDescription.Status;
import org.kie.kogito.uow.UnitOfWork;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class PublishEventIT extends AbstractCodegenIT {

    @Test
    public void testProcessWithMilestoneEvents() throws Exception {
        Application app = generateCodeProcessesOnly("cases/milestones/SimpleMilestone.bpmn");

        assertThat(app).isNotNull();
        TestEventPublisher publisher = new TestEventPublisher();
        app.unitOfWorkManager().eventManager().setService("http://myhost");
        app.unitOfWorkManager().eventManager().addPublisher(publisher);

        UnitOfWork uow = app.unitOfWorkManager().newUnitOfWork();
        uow.start();

        Process<? extends Model> p = app.get(Processes.class).processById("TestCase.SimpleMilestone");

        ProcessInstance<?> processInstance = p.createInstance(p.createModel());
        processInstance.start();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);

        uow.end();

        List<DataEvent<?>> events = publisher.extract();
        assertThat(events).hasSize(15);

        DataEvent<?> event = findProcessInstanceEvent(events, ProcessInstance.STATE_COMPLETED).get();
        assertThat(event).isInstanceOf(ProcessInstanceDataEvent.class);
        ProcessInstanceDataEvent processDataEvent = (ProcessInstanceDataEvent) event;
        assertThat(processDataEvent.getKogitoProcessInstanceId()).isNotNull();
        assertThat(processDataEvent.getKogitoProcessInstanceVersion()).isEqualTo("1.0");
        assertThat(processDataEvent.getKogitoParentProcessInstanceId()).isNull();
        assertThat(processDataEvent.getKogitoRootProcessInstanceId()).isNull();
        assertThat(processDataEvent.getKogitoProcessId()).isEqualTo("TestCase.SimpleMilestone");
        assertThat(processDataEvent.getKogitoProcessInstanceState()).isEqualTo("2");
        assertThat(processDataEvent.getSource()).hasToString("http://myhost/SimpleMilestone");

        Set<MilestoneEventBody> milestones = ((ProcessInstanceDataEvent) event).getData().getMilestones();
        assertThat(milestones)
                .hasSize(2)
                .extracting(e -> e.getName(), e -> e.getStatus())
                .containsExactlyInAnyOrder(tuple("AutoStartMilestone", Status.COMPLETED.name()),
                        tuple("SimpleMilestone", Status.COMPLETED.name()));
    }

    @Test
    public void testCompensationProcess() throws Exception {
        Map<TYPE, List<String>> resourcesTypeMap = new HashMap<>();
        resourcesTypeMap.put(TYPE.PROCESS, Collections.singletonList("compensation/compensateAll.bpmn2"));
        Application app = generateCode(resourcesTypeMap);
        assertThat(app).isNotNull();
        TestEventPublisher publisher = new TestEventPublisher();
        app.unitOfWorkManager().eventManager().setService("http://myhost");
        app.unitOfWorkManager().eventManager().addPublisher(publisher);

        UnitOfWork uow = app.unitOfWorkManager().newUnitOfWork();
        uow.start();

        Process<? extends Model> p = app.get(Processes.class).processById("compensateAll");

        ProcessInstance<?> processInstance = p.createInstance(p.createModel());
        processInstance.start();
        uow.end();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        Model result = (Model) processInstance.variables();
        assertThat(result.toMap()).hasSize(2).containsKeys("counter", "counter2");

        List<DataEvent<?>> events = publisher.extract();

        Optional<DataEvent<?>> event = findProcessInstanceEvent(events, ProcessInstance.STATE_COMPLETED);
        assertThat(event).as("There is no process instance event being published").isPresent();
        ProcessInstanceDataEvent processDataEvent = (ProcessInstanceDataEvent) event.orElseThrow();
        assertThat(processDataEvent.getKogitoProcessInstanceId()).isNotNull();
        assertThat(processDataEvent.getKogitoProcessInstanceVersion()).isEqualTo("1.0");
        assertThat(processDataEvent.getKogitoParentProcessInstanceId()).isNull();
        assertThat(processDataEvent.getKogitoRootProcessInstanceId()).isNull();
        assertThat(processDataEvent.getKogitoProcessId()).isEqualTo("compensateAll");
        assertThat(processDataEvent.getKogitoProcessInstanceState()).isEqualTo("2");
        assertThat(processDataEvent.getSource()).hasToString("http://myhost/compensateAll");

        ProcessInstanceEventBody body = assertProcessInstanceEvent(event.get(), "compensateAll", "Compensate All", 2);
        assertThat(body.getVariables()).hasSize(2).containsEntry("counter", 2).containsEntry("counter2", 2);

        List<NodeInstanceEventBody> nodes = findNodeInstanceEvents(events, 2);
        assertThat(nodes).hasSize(9).extractingResultOf("getNodeType").contains("StartNode", "ActionNode", "BoundaryEventNode", "EndNode");

    }

    private Optional<DataEvent<?>> findProcessInstanceEvent(List<DataEvent<?>> events, int state) {
        return events.stream().filter(ProcessInstanceDataEvent.class::isInstance).filter(e -> ((ProcessInstanceEventBody) e.getData()).getState() == state).findAny();
    }

    private List<NodeInstanceEventBody> findNodeInstanceEvents(List<DataEvent<?>> events, int eventType) {
        return events.stream().filter(NodeInstanceDataEvent.class::isInstance).map(e -> (NodeInstanceEventBody) e.getData()).filter(e -> e.getEventType() == eventType).collect(Collectors.toList());
    }

    @Test
    public void testBasicUserTaskProcess() throws Exception {
        Application app = generateCodeProcessesOnly("usertask/UserTasksProcess.bpmn2");
        assertThat(app).isNotNull();

        Process<? extends Model> p = app.get(Processes.class).processById("UserTasksProcess");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        m.fromMap(parameters);

        TestEventPublisher publisher = new TestEventPublisher();
        app.unitOfWorkManager().eventManager().setService("http://myhost");
        app.unitOfWorkManager().eventManager().addPublisher(publisher);

        UnitOfWork uow = app.unitOfWorkManager().newUnitOfWork();
        uow.start();

        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();
        uow.end();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        List<DataEvent<?>> events = publisher.extract();
        assertThat(events).hasSize(5);

        Optional<DataEvent<?>> processEvent = findProcessInstanceEvent(events, ProcessInstance.STATE_ACTIVE);
        assertProcessInstanceEvent(processEvent.get(), "UserTasksProcess", "UserTasksProcess", 1);

        List<NodeInstanceEventBody> triggered = findNodeInstanceEvents(events, 1);
        assertThat(triggered).hasSize(2).extractingResultOf("getNodeType").containsOnly("StartNode", "HumanTaskNode");

        List<NodeInstanceEventBody> left = findNodeInstanceEvents(events, 2);
        assertThat(left).hasSize(1).extractingResultOf("getNodeType").containsOnly("StartNode");

        assertUserTaskInstanceEvent(events.get(2), "FirstTask", null, "1", "Ready", "UserTasksProcess", "First Task");

        List<WorkItem> workItems = processInstance.workItems(SecurityPolicy.of(IdentityProviders.of("john")));
        assertThat(workItems).hasSize(1);
        assertThat(workItems.get(0).getName()).isEqualTo("FirstTask");

        uow = app.unitOfWorkManager().newUnitOfWork();
        uow.start();
        processInstance.completeWorkItem(workItems.get(0).getId(), null, SecurityPolicy.of(IdentityProviders.of("john")));
        uow.end();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        events = publisher.extract();

        assertThat(events).hasSize(4);

        triggered = findNodeInstanceEvents(events, 1);
        assertThat(triggered).hasSize(1).extractingResultOf("getNodeType").containsOnly("HumanTaskNode");

        left = findNodeInstanceEvents(events, 1);
        assertThat(left).hasSize(1).extractingResultOf("getNodeType").containsOnly("HumanTaskNode");

        assertUserTaskInstanceEvent(events.get(1), "SecondTask", null, "1", "Ready", "UserTasksProcess", "Second Task");
        assertUserTaskInstanceEvent(events.get(3), "FirstTask", null, "1", "Completed", "UserTasksProcess", "First Task");

        workItems = processInstance.workItems(SecurityPolicy.of(IdentityProviders.of("john")));
        assertThat(workItems).hasSize(1);
        assertThat(workItems.get(0).getName()).isEqualTo("SecondTask");

        uow = app.unitOfWorkManager().newUnitOfWork();
        uow.start();
        processInstance.completeWorkItem(workItems.get(0).getId(), null, SecurityPolicy.of(IdentityProviders.of("john")));
        uow.end();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        events = publisher.extract();

        assertThat(events).hasSize(5);
        assertProcessInstanceEvent(events.get(1), "UserTasksProcess", "UserTasksProcess", 2);

        triggered = findNodeInstanceEvents(events, 1);
        assertThat(triggered).hasSize(1).extractingResultOf("getNodeType").containsOnly("EndNode");

        left = findNodeInstanceEvents(events, 2);
        assertThat(left).hasSize(2).extractingResultOf("getNodeType").containsOnly("HumanTaskNode", "EndNode");

        assertUserTaskInstanceEvent(events.get(4), "SecondTask", null, "1", "Completed", "UserTasksProcess", "Second Task");
    }

    @Test
    public void testBasicUserTaskProcessAbort() throws Exception {
        Application app = generateCodeProcessesOnly("usertask/UserTasksProcess.bpmn2");
        assertThat(app).isNotNull();

        Process<? extends Model> p = app.get(Processes.class).processById("UserTasksProcess");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        m.fromMap(parameters);

        TestEventPublisher publisher = new TestEventPublisher();
        app.unitOfWorkManager().eventManager().setService("http://myhost");
        app.unitOfWorkManager().eventManager().addPublisher(publisher);

        UnitOfWork uow = app.unitOfWorkManager().newUnitOfWork();
        uow.start();

        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();
        uow.end();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        List<DataEvent<?>> events = publisher.extract();

        assertThat(events).hasSize(5);
        Optional<DataEvent<?>> active = findProcessInstanceEvent(events, ProcessInstance.STATE_ACTIVE);
        assertProcessInstanceEvent(active.get(), "UserTasksProcess", "UserTasksProcess", ProcessInstance.STATE_ACTIVE);

        List<NodeInstanceEventBody> triggered = findNodeInstanceEvents(events, 1);
        assertThat(triggered).hasSize(2).extractingResultOf("getNodeName").containsOnly("StartProcess", "First Task");

        assertUserTaskInstanceEvent(events.get(2), "FirstTask", null, "1", "Ready", "UserTasksProcess", "First Task");

        List<WorkItem> workItems = processInstance.workItems(SecurityPolicy.of(IdentityProviders.of("john")));
        assertThat(workItems).hasSize(1);
        assertThat(workItems.get(0).getName()).isEqualTo("FirstTask");

        uow = app.unitOfWorkManager().newUnitOfWork();
        uow.start();
        processInstance.abort();
        uow.end();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ABORTED);
        events = publisher.extract();
        assertThat(events).hasSize(3);

        triggered = findNodeInstanceEvents(events, 2);
        assertThat(triggered).hasSize(1).extractingResultOf("getNodeName").containsOnly("First Task");

        assertProcessInstanceEvent(events.get(2), "UserTasksProcess", "UserTasksProcess", ProcessInstance.STATE_ABORTED);

    }

    @Test
    public void testBasicUserTaskProcessWithSecurityRoles() throws Exception {

        Application app = generateCodeProcessesOnly("usertask/UserTasksProcessWithSecurityRoles.bpmn2");
        assertThat(app).isNotNull();

        Process<? extends Model> p = app.get(Processes.class).processById("UserTasksProcess");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        m.fromMap(parameters);

        TestEventPublisher publisher = new TestEventPublisher();
        app.unitOfWorkManager().eventManager().setService("http://myhost");
        app.unitOfWorkManager().eventManager().addPublisher(publisher);

        UnitOfWork uow = app.unitOfWorkManager().newUnitOfWork();
        uow.start();

        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();
        uow.end();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        List<DataEvent<?>> events = publisher.extract();

        assertThat(events).hasSize(5);
        Optional<DataEvent<?>> completed = findProcessInstanceEvent(events, 1);
        ProcessInstanceEventBody body = assertProcessInstanceEvent(completed.get(), "UserTasksProcess", "UserTasksProcess", 1);
        assertThat(body.getRoles()).hasSize(2).contains("employees", "managers");

        List<NodeInstanceEventBody> triggered = findNodeInstanceEvents(events, 1);
        assertThat(triggered).hasSize(2).extractingResultOf("getNodeType").containsOnly("StartNode", "HumanTaskNode");

        List<NodeInstanceEventBody> left = findNodeInstanceEvents(events, 2);
        assertThat(left).hasSize(1).extractingResultOf("getNodeType").containsOnly("StartNode");

        assertUserTaskInstanceEvent(events.get(2), "FirstTask", null, "1", "Ready", "UserTasksProcess", "First Task");
    }

    @Test
    public void testBasicCallActivityTask() throws Exception {

        Application app = generateCodeProcessesOnly("subprocess/CallActivity.bpmn2", "subprocess/CallActivitySubProcess.bpmn2");
        assertThat(app).isNotNull();

        Process<? extends Model> p = app.get(Processes.class).processById("ParentProcess");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("x", "a");
        parameters.put("y", "b");
        m.fromMap(parameters);

        TestEventPublisher publisher = new TestEventPublisher();
        app.unitOfWorkManager().eventManager().setService("http://myhost");
        app.unitOfWorkManager().eventManager().addPublisher(publisher);

        UnitOfWork uow = app.unitOfWorkManager().newUnitOfWork();
        uow.start();

        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();

        uow.end();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        Model result = (Model) processInstance.variables();
        assertThat(result.toMap()).hasSize(2).containsKeys("x", "y")
                .isNotNull().containsEntry("y", "new value")
                .isNotNull().containsEntry("x", "a");

        List<DataEvent<?>> events = publisher.extract();
        assertThat(events).hasSize(19);

        List<DataEvent<?>> parentEvents = events.stream().filter(e -> e.getKogitoProcessId().equals("ParentProcess")).collect(Collectors.toList());
        List<DataEvent<?>> childEvents = events.stream().filter(e -> e.getKogitoProcessId().equals("SubProcess")).collect(Collectors.toList());

        DataEvent<?> parentBody = findProcessInstanceEvent(parentEvents, 2).get();
        DataEvent<?> childBody = findProcessInstanceEvent(childEvents, 2).get();

        assertProcessInstanceEvent(parentBody, "ParentProcess", "Parent Process", 2);
        assertThat(findNodeInstanceEvents(parentEvents, 2)).hasSize(3).extractingResultOf("getNodeType").containsOnly("StartNode", "SubProcessNode", "EndNode");

        assertProcessInstanceEventWithParentId(childBody, "SubProcess", "Sub Process", 2);
        assertThat(findNodeInstanceEvents(childEvents, 2)).hasSize(3).extractingResultOf("getNodeType").containsOnly("StartNode", "ActionNode", "EndNode");

    }

    @Test
    public void testExclusiveGatewayStartToEnd() throws Exception {

        Application app = generateCodeProcessesOnly("gateway/ExclusiveSplit.bpmn2");
        assertThat(app).isNotNull();
        TestEventPublisher publisher = new TestEventPublisher();
        app.unitOfWorkManager().eventManager().setService("http://myhost");
        app.unitOfWorkManager().eventManager().addPublisher(publisher);

        UnitOfWork uow = app.unitOfWorkManager().newUnitOfWork();
        uow.start();

        Process<? extends Model> p = app.get(Processes.class).processById("ExclusiveSplit");

        Map<String, Object> params = new HashMap<>();
        params.put("x", "First");
        params.put("y", "None");
        Model m = p.createModel();
        m.fromMap(params);

        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        Model result = (Model) processInstance.variables();
        assertThat(result.toMap()).hasSize(2).containsKeys("x", "y");
        uow.end();

        List<DataEvent<?>> events = publisher.extract();

        ProcessInstanceDataEvent processDataEvent = events.stream().filter(ProcessInstanceDataEvent.class::isInstance).map(ProcessInstanceDataEvent.class::cast).findFirst().orElseThrow();
        assertThat(processDataEvent.getKogitoProcessInstanceId()).isNotNull();
        assertThat(processDataEvent.getKogitoProcessInstanceVersion()).isEqualTo("1.0");
        assertThat(processDataEvent.getKogitoParentProcessInstanceId()).isNull();
        assertThat(processDataEvent.getKogitoRootProcessInstanceId()).isNull();
        assertThat(processDataEvent.getKogitoProcessId()).isEqualTo("ExclusiveSplit");
        assertThat(processDataEvent.getKogitoProcessInstanceState()).isEqualTo("2");

        assertProcessInstanceEvent(processDataEvent, "ExclusiveSplit", "Test", 2);

        List<NodeInstanceEventBody> nodes = findNodeInstanceEvents(events, 2);
        assertThat(nodes).hasSize(6).extractingResultOf("getNodeType").contains("StartNode", "ActionNode", "Split", "Join", "EndNode", "WorkItemNode");

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testServiceTaskProcessWithError() throws Exception {

        Application app = generateCodeProcessesOnly("servicetask/ServiceProcessDifferentOperations.bpmn2");
        assertThat(app).isNotNull();
        TestEventPublisher publisher = new TestEventPublisher();
        app.unitOfWorkManager().eventManager().setService("http://myhost");
        app.unitOfWorkManager().eventManager().addPublisher(publisher);

        UnitOfWork uow = app.unitOfWorkManager().newUnitOfWork();
        uow.start();

        Process<? extends Model> p = app.get(Processes.class).processById("ServiceProcessDifferentOperations");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        m.fromMap(parameters);

        ProcessInstance processInstance = p.createInstance(m);
        processInstance.start();

        uow.end();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ERROR);
        List<DataEvent<?>> events = publisher.extract().stream().filter(ProcessInstanceDataEvent.class::isInstance).collect(Collectors.toList());
        events.stream().forEach(System.out::println);
        assertThat(events).hasSize(1);

        ProcessInstanceEventBody body = assertProcessInstanceEvent(events.get(0), "ServiceProcessDifferentOperations", "Service Process", 5);
        assertThat(body.getError()).isNotNull();
        assertThat(body.getError().getErrorMessage()).contains("java.lang.NullPointerException");
        assertThat(body.getError().getNodeDefinitionId()).isEqualTo("_38E04E27-3CCA-47F9-927B-E37DC4B8CE25");

        parameters.put("s", "john");
        m.fromMap(parameters);
        uow = app.unitOfWorkManager().newUnitOfWork();
        uow.start();
        processInstance.updateVariables(m);
        uow.end();

        events = publisher.extract();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(VariableInstanceDataEvent.class);

        uow = app.unitOfWorkManager().newUnitOfWork();
        uow.start();
        if (processInstance.error().isPresent()) {
            ((ProcessError) processInstance.error().get()).retrigger();
        }
        uow.end();

        events = publisher.extract().stream().filter(ProcessInstanceDataEvent.class::isInstance).collect(Collectors.toList());
        assertThat(events).hasSize(1);

        body = assertProcessInstanceEvent(events.get(0), "ServiceProcessDifferentOperations", "Service Process", 2);
        assertThat(body.getError()).isNull();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        Model result = (Model) processInstance.variables();
        assertThat(result.toMap()).hasSize(1).containsKeys("s")
                .isNotNull().containsEntry("s", "Goodbye Hello john!!");
    }

    /*
     * Helper methods
     */

    protected NodeInstanceEventBody assertNodeInstanceEvent(DataEvent<?> event, String processInstanceId, String nodeName, Integer eventType) {

        assertThat(event).isInstanceOf(NodeInstanceDataEvent.class);
        NodeInstanceEventBody body = ((NodeInstanceDataEvent) event).getData();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isNotNull();
        assertThat(body.getProcessInstanceId()).isEqualTo(processInstanceId);
        assertThat(body.getEventType()).isEqualTo(eventType);
        assertThat(body.getNodeName()).isEqualTo(nodeName);

        return body;
    }

    protected ProcessInstanceEventBody assertProcessInstanceEvent(DataEvent<?> event, String processId, String processName, Integer state) {

        assertThat(event).isInstanceOf(ProcessInstanceDataEvent.class);
        ProcessInstanceEventBody body = ((ProcessInstanceDataEvent) event).getData();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isNotNull();
        assertThat(body.getVersion()).isNotNull();
        assertThat(body.getStartDate()).isNotNull();
        if (state == ProcessInstance.STATE_ACTIVE || state == ProcessInstance.STATE_ERROR) {
            assertThat(body.getEndDate()).isNull();
        } else {
            assertThat(body.getEndDate()).isNotNull();
        }
        assertThat(body.getParentInstanceId()).isNull();
        assertThat(body.getRootInstanceId()).isNull();
        assertThat(body.getProcessId()).isEqualTo(processId);
        assertThat(body.getProcessName()).isEqualTo(processName);
        assertThat(body.getState()).isEqualTo(state);

        assertThat(event.getSource()).hasToString("http://myhost/" + processId);
        assertThat(event.getTime()).isBeforeOrEqualTo(ZonedDateTime.now().toOffsetDateTime());

        assertThat(((ProcessInstanceDataEvent) event).getKogitoAddons()).isEqualTo("test");

        return body;
    }

    protected UserTaskInstanceEventBody assertUserTaskInstanceEvent(DataEvent<?> event, String taskName, String taskDescription, String taskPriority, String taskState, String processId,
            String nodeName) {
        assertThat(event).isInstanceOf(UserTaskInstanceDataEvent.class);
        UserTaskInstanceEventBody body = ((UserTaskInstanceDataEvent) event).getData();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isNotNull();
        assertThat(body.getTaskName()).isEqualTo(taskName);
        assertThat(body.getTaskDescription()).isEqualTo(taskDescription);
        assertThat(body.getReferenceName()).isEqualTo(nodeName);
        assertThat(body.getTaskPriority()).isEqualTo(taskPriority);
        assertThat(body.getStartDate()).isNotNull();
        assertThat(body.getState()).isEqualTo(taskState);
        if (taskState.equals("Completed") || taskState.equals("Aborted")) {
            assertThat(body.getCompleteDate()).isNotNull();
        } else {
            assertThat(body.getCompleteDate()).isNull();
        }

        assertThat(event.getSource()).hasToString("http://myhost/" + processId);
        assertThat(event.getTime()).isBeforeOrEqualTo(ZonedDateTime.now().toOffsetDateTime());

        assertThat(((UserTaskInstanceDataEvent) event).getKogitoAddons()).isEqualTo("test");

        return body;
    }

    protected ProcessInstanceEventBody assertProcessInstanceEventWithParentId(DataEvent<?> event, String processId, String processName, Integer state) {

        assertThat(event).isInstanceOf(ProcessInstanceDataEvent.class);
        ProcessInstanceEventBody body = ((ProcessInstanceDataEvent) event).getData();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isNotNull();
        assertThat(body.getVersion()).isNotNull();
        assertThat(body.getStartDate()).isNotNull();
        if (state == ProcessInstance.STATE_ACTIVE) {
            assertThat(body.getEndDate()).isNull();
        } else {
            assertThat(body.getEndDate()).isNotNull();
        }
        assertThat(body.getParentInstanceId()).isNotNull();
        assertThat(body.getRootInstanceId()).isNotNull();
        assertThat(body.getProcessId()).isEqualTo(processId);
        assertThat(body.getProcessName()).isEqualTo(processName);
        assertThat(body.getState()).isEqualTo(state);

        return body;
    }

    private class TestEventPublisher implements EventPublisher {

        private List<DataEvent<?>> events = new ArrayList<>();

        @Override
        public void publish(DataEvent<?> event) {
            this.events.add(event);
        }

        @Override
        public void publish(Collection<DataEvent<?>> events) {
            this.events.addAll(events);
        }

        public List<DataEvent<?>> extract() {
            List<DataEvent<?>> copied = new ArrayList<>(this.events);
            this.events.clear();
            return copied;
        }
    }
}
