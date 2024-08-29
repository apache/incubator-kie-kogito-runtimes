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

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.bpmn2.sla.IntermediateCatchEventSignalWithSLAOnEventModel;
import org.jbpm.bpmn2.sla.IntermediateCatchEventSignalWithSLAOnEventProcess;
import org.jbpm.bpmn2.sla.UserTaskWithSLAExprModel;
import org.jbpm.bpmn2.sla.UserTaskWithSLAExprProcess;
import org.jbpm.bpmn2.sla.UserTaskWithSLAModel;
import org.jbpm.bpmn2.sla.UserTaskWithSLAOnTaskModel;
import org.jbpm.bpmn2.sla.UserTaskWithSLAOnTaskProcess;
import org.jbpm.bpmn2.sla.UserTaskWithSLAProcess;
import org.jbpm.process.workitem.builtin.SystemOutWorkItemHandler;
import org.jbpm.test.utils.ProcessTestHelper;
import org.junit.jupiter.api.Test;
import org.kie.api.event.process.SLAViolatedEvent;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.kogito.Application;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;
import org.kie.kogito.internal.process.event.KogitoProcessEventListener;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.AbstractProcessInstance;
import org.kie.kogito.process.impl.Sig;

import static org.assertj.core.api.Assertions.assertThat;

public class SLAComplianceTest extends JbpmBpmn2TestCase {

    @Test
    public void testSLAonProcessViolated() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final KogitoProcessEventListener listener = new DefaultKogitoProcessEventListener() {
            @Override
            public void afterSLAViolated(SLAViolatedEvent event) {
                latch.countDown();
            }
        };

