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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jbpm.bpmn2.flow.ConditionalFlowWithoutGatewayModel;
import org.jbpm.bpmn2.flow.ConditionalFlowWithoutGatewayProcess;
import org.jbpm.bpmn2.flow.ExclusiveSplitDefaultModel;
import org.jbpm.bpmn2.flow.ExclusiveSplitDefaultNoConditionModel;
import org.jbpm.bpmn2.flow.ExclusiveSplitDefaultNoConditionProcess;
import org.jbpm.bpmn2.flow.ExclusiveSplitDefaultProcess;
import org.jbpm.bpmn2.flow.ExclusiveSplitXPathAdvancedModel;
import org.jbpm.bpmn2.flow.ExclusiveSplitXPathAdvancedProcess;
import org.jbpm.bpmn2.flow.ExclusiveSplitXPathAdvancedVarsNotSignaledModel;
import org.jbpm.bpmn2.flow.ExclusiveSplitXPathAdvancedVarsNotSignaledProcess;
import org.jbpm.bpmn2.flow.ExclusiveSplitXPathAdvancedWithVarsModel;
import org.jbpm.bpmn2.flow.ExclusiveSplitXPathAdvancedWithVarsProcess;
import org.jbpm.bpmn2.flow.GatewayTestModel;
import org.jbpm.bpmn2.flow.GatewayTestProcess;
import org.jbpm.bpmn2.flow.InclusiveSplitDefaultModel;
import org.jbpm.bpmn2.flow.InclusiveSplitDefaultProcess;
import org.jbpm.bpmn2.flow.MultipleFlowEndNodeModel;
import org.jbpm.bpmn2.flow.MultipleFlowEndNodeProcess;
import org.jbpm.bpmn2.flow.MultipleInOutgoingSequenceFlowsModel;
import org.jbpm.bpmn2.flow.MultipleInOutgoingSequenceFlowsProcess;
import org.jbpm.bpmn2.loop.MultiInstanceLoopCharacteristicsTaskModel;
import org.jbpm.bpmn2.loop.MultiInstanceLoopCharacteristicsTaskProcess;
import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.process.instance.impl.humantask.InternalHumanTaskWorkItem;
import org.jbpm.test.util.NodeLeftCountDownProcessEventListener;
import org.jbpm.test.utils.EventTrackerProcessListener;
import org.jbpm.test.utils.ProcessTestHelper;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.jbpm.workflow.instance.node.ForEachNodeInstance;
import org.jbpm.workflow.instance.node.ForEachNodeInstance.ForEachJoinNodeInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.kie.api.command.ExecutableCommand;
import org.kie.api.definition.process.WorkflowElementIdentifier;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.internal.command.RegistryContext;
import org.kie.kogito.Application;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessRuntime;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class FlowTest extends JbpmBpmn2TestCase {

    @BeforeAll
    public static void setup() throws Exception {
        VariableScope.setVariableStrictOption(true);
    }

    @AfterEach
    public void clearProperties() {
        System.clearProperty("jbpm.enable.multi.con");
    }

    @Test
    public void testExclusiveSplitWithNoConditions() throws Exception {
        try {
            createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-ExclusiveGatewayWithNoConditionsDefined.bpmn2");
            fail("Should fail as XOR gateway does not have conditions defined");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("does not have a constraint for Connection");
        }

    }

    @Test
    public void testExclusiveSplit() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-ExclusiveSplit.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Email",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<>();
        params.put("x", "First");
        params.put("y", "Second");
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "ExclusiveSplit", params);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testExclusiveSplitXPathAdvanced() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        ProcessTestHelper.registerHandler(app, "Email", new SystemOutWorkItemHandler());

        Process<ExclusiveSplitXPathAdvancedModel> definition = ExclusiveSplitXPathAdvancedProcess.newProcess(app);
        ExclusiveSplitXPathAdvancedModel model = definition.createModel();

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element hi = doc.createElement("hi");
        Element ho = doc.createElement("ho");
        hi.appendChild(ho);
        Attr attr = doc.createAttribute("value");
        ho.setAttributeNode(attr);
        attr.setValue("a");

        model.setX(hi);
        model.setY("Second");

        ProcessInstance<ExclusiveSplitXPathAdvancedModel> instance = definition.createInstance(model);
        instance.start();

        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);

    }

    @Test
    public void testExclusiveSplitXPathAdvanced2() throws Exception {

        Application app = ProcessTestHelper.newApplication();
        ProcessTestHelper.registerHandler(app, "Email", new SystemOutWorkItemHandler());

        Process<ExclusiveSplitXPathAdvancedVarsNotSignaledModel> definition = ExclusiveSplitXPathAdvancedVarsNotSignaledProcess.newProcess(app);
        ExclusiveSplitXPathAdvancedVarsNotSignaledModel model = definition.createModel();

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element hi = doc.createElement("hi");
        Element ho = doc.createElement("ho");
        hi.appendChild(ho);
        Attr attr = doc.createAttribute("value");
        ho.setAttributeNode(attr);
        attr.setValue("a");

        model.setX(hi);
        model.setY("Second");

        ProcessInstance<ExclusiveSplitXPathAdvancedVarsNotSignaledModel> instance = definition.createInstance(model);
        instance.start();

        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);

    }

    @Test
    public void testExclusiveSplitXPathAdvancedWithVars() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        ProcessTestHelper.registerHandler(app, "Email", new SystemOutWorkItemHandler());

        Process<ExclusiveSplitXPathAdvancedWithVarsModel> definition = ExclusiveSplitXPathAdvancedWithVarsProcess.newProcess(app);
        ExclusiveSplitXPathAdvancedWithVarsModel model = definition.createModel();

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element hi = doc.createElement("hi");
        Element ho = doc.createElement("ho");
        hi.appendChild(ho);
        Attr attr = doc.createAttribute("value");
        ho.setAttributeNode(attr);
        attr.setValue("a");

        model.setX(hi);
        model.setY("Second");

        ProcessInstance<ExclusiveSplitXPathAdvancedWithVarsModel> instance = definition.createInstance(model);
        instance.start();

        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);

    }

    @Test
    public void testExclusiveSplitPriority() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-ExclusiveSplitPriority.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Email",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<>();
        params.put("x", "First");
        params.put("y", "Second");
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "ExclusiveSplitPriority", params);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testExclusiveSplitDefault() throws Exception {

        Application app = ProcessTestHelper.newApplication();

        ProcessTestHelper.registerHandler(app, "Email", new SystemOutWorkItemHandler());
        org.kie.kogito.process.Process<ExclusiveSplitDefaultModel> definition = ExclusiveSplitDefaultProcess.newProcess(app);
        ExclusiveSplitDefaultModel model = definition.createModel();
        model.setX("NotFirst");
        model.setY("Second");
        org.kie.kogito.process.ProcessInstance<ExclusiveSplitDefaultModel> instance = definition.createInstance(model);
        instance.start();

        assertThat(instance.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_COMPLETED);

    }

    @Test
    public void testExclusiveXORGateway() throws Exception {
        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(
                        "<instanceMetadata><user approved=\"false\" /></instanceMetadata>"
                                .getBytes()));

        Application app = ProcessTestHelper.newApplication();

        org.kie.kogito.process.Process<GatewayTestModel> definition = GatewayTestProcess.newProcess(app);
        GatewayTestModel model = definition.createModel();
        model.setInstanceMetadata(document);
        model.setStartMessage(DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(
                        "<task subject='foobar2'/>".getBytes()))
                .getFirstChild());
        org.kie.kogito.process.ProcessInstance<GatewayTestModel> instance = definition.createInstance(model);
        instance.start();

        assertThat(instance.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_COMPLETED);

    }

    @Test
    public void testInclusiveSplit() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveSplit.bpmn2");
        Map<String, Object> params = new HashMap<>();
        params.put("x", 15);
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "InclusiveSplit", params);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testInclusiveSplitDefaultConnection() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveGatewayWithDefault.bpmn2");
        Map<String, Object> params = new HashMap<>();
        params.put("test", "c");
        KogitoProcessInstance processInstance = kruntime.startProcess("InclusiveGatewayWithDefault", params);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testInclusiveSplitAndJoin() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveSplitAndJoin.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<>();
        params.put("x", 15);
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "InclusiveSplitAndJoin", params);

        List<KogitoWorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertThat(activeWorkItems).hasSize(2);

        for (KogitoWorkItem wi : activeWorkItems) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(wi.getStringId(), null);
        }
        assertProcessInstanceFinished(processInstance, kruntime);

    }

    @Test
    public void testInclusiveSplitAndJoinLoop() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveSplitAndJoinLoop.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<>();
        params.put("x", 21);
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "InclusiveSplitAndJoinLoop", params);

        List<KogitoWorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertThat(activeWorkItems).hasSize(3);

        for (KogitoWorkItem wi : activeWorkItems) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(wi.getStringId(), null);
        }
        assertProcessInstanceFinished(processInstance, kruntime);

    }

    @Test
    public void testInclusiveSplitAndJoinLoop2() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveSplitAndJoinLoop2.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);

        Map<String, Object> params = new HashMap<>();
        params.put("x", 21);
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "InclusiveSplitAndJoinLoop2", params);

        List<KogitoWorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertThat(activeWorkItems).hasSize(3);

        for (KogitoWorkItem wi : activeWorkItems) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(wi.getStringId(), null);
        }
        assertProcessInstanceFinished(processInstance, kruntime);

    }

    @Test
    public void testInclusiveSplitAndJoinNested() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveSplitAndJoinNested.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);

        Map<String, Object> params = new HashMap<>();
        params.put("x", 15);
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "InclusiveSplitAndJoinNested", params);

        List<KogitoWorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertThat(activeWorkItems).hasSize(2);
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);

        for (KogitoWorkItem wi : activeWorkItems) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(wi.getStringId(), null);
        }

        activeWorkItems = workItemHandler.getWorkItems();
        assertThat(activeWorkItems).hasSize(2);
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);

        for (KogitoWorkItem wi : activeWorkItems) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(wi.getStringId(), null);
        }
        assertProcessInstanceFinished(processInstance, kruntime);

    }

    @Test
    public void testInclusiveSplitAndJoinEmbedded() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveSplitAndJoinEmbedded.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);

        Map<String, Object> params = new HashMap<>();
        params.put("x", 15);
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "InclusiveSplitAndJoinEmbedded", params);

        List<KogitoWorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertThat(activeWorkItems).hasSize(2);

        for (KogitoWorkItem wi : activeWorkItems) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(wi.getStringId(), null);
        }
        assertProcessInstanceFinished(processInstance, kruntime);

    }

    @Test
    public void testInclusiveSplitAndJoinWithParallel() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveSplitAndJoinWithParallel.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);

        Map<String, Object> params = new HashMap<>();
        params.put("x", 25);
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "InclusiveSplitAndJoinWithParallel", params);

        List<KogitoWorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertThat(activeWorkItems).hasSize(4);

        for (KogitoWorkItem wi : activeWorkItems) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(wi.getStringId(), null);
        }
        assertProcessInstanceFinished(processInstance, kruntime);

    }

    @Test
    public void testInclusiveSplitAndJoinWithEnd() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveSplitAndJoinWithEnd.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);

        Map<String, Object> params = new HashMap<>();
        params.put("x", 25);
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "InclusiveSplitAndJoinWithEnd", params);

        List<KogitoWorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertThat(activeWorkItems).hasSize(3);

        for (int i = 0; i < 2; i++) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(
                    activeWorkItems.get(i).getStringId(), null);
        }
        assertProcessInstanceActive(processInstance);

        kruntime.getKogitoWorkItemManager().completeWorkItem(
                activeWorkItems.get(2).getStringId(), null);
        assertProcessInstanceFinished(processInstance, kruntime);

    }

    @Test
    @Timeout(10)
    public void testInclusiveSplitAndJoinWithTimer() throws Exception {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("timer", 2);
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveSplitAndJoinWithTimer.bpmn2");
        kruntime.getProcessEventManager().addEventListener(countDownListener);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);

        Map<String, Object> params = new HashMap<>();
        params.put("x", 15);
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "InclusiveSplitAndJoinWithTimer", params);

        List<KogitoWorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertThat(activeWorkItems).hasSize(1);
        kruntime.getKogitoWorkItemManager().completeWorkItem(
                activeWorkItems.get(0).getStringId(), null);

        countDownListener.waitTillCompleted();
        assertProcessInstanceActive(processInstance);

        activeWorkItems = workItemHandler.getWorkItems();
        assertThat(activeWorkItems).hasSize(2);
        kruntime.getKogitoWorkItemManager().completeWorkItem(
                activeWorkItems.get(0).getStringId(), null);
        assertProcessInstanceActive(processInstance);

        kruntime.getKogitoWorkItemManager().completeWorkItem(
                activeWorkItems.get(1).getStringId(), null);
        assertProcessInstanceFinished(processInstance, kruntime);

    }

    @Test
    public void testInclusiveSplitAndJoinExtraPath() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveSplitAndJoinExtraPath.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);

        Map<String, Object> params = new HashMap<>();
        params.put("x", 25);
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "InclusiveSplitAndJoinExtraPath", params);

        kruntime.signalEvent("signal", null);

        List<KogitoWorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertThat(activeWorkItems).hasSize(4);

        for (int i = 0; i < 3; i++) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(
                    activeWorkItems.get(i).getStringId(), null);
        }
        assertProcessInstanceActive(processInstance);

        kruntime.getKogitoWorkItemManager().completeWorkItem(
                activeWorkItems.get(3).getStringId(), null);
        assertProcessInstanceFinished(processInstance, kruntime);

    }

    @Test
    public void testInclusiveSplitDefault() throws Exception {
        Application app = ProcessTestHelper.newApplication();

        org.kie.kogito.process.Process<InclusiveSplitDefaultModel> definition = InclusiveSplitDefaultProcess.newProcess(app);
        InclusiveSplitDefaultModel model = definition.createModel();
        model.setX(-5);
        org.kie.kogito.process.ProcessInstance<InclusiveSplitDefaultModel> instance = definition.createInstance(model);
        instance.start();
        assertThat(instance.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_COMPLETED);

    }

    @Test
    public void testInclusiveParallelExclusiveSplitNoLoop() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveNestedInParallelNestedInExclusive.bpmn2");

        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("testWI", new SystemOutWorkItemHandler());
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("testWI2", new SystemOutWorkItemHandler() {

            @Override
            public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
                Integer x = (Integer) workItem.getParameter("input1");
                x++;
                Map<String, Object> results = new HashMap<>();
                results.put("output1", x);
                manager.completeWorkItem(workItem.getStringId(), results);
            }

        });
        final Map<String, Integer> nodeInstanceExecutionCounter = new HashMap<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                logger.info(event.getNodeInstance().getNodeName());
                Integer value = nodeInstanceExecutionCounter.get(event.getNodeInstance().getNodeName());
                if (value == null) {
                    value = 0;
                }

                value++;
                nodeInstanceExecutionCounter.put(event.getNodeInstance().getNodeName(), value);
            }

        });
        Map<String, Object> params = new HashMap<>();
        params.put("x", 0);
        KogitoProcessInstance processInstance = kruntime.startProcess("InclusiveNestedInParallelNestedInExclusive", params);
        assertProcessInstanceCompleted(processInstance);

        assertThat(nodeInstanceExecutionCounter).hasSize(12);
        assertThat((int) nodeInstanceExecutionCounter.get("Start")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("XORGateway-converging")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("ANDGateway-diverging")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("ORGateway-diverging")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("testWI3")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("testWI2")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("ORGateway-converging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("Script")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("XORGateway-diverging")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("ANDGateway-converging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("testWI6")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("End")).isEqualTo(1);
    }

    @Test
    public void testInclusiveParallelExclusiveSplitLoop() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveNestedInParallelNestedInExclusive.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("testWI", new SystemOutWorkItemHandler());
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("testWI2", new SystemOutWorkItemHandler() {

            @Override
            public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
                Integer x = (Integer) workItem.getParameter("input1");
                x++;
                Map<String, Object> results = new HashMap<>();
                results.put("output1", x);
                manager.completeWorkItem(workItem.getStringId(), results);
            }

        });
        final Map<String, Integer> nodeInstanceExecutionCounter = new HashMap<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                Integer value = nodeInstanceExecutionCounter.get(event.getNodeInstance().getNodeName());
                if (value == null) {
                    value = 0;
                }

                value++;
                nodeInstanceExecutionCounter.put(event.getNodeInstance().getNodeName(), value);
            }

        });
        Map<String, Object> params = new HashMap<>();
        params.put("x", -1);
        KogitoProcessInstance processInstance = kruntime.startProcess("InclusiveNestedInParallelNestedInExclusive", params);
        assertProcessInstanceCompleted(processInstance);

        assertThat(nodeInstanceExecutionCounter).hasSize(12);
        assertThat((int) nodeInstanceExecutionCounter.get("Start")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("XORGateway-converging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("ANDGateway-diverging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("ORGateway-diverging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("testWI3")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("testWI2")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("ORGateway-converging")).isEqualTo(4);
        assertThat((int) nodeInstanceExecutionCounter.get("Script")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("XORGateway-diverging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("ANDGateway-converging")).isEqualTo(4);
        assertThat((int) nodeInstanceExecutionCounter.get("testWI6")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("End")).isEqualTo(1);
    }

    @Test
    public void testInclusiveParallelExclusiveSplitNoLoopAsync() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveNestedInParallelNestedInExclusive.bpmn2");

        TestWorkItemHandler handler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("testWI", handler);
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("testWI2", new SystemOutWorkItemHandler() {

            @Override
            public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
                Integer x = (Integer) workItem.getParameter("input1");
                x++;
                Map<String, Object> results = new HashMap<>();
                results.put("output1", x);
                manager.completeWorkItem(workItem.getStringId(), results);
            }

        });
        final Map<String, Integer> nodeInstanceExecutionCounter = new HashMap<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                Integer value = nodeInstanceExecutionCounter.get(event.getNodeInstance().getNodeName());
                if (value == null) {
                    value = new Integer(0);
                }

                value++;
                nodeInstanceExecutionCounter.put(event.getNodeInstance().getNodeName(), value);
            }

        });
        Map<String, Object> params = new HashMap<>();
        params.put("x", 0);
        KogitoProcessInstance processInstance = kruntime.startProcess("InclusiveNestedInParallelNestedInExclusive", params);
        assertProcessInstanceActive(processInstance);

        List<KogitoWorkItem> workItems = handler.getWorkItems();
        assertThat(workItems).isNotNull().hasSize(2);
        // complete work items within OR gateway
        for (KogitoWorkItem KogitoWorkItem : workItems) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(KogitoWorkItem.getStringId(), null);
        }
        assertProcessInstanceActive(processInstance);

        workItems = handler.getWorkItems();
        assertThat(workItems).isNotNull().hasSize(1);
        // complete last KogitoWorkItem after AND gateway
        for (KogitoWorkItem KogitoWorkItem : workItems) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(KogitoWorkItem.getStringId(), null);
        }
        assertProcessInstanceCompleted(processInstance);

        assertThat(nodeInstanceExecutionCounter).hasSize(12);
        assertThat((int) nodeInstanceExecutionCounter.get("Start")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("XORGateway-converging")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("ANDGateway-diverging")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("ORGateway-diverging")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("testWI3")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("testWI2")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("ORGateway-converging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("Script")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("XORGateway-diverging")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("ANDGateway-converging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("testWI6")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("End")).isEqualTo(1);
    }

    @Test
    public void testInclusiveParallelExclusiveSplitLoopAsync() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveNestedInParallelNestedInExclusive.bpmn2");

        TestWorkItemHandler handler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("testWI", handler);
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("testWI2", new SystemOutWorkItemHandler() {

            @Override
            public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
                Integer x = (Integer) workItem.getParameter("input1");
                x++;
                Map<String, Object> results = new HashMap<>();
                results.put("output1", x);
                manager.completeWorkItem(workItem.getStringId(), results);
            }

        });
        final Map<String, Integer> nodeInstanceExecutionCounter = new HashMap<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                Integer value = nodeInstanceExecutionCounter.get(event.getNodeInstance().getNodeName());
                if (value == null) {
                    value = 0;
                }

                value++;
                nodeInstanceExecutionCounter.put(event.getNodeInstance().getNodeName(), value);
            }

        });
        Map<String, Object> params = new HashMap<>();
        params.put("x", -1);
        KogitoProcessInstance processInstance = kruntime.startProcess("InclusiveNestedInParallelNestedInExclusive", params);
        assertProcessInstanceActive(processInstance);

        List<KogitoWorkItem> workItems = handler.getWorkItems();
        assertThat(workItems).isNotNull().hasSize(2);
        // complete work items within OR gateway
        for (KogitoWorkItem KogitoWorkItem : workItems) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(KogitoWorkItem.getStringId(), null);
        }
        assertProcessInstanceActive(processInstance);

        workItems = handler.getWorkItems();
        assertThat(workItems).isNotNull().hasSize(2);
        // complete work items within OR gateway
        for (KogitoWorkItem KogitoWorkItem : workItems) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(KogitoWorkItem.getStringId(), null);
        }
        assertProcessInstanceActive(processInstance);

        workItems = handler.getWorkItems();
        assertThat(workItems).isNotNull().hasSize(1);
        // complete last KogitoWorkItem after AND gateway
        for (KogitoWorkItem KogitoWorkItem : workItems) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(KogitoWorkItem.getStringId(), null);
        }
        assertProcessInstanceCompleted(processInstance);

        assertThat(nodeInstanceExecutionCounter).hasSize(12);
        assertThat((int) nodeInstanceExecutionCounter.get("Start")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("XORGateway-converging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("ANDGateway-diverging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("ORGateway-diverging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("testWI3")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("testWI2")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("ORGateway-converging")).isEqualTo(4);
        assertThat((int) nodeInstanceExecutionCounter.get("Script")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("XORGateway-diverging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("ANDGateway-converging")).isEqualTo(4);
        assertThat((int) nodeInstanceExecutionCounter.get("testWI6")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("End")).isEqualTo(1);
    }

    @Test
    public void testInclusiveSplitNested() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveGatewayNested.bpmn2");

        TestWorkItemHandler handler = new TestWorkItemHandler();
        TestWorkItemHandler handler2 = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("testWI", handler);
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("testWI2", handler2);
        Map<String, Object> params = new HashMap<>();
        KogitoProcessInstance processInstance = kruntime.startProcess("InclusiveGatewayNested", params);

        assertProcessInstanceActive(processInstance);
        kruntime.getKogitoWorkItemManager().completeWorkItem(handler.getWorkItem().getStringId(), null);
        kruntime.getKogitoWorkItemManager().completeWorkItem(handler2.getWorkItem().getStringId(), null);
        assertProcessInstanceActive(processInstance);

        List<KogitoWorkItem> workItems = handler.getWorkItems();
        assertThat(workItems).isNotNull().hasSize(2);

        for (KogitoWorkItem wi : workItems) {
            assertProcessInstanceActive(processInstance);
            kruntime.getKogitoWorkItemManager().completeWorkItem(wi.getStringId(), null);
        }
        assertProcessInstanceActive(processInstance);
        kruntime.getKogitoWorkItemManager().completeWorkItem(handler.getWorkItem().getStringId(), null);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    @Disabled("On Exit not supported, see https://issues.redhat.com/browse/KOGITO-2067")
    public void testInclusiveSplitWithLoopInside() throws Exception {
        kruntime = createKogitoProcessRuntime("BPMN2-InclusiveGatewayWithLoopInside.bpmn2");

        final Map<String, Integer> nodeInstanceExecutionCounter = new HashMap<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                logger.info("{} {}", event.getNodeInstance().getNodeName(), ((NodeInstanceImpl) event.getNodeInstance()).getLevel());
                Integer value = nodeInstanceExecutionCounter.get(event.getNodeInstance().getNodeName());
                if (value == null) {
                    value = new Integer(0);
                }

                value++;
                nodeInstanceExecutionCounter.put(event.getNodeInstance().getNodeName(), value);
            }

        });
        TestWorkItemHandler handler = new TestWorkItemHandler();
        TestWorkItemHandler handler2 = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("testWI", handler);
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("testWI2", handler2);
        Map<String, Object> params = new HashMap<>();
        params.put("x", -1);
        KogitoProcessInstance processInstance = kruntime.startProcess("Process_1", params);

        assertProcessInstanceActive(processInstance);
        List<KogitoWorkItem> workItems = handler.getWorkItems();
        assertThat(workItems).isNotNull().hasSize(2);

        for (KogitoWorkItem wi : workItems) {
            assertProcessInstanceActive(processInstance);
            kruntime.getKogitoWorkItemManager().completeWorkItem(wi.getStringId(), null);
        }

        assertProcessInstanceActive(processInstance);
        kruntime.getKogitoWorkItemManager().completeWorkItem(handler2.getWorkItem().getStringId(), null);
        assertProcessInstanceActive(processInstance);
        kruntime.getKogitoWorkItemManager().completeWorkItem(handler2.getWorkItem().getStringId(), null);
        assertProcessInstanceActive(processInstance);
        kruntime.getKogitoWorkItemManager().completeWorkItem(handler.getWorkItem().getStringId(), null);
        assertProcessInstanceCompleted(processInstance);
        assertThat(nodeInstanceExecutionCounter).hasSize(10);
        assertThat((int) nodeInstanceExecutionCounter.get("Start")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("OR diverging")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("tareaWorkflow3")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("tareaWorkflow2")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("OR converging")).isEqualTo(3);
        assertThat((int) nodeInstanceExecutionCounter.get("tareaWorkflow6")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("Script")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("XOR diverging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("XOR converging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("End")).isEqualTo(1);
    }

    @Test
    @Disabled("On Exit not supported, see https://issues.redhat.com/browse/KOGITO-2067")
    public void testInclusiveSplitWithLoopInsideSubprocess() throws Exception {
        kruntime = createKogitoProcessRuntime("BPMN2-InclusiveGatewayWithLoopInsideSubprocess.bpmn2");

        final Map<String, Integer> nodeInstanceExecutionCounter = new HashMap<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                logger.info("{} {}", event.getNodeInstance().getNodeName(), ((NodeInstanceImpl) event.getNodeInstance()).getLevel());
                Integer value = nodeInstanceExecutionCounter.get(event.getNodeInstance().getNodeName());
                if (value == null) {
                    value = 0;
                }

                value++;
                nodeInstanceExecutionCounter.put(event.getNodeInstance().getNodeName(), value);
            }

        });
        TestWorkItemHandler handler = new TestWorkItemHandler();
        TestWorkItemHandler handler2 = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("testWI", handler);
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("testWI2", handler2);
        Map<String, Object> params = new HashMap<>();
        params.put("x", -1);
        KogitoProcessInstance processInstance = kruntime.startProcess("Process_1", params);

        assertProcessInstanceActive(processInstance);
        List<KogitoWorkItem> workItems = handler.getWorkItems();
        assertThat(workItems).isNotNull().hasSize(2);

        for (KogitoWorkItem wi : workItems) {
            assertProcessInstanceActive(processInstance);
            kruntime.getKogitoWorkItemManager().completeWorkItem(wi.getStringId(), null);
        }

        assertProcessInstanceActive(processInstance);
        kruntime.getKogitoWorkItemManager().completeWorkItem(handler2.getWorkItem().getStringId(), null);
        assertProcessInstanceActive(processInstance);
        kruntime.getKogitoWorkItemManager().completeWorkItem(handler2.getWorkItem().getStringId(), null);
        assertProcessInstanceActive(processInstance);
        kruntime.getKogitoWorkItemManager().completeWorkItem(handler.getWorkItem().getStringId(), null);
        assertProcessInstanceCompleted(processInstance);
        assertThat(nodeInstanceExecutionCounter).hasSize(13);
        assertThat((int) nodeInstanceExecutionCounter.get("Start")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("Sub Process 1")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("sb-start")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("sb-end")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("OR diverging")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("tareaWorkflow3")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("tareaWorkflow2")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("OR converging")).isEqualTo(3);
        assertThat((int) nodeInstanceExecutionCounter.get("tareaWorkflow6")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("Script")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("XOR diverging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("XOR converging")).isEqualTo(2);
        assertThat((int) nodeInstanceExecutionCounter.get("End")).isEqualTo(1);
    }

    @Test
    public void testMultiInstanceLoopCharacteristicsProcessWithORGateway()
            throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-MultiInstanceLoopCharacteristicsProcessWithORgateway.bpmn2");

        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<>();
        List<Integer> myList = new ArrayList<>();
        myList.add(12);
        myList.add(15);
        params.put("list", myList);
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "MultiInstanceLoopCharacteristicsProcessWithORgateway", params);

        List<KogitoWorkItem> workItems = workItemHandler.getWorkItems();
        assertThat(workItems).hasSize(4);

        Collection<NodeInstance> nodeInstances = ((WorkflowProcessInstanceImpl) processInstance)
                .getNodeInstances();
        assertThat(nodeInstances).hasSize(1);
        NodeInstance nodeInstance = nodeInstances.iterator().next();
        assertThat(nodeInstance).isInstanceOf(ForEachNodeInstance.class);

        Collection<NodeInstance> nodeInstancesChild = ((ForEachNodeInstance) nodeInstance)
                .getNodeInstances();
        assertThat(nodeInstancesChild).hasSize(2);

        for (NodeInstance child : nodeInstancesChild) {
            assertThat(child).isInstanceOf(CompositeContextNodeInstance.class);
            assertThat(((CompositeContextNodeInstance) child)
                    .getNodeInstances()).hasSize(2);
        }

        kruntime.getKogitoWorkItemManager().completeWorkItem(
                workItems.get(0).getStringId(), null);
        kruntime.getKogitoWorkItemManager().completeWorkItem(
                workItems.get(1).getStringId(), null);

        processInstance = kruntime.getProcessInstance(processInstance.getStringId());
        nodeInstances = ((WorkflowProcessInstanceImpl) processInstance)
                .getNodeInstances();
        assertThat(nodeInstances).hasSize(1);
        nodeInstance = nodeInstances.iterator().next();
        assertThat(nodeInstance).isInstanceOf(ForEachNodeInstance.class);

        nodeInstancesChild = ((ForEachNodeInstance) nodeInstance)
                .getNodeInstances();
        assertThat(nodeInstancesChild).hasSize(2);

        Iterator<NodeInstance> childIterator = nodeInstancesChild
                .iterator();

        assertThat(childIterator.next()).isInstanceOf(CompositeContextNodeInstance.class);
        assertThat(childIterator.next()).isInstanceOf(ForEachJoinNodeInstance.class);

        kruntime.getKogitoWorkItemManager().completeWorkItem(
                workItems.get(2).getStringId(), null);
        kruntime.getKogitoWorkItemManager().completeWorkItem(
                workItems.get(3).getStringId(), null);

        assertProcessInstanceFinished(processInstance, kruntime);

    }

    @Test
    public void testInclusiveJoinWithLoopAndHumanTasks() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-InclusiveGatewayWithHumanTasksProcess.bpmn2");

        final Map<String, Integer> nodeInstanceExecutionCounter = new HashMap<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                logger.info("{} {}", event.getNodeInstance().getNodeName(), ((NodeInstanceImpl) event.getNodeInstance()).getLevel());
                Integer value = nodeInstanceExecutionCounter.get(event.getNodeInstance().getNodeName());
                if (value == null) {
                    value = 0;
                }

                value++;
                nodeInstanceExecutionCounter.put(event.getNodeInstance().getNodeName(), value);
            }

        });
        TestWorkItemHandler handler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task", handler);
        Map<String, Object> params = new HashMap<>();
        params.put("firstXor", true);
        params.put("secondXor", true);
        params.put("thirdXor", true);
        KogitoProcessInstance processInstance = kruntime.startProcess("InclusiveGatewayWithHumanTasksProcess", params);
        // simulate completion of first task
        assertProcessInstanceActive(processInstance);
        kruntime.getKogitoWorkItemManager().completeWorkItem(handler.getWorkItem().getStringId(), null);
        assertProcessInstanceActive(processInstance);
        List<KogitoWorkItem> workItems = handler.getWorkItems();
        assertThat(workItems).isNotNull().hasSize(2);

        KogitoWorkItem remainingWork = null;
        for (KogitoWorkItem wi : workItems) {
            assertProcessInstanceActive(processInstance);
            // complete second task that will trigger converging OR gateway
            if (wi.getParameter("NodeName").equals("HT Form2")) {
                kruntime.getKogitoWorkItemManager().completeWorkItem(wi.getStringId(), null);
            } else {
                remainingWork = wi;
            }
        }

        assertProcessInstanceActive(processInstance);
        kruntime.getKogitoWorkItemManager().completeWorkItem(remainingWork.getStringId(), null);
        assertProcessInstanceActive(processInstance);
        kruntime.getKogitoWorkItemManager().completeWorkItem(handler.getWorkItem().getStringId(), null);
        assertProcessInstanceCompleted(processInstance);
        assertThat(nodeInstanceExecutionCounter).hasSize(13);
        assertThat((int) nodeInstanceExecutionCounter.get("Start")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("HT Form1")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("and1")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("HT Form2")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("xor1")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("xor2")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("HT Form3")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("Koniec")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("xor 3")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("HT Form4")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("xor4")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("Koniec2")).isEqualTo(1);
        assertThat((int) nodeInstanceExecutionCounter.get("or1")).isEqualTo(1);
    }

    @Test
    public void testMultiInstanceLoopCharacteristicsProcess() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-MultiInstanceLoopCharacteristicsProcess.bpmn2");
        Map<String, Object> params = new HashMap<>();
        List<String> myList = new ArrayList<>();
        myList.add("First Item");
        myList.add("Second Item");
        params.put("list", myList);
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "MultiInstanceLoopCharacteristicsProcess", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testMultiInstanceLoopNumberTest() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-MultiInstanceLoopNumbering.bpmn2");
        Map<String, Object> params = new HashMap<>();

        final Map<String, String> nodeIdNodeNameMap = new HashMap<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                NodeInstance nodeInstance = event.getNodeInstance();
                String uniqId = ((NodeInstanceImpl) nodeInstance).getUniqueId();
                String nodeName = nodeInstance.getNode().getName();

                String prevNodeName = nodeIdNodeNameMap.put(uniqId, nodeName);
                if (prevNodeName != null) {
                    assertThat(prevNodeName).as(uniqId + " is used for more than one node instance: ").isEqualTo(nodeName);
                }
            }

        });

        TestWorkItemHandler handler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task", handler);
        KogitoProcessInstance processInstance = kruntime.startProcess("MultiInstanceLoopNumbering", params);

        List<KogitoWorkItem> workItems = handler.getWorkItems();
        logger.debug("COMPLETING TASKS.");
        kruntime.getKogitoWorkItemManager().completeWorkItem(workItems.remove(0).getStringId(), null);
        kruntime.getKogitoWorkItemManager().completeWorkItem(workItems.remove(0).getStringId(), null);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testMultiInstanceLoopCharacteristicsProcess2() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-MultiInstanceProcessWithOutputOnTask.bpmn2");

        TestWorkItemHandler handler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task", handler);
        Map<String, Object> params = new HashMap<>();
        List<String> myList = new ArrayList<>();
        List<String> myOutList = null;
        myList.add("John");
        myList.add("Mary");
        params.put("miinput", myList);

        KogitoProcessInstance processInstance = kruntime.startProcess("MultiInstanceProcessWithOutputOnTask", params);
        List<KogitoWorkItem> workItems = handler.getWorkItems();
        assertThat(workItems).isNotNull().hasSize(2);

        myOutList = (List<String>) kruntime.getKieSession().execute(new GetProcessVariableCommand(processInstance.getStringId(), "mioutput"));
        assertThat(myOutList).isNull();

        Map<String, Object> results = new HashMap<>();
        results.put("reply", "Hello John");
        kruntime.getKogitoWorkItemManager().completeWorkItem(workItems.get(0).getStringId(), results);
        myOutList = (List<String>) kruntime.getKieSession().execute(new GetProcessVariableCommand(processInstance.getStringId(), "mioutput"));
        assertThat(myOutList).isNull();

        results = new HashMap<>();
        results.put("reply", "Hello Mary");
        kruntime.getKogitoWorkItemManager().completeWorkItem(workItems.get(1).getStringId(), results);

        myOutList = (List<String>) kruntime.getKieSession().execute(new GetProcessVariableCommand(processInstance.getStringId(), "mioutput"));
        assertThat(myOutList).isNotNull().hasSize(2).contains("Hello John", "Hello Mary");

        kruntime.getKogitoWorkItemManager().completeWorkItem(handler.getWorkItem().getStringId(), null);
        assertProcessInstanceFinished(processInstance, kruntime);

    }

    @Test
    public void testMultiInstanceLoopCharacteristicsProcessWithOutput()
            throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-MultiInstanceLoopCharacteristicsProcessWithOutput.bpmn2");
        Map<String, Object> params = new HashMap<>();
        List<String> myList = new ArrayList<>();
        List<String> myListOut = new ArrayList<>();
        myList.add("First Item");
        myList.add("Second Item");
        params.put("list", myList);
        params.put("listOut", myListOut);
        assertThat(myListOut).isEmpty();
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "MultiInstanceLoopCharacteristicsProcessWithOutput", params);
        assertProcessInstanceCompleted(processInstance);
        assertThat(myListOut).hasSize(2);

    }

    @Test
    public void testMultiInstanceLoopCharacteristicsProcessWithOutputCompletionCondition()
            throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-MultiInstanceLoopCharacteristicsProcessWithOutputCmpCond.bpmn2");
        Map<String, Object> params = new HashMap<>();
        List<String> myList = new ArrayList<>();
        List<String> myListOut = new ArrayList<>();
        myList.add("First Item");
        myList.add("Second Item");
        params.put("list", myList);
        params.put("listOut", myListOut);
        assertThat(myListOut).isEmpty();
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "MultiInstanceLoopCharacteristicsProcessWithOutputCmpCond", params);
        assertProcessInstanceCompleted(processInstance);
        assertThat(myListOut).hasSize(1);

    }

    @Test
    @Disabled("On Exit not supported, see https://issues.redhat.com/browse/KOGITO-2067")
    public void testMultiInstanceLoopCharacteristicsProcessWithOutputAndScripts()
            throws Exception {
        kruntime = createKogitoProcessRuntime("BPMN2-MultiInstanceLoopCharacteristicsProcessWithOutputAndScripts.bpmn2");
        Map<String, Object> params = new HashMap<>();
        List<String> myList = new ArrayList<>();
        List<String> myListOut = new ArrayList<>();
        List<String> scriptList = new ArrayList<>();
        myList.add("First Item");
        myList.add("Second Item");
        params.put("list", myList);
        params.put("listOut", myListOut);
        params.put("scriptList", scriptList);
        assertThat(myListOut).isEmpty();
        KogitoProcessInstance processInstance = kruntime.startProcess("MultiInstanceLoopCharacteristicsProcessWithOutput", params);
        assertProcessInstanceCompleted(processInstance);
        assertThat(myListOut).hasSize(2);
        assertThat(scriptList).hasSize(2);

    }

    @Test
    @Disabled("On Exit not supported, see https://issues.redhat.com/browse/KOGITO-2067")
    public void testMultiInstanceLoopCharacteristicsTaskWithOutput()
            throws Exception {
        kruntime = createKogitoProcessRuntime("BPMN2-MultiInstanceLoopCharacteristicsTaskWithOutput.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<>();
        List<String> myList = new ArrayList<>();
        List<String> myListOut = new ArrayList<>();
        myList.add("First Item");
        myList.add("Second Item");
        params.put("list", myList);
        params.put("listOut", myListOut);
        assertThat(myListOut).isEmpty();
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "MultiInstanceLoopCharacteristicsTask", params);
        assertProcessInstanceCompleted(processInstance);
        assertThat(myListOut).hasSize(2);

    }

    @Test
    @Disabled("On Exit not supported, see https://issues.redhat.com/browse/KOGITO-2067")
    public void testMultiInstanceLoopCharacteristicsTaskWithOutputCompletionCondition()
            throws Exception {
        kruntime = createKogitoProcessRuntime("BPMN2-MultiInstanceLoopCharacteristicsTaskWithOutputCmpCond.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<>();
        List<String> myList = new ArrayList<>();
        List<String> myListOut = new ArrayList<>();
        myList.add("First Item");
        myList.add("Second Item");
        params.put("list", myList);
        params.put("listOut", myListOut);
        assertThat(myListOut).isEmpty();
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "MultiInstanceLoopCharacteristicsTask", params);
        assertProcessInstanceCompleted(processInstance);
        assertThat(myListOut).hasSize(1);

    }

    @Test
    @Disabled("On Exit not supported, see https://issues.redhat.com/browse/KOGITO-2067")
    public void testMultiInstanceLoopCharacteristicsTaskWithOutputCompletionCondition2()
            throws Exception {
        kruntime = createKogitoProcessRuntime("BPMN2-MultiInstanceLoopCharacteristicsTaskWithOutputCmpCond2.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<>();
        List<String> myList = new ArrayList<>();
        List<String> myListOut = new ArrayList<>();
        myList.add("approved");
        myList.add("rejected");
        myList.add("approved");
        myList.add("approved");
        myList.add("rejected");
        params.put("list", myList);
        params.put("listOut", myListOut);
        assertThat(myListOut).isEmpty();
        KogitoProcessInstance processInstance = kruntime.startProcess(
                "MultiInstanceLoopCharacteristicsTask", params);
        assertProcessInstanceCompleted(processInstance);
        // only two approved outcomes are required to complete multiinstance and since there was reject in between we should have
        // three elements in the list
        assertThat(myListOut).hasSize(3);

    }

    @Test
    public void testMultiInstanceLoopCharacteristicsTask() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ProcessTestHelper.registerHandler(app, "Human Task", handler);

        org.kie.kogito.process.Process<MultiInstanceLoopCharacteristicsTaskModel> definition = MultiInstanceLoopCharacteristicsTaskProcess.newProcess(app);
        MultiInstanceLoopCharacteristicsTaskModel model = definition.createModel();
        model.setList(new ArrayList<>(List.of("First Item", "Second Item")));

        org.kie.kogito.process.ProcessInstance<MultiInstanceLoopCharacteristicsTaskModel> instance = definition.createInstance(model);
        instance.start();

        List<KogitoWorkItem> workItems = handler.getWorkItems();
        assertThat(workItems).isNotNull().hasSize(2);
        assertThat(workItems.get(0).getParameter("Item")).isEqualTo("First Item");
        assertThat(workItems.get(1).getParameter("Item")).isEqualTo("Second Item");
        ProcessTestHelper.completeWorkItem(instance, "john", Collections.emptyMap());
        ProcessTestHelper.completeWorkItem(instance, "john", Collections.emptyMap());

        assertThat(instance.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_COMPLETED);

    }

    @Test
    public void testMultipleInOutgoingSequenceFlows() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("timer", 1);
        ProcessTestHelper.registerProcessEventListener(app, countDownListener);
        List<String> list = new ArrayList<>();
        ProcessTestHelper.registerProcessEventListener(app, new DefaultKogitoProcessEventListener() {
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list.add(((KogitoProcessInstance) event.getProcessInstance()).getStringId());
            }
        });

        org.kie.kogito.process.Process<MultipleInOutgoingSequenceFlowsModel> definition = MultipleInOutgoingSequenceFlowsProcess.newProcess(app);
        assertThat(list).isEmpty();

        countDownListener.waitTillCompleted();

        assertThat(list).hasSize(1);

    }

    @Test
    public void testMultipleIncomingFlowToEndNode() throws Exception {
        Application app = ProcessTestHelper.newApplication();

        org.kie.kogito.process.Process<MultipleFlowEndNodeModel> definition = MultipleFlowEndNodeProcess.newProcess(app);
        org.kie.kogito.process.ProcessInstance<MultipleFlowEndNodeModel> instance = definition.createInstance(definition.createModel());
        instance.start();

        assertThat(instance.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_COMPLETED);

    }

    @Test
    public void testMultipleEnabledOnSingleConditionalSequenceFlow() throws Exception {
        System.setProperty("jbpm.enable.multi.con", "true");
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-MultiConnEnabled.bpmn2");

        final List<WorkflowElementIdentifier> list = new ArrayList<>();
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {
            public void afterNodeTriggered(org.kie.api.event.process.ProcessNodeTriggeredEvent event) {
                if ("Task2".equals(event.getNodeInstance().getNodeName())) {
                    list.add(event.getNodeInstance().getNodeId());
                }
            }
        });

        assertThat(list).isEmpty();
        KogitoProcessInstance processInstance = kruntime.startProcess("MultiConnEnabled");
        assertProcessInstanceActive(processInstance);
        kruntime.signalEvent("signal", null, processInstance.getStringId());
        assertProcessInstanceCompleted(processInstance);

        assertThat(list).hasSize(1);
        System.clearProperty("jbpm.enable.multi.con");

    }

    @Test
    public void testMultipleInOutgoingSequenceFlowsDisable() throws Exception {
        try {
            createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-MultipleInOutgoingSequenceFlowsDisabled.bpmn2");
            fail("Should fail as multiple outgoing and incoming connections are disabled by default");
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("This type of node [ScriptTask_1, Script Task] cannot have more than one outgoing connection!");
        }
    }

    @Test
    public void testConditionalFlow() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        EventTrackerProcessListener listener = new EventTrackerProcessListener();
        ProcessTestHelper.registerProcessEventListener(app, listener);
        org.kie.kogito.process.Process<ConditionalFlowWithoutGatewayModel> definition = ConditionalFlowWithoutGatewayProcess.newProcess(app);
        org.kie.kogito.process.ProcessInstance<ConditionalFlowWithoutGatewayModel> instance = definition.createInstance(definition.createModel());
        instance.start();

        assertThat(instance.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_COMPLETED);
        assertThat(listener.tracked())
                .anyMatch(ProcessTestHelper.triggered("start"))
                .anyMatch(ProcessTestHelper.triggered("script"))
                .anyMatch(ProcessTestHelper.triggered("end1"));
        assertThat(listener.tracked())
                .noneMatch(ProcessTestHelper.triggered("end2"));
    }

    @Test
    public void testLane() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-Lane.bpmn2");

        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        KogitoProcessInstance processInstance = kruntime.startProcess("Lane");
        assertThat(processInstance.getState()).isEqualTo(KogitoProcessInstance.STATE_ACTIVE);
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        KogitoWorkItem KogitoWorkItem = workItemHandler.getWorkItem();
        assertThat(KogitoWorkItem).isNotNull();
        assertThat(KogitoWorkItem.getParameter("ActorId")).isEqualTo("john");
        Map<String, Object> results = new HashMap<>();
        ((InternalHumanTaskWorkItem) KogitoWorkItem).setActualOwner("mary");
        kruntime.getKogitoWorkItemManager().completeWorkItem(KogitoWorkItem.getStringId(),
                results);
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        KogitoWorkItem = workItemHandler.getWorkItem();
        assertThat(KogitoWorkItem).isNotNull();
        assertThat(KogitoWorkItem.getParameter("SwimlaneActorId")).isEqualTo("mary");
        kruntime.getKogitoWorkItemManager().completeWorkItem(KogitoWorkItem.getStringId(), null);
        assertProcessInstanceFinished(processInstance, kruntime);
    }

    @Test
    public void testExclusiveSplitDefaultNoCondition() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        org.kie.kogito.process.Process<ExclusiveSplitDefaultNoConditionModel> definition = ExclusiveSplitDefaultNoConditionProcess.newProcess(app);
        org.kie.kogito.process.ProcessInstance<ExclusiveSplitDefaultNoConditionModel> instance = definition.createInstance(definition.createModel());

        instance.start();

        assertThat(instance.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_COMPLETED);
    }

    @Test
    public void testMultipleGatewaysProcess() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/flow/BPMN2-MultipleGatewaysProcess.bpmn2");
        kruntime.getProcessEventManager().addEventListener(new DefaultKogitoProcessEventListener() {
            KogitoProcessInstance pi;

            @Override
            public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
                if (event.getNodeInstance().getNodeName().equals("CreateAgent")) {
                    pi.signalEvent("Signal_1", null);
                }
            }

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                logger.info("Before Node triggered event received for node: {}", event.getNodeInstance().getNodeName());
            }

            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                pi = (KogitoProcessInstance) event.getProcessInstance();

            }
        });
        Map<String, Object> params = new HashMap<>();
        params.put("action", "CreateAgent");
        KogitoProcessInstance processInstance = kruntime.startProcess("MultipleGatewaysProcess", params);

        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testTimerAndGateway() throws Exception {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("timer", 1);
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/timer/BPMN2-ParallelSplitWithTimerProcess.bpmn2");

        kruntime.getProcessEventManager().addEventListener(countDownListener);

        TestWorkItemHandler handler1 = new TestWorkItemHandler();
        TestWorkItemHandler handler2 = new TestWorkItemHandler();

        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("task1", handler1);
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("task2", handler2);

        KogitoProcessInstance instance = kruntime.createProcessInstance("ParallelSplitWithTimerProcess", new HashMap<>());
        kruntime.startProcessInstance(instance.getStringId());

        KogitoWorkItem workItem1 = handler1.getWorkItem();
        assertThat(workItem1).isNotNull();
        assertThat(handler1.getWorkItem()).isNull();
        //first safe state: task1 completed
        kruntime.getKogitoWorkItemManager().completeWorkItem(workItem1.getStringId(), null);
        kruntime.getProcessEventManager().addEventListener(countDownListener);
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("task1", handler1);
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("task2", handler2);
        //second safe state: timer completed, waiting on task2
        countDownListener.waitTillCompleted();

        KogitoWorkItem workItem2 = handler2.getWorkItem();
        //Both sides of the join are completed. But on the process instance, there are two
        //JoinInstance for the same Join, and since it is an AND join, it never reaches task2
        //It fails after the next assertion
        assertThat(workItem2).isNotNull();
        assertThat(handler2.getWorkItem()).isNull();

        kruntime.getKogitoWorkItemManager().completeWorkItem(workItem2.getStringId(), null);
        assertProcessInstanceCompleted(instance);
    }

    private static class GetProcessVariableCommand implements ExecutableCommand<Object> {

        private String processInstanceId;
        private String variableName;

        public GetProcessVariableCommand(String processInstanceId, String variableName) {
            this.processInstanceId = processInstanceId;
            this.variableName = variableName;
        }

        public Object execute(Context context) {
            KogitoProcessRuntime kruntime = InternalProcessRuntime.asKogitoProcessRuntime(((RegistryContext) context).lookup(KieSession.class));

            org.jbpm.process.instance.ProcessInstance processInstance =
                    (org.jbpm.process.instance.ProcessInstance) kruntime.getProcessInstance(processInstanceId);

            VariableScopeInstance variableScope =
                    (VariableScopeInstance) processInstance.getContextInstance(VariableScope.VARIABLE_SCOPE);

            Object variable = variableScope.getVariable(variableName);

            return variable;
        }

    }
}
