/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.serverless.workflow;

import org.jbpm.process.core.Work;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.serverless.workflow.api.end.End;
import org.jbpm.serverless.workflow.api.events.EventDefinition;
import org.jbpm.serverless.workflow.api.functions.Function;
import org.jbpm.serverless.workflow.api.produce.ProduceEvent;
import org.jbpm.workflow.core.impl.ConstraintImpl;
import org.jbpm.workflow.core.impl.ExtendedNodeImpl;
import org.jbpm.workflow.core.node.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkflowFactoryTest extends BaseServerlessTest {

    @Test
    public void testCreateProcess() {
        RuleFlowProcess process = testFactory.createProcess(singleRelayStateWorkflow);
        assertThat(process).isNotNull();
        assertThat(process.getId()).isEqualTo("serverless");
        assertThat(process.getName()).isEqualTo("workflow");
        assertThat(process.getVersion()).isEqualTo("1.0");
        assertThat(process.getPackageName()).isEqualTo("org.kie.kogito.serverless");
        assertThat(process.isAutoComplete()).isTrue();
        assertThat(process.getVisibility()).isEqualTo("Public");
        assertThat(process.getImports()).isNotNull();
        assertThat(process.getVariableScope()).isNotNull();
        assertThat(process.getVariableScope().getVariables()).isNotNull();
        assertThat(process.getVariableScope().getVariables()).hasSize(1);
    }

    @Test
    public void testStartNode() {
        TestNodeContainer nodeContainer = new TestNodeContainer();
        StartNode startNode = testFactory.startNode(1L, "start", nodeContainer);
        assertThat(startNode).isNotNull();
        assertThat(startNode.getName()).isEqualTo("start");
    }

    @Test
    public void testMessageStartNode() {
        TestNodeContainer nodeContainer = new TestNodeContainer();
        EventDefinition eventDefinition = new EventDefinition().withName("testEvent")
                .withSource("testSource").withType("testType");
        StartNode startNode = testFactory.messageStartNode(1L, eventDefinition, nodeContainer);

        assertThat(startNode).isNotNull();
        assertThat(startNode.getName()).isEqualTo(eventDefinition.getName());
        assertThat(startNode.getMetaData()).isNotNull();
        assertThat(startNode.getMetaData().get("TriggerType")).isEqualTo("ConsumeMessage");
        assertThat(startNode.getMetaData().get("TriggerRef")).isEqualTo(eventDefinition.getSource());
        assertThat(startNode.getMetaData().get("MessageType")).isEqualTo("com.fasterxml.jackson.databind.JsonNode");
    }

    @Test
    public void testEndNode() {
        TestNodeContainer nodeContainer = new TestNodeContainer();
        EndNode endNode = testFactory.endNode(1L, "end", true, nodeContainer);
        assertThat(endNode).isNotNull();
        assertThat(endNode.getName()).isEqualTo("end");
    }

    @Test
    public void testMessageEndNode() {
        TestNodeContainer nodeContainer = new TestNodeContainer();

        End endDef = new End().withKind(End.Kind.EVENT).withProduceEvent(
                new ProduceEvent().withNameRef("sampleEvent").withData("sampleData"));

        EndNode endNode = testFactory.messageEndNode(1L, "End", eventDefOnlyWorkflow, endDef, nodeContainer);

        assertThat(endNode).isNotNull();
        assertThat(endNode.getName()).isEqualTo("End");
        assertThat(endNode.getMetaData()).isNotNull();
        assertThat(endNode.getMetaData().get("TriggerRef")).isEqualTo("sampleSource");
        assertThat(endNode.getMetaData().get("TriggerType")).isEqualTo("ProduceMessage");
        assertThat(endNode.getMetaData().get("MessageType")).isEqualTo("com.fasterxml.jackson.databind.JsonNode");
        assertThat(endNode.getMetaData().get("MappingVariable")).isEqualTo("sampleData");
    }

    @Test
    public void testTimerNode() {
        TestNodeContainer nodeContainer = new TestNodeContainer();

        TimerNode timerNode = testFactory.timerNode(1L, "timer", "sampleDelay", nodeContainer);
        assertThat(timerNode).isNotNull();
        assertThat(timerNode.getName()).isEqualTo("timer");
        assertThat(timerNode.getMetaData().get("EventType")).isEqualTo("timer");
    }

    @Test
    public void testCallActivity() {
        TestNodeContainer nodeContainer = new TestNodeContainer();

        SubProcessNode subProcessNode = testFactory.callActivity(1L, "subprocess", "calledId", true, nodeContainer);
        assertThat(subProcessNode).isNotNull();
        assertThat(subProcessNode.getName()).isEqualTo("subprocess");
        assertThat(subProcessNode.getProcessId()).isEqualTo("calledId");
        assertThat(subProcessNode.getInMappings()).isNotNull();
        assertThat(subProcessNode.getInMappings()).hasSize(1);
        assertThat(subProcessNode.getOutMappings()).isNotNull();
        assertThat(subProcessNode.getOutMappings()).hasSize(1);
        assertThat(subProcessNode.getMetaData("BPMN.InputTypes")).isNotNull();
        assertThat(subProcessNode.getMetaData("BPMN.OutputTypes")).isNotNull();
    }

    @Test
    public void testaAdMessageEndNodeAction() {
        TestNodeContainer nodeContainer = new TestNodeContainer();
        EndNode endNode = testFactory.endNode(1L, "end", true, nodeContainer);
        assertThat(endNode).isNotNull();
        assertThat(endNode.getName()).isEqualTo("end");

        testFactory.addMessageEndNodeAction(endNode, "testVar", "testMessageType");
        assertThat(endNode.getActions(ExtendedNodeImpl.EVENT_NODE_ENTER)).hasSize(1);
    }

    @Test
    public void testAddTriggerToStartNode() {
        TestNodeContainer nodeContainer = new TestNodeContainer();
        StartNode startNode = testFactory.startNode(1L, "start", nodeContainer);
        assertThat(startNode).isNotNull();
        assertThat(startNode.getName()).isEqualTo("start");

        testFactory.addTriggerToStartNode(startNode, "testTriggerType");
        assertThat(startNode.getTriggers()).hasSize(1);
    }

    @Test
    public void testScriptNode() {
        TestNodeContainer nodeContainer = new TestNodeContainer();

        ActionNode actionNode = testFactory.scriptNode(1L, "script", "testScript", nodeContainer);
        assertThat(actionNode).isNotNull();
        assertThat(actionNode.getName()).isEqualTo("script");
        assertThat(actionNode.getAction()).isNotNull();
    }

    @Test
    public void testServiceNode() {
        TestNodeContainer nodeContainer = new TestNodeContainer();
        Function function = new Function().withName("testFunction").withType("testType")
                .withResource("testResource").withMetadata(
                        new HashMap<String, String>() {{
                            put("interface", "testInterface");
                            put("operation", "testOperation");
                            put("implementation", "testImplementation");
                        }}
                );
        WorkItemNode workItemNode = testFactory.serviceNode(1L, "testService", function, nodeContainer);
        assertThat(workItemNode).isNotNull();
        assertThat(workItemNode.getName()).isEqualTo("testService");
        assertThat(workItemNode.getMetaData().get("Type")).isEqualTo("Service Task");
        assertThat(workItemNode.getWork()).isNotNull();

        Work work = workItemNode.getWork();
        assertThat(work.getName()).isEqualTo("Service Task");
        assertThat(work.getParameter("Interface")).isEqualTo("testInterface");
        assertThat(work.getParameter("Operation")).isEqualTo("testOperation");
        assertThat(work.getParameter("interfaceImplementationRef")).isEqualTo("testInterface");
        assertThat(work.getParameter("operationImplementationRef")).isEqualTo("testOperation");
        assertThat(work.getParameter("ParameterType")).isEqualTo("com.fasterxml.jackson.databind.JsonNode");
        assertThat(work.getParameter("implementation")).isEqualTo("testImplementation");

        assertThat(workItemNode.getInMappings()).isNotNull();
        assertThat(workItemNode.getInMappings()).hasSize(1);
        assertThat(workItemNode.getOutMappings()).isNotNull();
        assertThat(workItemNode.getOutMappings()).hasSize(1);
    }

    @Test
    public void testSubProcessNode() {
        TestNodeContainer nodeContainer = new TestNodeContainer();

        CompositeContextNode compositeContextNode = testFactory.subProcessNode(1L, "subprocess", nodeContainer);
        assertThat(compositeContextNode).isNotNull();
        assertThat(compositeContextNode.getName()).isEqualTo("subprocess");
        assertThat(compositeContextNode.isAutoComplete()).isTrue();
    }

    @Test
    public void testSplitConstraint() {
        ConstraintImpl constraint = testFactory.splitConstraint("testName", "testType", "testDialect", "testConstraint", 0, true);
        assertThat(constraint).isNotNull();
        assertThat(constraint.getName()).isEqualTo("testName");
        assertThat(constraint.getType()).isEqualTo("testType");
        assertThat(constraint.getDialect()).isEqualTo("testDialect");
        assertThat(constraint.getConstraint()).isEqualTo("testConstraint");
        assertThat(constraint.getPriority()).isEqualTo(0);
        assertThat(constraint.isDefault()).isTrue();
    }

    @Test
    public void testSplitNode() {
        TestNodeContainer nodeContainer = new TestNodeContainer();

        Split split = testFactory.splitNode(1L, "testSplit", Split.TYPE_XOR, nodeContainer);
        assertThat(split).isNotNull();
        assertThat(split.getId()).isEqualTo(1L);
        assertThat(split.getName()).isEqualTo("testSplit");
        assertThat(split.getType()).isEqualTo(Split.TYPE_XOR);
        assertThat(split.getMetaData().get("UniqueId")).isEqualTo("1");
    }

    @Test
    public void testJoinNode() {
        TestNodeContainer nodeContainer = new TestNodeContainer();

        Join join = testFactory.joinNode(1L, "testJoin", Join.TYPE_XOR, nodeContainer);
        assertThat(join).isNotNull();
        assertThat(join.getId()).isEqualTo(1L);
        assertThat(join.getName()).isEqualTo("testJoin");
        assertThat(join.getType()).isEqualTo(Join.TYPE_XOR);
        assertThat(join.getMetaData().get("UniqueId")).isEqualTo("1");
    }
}