        Application app = ProcessTestHelper.newApplication();

        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ProcessTestHelper.registerHandler(app, "Human Task", workItemHandler);
        ProcessTestHelper.registerProcessEventListener(app, listener);
        org.kie.kogito.process.Process<UserTaskWithSLAModel> processDefinition = UserTaskWithSLAProcess.newProcess(app);
        ProcessInstance<UserTaskWithSLAModel> processInstance = processDefinition.createInstance(processDefinition.createModel());
        processInstance.start();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        KogitoWorkItem workItem = workItemHandler.getWorkItem();
        assertThat(workItem).isNotNull();
        assertThat(workItem.getParameter("ActorId")).isEqualTo("john");
        boolean slaViolated = latch.await(10, TimeUnit.SECONDS);
        assertThat(slaViolated).as("SLA was not violated while it is expected").isTrue();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        int slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED);
        processInstance.completeWorkItem(workItem.getStringId(), null);
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED);
    }

    @Test
    public void testSLAonProcessMet() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ProcessTestHelper.registerHandler(app, "Human Task", workItemHandler);
        org.kie.kogito.process.Process<UserTaskWithSLAModel> processDefinition = UserTaskWithSLAProcess.newProcess(app);
        ProcessInstance<UserTaskWithSLAModel> processInstance = processDefinition.createInstance(processDefinition.createModel());
        processInstance.start();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        KogitoWorkItem workItem = workItemHandler.getWorkItem();
        assertThat(workItem).isNotNull();
        assertThat(workItem.getParameter("ActorId")).isEqualTo("john");
        ProcessTestHelper.completeWorkItem(processInstance, Collections.emptyMap(), "john");
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        int slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_MET);
    }

    @Test
    public void testSLAonUserTaskViolated() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final KogitoProcessEventListener listener = new DefaultKogitoProcessEventListener() {
            @Override
            public void afterSLAViolated(SLAViolatedEvent event) {
                latch.countDown();
            }
        };

        Application app = ProcessTestHelper.newApplication();
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ProcessTestHelper.registerHandler(app, "Human Task", workItemHandler);
        ProcessTestHelper.registerProcessEventListener(app, listener);
        org.kie.kogito.process.Process<UserTaskWithSLAOnTaskModel> processDefinition = UserTaskWithSLAOnTaskProcess.newProcess(app);
        ProcessInstance<UserTaskWithSLAOnTaskModel> processInstance = processDefinition.createInstance(processDefinition.createModel());
        processInstance.start();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        KogitoWorkItem workItem = workItemHandler.getWorkItem();
        assertThat(workItem).isNotNull();
        assertThat(workItem.getParameter("ActorId")).isEqualTo("john");
        boolean slaViolated = latch.await(10, TimeUnit.SECONDS);
        assertThat(slaViolated).as("SLA was not violated while it is expected").isTrue();
        int slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_NA);
        Collection<org.kie.kogito.internal.process.runtime.KogitoNodeInstance> activeNodes = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getKogitoNodeInstances();
        assertThat(activeNodes).hasSize(1);
        org.kie.kogito.internal.process.runtime.KogitoNodeInstance userTaskNode = activeNodes.iterator().next();
        slaCompliance = ((org.jbpm.workflow.instance.NodeInstance) userTaskNode).getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED);
        ProcessTestHelper.completeWorkItem(processInstance, Collections.emptyMap(), "john");
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_NA);
        slaCompliance = ((org.jbpm.workflow.instance.NodeInstance) userTaskNode).getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED);
    }

    @Test
    public void testSLAonUserTaskMet() {
        Application app = ProcessTestHelper.newApplication();
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ProcessTestHelper.registerHandler(app, "Human Task", workItemHandler);
        org.kie.kogito.process.Process<UserTaskWithSLAOnTaskModel> processDefinition = UserTaskWithSLAOnTaskProcess.newProcess(app);
        ProcessInstance<UserTaskWithSLAOnTaskModel> processInstance = processDefinition.createInstance(processDefinition.createModel());
        processInstance.start();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        KogitoWorkItem workItem = workItemHandler.getWorkItem();
        assertThat(workItem).isNotNull();
        assertThat(workItem.getParameter("ActorId")).isEqualTo("john");
        processInstance = processDefinition.instances().findById(processInstance.id()).orElse(null);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        Collection<NodeInstance> activeNodes = ((AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getNodeInstances();
        assertThat(activeNodes).hasSize(1);
        org.kie.kogito.internal.process.runtime.KogitoNodeInstance userTaskNode = (KogitoNodeInstance) activeNodes.iterator().next();
        ProcessTestHelper.completeWorkItem(processInstance, Collections.emptyMap(), "john");
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        int slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_NA);
        slaCompliance = ((org.jbpm.workflow.instance.NodeInstance) userTaskNode).getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_MET);
    }

    @Test
    public void testSLAonProcessViolatedExternalTracking() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final KogitoProcessEventListener listener = new DefaultKogitoProcessEventListener() {
            @Override
            public void afterSLAViolated(SLAViolatedEvent event) {
                latch.countDown();
            }
        };
        Application app = ProcessTestHelper.newApplication();
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ProcessTestHelper.registerHandler(app, "Human Task", workItemHandler);
        ProcessTestHelper.registerProcessEventListener(app, listener);
        org.kie.kogito.process.Process<UserTaskWithSLAModel> processDefinition = UserTaskWithSLAProcess.newProcess(app);
        UserTaskWithSLAModel model = processDefinition.createModel();
        ProcessInstance<UserTaskWithSLAModel> processInstance = processDefinition.createInstance(model);
        processInstance.start();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        KogitoWorkItem workItem = workItemHandler.getWorkItem();
        assertThat(workItem).isNotNull();
        assertThat(workItem.getParameter("ActorId")).isEqualTo("john");
        boolean slaViolated = latch.await(5, TimeUnit.SECONDS);
        assertThat(slaViolated).as("SLA should not be violated by timer").isTrue();
        processInstance.send(Sig.of("slaViolation", null));
        slaViolated = latch.await(10, TimeUnit.SECONDS);
        assertThat(slaViolated).as("SLA was not violated while it is expected").isTrue();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        int slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED);
        ProcessTestHelper.completeWorkItem(processInstance, Collections.emptyMap(), "john");
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED);
    }

    @Test
    public void testSLAonUserTaskViolatedExternalTracking() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final KogitoProcessEventListener listener = new DefaultKogitoProcessEventListener() {
            @Override
            public void afterSLAViolated(SLAViolatedEvent event) {
                latch.countDown();
            }
        };

        Application app = ProcessTestHelper.newApplication();
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ProcessTestHelper.registerHandler(app, "Human Task", workItemHandler);
        ProcessTestHelper.registerProcessEventListener(app, listener);

        org.kie.kogito.process.Process<UserTaskWithSLAOnTaskModel> processDefinition = UserTaskWithSLAOnTaskProcess.newProcess(app);
        UserTaskWithSLAOnTaskModel model = processDefinition.createModel();
        ProcessInstance<UserTaskWithSLAOnTaskModel> processInstance = processDefinition.createInstance(model);
        processInstance.start();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);

        KogitoWorkItem workItem = workItemHandler.getWorkItem();
        assertThat(workItem).isNotNull();
        assertThat(workItem.getParameter("ActorId")).isEqualTo("john");

        boolean slaViolated = latch.await(5, TimeUnit.SECONDS);
        assertThat(slaViolated).as("SLA should not be violated by timer").isTrue();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);

        Collection<NodeInstance> activeNodes = ((AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getNodeInstances();
        assertThat(activeNodes).hasSize(1);
        KogitoNodeInstance userTaskNode = (KogitoNodeInstance) activeNodes.iterator().next();

        processInstance.send(Sig.of("slaViolation:" + userTaskNode.getStringId(), null));

        slaViolated = latch.await(10, TimeUnit.SECONDS);
        assertThat(slaViolated).as("SLA was not violated while it is expected").isTrue();

        ProcessTestHelper.completeWorkItem(processInstance, Collections.emptyMap(), "john");
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);

        int slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_NA);

        slaCompliance = ((org.jbpm.workflow.instance.NodeInstance) userTaskNode).getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED);
    }

    @Test
    public void testSLAonProcessViolatedWithExpression() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final KogitoProcessEventListener listener = new DefaultKogitoProcessEventListener() {
            @Override
            public void afterSLAViolated(SLAViolatedEvent event) {
                latch.countDown();
            }
        };
        Application app = ProcessTestHelper.newApplication();
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ProcessTestHelper.registerHandler(app, "Human Task", workItemHandler);
        ProcessTestHelper.registerProcessEventListener(app, listener);
        org.kie.kogito.process.Process<UserTaskWithSLAExprModel> processDefinition = UserTaskWithSLAExprProcess.newProcess(app);
        UserTaskWithSLAExprModel model = processDefinition.createModel();
        model.setS("3s");
        ProcessInstance<UserTaskWithSLAExprModel> processInstance = processDefinition.createInstance(model);
        processInstance.start();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        KogitoWorkItem workItem = workItemHandler.getWorkItem();
        assertThat(workItem).isNotNull();
        assertThat(workItem.getParameter("ActorId")).isEqualTo("john");
        boolean slaViolated = latch.await(10, TimeUnit.SECONDS);
        assertThat(slaViolated).as("SLA was not violated while it is expected").isTrue();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        int slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED);
        ProcessTestHelper.completeWorkItem(processInstance, Collections.emptyMap(), "john");
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED);
    }

    @Test
    public void testSLAonProcessViolatedNoTracking() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final KogitoProcessEventListener listener = new DefaultKogitoProcessEventListener() {
            @Override
            public void afterSLAViolated(SLAViolatedEvent event) {
                latch.countDown();
            }
        };
        Application app = ProcessTestHelper.newApplication();
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ProcessTestHelper.registerHandler(app, "Human Task", workItemHandler);
        ProcessTestHelper.registerProcessEventListener(app, listener);
        org.kie.kogito.process.Process<UserTaskWithSLAModel> processDefinition = UserTaskWithSLAProcess.newProcess(app);
        UserTaskWithSLAModel model = processDefinition.createModel();
        ProcessInstance<UserTaskWithSLAModel> processInstance = processDefinition.createInstance(model);
        processInstance.start();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        KogitoWorkItem workItem = workItemHandler.getWorkItem();
        assertThat(workItem).isNotNull();
        assertThat(workItem.getParameter("ActorId")).isEqualTo("john");

        boolean slaViolated = latch.await(5, TimeUnit.SECONDS);
        assertThat(slaViolated).as("SLA should not be violated by timer").isTrue();

        int slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED);

        ProcessTestHelper.completeWorkItem(processInstance, Collections.emptyMap(), "john");
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);

        slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED);
    }

    @Test
    public void testSLAonCatchEventViolated() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final KogitoProcessEventListener listener = new DefaultKogitoProcessEventListener() {
            @Override
            public void afterSLAViolated(SLAViolatedEvent event) {
                latch.countDown();
            }
        };

        Application app = ProcessTestHelper.newApplication();
        ProcessTestHelper.registerProcessEventListener(app, listener);
        ProcessTestHelper.registerHandler(app, "Human Task", new SystemOutWorkItemHandler());
        org.kie.kogito.process.Process<IntermediateCatchEventSignalWithSLAOnEventModel> processDefinition = IntermediateCatchEventSignalWithSLAOnEventProcess.newProcess(app);
        ProcessInstance<IntermediateCatchEventSignalWithSLAOnEventModel> processInstance = processDefinition.createInstance(processDefinition.createModel());
        processInstance.start();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        boolean slaViolated = latch.await(5, TimeUnit.SECONDS);
        assertThat(slaViolated).as("SLA should be violated by timer").isTrue();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        Collection<NodeInstance> activeNodes = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getNodeInstances();
        assertThat(activeNodes).hasSize(1);
        KogitoNodeInstance eventNode = (KogitoNodeInstance) activeNodes.iterator().next();
        processInstance.send(Sig.of("MyMessage", null));
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        int slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_NA);
        slaCompliance = ((org.jbpm.workflow.instance.NodeInstance) eventNode).getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED);

    }

    @Test
    public void testSLAonCatchEventNotViolated() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final KogitoProcessEventListener listener = new DefaultKogitoProcessEventListener() {
            @Override
            public void afterSLAViolated(SLAViolatedEvent event) {
                latch.countDown();
            }
        };
        Application app = ProcessTestHelper.newApplication();
        ProcessTestHelper.registerProcessEventListener(app, listener);
        ProcessTestHelper.registerHandler(app, "Human Task", new SystemOutWorkItemHandler());
        org.kie.kogito.process.Process<IntermediateCatchEventSignalWithSLAOnEventModel> processDefinition = IntermediateCatchEventSignalWithSLAOnEventProcess.newProcess(app);
        ProcessInstance<IntermediateCatchEventSignalWithSLAOnEventModel> processInstance = processDefinition.createInstance(processDefinition.createModel());
        processInstance.start();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        Collection<NodeInstance> activeNodes = ((AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getNodeInstances();
        assertThat(activeNodes).hasSize(1);
        org.kie.kogito.internal.process.runtime.KogitoNodeInstance eventNode = (KogitoNodeInstance) activeNodes.iterator().next();
        processInstance.send(Sig.of("MyMessage", null));
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        int slaCompliance = ((org.kie.kogito.process.impl.AbstractProcessInstance<?>) processInstance)
                .internalGetProcessInstance().getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_NA);
        slaCompliance = ((org.jbpm.workflow.instance.NodeInstance) eventNode).getSlaCompliance();
        assertThat(slaCompliance).isEqualTo(org.kie.api.runtime.process.ProcessInstance.SLA_MET);
        boolean slaViolated = latch.await(3, TimeUnit.SECONDS);
        assertThat(slaViolated).as("SLA should not be violated by timer").isFalse();
    }

    /*
     * Helper methods
     */

    private int getSLAComplianceForProcessInstance(KogitoProcessInstance processInstance) {
        int slaCompliance = ((org.jbpm.process.instance.ProcessInstance) processInstance).getSlaCompliance();
        return slaCompliance;
    }

    private int getSLAComplianceForNodeInstance(String processInstanceId, org.jbpm.workflow.instance.NodeInstance nodeInstance, int logType) {
        int slaCompliance = nodeInstance.getSlaCompliance();

        return slaCompliance;
    }
}
