/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.componenttests;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.drools.core.io.impl.ReaderResource;
import org.jbpm.componenttests.handler.TestWorkItemHandler;
import org.jbpm.componenttests.test.Person;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.test.util.AbstractBaseTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kie.api.io.ResourceType;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.kogito.internal.process.runtime.KogitoProcessRuntime;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jbpm.componenttests.JbpmSerializationHelper.getSerialisedStatefulKnowledgeSession;
import static org.junit.jupiter.api.Assertions.*;

public class ProcessMarshallingTest extends AbstractBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(ProcessMarshallingTest.class);

    @Test
    public void testMarshallingProcessInstancesAndGlobals() throws Exception {
        String rule = "package org.test;\n";
        rule += "import org.jbpm.componenttests.test.Person\n";
        rule += "global java.util.List list\n";
        rule += "rule \"Rule 1\"\n";
        rule += "  ruleflow-group \"hello\"\n";
        rule += "when\n";
        rule += "    $p : Person( ) \n";
        rule += "then\n";
        rule += "    list.add( $p );\n";
        rule += "end";

        String process =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<process xmlns=\"http://drools.org/drools-5.0/process\"\n" +
                        "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "    xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n" +
                        "    type=\"RuleFlow\" name=\"ruleflow\" id=\"org.test.ruleflow\" package-name=\"org.test\" >\n" +
                        "  <header>\n" +
                        "  </header>\n" +
                        "  <nodes>\n" +
                        "    <start id=\"1\" name=\"Start\" />\n" +
                        "    <ruleSet id=\"2\" name=\"Hello\" ruleFlowGroup=\"hello\" />\n" +
                        "    <end id=\"3\" name=\"End\" />\n" +
                        "  </nodes>\n" +
                        "  <connections>\n" +
                        "    <connection from=\"1\" to=\"2\"/>\n" +
                        "    <connection from=\"2\" to=\"3\"/>\n" +
                        "  </connections>\n" +
                        "</process>";

        builder.add(ResourceFactory.newReaderResource(new StringReader(rule)), ResourceType.DRL);
        builder.add(ResourceFactory.newReaderResource(new StringReader(process)), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();
        kruntime.getKieRuntime().getEnvironment().set("org.jbpm.rule.task.waitstate", true);

        List<Object> list = new ArrayList<Object>();
        kruntime.getKieSession().setGlobal("list", list);

        Person p = new Person("bobba fet", 32);
        kruntime.getKieSession().insert(p);
        kruntime.startProcess("org.test.ruleflow");

        assertEquals(1, kruntime.getKieSession().getProcessInstances().size());

        kruntime.getKieSession().fireAllRules();

        assertEquals(1, ((List<Object>) kruntime.getKieSession().getGlobal("list")).size());
        assertEquals(p, ((List<Object>) kruntime.getKieSession().getGlobal("list")).get(0));
        assertEquals(0, kruntime.getKieSession().getProcessInstances().size());
    }

    @Test
    public void testMarshallingProcessInstanceWithWorkItem() throws Exception {
        String process =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<process xmlns=\"http://drools.org/drools-5.0/process\"\n" +
                        "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "    xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n" +
                        "    type=\"RuleFlow\" name=\"ruleflow\" id=\"org.test.ruleflow\" package-name=\"org.test\" >\n" +
                        "  <header>\n" +
                        "    <variables>\n" +
                        "      <variable name=\"myVariable\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "        <value>OldValue</value>\n" +
                        "      </variable>\n" +
                        "    </variables>\n" +
                        "  </header>\n" +
                        "  <nodes>\n" +
                        "    <start id=\"1\" name=\"Start\" />\n" +
                        "    <workItem id=\"2\" name=\"Email\" >\n" +
                        "      <work name=\"Email\" >\n" +
                        "        <parameter name=\"Subject\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "          <value>Mail</value>\n" +
                        "        </parameter>\n" +
                        "        <parameter name=\"Text\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "          <value>This is an email</value>\n" +
                        "        </parameter>\n" +
                        "        <parameter name=\"To\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "          <value>you@mail.com</value>\n" +
                        "        </parameter>\n" +
                        "        <parameter name=\"From\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "          <value>me@mail.com</value>\n" +
                        "        </parameter>\n" +
                        "      </work>\n" +
                        "    </workItem>\n" +
                        "    <end id=\"3\" name=\"End\" />\n" +
                        "  </nodes>\n" +
                        "  <connections>\n" +
                        "    <connection from=\"1\" to=\"2\"/>\n" +
                        "    <connection from=\"2\" to=\"3\"/>\n" +
                        "  </connections>\n" +
                        "</process>";
        builder.add(new ReaderResource(new StringReader(process)), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();

        TestWorkItemHandler handler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Email", handler);
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("myVariable", "ThisIsMyValue");
        kruntime.startProcess("org.test.ruleflow", variables);

        assertEquals(1, kruntime.getKogitoProcessInstances().size());
        assertNotNull(handler.getWorkItem());

        assertEquals(1, kruntime.getKogitoProcessInstances().size());
        VariableScopeInstance variableScopeInstance =
                (VariableScopeInstance) ((ProcessInstance) kruntime.getKogitoProcessInstances().iterator().next()).getContextInstance(VariableScope.VARIABLE_SCOPE);
        assertEquals("ThisIsMyValue", variableScopeInstance.getVariable("myVariable"));

        kruntime.getKogitoWorkItemManager().completeWorkItem(handler.getWorkItem().getStringId(), null);
        assertEquals(0, kruntime.getKogitoProcessInstances().size());
    }

    @Test
    public void testMarshallingWithMultipleHumanTasks() throws Exception {
        String process =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<process xmlns=\"http://drools.org/drools-5.0/process\"\n" +
                        "  xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "  xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n" +
                        "  type=\"RuleFlow\" name=\"ruleflow\" id=\"com.sample.ruleflow\" package-name=\"com.sample\" >\n" +
                        "\n" +
                        "    <header>\n" +
                        "      <variables>\n" +
                        "        <variable name=\"list\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.ObjectDataType\" className=\"java.util.List\" />\n" +
                        "        </variable>\n" +
                        "      </variables>\n" +
                        "    </header>\n" +
                        "\n" +
                        "    <nodes>\n" +
                        "      <forEach id=\"4\" name=\"ForEach\" variableName=\"item\" collectionExpression=\"list\" >\n" +
                        "        <nodes>\n" +
                        "          <humanTask id=\"1\" name=\"Human Task\" >\n" +
                        "            <work name=\"Human Task\" >\n" +
                        "              <parameter name=\"Comment\" >\n" +
                        "                <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "              </parameter>\n" +
                        "              <parameter name=\"ActorId\" >\n" +
                        "                <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "              </parameter>\n" +
                        "              <parameter name=\"Priority\" >\n" +
                        "                <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "              </parameter>\n" +
                        "              <parameter name=\"TaskName\" >\n" +
                        "                <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "                <value>Do something: #{item}</value>\n" +
                        "              </parameter>\n" +
                        "            </work>\n" +
                        "          </humanTask>\n" +
                        "          <humanTask id=\"2\" name=\"Human Task Again\" >\n" +
                        "            <work name=\"Human Task\" >\n" +
                        "              <parameter name=\"Comment\" >\n" +
                        "                <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "              </parameter>\n" +
                        "              <parameter name=\"ActorId\" >\n" +
                        "                <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "              </parameter>\n" +
                        "              <parameter name=\"Priority\" >\n" +
                        "                <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "              </parameter>\n" +
                        "              <parameter name=\"TaskName\" >\n" +
                        "                <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "                <value>Do something else: #{item}</value>\n" +
                        "              </parameter>\n" +
                        "            </work>\n" +
                        "          </humanTask>\n" +
                        "        </nodes>\n" +
                        "        <connections>\n" +
                        "          <connection from=\"1\" to=\"2\" />\n" +
                        "        </connections>\n" +
                        "        <in-ports>\n" +
                        "          <in-port type=\"DROOLS_DEFAULT\" nodeId=\"1\" nodeInType=\"DROOLS_DEFAULT\" />\n" +
                        "        </in-ports>\n" +
                        "        <out-ports>\n" +
                        "          <out-port type=\"DROOLS_DEFAULT\" nodeId=\"2\" nodeOutType=\"DROOLS_DEFAULT\" />\n" +
                        "        </out-ports>\n" +
                        "      </forEach>\n" +
                        "      <start id=\"1\" name=\"Start\" />\n" +
                        "      <end id=\"3\" name=\"End\" />\n" +
                        "    </nodes>\n" +
                        "\n" +
                        "    <connections>\n" +
                        "      <connection from=\"1\" to=\"4\" />\n" +
                        "      <connection from=\"4\" to=\"3\" />\n" +
                        "    </connections>\n" +
                        "\n" +
                        "</process>\n";
        builder.add(new ReaderResource(new StringReader(process)), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();

        TestListWorkItemHandler handler = new TestListWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task", handler);
        List<String> list = new ArrayList<String>();
        list.add("one");
        list.add("two");
        list.add("three");
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("list", list);
        kruntime.startProcess("com.sample.ruleflow", parameters);

        assertEquals(1, kruntime.getKogitoProcessInstances().size());
        assertEquals(3, handler.getWorkItems().size());

        List<KogitoWorkItem> workItems = new ArrayList<>(handler.getWorkItems());
        handler.reset();
        for (KogitoWorkItem workItem : workItems) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(workItem.getStringId(), null);
        }
        assertEquals(1, kruntime.getKogitoProcessInstances().size());
        assertEquals(3, handler.getWorkItems().size());

        for (KogitoWorkItem workItem : handler.getWorkItems()) {
            kruntime.getKogitoWorkItemManager().completeWorkItem(workItem.getStringId(), null);
        }
        assertEquals(0, kruntime.getKogitoProcessInstances().size());
    }

    @Test
    @Disabled("Needs fix")
    public void testMarshallingProcessInstanceWithTimer() throws Exception {
        String process =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<process xmlns=\"http://drools.org/drools-5.0/process\"\n" +
                        "  xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "  xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n" +
                        "  type=\"RuleFlow\" name=\"ruleflow\" id=\"com.sample.ruleflow\" package-name=\"com.sample\" >\n" +
                        "\n" +
                        "    <header>\n" +
                        "    </header>\n" +
                        "\n" +
                        "    <nodes>\n" +
                        "      <start id=\"1\" name=\"Start\" />\n" +
                        "      <timerNode id=\"4\" name=\"Timer\" delay=\"200\" />\n" +
                        "      <end id=\"3\" name=\"End\" />\n" +
                        "    </nodes>\n" +
                        "\n" +
                        "    <connections>\n" +
                        "      <connection from=\"1\" to=\"4\" />\n" +
                        "      <connection from=\"4\" to=\"3\" />\n" +
                        "    </connections>\n" +
                        "\n" +
                        "</process>\n";
        builder.add(new ReaderResource(new StringReader(process)), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();

        kruntime.startProcess("com.sample.ruleflow");
        assertEquals(1, kruntime.getKieSession().getProcessInstances().size());
        kruntime.getKieSession().halt();

        final StatefulKnowledgeSession session2 = getSerialisedStatefulKnowledgeSession(kruntime.getKieSession());
        int sleeps = 3;
        int procInstsAlive = session2.getProcessInstances().size();
        while (procInstsAlive > 0 && sleeps > 0) {
            TimeUnit.SECONDS.sleep(1);
            --sleeps;
            procInstsAlive = session2.getProcessInstances().size();
        }
        assertEquals(0, session2.getProcessInstances().size());

        session2.halt();
    }

    private static class TestListWorkItemHandler implements KogitoWorkItemHandler {
        private List<KogitoWorkItem> workItems = new ArrayList<>();

        public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
            logger.debug("Executing workItem {}", workItem.getParameter("TaskName"));
            workItems.add(workItem);
        }

        public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
            workItems.remove(workItem);
        }

        public List<KogitoWorkItem> getWorkItems() {
            return workItems;
        }

        public void reset() {
            workItems.clear();
        }
    }

    @Test
    public void testVariablePersistenceMarshallingStrategies() throws Exception {
        String process =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<process xmlns=\"http://drools.org/drools-5.0/process\"\n" +
                        "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "    xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n" +
                        "    type=\"RuleFlow\" name=\"ruleflow\" id=\"org.test.ruleflow\" package-name=\"org.test\" >\n" +
                        "  <header>\n" +
                        "    <variables>\n" +
                        "      <variable name=\"myVariable\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "        <value>OldValue</value>\n" +
                        "      </variable>\n" +
                        "      <variable name=\"myPerson\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.ObjectDataType\" className=\"org.jbpm.componenttests.test.Person\"/>\n" +
                        "      </variable>\n" +
                        "    </variables>\n" +
                        "  </header>\n" +
                        "  <nodes>\n" +
                        "    <start id=\"1\" name=\"Start\" />\n" +
                        "    <workItem id=\"2\" name=\"Email\" >\n" +
                        "      <work name=\"Report\" >\n" +
                        "        <parameter name=\"Subject\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "          <value>Mail</value>\n" +
                        "        </parameter>\n" +
                        "        <parameter name=\"Subject\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "          <value>Mail</value>\n" +
                        "        </parameter>\n" +
                        "      </work>\n" +
                        "    </workItem>\n" +
                        "    <end id=\"3\" name=\"End\" />\n" +
                        "  </nodes>\n" +
                        "  <connections>\n" +
                        "    <connection from=\"1\" to=\"2\"/>\n" +
                        "    <connection from=\"2\" to=\"3\"/>\n" +
                        "  </connections>\n" +
                        "</process>";
        builder.add(new ReaderResource(new StringReader(process)), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();

        TestWorkItemHandler handler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Report", handler);
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("myVariable", "ThisIsMyValue");
        Person myPerson = new Person("Nikola Tesla", 156);
        variables.put("myPerson", myPerson);
        kruntime.startProcess("org.test.ruleflow", variables);

        assertEquals(1, kruntime.getKogitoProcessInstances().size());
        assertNotNull(handler.getWorkItem());

        assertEquals(1, kruntime.getKogitoProcessInstances().size());
        VariableScopeInstance variableScopeInstance =
                (VariableScopeInstance) ((ProcessInstance) kruntime.getKogitoProcessInstances().iterator().next()).getContextInstance(VariableScope.VARIABLE_SCOPE);
        assertEquals("ThisIsMyValue", variableScopeInstance.getVariable("myVariable"));
        assertEquals(myPerson, variableScopeInstance.getVariable("myPerson"));

        kruntime.getKogitoWorkItemManager().completeWorkItem(handler.getWorkItem().getStringId(), null);
        assertEquals(0, kruntime.getKogitoProcessInstances().size());
    }
}
