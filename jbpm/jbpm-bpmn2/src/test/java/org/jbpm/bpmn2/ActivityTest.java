/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.bpmn2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineManager;
import org.assertj.core.api.Assumptions;
import org.drools.compiler.rule.builder.PackageBuildContext;
import org.jbpm.bpmn2.handler.ReceiveTaskHandler;
import org.jbpm.bpmn2.handler.SendTaskHandler;
import org.jbpm.bpmn2.handler.ServiceTaskHandler;
import org.jbpm.bpmn2.objects.Account;
import org.jbpm.bpmn2.objects.Address;
import org.jbpm.bpmn2.objects.HelloService;
import org.jbpm.bpmn2.objects.Person;
import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.bpmn2.test.RequirePersistence;
import org.jbpm.process.builder.ActionBuilder;
import org.jbpm.process.builder.AssignmentBuilder;
import org.jbpm.process.builder.ProcessBuildContext;
import org.jbpm.process.builder.ProcessClassBuilder;
import org.jbpm.process.builder.ReturnValueEvaluatorBuilder;
import org.jbpm.process.builder.dialect.ProcessDialect;
import org.jbpm.process.builder.dialect.ProcessDialectRegistry;
import org.jbpm.process.core.ContextResolver;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.impl.DataTransformerRegistry;
import org.jbpm.process.instance.event.listeners.RuleAwareProcessEventListener;
import org.jbpm.process.instance.event.listeners.TriggerRulesEventListener;
import org.jbpm.process.instance.impl.AssignmentAction;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.test.util.NodeLeftCountDownProcessEventListener;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.Assignment;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.DynamicUtils;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.kie.api.KieBase;
import org.kie.api.command.ExecutableCommand;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.NodeContainer;
import org.kie.api.definition.process.Process;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.DataTransformer;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessContext;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.command.RegistryContext;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.kogito.process.workitems.KogitoWorkItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActivityTest extends JbpmBpmn2TestCase {

    private KieSession ksession2;

    @AfterEach
    @Override
    public void disposeSession() {
        super.disposeSession();
        if (ksession2 != null) {
            ksession2.dispose();
            ksession2 = null;
        }
    }

    @Test
    public void testMinimalProcess() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-MinimalProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess("Minimal");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testMinimalProcessImplicit() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-MinimalProcessImplicit.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess("Minimal");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testMinimalProcessWithGraphical() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-MinimalProcessWithGraphical.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess("Minimal");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testMinimalProcessWithDIGraphical() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-MinimalProcessWithDIGraphical.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess("Minimal");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testMinimalProcessMetaData() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-MinimalProcessMetaData.bpmn2");
        ksession = createKnowledgeSession(kbase);
        final List<String> list1 = new ArrayList<String>();
        final List<String> list2 = new ArrayList<String>();
        final List<String> list3 = new ArrayList<String>();
        final List<String> list4 = new ArrayList<String>();
        ksession.addEventListener(new DefaultProcessEventListener() {
			public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
				logger.debug("before node");
				Map<String, Object> metaData = event.getNodeInstance().getNode().getMetaData();
				for (Map.Entry<String, Object> entry: metaData.entrySet()) {
					logger.debug(entry.getKey() + " " + entry.getValue());
				}
				String customTag = (String) metaData.get("customTag");
				if (customTag != null) {
					list1.add(customTag);
				}
				String customTag2 = (String) metaData.get("customTag2");
				if (customTag2 != null) {
					list2.add(customTag2);
				}
			}
			public void afterVariableChanged(ProcessVariableChangedEvent event) {
				logger.debug("after variable");
				VariableScope variableScope = (VariableScope)
					((org.jbpm.process.core.impl.ProcessImpl) event.getProcessInstance().getProcess())
						.resolveContext(VariableScope.VARIABLE_SCOPE, event.getVariableId());
	        	if (variableScope == null) {
	        		return;
	        	}
	        	Map<String, Object> metaData = variableScope.findVariable(event.getVariableId()).getMetaData();
	        	for (Map.Entry<String, Object> entry: metaData.entrySet()) {
					logger.debug(entry.getKey() + " " + entry.getValue());
				}
				String customTag = (String) metaData.get("customTagVar");
				if (customTag != null) {
					list3.add(customTag);
				}
			}
			public void afterProcessStarted(ProcessStartedEvent event) {
				logger.debug("after process");
	        	Map<String, Object> metaData = event.getProcessInstance().getProcess().getMetaData();
	        	for (Map.Entry<String, Object> entry: metaData.entrySet()) {
					logger.debug(entry.getKey() + " " + entry.getValue());
				}
				String customTag = (String) metaData.get("customTagProcess");
				if (customTag != null) {
					list4.add(customTag);
				}
			}
		});
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "krisv");
        ProcessInstance processInstance = ksession.startProcess("Minimal", params);
        assertProcessInstanceCompleted(processInstance);
        assertEquals(3, list1.size());
        assertEquals(2, list2.size());
        assertEquals(1, list3.size());
        assertEquals(1, list4.size());
    }

    @Test
    public void testCompositeProcessWithDIGraphical() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-CompositeProcessWithDIGraphical.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess("Composite");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testScriptTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ScriptTask.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess("ScriptTask");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testScriptTaskJS() throws Exception {
        Assumptions.assumeThat(
                new ScriptEngineManager().getEngineByName("JavaScript")
                        .getClass().getSimpleName())
                .describedAs("GraalJS is not supported.")
                .isNotEqualTo("GraalJSScriptEngine");

        KieBase kbase = createKnowledgeBase("BPMN2-ScriptTaskJS.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "krisv");
        Person person = new Person();
        person.setName("krisv");
        params.put("person", person);

        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.startProcess("ScriptTask", params);
        assertEquals("Entry", processInstance.getVariable("x"));
        assertNull(processInstance.getVariable("y"));

        ksession.getWorkItemManager().completeWorkItem(handler.getWorkItem().getId(), null);
        assertEquals("Exit", getProcessVarValue(processInstance, "y"));
        assertEquals("tester", processInstance.getVariable("surname"));
    }
    
    @Test
    public void testScriptTaskWithIO() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-ScriptTaskWithIO.bpmn2");

        Process scriptProcess = kbase.getProcess("ScriptTask");
        assertThat(scriptProcess).isNotNull();
        Node[] nodes = ((NodeContainer) scriptProcess).getNodes();
        assertThat(nodes).hasSize(3);
        assertThat(nodes).filteredOn(n -> n instanceof ActionNode).allMatch(n -> ((ActionNode) n).getInAssociations().size() == 1 && ((ActionNode) n).getOutAssociations().size() == 1);
        ksession = createKnowledgeSession(kbase);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "John");
        ProcessInstance processInstance = ksession.startProcess("ScriptTask", params);

        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testRuleTask() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-RuleTask.bpmn2",
                "BPMN2-RuleTask.drl");
        ksession = createKnowledgeSession(kbase);
        List<String> list = new ArrayList<String>();
        ksession.setGlobal("list", list);
        ProcessInstance processInstance = ksession.startProcess("RuleTask");        
        ksession.setGlobal("list", list);
        assertTrue(list.size() == 1);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testRuleTask2() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-RuleTask2.bpmn2",
                "BPMN2-RuleTask2.drl");
        ksession = createKnowledgeSession(kbase);
        List<String> list = new ArrayList<String>();
        ksession.setGlobal("list", list);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "SomeString");
        ProcessInstance processInstance = ksession.startProcess("RuleTask",
                params);
        assertTrue(list.size() == 0);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testRuleTaskSetVariableWithReconnect() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-RuleTask2.bpmn2",
                "BPMN2-RuleTaskSetVariableReconnect.drl");
        ksession = createKnowledgeSession(kbase);

        List<String> list = new ArrayList<String>();
        ksession.setGlobal("list", list);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "SomeString");

        ProcessInstance processInstance = ksession.startProcess("RuleTask",
                params);       
        assertTrue(list.size() == 1);

        assertProcessVarValue(processInstance, "x", "AnotherString");
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    @RequirePersistence(false)
    public void testRuleTaskWithFacts() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-RuleTaskWithFact.bpmn2",
                "BPMN2-RuleTask3.drl");
        ksession = createKnowledgeSession(kbase);

        ksession.addEventListener(new AgendaEventListener() {
            public void matchCreated(MatchCreatedEvent event) {
            }

            public void matchCancelled(MatchCancelledEvent event) {
            }

            public void beforeRuleFlowGroupDeactivated(
                    org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent event) {
            }

            public void beforeRuleFlowGroupActivated(
                    org.kie.api.event.rule.RuleFlowGroupActivatedEvent event) {
            }

            public void beforeMatchFired(BeforeMatchFiredEvent event) {
            }

            public void agendaGroupPushed(
                    org.kie.api.event.rule.AgendaGroupPushedEvent event) {
            }

            public void agendaGroupPopped(
                    org.kie.api.event.rule.AgendaGroupPoppedEvent event) {
            }

            public void afterRuleFlowGroupDeactivated(
                    org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent event) {
            }

            public void afterRuleFlowGroupActivated(
                    org.kie.api.event.rule.RuleFlowGroupActivatedEvent event) {
                ksession.fireAllRules();
            }

            public void afterMatchFired(AfterMatchFiredEvent event) {
            }

        });

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "SomeString");
        ProcessInstance processInstance = ksession.startProcess("RuleTask",
                params);
        assertProcessInstanceFinished(processInstance, ksession);

        params = new HashMap<String, Object>();
        
        processInstance = ksession.startProcess("RuleTask", params);
        
        assertEquals(ProcessInstance.STATE_ERROR, processInstance.getState());           

        params = new HashMap<String, Object>();
        params.put("x", "SomeString");
        processInstance = ksession.startProcess("RuleTask", params);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testRuleTaskAcrossSessions() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-RuleTask.bpmn2",
                "BPMN2-RuleTask.drl");
        ksession = createKnowledgeSession(kbase);
        ksession2 = createKnowledgeSession(kbase);
        List<String> list1 = new ArrayList<String>();
        ksession.setGlobal("list", list1);
        List<String> list2 = new ArrayList<String>();
        ksession2.setGlobal("list", list2);
        ProcessInstance processInstance1 = ksession.startProcess("RuleTask");
        ProcessInstance processInstance2 = ksession2.startProcess("RuleTask");       
        assertProcessInstanceFinished(processInstance1, ksession);
        assertProcessInstanceFinished(processInstance2, ksession2);
    }

    @Test
    public void testUserTaskWithDataStoreScenario() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-UserTaskWithDataStore.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new DoNothingWorkItemHandler());
        ksession.startProcess("UserProcess");
        // we can't test further as user tasks are asynchronous.
    }

    @Test
    public void testUserTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-UserTask.bpmn2");
        KieSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("UserTask");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession = restoreSession(ksession, true);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("john", workItem.getParameter("ActorId"));
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
        ksession.dispose();
    }

    @Test
    public void testUserTaskVerifyParameters() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-UserTask.bpmn2");
        KieSession ksession = createKnowledgeSession(kbase);
        ksession.getEnvironment().set("deploymentId", "test-deployment-id");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("UserTask");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession = restoreSession(ksession, true);
        final WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("john", workItem.getParameter("ActorId"));
        final String pId = processInstance.getId();

        ksession.execute(new ExecutableCommand<Void>() {

            @Override
            public Void execute(Context context) {

                KieSession ksession = ((RegistryContext) context).lookup( KieSession.class );
                ProcessInstance processInstance = ksession.getProcessInstance(pId);
                assertNotNull(processInstance);
                NodeInstance nodeInstance = ((WorkflowProcessInstance) processInstance)
                        .getNodeInstance((( KogitoWorkItem ) workItem).getNodeInstanceStringId());

                assertNotNull(nodeInstance);
                assertTrue(nodeInstance instanceof WorkItemNodeInstance);
                String deploymentId = ((WorkItemNodeInstance) nodeInstance).getWorkItem().getDeploymentId();
                String nodeInstanceId = ((WorkItemNodeInstance) nodeInstance).getWorkItem().getNodeInstanceStringId();
                long nodeId = ((WorkItemNodeInstance) nodeInstance).getWorkItem().getNodeId();

                assertEquals((( KogitoWorkItem ) workItem).getDeploymentId(), deploymentId);
                assertEquals((( KogitoWorkItem ) workItem).getNodeId(), nodeId);
                assertEquals((( KogitoWorkItem ) workItem).getNodeInstanceStringId(), nodeInstanceId);

                return null;
            }
        });



        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
        ksession.dispose();
    }
    

    @Test
    public void testCallActivityWithContantsAssignment() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("subprocess/SingleTaskWithVarDef.bpmn2",
                "subprocess/InputMappingUsingValue.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("CustomTask", handler);
        Map<String, Object> params = new HashMap<String, Object>();
        ProcessInstance processInstance = ksession.startProcess("defaultPackage.InputMappingUsingValue", params);

        WorkItem workItem = handler.getWorkItem();
        assertNotNull(workItem);

        Object value = workItem.getParameter("TaskName");
        assertNotNull(value);
        assertEquals("test string", value);

        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testSubProcessWithEntryExitScripts() throws Exception {
        KieBase kbase = createKnowledgeBase("subprocess/BPMN2-SubProcessWithEntryExitScripts.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

        assertNodeTriggered(processInstance.getId(), "Task1");
        Object var1 = getProcessVarValue(processInstance, "var1");
        assertNotNull(var1);
        assertEquals("10", var1.toString());

        assertNodeTriggered(processInstance.getId(), "Task2");
        Object var2 = getProcessVarValue(processInstance, "var2");
        assertNotNull(var2);
        assertEquals("20", var2.toString());

        assertNodeTriggered(processInstance.getId(), "Task3");
        Object var3 = getProcessVarValue(processInstance, "var3");
        assertNotNull(var3);
        assertEquals("30", var3.toString());

        assertNodeTriggered(processInstance.getId(), "SubProcess");
        Object var4 = getProcessVarValue(processInstance, "var4");
        assertNotNull(var4);
        assertEquals("40", var4.toString());

        Object var5 = getProcessVarValue(processInstance, "var5");
        assertNotNull(var5);
        assertEquals("50", var5.toString());


        WorkItem workItem = handler.getWorkItem();
        assertNotNull(workItem);

        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testCallActivity() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-CallActivity.bpmn2",
                "BPMN2-CallActivitySubProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "oldValue");
        ProcessInstance processInstance = ksession.startProcess(
                "ParentProcess", params);
        assertProcessInstanceCompleted(processInstance);
        assertEquals("new value",
                ((WorkflowProcessInstance) processInstance).getVariable("y"));
    }

    @Test
    public void testCallActivityMI() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-CallActivityMI.bpmn2",
                "BPMN2-CallActivitySubProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        final List<String> subprocessStarted = new ArrayList<>();
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                if (event.getProcessInstance().getProcessId().equals("SubProcess")) {
                    subprocessStarted.add(event.getProcessInstance().getId());
                }
            }

        });

        List<String> list = new ArrayList<String>();
        list.add("first");
        list.add("second");
        List<String> listOut = new ArrayList<String>();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "oldValue");
        params.put("list", list);
        params.put("listOut", listOut);

        ProcessInstance processInstance = ksession.startProcess("ParentProcess", params);
        assertProcessInstanceCompleted(processInstance);

        assertEquals(2, subprocessStarted.size());
        listOut = (List)((WorkflowProcessInstance) processInstance).getVariable("listOut");
        assertNotNull(listOut);
        assertEquals(2, listOut.size());

        assertEquals("new value", listOut.get(0));
        assertEquals("new value", listOut.get(1));
    }

	@Test
	public void testCallActivity2() throws Exception {
		KieBase kbase = createKnowledgeBase("BPMN2-CallActivity2.bpmn2",
				"BPMN2-CallActivitySubProcess.bpmn2");
		ksession = createKnowledgeSession(kbase);
		TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("x", "oldValue");
		ProcessInstance processInstance = ksession.startProcess(
				"ParentProcess", params);
		assertProcessInstanceActive(processInstance);
		assertEquals("new value",
				((WorkflowProcessInstance) processInstance).getVariable("y"));

		ksession = restoreSession(ksession, true);
		WorkItem workItem = workItemHandler.getWorkItem();
		assertNotNull(workItem);
		assertEquals("krisv", workItem.getParameter("ActorId"));
		ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

		assertProcessInstanceFinished(processInstance, ksession);
	}

    @Test
    public void testCallActivityByName() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-CallActivityByName.bpmn2",
                "BPMN2-CallActivitySubProcess.bpmn2",
                "BPMN2-CallActivitySubProcessV2.bpmn2");
        ksession = createKnowledgeSession(kbase);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "oldValue");
        ProcessInstance processInstance = ksession.startProcess(
                "ParentProcess", params);
        assertProcessInstanceCompleted(processInstance);
        assertEquals("new value V2",
                ((WorkflowProcessInstance) processInstance).getVariable("y"));
    }
  
    @Test
    public void testSubProcess() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-SubProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                logger.debug(event.toString());
            }

            public void beforeVariableChanged(ProcessVariableChangedEvent event) {
                logger.debug(event.toString());
            }

            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                logger.debug(event.toString());
            }
        });
        ProcessInstance processInstance = ksession.startProcess("SubProcess");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testInvalidSubProcess() throws Exception {
    	try {
    		KieBase kbase = createKnowledgeBase("BPMN2-SubProcessInvalid.bpmn2");
    		ksession = createKnowledgeSession(kbase);
    		fail("Process should be invalid, there should be build errors");
    	} catch (RuntimeException e) {
    		// there should be build errors
    	}
    }
    
    @Test
    public void testSubProcessWrongStartEvent() throws Exception {
        try {
            KieBase kbase = createKnowledgeBase("BPMN2-SubProcessWrongStartEvent.bpmn2");
            ksession = createKnowledgeSession(kbase);
            fail("Process should be invalid, there should be build errors");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Embedded subprocess can only have none start event.");
        }
    }
    
    @Test
    public void testSubProcessWrongStartEventTimer() throws Exception {
        try {
            KieBase kbase = createKnowledgeBase("SubprocessWithTimer.bpmn2");
            ksession = createKnowledgeSession(kbase);
            fail("Process should be invalid, there should be build errors");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Embedded subprocess can only have none start event.");
        }
    }
    
    @Test
    public void testMultiinstanceSubProcessWrongStartEvent() throws Exception {
        try {
            KieBase kbase = createKnowledgeBase("MultipleSubprocessWithSignalStartEvent.bpmn2");
            ksession = createKnowledgeSession(kbase);
            fail("Process should be invalid, there should be build errors");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("MultiInstance subprocess can only have none start event.");
        }
    }

    @Test
    public void testSubProcessWithTerminateEndEvent() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-SubProcessWithTerminateEndEvent.bpmn2");
        ksession = createKnowledgeSession(kbase);
        final List<String> list = new ArrayList<String>();
        ksession.addEventListener(new DefaultProcessEventListener() {

            public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
                list.add(event.getNodeInstance().getNodeName());
            }
        });
        ProcessInstance processInstance = ksession
                .startProcess("SubProcessTerminate");
        assertProcessInstanceCompleted(processInstance);
        assertEquals(7, list.size());
    }

    @Test
    public void testSubProcessWithTerminateEndEventProcessScope()
            throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-SubProcessWithTerminateEndEventProcessScope.bpmn2");
        ksession = createKnowledgeSession(kbase);
        final List<String> list = new ArrayList<String>();
        ksession.addEventListener(new DefaultProcessEventListener() {

            public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
                list.add(event.getNodeInstance().getNodeName());
            }
        });
        ProcessInstance processInstance = ksession
                .startProcess("SubProcessTerminate");
        assertProcessInstanceCompleted(processInstance);
        assertEquals(5, list.size());
    }

    @Test
    public void testAdHocProcess() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-AdHocProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess("AdHocProcess");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new DoNothingWorkItemHandler());
        logger.debug("Triggering node");
        ksession.signalEvent("Task1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        ksession.signalEvent("User1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        ksession.insert(new Person());
        ksession.signalEvent("Task3", null, processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testAdHocProcessDynamicTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-AdHocProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess("AdHocProcess");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new DoNothingWorkItemHandler());
        logger.debug("Triggering node");
        ksession.signalEvent("Task1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        TestWorkItemHandler workItemHandler2 = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("OtherTask",
                workItemHandler2);
        DynamicUtils.addDynamicWorkItem(processInstance, ksession, "OtherTask",
                new HashMap<String, Object>());
        WorkItem workItem = workItemHandler2.getWorkItem();
        assertNotNull(workItem);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        ksession.signalEvent("User1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        ksession.insert(new Person());
        ksession.signalEvent("Task3", null, processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testAdHocProcessDynamicSubProcess() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-AdHocProcess.bpmn2",
                "BPMN2-MinimalProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess("AdHocProcess");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new DoNothingWorkItemHandler());
        logger.debug("Triggering node");
        ksession.signalEvent("Task1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        TestWorkItemHandler workItemHandler2 = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("OtherTask",
                workItemHandler2);
        DynamicUtils.addDynamicSubProcess(processInstance, ksession, "Minimal",
                new HashMap<String, Object>());
        ksession = restoreSession(ksession, true);
        ksession.signalEvent("User1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        ksession.insert(new Person());
        ksession.signalEvent("Task3", null, processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testServiceTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ServiceProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Service Task",
                new ServiceTaskHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("s", "john");
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession
                .startProcess("ServiceProcess", params);
        assertProcessInstanceFinished(processInstance, ksession);
        assertEquals("Hello john!", processInstance.getVariable("s"));
    }

    @Test
    public void testServiceTaskWithAccessToWorkItemInfo() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ServiceProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Service Task",
                                                              new ServiceTaskHandler() {

                                                                  @Override
                                                                  public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
                                                                      assertThat(workItem.getProcessInstance()).isNotNull();
                                                                      assertThat(workItem.getNodeInstance()).isNotNull();
                                                                      super.executeWorkItem(workItem, manager);
                                                                  }

                                                              });
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("s", "john");
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession
                                                                                    .startProcess("ServiceProcess", params);
        assertProcessInstanceFinished(processInstance, ksession);
        assertEquals("Hello john!", processInstance.getVariable("s"));
    }

    @Test
    @Disabled("Transfomer has been disabled")
    public void testServiceTaskWithTransformation() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-ServiceProcessWithTransformation.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Service Task",
                new ServiceTaskHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("s", "JoHn");
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession
                .startProcess("ServiceProcess", params);
        assertProcessInstanceFinished(processInstance, ksession);
        assertEquals("hello john!", processInstance.getVariable("s"));
    }

    @Test
    public void testServiceTaskWithMvelTransformation() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-ServiceProcessWithMvelTransformation.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Service Task",
                new ServiceTaskHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("s", "JoHn");
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession
                .startProcess("ServiceProcess", params);
        assertProcessInstanceFinished(processInstance, ksession);
        assertEquals("hello john!", processInstance.getVariable("s"));
    }

    @Test
    public void testServiceTaskWithCustomTransformation() throws Exception {
    	DataTransformerRegistry.get().register("http://custom/transformer", new DataTransformer() {

			@Override
			public Object transform(Object expression, Map<String, Object> parameters) {
				// support only single object
				String value = parameters.values().iterator().next().toString();
				Object result = null;
				if ("caplitalizeFirst".equals(expression)) {
					String first = value.substring(0, 1);
					String main = value.substring(1, value.length());

					result = first.toUpperCase() + main;
				} else if ("caplitalizeLast".equals(expression)) {
					String last = value.substring(value.length()-1);
					String main = value.substring(0, value.length()-1);

					result = main + last.toUpperCase();
				} else {
					throw new IllegalArgumentException("Unknown expression " + expression);
				}
				return result;
			}

			@Override
			public Object compile(String expression, Map<String, Object> parameters) {
				// compilation not supported
				return expression;
			}
		});
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-ServiceProcessWithCustomTransformation.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Service Task",
                new ServiceTaskHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("s", "john doe");

        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession
                .startProcess("ServiceProcess", params);
        assertProcessInstanceFinished(processInstance, ksession);
        assertEquals("John doE", processInstance.getVariable("s"));
    }

    @Test
    public void testServiceTaskNoInterfaceName() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ServiceTask-web-service.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Service Task",
                new SystemOutWorkItemHandler() {

                    @Override
                    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
                        assertEquals("SimpleService", workItem.getParameter("Interface"));
                        assertEquals("hello", workItem.getParameter("Operation"));
                        assertEquals("java.lang.String", workItem.getParameter("ParameterType"));
                        assertEquals("##WebService", workItem.getParameter("implementation"));
                        assertEquals("hello", workItem.getParameter("operationImplementationRef"));
                        assertEquals("SimpleService", workItem.getParameter("interfaceImplementationRef"));
                        super.executeWorkItem(workItem, manager);
                    }

                });
        Map<String, Object> params = new HashMap<String, Object>();
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession
                .startProcess("org.jboss.qa.jbpm.CallWS", params);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testSendTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-SendTask.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Send Task",
                new SendTaskHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("s", "john");
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession
                .startProcess("SendTask", params);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testReceiveTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ReceiveTask.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ReceiveTaskHandler receiveTaskHandler = new ReceiveTaskHandler(ksession);
        ksession.getWorkItemManager().registerWorkItemHandler("Receive Task",
                receiveTaskHandler);
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession
                .startProcess("ReceiveTask");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        receiveTaskHandler.setKnowledgeRuntime(ksession);
        receiveTaskHandler.messageReceived("HelloMessage", "Hello john!");
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    @RequirePersistence(false)
    public void testBusinessRuleTask() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-BusinessRuleTask.bpmn2",
                "BPMN2-BusinessRuleTask.drl");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(new RuleAwareProcessEventListener());
        ProcessInstance processInstance = ksession
                .startProcess("BPMN2-BusinessRuleTask");
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    @RequirePersistence(true)
    public void testBusinessRuleTaskWithPersistence() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-BusinessRuleTask.bpmn2",
                "BPMN2-BusinessRuleTask.drl");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(new RuleAwareProcessEventListener());
        ProcessInstance processInstance = ksession
                .startProcess("BPMN2-BusinessRuleTask");

        ksession = restoreSession(ksession, true);
        ksession.addEventListener(new RuleAwareProcessEventListener());

        assertProcessInstanceFinished(processInstance, ksession);

    }

    @Test
    public void testBusinessRuleTaskDynamic() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper(
                "BPMN2-BusinessRuleTaskDynamic.bpmn2",
                "BPMN2-BusinessRuleTask.drl");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(new RuleAwareProcessEventListener());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dynamicrule", "MyRuleFlow");
        ProcessInstance processInstance = ksession.startProcess(
                "BPMN2-BusinessRuleTask", params);

        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testBusinessRuleTaskWithDataInputsWithPersistence()
            throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper(
                "BPMN2-BusinessRuleTaskWithDataInputs.bpmn2",
                "BPMN2-BusinessRuleTaskWithDataInput.drl");
        ksession = createKnowledgeSession(kbase);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", new Person());
        ProcessInstance processInstance = ksession.startProcess(
                "BPMN2-BusinessRuleTask", params);
       
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testBusinessRuleTaskWithDataInputs2WithPersistence()
            throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper(
                "BPMN2-BusinessRuleTaskWithDataInput.bpmn2",
                "BPMN2-BusinessRuleTaskWithDataInput.drl");
        ksession = createKnowledgeSession(kbase);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", new Person());
        ProcessInstance processInstance = ksession.startProcess(
                "BPMN2-BusinessRuleTask", params);

        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testBusinessRuleTaskWithContionalEvent() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-ConditionalEventRuleTask.bpmn2",
                "BPMN2-ConditionalEventRuleTask.drl");
        ksession = createKnowledgeSession(kbase);
        List<String> list = new ArrayList<String>();
        ksession.setGlobal("list", list);
        ProcessInstance processInstance = ksession.startProcess("TestFlow");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        Person person = new Person();
        person.setName("john");
        ksession.insert(person);
        

        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertTrue(list.size() == 1);
    }

    @Test
    public void testScriptTaskWithVariableByName() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("myVar", "test");
        KieBase kbase = createKnowledgeBase("BPMN2-ProcessWithVariableName.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess(
                "BPMN2-ProcessWithVariableName", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testCallActivityWithBoundaryEvent() throws Exception {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("Boundary event", 1);
        KieBase kbase = createKnowledgeBase(
                "BPMN2-CallActivityWithBoundaryEvent.bpmn2",
                "BPMN2-CallActivitySubProcessWithBoundaryEvent.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(countDownListener);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "oldValue");
        ProcessInstance processInstance = ksession.startProcess(
                "ParentProcess", params);

        countDownListener.waitTillCompleted();

        assertProcessInstanceFinished(processInstance, ksession);
        // assertEquals("new timer value",
        // ((WorkflowProcessInstance) processInstance).getVariable("y"));
        // first check the parent process executed nodes
        assertNodeTriggered(processInstance.getId(), "StartProcess",
                "CallActivity", "Boundary event", "Script Task", "end");
        // then check child process executed nodes - is there better way to get child process id than simply increment?
        assertNodeTriggered(processInstance.getId() + 1, "StartProcess2",
                "User Task");
    }

    @Test
    public void testCallActivityWithSubProcessWaitState() throws Exception {
        KieBase kbase = createKnowledgeBase(
                "BPMN2-CallActivity.bpmn2",
                "BPMN2-CallActivitySubProcessWithBoundaryEvent.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        ProcessInstance processInstance = ksession.startProcess("ParentProcess", params);
        assertProcessInstanceActive(processInstance.getId(), ksession);

        WorkItem wi = workItemHandler.getWorkItem();
        assertNotNull(wi);

        ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);

        assertProcessInstanceFinished(processInstance, ksession);
        // first check the parent process executed nodes
        assertNodeTriggered(processInstance.getId(), "StartProcess", "CallActivity", "EndProcess");
        // then check child process executed nodes - is there better way to get child process id than simply increment?
        assertNodeTriggered(processInstance.getId() + 1, "StartProcess2", "User Task", "EndProcess");
    }

    @Test
    public void testUserTaskWithBooleanOutput() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-UserTaskWithBooleanOutput.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ProcessInstance processInstance = ksession
                .startProcess("com.sample.boolean");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("john", workItem.getParameter("ActorId"));
        HashMap<String, Object> output = new HashMap<String, Object>();
        output.put("isCheckedCheckbox", "true");
        ksession.getWorkItemManager()
                .completeWorkItem(workItem.getId(), output);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testUserTaskWithSimData() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-UserTaskWithSimulationMetaData.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("UserTask");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession = restoreSession(ksession, true);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("john", workItem.getParameter("ActorId"));
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testCallActivityWithBoundaryErrorEvent() throws Exception {
        KieBase kbase = createKnowledgeBase(
                "BPMN2-CallActivityProcessBoundaryError.bpmn2",
                "BPMN2-CallActivitySubProcessBoundaryError.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("task1",
                new SystemOutWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("ParentProcess");

        assertProcessInstanceFinished(processInstance, ksession);
        assertNodeTriggered(processInstance.getId(), "StartProcess",
                "Call Activity 1", "Boundary event", "Task Parent", "End2");
        // then check child process executed nodes - is there better way to get child process id than simply increment?
        assertNodeTriggered(processInstance.getId() + 1, "StartProcess", "Task 1", "End");
    }

    @Test
    public void testCallActivityWithBoundaryErrorEventWithWaitState() throws Exception {
        KieBase kbase = createKnowledgeBase(
                "BPMN2-CallActivityProcessBoundaryError.bpmn2",
                "BPMN2-CallActivitySubProcessBoundaryError.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("task1", workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("ParentProcess");

        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

        workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

        assertProcessInstanceFinished(processInstance, ksession);
        assertNodeTriggered(processInstance.getId(), "StartProcess",
                "Call Activity 1", "Boundary event", "Task Parent", "End2");
        // then check child process executed nodes - is there better way to get child process id than simply increment?
        assertNodeTriggered(processInstance.getId() + 1, "StartProcess", "Task 1", "End");
    }

    @Test
    @Timeout(10)
    public void testInvalidServiceTask() {
        assertThrows(RuntimeException.class, () -> createKnowledgeBase("BPMN2-InvalidServiceProcess.bpmn2"));
    }

    @Test // JBPM-3951
    public void testServiceTaskInterface() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ServiceTask.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Service Task", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();

        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.startProcess("EAID_DP000000_23D3_4e7e_80FE_6D8C0AF83CAA", params);
        assertProcessInstanceFinished(processInstance, ksession);

    }

    @SuppressWarnings("unchecked")
	@Test
    @Disabled("Transfomer has been disabled")
    public void testBusinessRuleTaskWithTransformation() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-RuleTaskWithTransformation.bpmn2",
                "BPMN2-RuleTaskWithTransformation.drl");
        ksession = createKnowledgeSession(kbase);
        List<String> data = new ArrayList<String>();

        ksession.setGlobal("data", data);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "JoHn");
        ProcessInstance processInstance = ksession.startProcess("BPMN2-RuleTaskWithTransformation", params);

        assertProcessInstanceFinished(processInstance, ksession);

        data = (List<String>) ksession.getGlobal("data");
        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals("JOHN", data.get(0));

        String nameVar = getProcessVarValue(processInstance, "name");
        assertNotNull(nameVar);
        assertEquals("john", nameVar);

    }

    @Test
    @Disabled("Transfomer has been disabled")
    public void testCallActivityWithTransformation() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-CallActivityWithTransformation.bpmn2", "BPMN2-CallActivitySubProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        final List<ProcessInstance> instances = new ArrayList<ProcessInstance>();
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                instances.add(event.getProcessInstance());
            }

        });


        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "oldValue");
        ProcessInstance processInstance = ksession.startProcess("ParentProcess", params);
        assertProcessInstanceCompleted(processInstance);

        assertEquals(2, instances.size());
        // assert variables of parent process, first in start (input transformation, then on end output transformation)
        assertEquals("oldValue",((WorkflowProcessInstance) instances.get(0)).getVariable("x"));
        assertEquals("NEW VALUE",((WorkflowProcessInstance) instances.get(0)).getVariable("y"));
        // assert variables of subprocess, first in start (input transformation, then on end output transformation)
        assertEquals("OLDVALUE", ((WorkflowProcessInstance) instances.get(1)).getVariable("subX"));
        assertEquals("new value",((WorkflowProcessInstance) instances.get(1)).getVariable("subY"));
    }

    @Test
    public void testServiceTaskWithMvelCollectionTransformation() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-ServiceProcessWithMvelCollectionTransformation.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Service Task",
                new ServiceTaskHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("s", "john,poul,mary");
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession
                .startProcess("ServiceProcess", params);
        assertProcessInstanceFinished(processInstance, ksession);
        @SuppressWarnings("unchecked")
		List<String> result = (List<String>)processInstance.getVariable("list");
        assertEquals(3, result.size());
    }

    @Test
    public void testServiceTaskWithMvelJaxbTransformation() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-ServiceProcessWithMvelJaxbTransformation.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Service Task",
                new ServiceTaskHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        Person person = new Person();
        person.setId(123);
        person.setName("john");
        params.put("s", person);

        HelloService.VALIDATE_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><person><id>123</id><name>john</name></person>";

        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession
                .startProcess("ServiceProcess", params);
        assertProcessInstanceFinished(processInstance, ksession);

    }

    @Test
    public void testErrorBetweenProcessesProcess() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("subprocess/ErrorsBetweenProcess-Process.bpmn2",
        		"subprocess/ErrorsBetweenProcess-SubProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);

        Map<String, Object> variables = new HashMap<String, Object>();

        variables.put("tipoEvento", "error");
        variables.put("pasoVariable", 3);
        ProcessInstance processInstance = ksession.startProcess("Principal", variables);

        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertProcessInstanceAborted(processInstance.getId()+1, ksession);

        assertProcessVarValue(processInstance, "event", "error desde Subproceso");
    }

    @Test
    public void testProcessCustomDescriptionMetaData() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ProcessCustomDescriptionMetaData.bpmn2");
        ksession = createKnowledgeSession(kbase);

        Map<String, Object> params = new HashMap<String, Object>();

        ProcessInstance processInstance = ksession.startProcess("Minimal", params);
        assertProcessInstanceCompleted(processInstance);

        String description = ((org.jbpm.process.instance.impl.ProcessInstanceImpl)processInstance).getDescription();
        assertNotNull(description);
        assertEquals("my process with description", description);
    }

    @Test
    public void testProcessVariableCustomDescriptionMetaData() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ProcessVariableCustomDescriptionMetaData.bpmn2");
        ksession = createKnowledgeSession(kbase);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "variable name for process");
        ProcessInstance processInstance = ksession.startProcess("Minimal", params);
        assertProcessInstanceCompleted(processInstance);

        String description = ((org.jbpm.process.instance.impl.ProcessInstanceImpl)processInstance).getDescription();
        assertNotNull(description);
        assertEquals("variable name for process", description);
    }

    @Test
    public void testInvalidSubProcessNoOutgoingSF() throws Exception {
    	try {
    		KieBase kbase = createKnowledgeBase("subprocess/BPMN2-InvalidEmdeddedSubProcess.bpmn2");
    		ksession = createKnowledgeSession(kbase);
    		fail("Process should be invalid, there should be build errors");
    	} catch (RuntimeException e) {
    		// there should be build errors
    	}
    }

    @Test
    public void testAdHocSubProcessEmptyCompleteExpression() throws Exception {
        try {
        	createKnowledgeBaseWithoutDumper("BPMN2-AdHocSubProcessEmptyCompleteExpression.bpmn2");
        	fail("Process should be invalid, there should be build errors");
    	} catch (RuntimeException e) {
    		// there should be build errors
    	}
    }

    @Test
    public void testSubProcessWithTypeVariable() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("subprocess/BPMN2-SubProcessWithTypeVariable.bpmn2");
        ksession = createKnowledgeSession(kbase);
        final List<String> list = new ArrayList<String>();
        ksession.addEventListener(new DefaultProcessEventListener() {

            public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
                if (event.getNodeInstance().getNodeName().equals("Read Map")) {
                    list.add(event.getNodeInstance().getNodeName());
                }
            }
        });
        ProcessInstance processInstance = ksession.startProcess("sub_variable.sub_variables");
        assertProcessInstanceCompleted(processInstance);
        assertEquals(2, list.size());
    }

    @Test
    public void testUserTaskParametrizedInput() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-UserTaskWithParametrizedInput.bpmn2");
        KieSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("UserTask");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession = restoreSession(ksession, true);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("Executing task of process instance " + processInstance.getId() + " as work item with Hello",
                workItem.getParameter("Description").toString().trim());
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
        ksession.dispose();
    }

    @Test
    public void testMultipleBusinessRuleTaskWithDataInputsWithPersistence()
            throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper(
                "BPMN2-MultipleRuleTasksWithDataInput.bpmn2",
                "BPMN2-MultipleRuleTasks.drl");
        ksession = createKnowledgeSession(kbase);

        ksession.addEventListener(new TriggerRulesEventListener(ksession));

        List<String> listPerson = new ArrayList<String>();
        List<String> listAddress = new ArrayList<String>();

        ksession.setGlobal("listPerson", listPerson);
        ksession.setGlobal("listAddress", listAddress);

        Person person = new Person();
        person.setName("john");

        Address address = new Address();
        address.setStreet("5th avenue");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);
        params.put("address", address);
        ProcessInstance processInstance = ksession.startProcess("multiple-rule-tasks", params);

        assertEquals(1, listPerson.size());
        assertEquals(1, listAddress.size());
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testSubProcessInAdHocProcess() throws Exception {
        // JBPM-5374
        KieBase kbase = createKnowledgeBaseWithoutDumper(
                "BPMN2-SubProcessInAdHocProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> parameters = new HashMap<String, Object>();
        ProcessInstance processInstance = ksession.startProcess("SubProcessInAdHocProcess", parameters);
        assertProcessInstanceActive(processInstance);

        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
    }
    
    @Test
    public void testCallActivityWithDataAssignment() throws Exception {
        KieBase kbase = createKnowledgeBase("subprocess/AssignmentProcess.bpmn2", "subprocess/AssignmentSubProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "oldValue");
        ProcessInstance processInstance = ksession.startProcess("assignmentProcess", params);
        assertProcessInstanceCompleted(processInstance);
        assertEquals("Hello Genworth welcome to jBPMS!", ((WorkflowProcessInstance) processInstance).getVariable("message"));
    }
    
    @Test
    public void testDMNBusinessRuleTask()throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper(
                "dmn/BPMN2-BusinessRuleTaskDMN.bpmn2", "dmn/0020-vacation-days.dmn");
        ksession = createKnowledgeSession(kbase);
        // first run 16, 1 and expected days is 27
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("age", 16);
        params.put("yearsOfService", 1);
        ProcessInstance processInstance = ksession.startProcess("BPMN2-BusinessRuleTask", params);

        assertProcessInstanceFinished(processInstance, ksession);
        BigDecimal vacationDays = (BigDecimal) ((WorkflowProcessInstance) processInstance).getVariable("vacationDays");
        assertEquals(BigDecimal.valueOf(27), vacationDays);
        
        // second run 44, 20 and expected days is 24
        params = new HashMap<String, Object>();
        params.put("age", 44);
        params.put("yearsOfService", 20);
        processInstance = ksession.startProcess("BPMN2-BusinessRuleTask", params);

        assertProcessInstanceFinished(processInstance, ksession);
        vacationDays = (BigDecimal) ((WorkflowProcessInstance) processInstance).getVariable("vacationDays");
        assertEquals(BigDecimal.valueOf(24), vacationDays);
        
        // second run 50, 30 and expected days is 30
        params = new HashMap<String, Object>();
        params.put("age", 50);
        params.put("yearsOfService", 30);
        processInstance = ksession.startProcess("BPMN2-BusinessRuleTask", params);

        assertProcessInstanceFinished(processInstance, ksession);
        vacationDays = (BigDecimal) ((WorkflowProcessInstance) processInstance).getVariable("vacationDays");
        assertEquals(BigDecimal.valueOf(30), vacationDays);
    }

    @Disabled
    @Test
    public void testDMNBusinessRuleTaskByDecisionName()throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper(
                "dmn/BPMN2-BusinessRuleTaskDMNByDecisionName.bpmn2", "dmn/0020-vacation-days.dmn");
        ksession = createKnowledgeSession(kbase);
        // first run 16, 1 and expected days is 5
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("age", 16);
        params.put("yearsOfService", 1);
        ProcessInstance processInstance = ksession.startProcess("BPMN2-BusinessRuleTask", params);

        assertProcessInstanceFinished(processInstance, ksession);
        BigDecimal vacationDays = (BigDecimal) ((WorkflowProcessInstance) processInstance).getVariable("vacationDays");
        assertEquals(BigDecimal.valueOf(5), vacationDays);
    }

    @Disabled
    @Test
    public void testDMNBusinessRuleTaskMultipleDecisionsOutput()throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper(
                "dmn/BPMN2-BusinessRuleTaskDMNMultipleDecisionsOutput.bpmn2", "dmn/0020-vacation-days.dmn");
        ksession = createKnowledgeSession(kbase);
        // first run 16, 1 and expected days is 5
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("age", 16);
        params.put("yearsOfService", 1);
        ProcessInstance processInstance = ksession.startProcess("BPMN2-BusinessRuleTask", params);

        assertProcessInstanceFinished(processInstance, ksession);
        BigDecimal vacationDays = (BigDecimal) ((WorkflowProcessInstance) processInstance).getVariable("vacationDays");
        assertEquals(BigDecimal.valueOf(27), vacationDays);
        BigDecimal extraDays = (BigDecimal) ((WorkflowProcessInstance) processInstance).getVariable("extraDays");
        assertEquals(BigDecimal.valueOf(5), extraDays);
    }

    @Disabled
    @Test
    public void testDMNBusinessRuleTaskInvalidExecution()throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper(
                "dmn/BPMN2-BusinessRuleTaskDMNByDecisionName.bpmn2", "dmn/0020-vacation-days.dmn");
        ksession = createKnowledgeSession(kbase);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("age", 16);        
        
        try {
            ksession.startProcess("BPMN2-BusinessRuleTask", params);
        } catch (Exception e) {
            assertTrue(e instanceof WorkflowRuntimeException);
            assertTrue(e.getCause() instanceof RuntimeException);
            assertTrue(e.getCause().getMessage().contains("DMN result errors"));
        }
    }

    @Disabled
    @Test
    public void testDMNBusinessRuleTaskModelById()throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper(
                "dmn/BPMN2-BusinessRuleTaskDMNModelById.bpmn2", "dmn/0020-vacation-days.dmn");
        ksession = createKnowledgeSession(kbase);
        // first run 16, 1 and expected days is 27
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("age", 16);
        params.put("yearsOfService", 1);
        ProcessInstance processInstance = ksession.startProcess("BPMN2-BusinessRuleTask", params);

        assertProcessInstanceFinished(processInstance, ksession);
        BigDecimal vacationDays = (BigDecimal) ((WorkflowProcessInstance) processInstance).getVariable("vacationDays");
        assertEquals(BigDecimal.valueOf(27), vacationDays);
        
        // second run 44, 20 and expected days is 24
        params = new HashMap<String, Object>();
        params.put("age", 44);
        params.put("yearsOfService", 20);
        processInstance = ksession.startProcess("BPMN2-BusinessRuleTask", params);

        assertProcessInstanceFinished(processInstance, ksession);
        vacationDays = (BigDecimal) ((WorkflowProcessInstance) processInstance).getVariable("vacationDays");
        assertEquals(BigDecimal.valueOf(24), vacationDays);
        
        // second run 50, 30 and expected days is 30
        params = new HashMap<String, Object>();
        params.put("age", 50);
        params.put("yearsOfService", 30);
        processInstance = ksession.startProcess("BPMN2-BusinessRuleTask", params);

        assertProcessInstanceFinished(processInstance, ksession);
        vacationDays = (BigDecimal) ((WorkflowProcessInstance) processInstance).getVariable("vacationDays");
        assertEquals(BigDecimal.valueOf(30), vacationDays);
    }
    
    @Test
    public void testBusinessRuleTaskFireLimit() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-BusinessRuleTaskLoop.bpmn2",
                "BPMN2-BusinessRuleTaskInfiniteLoop.drl");
        ksession = createKnowledgeSession(kbase);
        ksession.insert(new Person());
        ProcessInstance processInstance = ksession.startProcess("BPMN2-BusinessRuleTask");
        
        assertEquals(ProcessInstance.STATE_ERROR, processInstance.getState());
        assertThat(((WorkflowProcessInstanceImpl)processInstance).getErrorMessage()).contains("Fire rule limit reached 10000");
    }
    
    @Test
    public void testBusinessRuleTaskFireLimitAsParameter() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-BusinessRuleTaskWithDataInputLoop.bpmn2",
                "BPMN2-BusinessRuleTaskInfiniteLoop.drl");
        ksession = createKnowledgeSession(kbase);
        ksession.insert(new Person());
        
        Map<String, Object> parameters = Collections.singletonMap("limit", 5);
        
        ProcessInstance processInstance = ksession.startProcess("BPMN2-BusinessRuleTask", parameters);
        assertEquals(ProcessInstance.STATE_ERROR, processInstance.getState());
        assertThat(((WorkflowProcessInstanceImpl)processInstance).getErrorMessage()).contains("Fire rule limit reached 5");        
    }

    @Disabled
    @Test
    public void testScriptTaskFEEL() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ScriptTaskFEEL.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "krisv");
        Person person = new Person();
        person.setName("krisv");
        params.put("person", person);

        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.startProcess("ScriptTask", params);
        assertEquals("Entry", processInstance.getVariable("x"));
        assertNull(processInstance.getVariable("y"));

        ksession.getWorkItemManager().completeWorkItem(handler.getWorkItem().getId(), null);
        assertEquals("Exit", getProcessVarValue(processInstance, "y"));
        assertEquals("tester", processInstance.getVariable("surname"));
        
        assertNodeTriggered(processInstance.getId(), "Script1");
    }
    
    @Test
    public void testBusinessRuleTaskException() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-BusinessRuleTask.bpmn2",
                "BPMN2-BusinessRuleTaskWithException.drl");
        ksession = createKnowledgeSession(kbase);
        ksession.insert(new Person());
        ProcessInstance processInstance = ksession.startProcess("BPMN2-BusinessRuleTask");
               
        assertEquals(ProcessInstance.STATE_ERROR, processInstance.getState());
        assertThat(((WorkflowProcessInstanceImpl)processInstance).getErrorMessage()).contains("On purpose");
    }

    
    @Test
    public void testXORWithSameTargetProcess() throws Exception {
        KieBase kbase = createKnowledgeBase("build/XORSameTarget.bpmn2");
        ksession = createKnowledgeSession(kbase);
        
        Map<String, Object> params = new HashMap<>();
        params.put("choice", 1);
        ProcessInstance processInstance = ksession.startProcess("XORTest.XOR2", params);
        assertProcessInstanceCompleted(processInstance);
        
        params = new HashMap<>();
        params.put("choice", 2);
        processInstance = ksession.startProcess("XORTest.XOR2", params);
        assertProcessInstanceCompleted(processInstance);
    }
    
    @Test
    public void testUserTaskWithExpressionsForIO() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-UserTaskWithIOexpression.bpmn2");
        KieSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("person", new Person("john"));
        
        ProcessInstance processInstance = ksession.startProcess("UserTask", parameters);
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession = restoreSession(ksession, true);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("john", workItem.getParameter("ActorId"));
        assertEquals("john", workItem.getParameter("personName"));
        
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), Collections.singletonMap("personAge", 50));
        
        Person person = (Person) processInstance.getVariables().get("person");
        assertEquals(50, person.getAge());
        assertProcessInstanceFinished(processInstance, ksession);
        ksession.dispose();
    }
    
    @Test
    public void testCallActivitykWithExpressionsForIO() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-CallActivityWithIOexpression.bpmn2", "BPMN2-CallActivitySubProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", new Person("john"));
        ProcessInstance processInstance = ksession.startProcess("ParentProcess", params);
        assertProcessInstanceActive(processInstance);
        
        Person person = (Person) processInstance.getVariables().get("person");
        assertEquals("new value", person.getName());

        ksession = restoreSession(ksession, true);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("krisv", workItem.getParameter("ActorId"));
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

        assertProcessInstanceFinished(processInstance, ksession);
    }
    
    @Test
    @RequirePersistence(false)
    public void testBusinessRuleTaskWithExpressionsForIO() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-BusinessRuleTaskWithDataInputIOExpression.bpmn2",
                "BPMN2-BusinessRuleTaskWithDataInput.drl");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(new RuleAwareProcessEventListener());
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", new Person(null));
        params.put("account", new Account());
        ProcessInstance processInstance = ksession
                .startProcess("BPMN2-BusinessRuleTask", params);
        assertProcessInstanceFinished(processInstance, ksession);
        Person person = (Person) processInstance.getVariables().get("person");
        assertEquals("john", person.getName());
        
        Account account = (Account) processInstance.getVariables().get("account");
        assertNotNull(account.getPerson());
    }
    
    @Test
    public void testUserTaskWithAssignment() throws Exception {
        ProcessDialectRegistry.setDialect("custom", new ProcessDialect() {

            @Override
            public ReturnValueEvaluatorBuilder getReturnValueEvaluatorBuilder() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ProcessClassBuilder getProcessClassBuilder() {
                return null;
            }

            @Override
            public AssignmentBuilder getAssignmentBuilder() {
                return new AssignmentBuilder() {

                    @Override
                    public void build(PackageBuildContext context, Assignment assignment, String sourceExpr, String targetExpr, ContextResolver contextResolver, boolean isInput) {
                        assignment.setMetaData("Action", new AssignmentAction() {

                            @Override
                            public void execute( KogitoWorkItem workItem, ProcessContext context) throws Exception {
                                assertEquals("from_expression", assignment.getFrom());
                                assertEquals("to_expression", assignment.getTo());
                            }
                        });

                    }
                };
            }

            @Override
            public ActionBuilder getActionBuilder() {
                return null;
            }

            @Override
            public void addProcess(ProcessBuildContext context) {

            }
        });
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-DataOutputAssignmentCustomExpressionLang.bpmn2");

        Process scriptProcess = kbase.getProcess("process");
        assertThat(scriptProcess).isNotNull();
        Node[] nodes = ((NodeContainer) scriptProcess).getNodes();
        assertThat(nodes).hasSize(3);
        assertThat(nodes).filteredOn(n -> n instanceof WorkItemNode).allMatch(n -> matchExpectedAssociationSetup(n));
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "John");
        ProcessInstance processInstance = ksession.startProcess("process", params);

        ksession.abortProcessInstance(processInstance.getId());

        assertProcessInstanceAborted(processInstance);
    }

    protected boolean matchExpectedAssociationSetup(Node node) {
        List<DataAssociation> inputs = ((WorkItemNode) node).getInAssociations();
        List<DataAssociation> outputs = ((WorkItemNode) node).getOutAssociations();

        assertThat(inputs).hasSize(1);
        assertThat(outputs).hasSize(1);

        DataAssociation association = inputs.get(0);
        assertThat(association.getAssignments()).hasSize(1);
        assertThat(association.getSources()).hasSize(2);

        Assignment assignment = association.getAssignments().get(0);
        assertThat(assignment.getDialect()).isEqualTo("custom");
        assertThat(assignment.getFrom()).isEqualTo("from_expression");
        assertThat(assignment.getTo()).isEqualTo("to_expression");

        association = outputs.get(0);
        assertThat(association.getAssignments()).hasSize(1);
        assertThat(association.getSources()).hasSize(2);

        assignment = association.getAssignments().get(0);
        assertThat(assignment.getDialect()).isEqualTo("custom");
        assertThat(assignment.getFrom()).isEqualTo("from_expression");
        assertThat(assignment.getTo()).isEqualTo("to_expression");

        return true;
    }
}
