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

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.componenttests.handler.TestWorkItemHandler;
import org.jbpm.componenttests.test.Person;
import org.jbpm.test.util.AbstractBaseTest;
import org.junit.jupiter.api.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.io.ResourceFactory;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessRuntime;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProcessWorkItemTest extends AbstractBaseTest {

    @Test
    public void testWorkItem() {
        Reader source = new StringReader(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<process xmlns=\"http://drools.org/drools-5.0/process\"\n" +
                        "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n" +
                        "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.actions\" package-name=\"org.drools\" version=\"1\" >\n" +
                        "\n" +
                        "  <header>\n" +
                        "    <variables>\n" +
                        "      <variable name=\"UserName\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "        <value>John Doe</value>\n" +
                        "      </variable>\n" +
                        "      <variable name=\"Person\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.ObjectDataType\" className=\"org.jbpm.componenttests.test.Person\" />\n" +
                        "      </variable>\n" +
                        "      <variable name=\"MyObject\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "      </variable>\n" +
                        "      <variable name=\"Number\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.IntegerDataType\" />\n" +
                        "      </variable>\n" +
                        "    </variables>\n" +
                        "  </header>\n" +
                        "\n" +
                        "  <nodes>\n" +
                        "    <start id=\"1\" name=\"Start\" />\n" +
                        "    <workItem id=\"2\" name=\"HumanTask\" >\n" +
                        "      <work name=\"Human Task\" >\n" +
                        "        <parameter name=\"ActorId\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "          <value>#{UserName}</value>\n" +
                        "        </parameter>\n" +
                        "        <parameter name=\"Content\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "          <value>#{Person.name}</value>\n" +
                        "        </parameter>\n" +
                        "        <parameter name=\"TaskName\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "          <value>Do something</value>\n" +
                        "        </parameter>\n" +
                        "        <parameter name=\"Priority\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "        </parameter>\n" +
                        "        <parameter name=\"Comment\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "        </parameter>\n" +
                        "        <parameter name=\"Attachment\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "        </parameter>\n" +
                        "      </work>\n" +
                        "      <mapping type=\"in\" from=\"MyObject\" to=\"Attachment\" />" +
                        "      <mapping type=\"in\" from=\"Person.name\" to=\"Comment\" />" +
                        "      <mapping type=\"out\" from=\"Result\" to=\"MyObject\" />" +
                        "      <mapping type=\"out\" from=\"Result.length()\" to=\"Number\" />" +
                        "    </workItem>\n" +
                        "    <end id=\"3\" name=\"End\" />\n" +
                        "  </nodes>\n" +
                        "\n" +
                        "  <connections>\n" +
                        "    <connection from=\"1\" to=\"2\" />\n" +
                        "    <connection from=\"2\" to=\"3\" />\n" +
                        "  </connections>\n" +
                        "\n" +
                        "</process>");
        builder.add(ResourceFactory.newReaderResource(source), ResourceType.DRF);
        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();

        TestWorkItemHandler handler = new TestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task", handler);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("UserName", "John Doe");
        Person person = new Person();
        person.setName("John Doe");
        parameters.put("Person", person);
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) kruntime.startProcess("org.drools.actions", parameters);
        assertEquals(KogitoProcessInstance.STATE_ACTIVE, processInstance.getState());
        KogitoWorkItem workItem = handler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("John Doe", workItem.getParameter("ActorId"));
        assertEquals("John Doe", workItem.getParameter("Content"));
        assertEquals("John Doe", workItem.getParameter("Comment"));
        kruntime.getKogitoWorkItemManager().completeWorkItem(workItem.getStringId(), null);
        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance.getState());
        parameters = new HashMap<String, Object>();
        parameters.put("UserName", "Jane Doe");
        parameters.put("MyObject", "SomeString");
        person = new Person();
        person.setName("Jane Doe");
        parameters.put("Person", person);
        processInstance = (WorkflowProcessInstance) kruntime.startProcess("org.drools.actions", parameters);
        assertEquals(KogitoProcessInstance.STATE_ACTIVE, processInstance.getState());
        workItem = handler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("Jane Doe", workItem.getParameter("ActorId"));
        assertEquals("SomeString", workItem.getParameter("Attachment"));
        assertEquals("Jane Doe", workItem.getParameter("Content"));
        assertEquals("Jane Doe", workItem.getParameter("Comment"));
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("Result", "SomeOtherString");
        kruntime.getKogitoWorkItemManager().completeWorkItem(workItem.getStringId(), results);
        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance.getState());
        assertEquals("SomeOtherString", processInstance.getVariable("MyObject"));
        assertEquals(15, processInstance.getVariable("Number"));
    }

    @Test
    public void testWorkItemImmediateCompletion() {
        Reader source = new StringReader(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<process xmlns=\"http://drools.org/drools-5.0/process\"\n" +
                        "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n" +
                        "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.actions\" package-name=\"org.drools\" version=\"1\" >\n" +
                        "\n" +
                        "  <header>\n" +
                        "    <variables>\n" +
                        "      <variable name=\"UserName\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "        <value>John Doe</value>\n" +
                        "      </variable>\n" +
                        "      <variable name=\"Person\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.ObjectDataType\" className=\"org.jbpm.componenttests.test.Person\" />\n" +
                        "      </variable>\n" +
                        "      <variable name=\"MyObject\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "      </variable>\n" +
                        "      <variable name=\"Number\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.IntegerDataType\" />\n" +
                        "      </variable>\n" +
                        "    </variables>\n" +
                        "  </header>\n" +
                        "\n" +
                        "  <nodes>\n" +
                        "    <start id=\"1\" name=\"Start\" />\n" +
                        "    <workItem id=\"2\" name=\"HumanTask\" >\n" +
                        "      <work name=\"Human Task\" >\n" +
                        "        <parameter name=\"ActorId\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "          <value>#{UserName}</value>\n" +
                        "        </parameter>\n" +
                        "        <parameter name=\"Content\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "          <value>#{Person.name}</value>\n" +
                        "        </parameter>\n" +
                        "        <parameter name=\"TaskName\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "          <value>Do something</value>\n" +
                        "        </parameter>\n" +
                        "        <parameter name=\"Priority\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "        </parameter>\n" +
                        "        <parameter name=\"Comment\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "        </parameter>\n" +
                        "        <parameter name=\"Attachment\" >\n" +
                        "          <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "        </parameter>\n" +
                        "      </work>\n" +
                        "      <mapping type=\"in\" from=\"MyObject\" to=\"Attachment\" />" +
                        "      <mapping type=\"in\" from=\"Person.name\" to=\"Comment\" />" +
                        "      <mapping type=\"out\" from=\"Result\" to=\"MyObject\" />" +
                        "      <mapping type=\"out\" from=\"Result.length()\" to=\"Number\" />" +
                        "    </workItem>\n" +
                        "    <end id=\"3\" name=\"End\" />\n" +
                        "  </nodes>\n" +
                        "\n" +
                        "  <connections>\n" +
                        "    <connection from=\"1\" to=\"2\" />\n" +
                        "    <connection from=\"2\" to=\"3\" />\n" +
                        "  </connections>\n" +
                        "\n" +
                        "</process>");
        builder.add(ResourceFactory.newReaderResource(source), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();

        ImmediateTestWorkItemHandler handler = new ImmediateTestWorkItemHandler();
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task", handler);
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task", handler);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("UserName", "John Doe");
        Person person = new Person();
        person.setName("John Doe");
        parameters.put("Person", person);
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) kruntime.startProcess("org.drools.actions", parameters);
        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance.getState());
    }

    private static class ImmediateTestWorkItemHandler implements KogitoWorkItemHandler {
        @Override
        public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
            manager.completeWorkItem(workItem.getStringId(), null);
        }

        @Override
        public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        }
    }
}
