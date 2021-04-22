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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.io.impl.ReaderResource;
import org.jbpm.componenttests.test.Person;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.test.util.AbstractBaseTest;
import org.junit.jupiter.api.Test;
import org.kie.api.io.ResourceType;
import org.kie.internal.builder.KnowledgeBuilderError;
import org.kie.internal.io.ResourceFactory;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProcessSplitTest extends AbstractBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(ProcessSplitTest.class);

    @Test
    public void testSplitWithProcessInstanceConstraint() {
        Reader source = new StringReader(
                "<process xmlns=\"http://drools.org/drools-5.0/process\"" +
                        "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"" +
                        "         type=\"RuleFlow\" name=\"ruleflow\" id=\"org.jbpm.process-split\" package-name=\"org.jbpm\" >" +
                        "" +
                        "  <header>" +
                        "    <imports>" +
                        "      <import name=\"org.jbpm.componenttests.test.Person\" />" +
                        "      <import name=\"org.jbpm.componenttests.ProcessSplitTest.ProcessUtils\" />" +
                        "    </imports>" +
                        "    <globals>" +
                        "      <global identifier=\"list\" type=\"java.util.List\" />" +
                        "    </globals>" +
                        "    <variables>\n" +
                        "      <variable name=\"name\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "      </variable>\n" +
                        "    </variables>\n" +
                        "  </header>" +
                        "" +
                        "  <nodes>" +
                        "    <actionNode id=\"2\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >insert(kcontext.getProcessInstance());</action>" +
                        "    </actionNode>" +
                        "    <split id=\"4\" name=\"Split\" type=\"2\" >" +
                        "      <constraints>" +
                        "        <constraint toNodeId=\"8\" priority=\"2\" type=\"rule\" dialect=\"mvel\" >eval(true)</constraint>" +
                        "        <constraint toNodeId=\"6\" name=\"constraint\" priority=\"1\" type=\"rule\" dialect=\"mvel\" >processInstance: org.jbpm.ruleflow.instance.RuleFlowProcessInstance()" +
                        "Person( name == (ProcessUtils.getValue(processInstance, \"name\")) )</constraint>" +
                        "      </constraints>" +
                        "    </split>" +
                        "    <end id=\"8\" name=\"End\" />" +
                        "    <actionNode id=\"6\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >list.add(kcontext.getProcessInstance().getStringId());</action>" +
                        "    </actionNode>" +
                        "    <start id=\"1\" name=\"Start\" />" +
                        "    <end id=\"3\" name=\"End\" />" +
                        "  </nodes>" +
                        "  <connections>" +
                        "    <connection from=\"1\" to=\"2\" />" +
                        "    <connection from=\"2\" to=\"4\" />" +
                        "    <connection from=\"4\" to=\"8\" />" +
                        "    <connection from=\"4\" to=\"6\" />" +
                        "    <connection from=\"6\" to=\"3\" />" +
                        "  </connections>" +
                        "" +
                        "</process>");
        builder.add(new ReaderResource(source), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();
        List<Long> list = new ArrayList<Long>();
        kruntime.getKieSession().setGlobal("list", list);

        Person john = new Person("John Doe", 20);
        Person jane = new Person("Jane Doe", 20);
        Person julie = new Person("Julie Doe", 20);
        kruntime.getKieSession().insert(john);
        kruntime.getKieSession().insert(jane);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", john.getName());
        KogitoProcessInstance processInstance1 = kruntime.startProcess("org.jbpm.process-split", params);
        params = new HashMap<String, Object>();
        params.put("name", jane.getName());
        KogitoProcessInstance processInstance2 = kruntime.startProcess("org.jbpm.process-split", params);
        params = new HashMap<String, Object>();
        params.put("name", julie.getName());
        KogitoProcessInstance processInstance3 = kruntime.startProcess("org.jbpm.process-split", params);

        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance1.getState());
        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance2.getState());
        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance3.getState());
        assertEquals(2, list.size());
    }

    @Test
    public void testSplitWithProcessInstanceConstraint2() {
        Reader source = new StringReader(
                "<process xmlns=\"http://drools.org/drools-5.0/process\"" +
                        "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"" +
                        "         type=\"RuleFlow\" name=\"ruleflow\" id=\"org.jbpm.process-split\" package-name=\"org.jbpm\" >" +
                        "" +
                        "  <header>" +
                        "    <imports>" +
                        "      <import name=\"org.jbpm.componenttests.test.Person\" />" +
                        "      <import name=\"org.kie.api.runtime.process.WorkflowProcessInstance\" />" +
                        "    </imports>" +
                        "    <globals>" +
                        "      <global identifier=\"list\" type=\"java.util.List\" />" +
                        "    </globals>" +
                        "    <variables>\n" +
                        "      <variable name=\"name\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "      </variable>\n" +
                        "    </variables>\n" +
                        "  </header>" +
                        "" +
                        "  <nodes>" +
                        "    <actionNode id=\"2\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >insert(kcontext.getProcessInstance());</action>" +
                        "    </actionNode>" +
                        "    <split id=\"4\" name=\"Split\" type=\"2\" >" +
                        "      <constraints>" +
                        "        <constraint toNodeId=\"8\" toType=\"DROOLS_DEFAULT\" priority=\"2\" type=\"rule\" dialect=\"mvel\" >eval(true)</constraint>" +
                        "        <constraint toNodeId=\"6\" toType=\"DROOLS_DEFAULT\" name=\"constraint\" priority=\"1\" type=\"rule\" dialect=\"mvel\" >processInstance: WorkflowProcessInstance()" +
                        "Person( name == ( processInstance.getVariable(\"name\") ) )</constraint>" +
                        "      </constraints>" +
                        "    </split>" +
                        "    <end id=\"8\" name=\"End\" />" +
                        "    <actionNode id=\"6\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >list.add(kcontext.getProcessInstance().getStringId());</action>" +
                        "    </actionNode>" +
                        "    <start id=\"1\" name=\"Start\" />" +
                        "    <end id=\"3\" name=\"End\" />" +
                        "  </nodes>" +
                        "  <connections>" +
                        "    <connection from=\"1\" to=\"2\" />" +
                        "    <connection from=\"2\" to=\"4\" />" +
                        "    <connection from=\"4\" to=\"8\" />" +
                        "    <connection from=\"4\" to=\"6\" />" +
                        "    <connection from=\"6\" to=\"3\" />" +
                        "  </connections>" +
                        "" +
                        "</process>");
        builder.add(ResourceFactory.newReaderResource(source), ResourceType.DRF);
        for (KnowledgeBuilderError error : builder.getErrors()) {
            logger.error(error.toString());
        }

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();
        List<Long> list = new ArrayList<Long>();
        kruntime.getKieSession().setGlobal("list", list);

        Person john = new Person("John Doe", 20);
        Person jane = new Person("Jane Doe", 20);
        Person julie = new Person("Julie Doe", 20);
        kruntime.getKieSession().insert(john);
        kruntime.getKieSession().insert(jane);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", john.getName());
        KogitoProcessInstance processInstance1 = kruntime.startProcess("org.jbpm.process-split", params);
        params = new HashMap<String, Object>();
        params.put("name", jane.getName());
        KogitoProcessInstance processInstance2 = kruntime.startProcess("org.jbpm.process-split", params);
        params = new HashMap<String, Object>();
        params.put("name", julie.getName());
        KogitoProcessInstance processInstance3 = kruntime.startProcess("org.jbpm.process-split", params);

        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance1.getState());
        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance2.getState());
        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance3.getState());
        assertEquals(2, list.size());
    }

    @Test
    public void testSplitWithMVELContextConstraint() {
        Reader source = new StringReader(
                "<process xmlns=\"http://drools.org/drools-5.0/process\"" +
                        "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"" +
                        "         type=\"RuleFlow\" name=\"ruleflow\" id=\"org.jbpm.process-split\" package-name=\"org.jbpm\" >" +
                        "" +
                        "  <header>" +
                        "    <imports>" +
                        "      <import name=\"org.jbpm.componenttests.test.Person\" />" +
                        "      <import name=\"org.jbpm.componenttests.ProcessSplitTest.ProcessUtils\" />" +
                        "    </imports>" +
                        "    <globals>" +
                        "      <global identifier=\"list\" type=\"java.util.List\" />" +
                        "    </globals>" +
                        "    <variables>\n" +
                        "      <variable name=\"person\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.ObjectDataType\" className=\"org.jbpm.componenttests.test.Person\" />\n" +
                        "      </variable>\n" +
                        "    </variables>\n" +
                        "  </header>" +
                        "" +
                        "  <nodes>" +
                        "    <actionNode id=\"2\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >insert(kcontext.getProcessInstance());</action>" +
                        "    </actionNode>" +
                        "    <split id=\"4\" name=\"Split\" type=\"2\" >" +
                        "      <constraints>" +
                        "        <constraint toNodeId=\"8\" toType=\"DROOLS_DEFAULT\" priority=\"2\" type=\"code\" dialect=\"mvel\" >return true;</constraint>" +
                        "        <constraint toNodeId=\"6\" toType=\"DROOLS_DEFAULT\" priority=\"1\" type=\"code\" dialect=\"mvel\" >return kcontext.getVariable(\"person\") != null &amp;&amp; ((Person) kcontext.getVariable(\"person\")).name != null;</constraint>"
                        +
                        "      </constraints>" +
                        "    </split>" +
                        "    <end id=\"8\" name=\"End\" />" +
                        "    <actionNode id=\"6\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >list.add(kcontext.getProcessInstance().getStringId());</action>" +
                        "    </actionNode>" +
                        "    <start id=\"1\" name=\"Start\" />" +
                        "    <end id=\"3\" name=\"End\" />" +
                        "  </nodes>" +
                        "  <connections>" +
                        "    <connection from=\"1\" to=\"2\" />" +
                        "    <connection from=\"2\" to=\"4\" />" +
                        "    <connection from=\"4\" to=\"8\" />" +
                        "    <connection from=\"4\" to=\"6\" />" +
                        "    <connection from=\"6\" to=\"3\" />" +
                        "  </connections>" +
                        "" +
                        "</process>");
        builder.add(new ReaderResource(source), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();
        List<Long> list = new ArrayList<Long>();
        kruntime.getKieSession().setGlobal("list", list);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", new Person("John Doe"));
        KogitoProcessInstance processInstance = kruntime.startProcess("org.jbpm.process-split", params);

        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance.getState());
        assertEquals(1, list.size());
    }

    @Test
    public void testSplitWithJavaContextConstraint() {
        Reader source = new StringReader(
                "<process xmlns=\"http://drools.org/drools-5.0/process\"" +
                        "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"" +
                        "         type=\"RuleFlow\" name=\"ruleflow\" id=\"org.jbpm.process-split\" package-name=\"org.jbpm\" >" +
                        "" +
                        "  <header>" +
                        "    <imports>" +
                        "      <import name=\"org.jbpm.componenttests.test.Person\" />" +
                        "      <import name=\"org.jbpm.componenttests.ProcessSplitTest.ProcessUtils\" />" +
                        "    </imports>" +
                        "    <globals>" +
                        "      <global identifier=\"list\" type=\"java.util.List\" />" +
                        "    </globals>" +
                        "    <variables>\n" +
                        "      <variable name=\"name\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "      </variable>\n" +
                        "    </variables>\n" +
                        "  </header>" +
                        "" +
                        "  <nodes>" +
                        "    <actionNode id=\"2\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >insert(context.getProcessInstance());</action>" +
                        "    </actionNode>" +
                        "    <split id=\"4\" name=\"Split\" type=\"2\" >" +
                        "      <constraints>" +
                        "        <constraint toNodeId=\"8\" toType=\"DROOLS_DEFAULT\" priority=\"2\" type=\"code\" dialect=\"java\" >return true;</constraint>" +
                        "        <constraint toNodeId=\"6\" toType=\"DROOLS_DEFAULT\" priority=\"1\" type=\"code\" dialect=\"java\" >return context.getVariable(\"name\") != null &amp;&amp; ((String) context.getVariable(\"name\")).length() > 0;</constraint>"
                        +
                        "      </constraints>" +
                        "    </split>" +
                        "    <end id=\"8\" name=\"End\" />" +
                        "    <actionNode id=\"6\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >list.add(context.getProcessInstance().getStringId());</action>" +
                        "    </actionNode>" +
                        "    <start id=\"1\" name=\"Start\" />" +
                        "    <end id=\"3\" name=\"End\" />" +
                        "  </nodes>" +
                        "  <connections>" +
                        "    <connection from=\"1\" to=\"2\" />" +
                        "    <connection from=\"2\" to=\"4\" />" +
                        "    <connection from=\"4\" to=\"8\" />" +
                        "    <connection from=\"4\" to=\"6\" />" +
                        "    <connection from=\"6\" to=\"3\" />" +
                        "  </connections>" +
                        "" +
                        "</process>");
        builder.add(new ReaderResource(source), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();
        List<Long> list = new ArrayList<Long>();
        kruntime.getKieSession().setGlobal("list", list);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "John Doe");
        KogitoProcessInstance processInstance = kruntime.startProcess("org.jbpm.process-split", params);

        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance.getState());
        assertEquals(1, list.size());
    }

    @Test
    public void testSplitWithMVELkContextConstraint() {
        Reader source = new StringReader(
                "<process xmlns=\"http://drools.org/drools-5.0/process\"" +
                        "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"" +
                        "         type=\"RuleFlow\" name=\"ruleflow\" id=\"org.jbpm.process-split\" package-name=\"org.jbpm\" >" +
                        "" +
                        "  <header>" +
                        "    <imports>" +
                        "      <import name=\"org.jbpm.componenttests.test.Person\" />" +
                        "      <import name=\"org.jbpm.componenttests.ProcessSplitTest.ProcessUtils\" />" +
                        "    </imports>" +
                        "    <globals>" +
                        "      <global identifier=\"list\" type=\"java.util.List\" />" +
                        "    </globals>" +
                        "    <variables>\n" +
                        "      <variable name=\"person\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.ObjectDataType\" className=\"org.jbpm.componenttests.test.Person\" />\n" +
                        "      </variable>\n" +
                        "    </variables>\n" +
                        "  </header>" +
                        "" +
                        "  <nodes>" +
                        "    <actionNode id=\"2\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >insert(kcontext.getProcessInstance());</action>" +
                        "    </actionNode>" +
                        "    <split id=\"4\" name=\"Split\" type=\"2\" >" +
                        "      <constraints>" +
                        "        <constraint toNodeId=\"8\" toType=\"DROOLS_DEFAULT\" priority=\"2\" type=\"code\" dialect=\"mvel\" >return true;</constraint>" +
                        "        <constraint toNodeId=\"6\" toType=\"DROOLS_DEFAULT\" priority=\"1\" type=\"code\" dialect=\"mvel\" >return context.getVariable(\"person\") != null &amp;&amp; ((org.jbpm.componenttests.test.Person) context.getVariable(\"person\")).name != null;</constraint>"
                        +
                        "      </constraints>" +
                        "    </split>" +
                        "    <end id=\"8\" name=\"End\" />" +
                        "    <actionNode id=\"6\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >list.add(kcontext.getProcessInstance().getStringId());</action>" +
                        "    </actionNode>" +
                        "    <start id=\"1\" name=\"Start\" />" +
                        "    <end id=\"3\" name=\"End\" />" +
                        "  </nodes>" +
                        "  <connections>" +
                        "    <connection from=\"1\" to=\"2\" />" +
                        "    <connection from=\"2\" to=\"4\" />" +
                        "    <connection from=\"4\" to=\"8\" />" +
                        "    <connection from=\"4\" to=\"6\" />" +
                        "    <connection from=\"6\" to=\"3\" />" +
                        "  </connections>" +
                        "" +
                        "</process>");
        builder.add(new ReaderResource(source), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();
        List<Long> list = new ArrayList<Long>();
        kruntime.getKieSession().setGlobal("list", list);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", new Person("John Doe"));
        KogitoProcessInstance processInstance = kruntime.startProcess("org.jbpm.process-split", params);

        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance.getState());
        assertEquals(1, list.size());
    }

    @Test
    public void testSplitWithJavakContextConstraint() {
        Reader source = new StringReader(
                "<process xmlns=\"http://drools.org/drools-5.0/process\"" +
                        "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"" +
                        "         type=\"RuleFlow\" name=\"ruleflow\" id=\"org.jbpm.process-split\" package-name=\"org.jbpm\" >" +
                        "" +
                        "  <header>" +
                        "    <imports>" +
                        "      <import name=\"org.jbpm.componenttests.test.Person\" />" +
                        "      <import name=\"org.jbpm.componenttests.ProcessSplitTest.ProcessUtils\" />" +
                        "    </imports>" +
                        "    <globals>" +
                        "      <global identifier=\"list\" type=\"java.util.List\" />" +
                        "    </globals>" +
                        "    <variables>\n" +
                        "      <variable name=\"name\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "      </variable>\n" +
                        "    </variables>\n" +
                        "  </header>" +
                        "" +
                        "  <nodes>" +
                        "    <actionNode id=\"2\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >insert(kcontext.getProcessInstance());</action>" +
                        "    </actionNode>" +
                        "    <split id=\"4\" name=\"Split\" type=\"2\" >" +
                        "      <constraints>" +
                        "        <constraint toNodeId=\"8\" toType=\"DROOLS_DEFAULT\" priority=\"2\" type=\"code\" dialect=\"java\" >return true;</constraint>" +
                        "        <constraint toNodeId=\"6\" toType=\"DROOLS_DEFAULT\" priority=\"1\" type=\"code\" dialect=\"java\" >return kcontext.getVariable(\"name\") != null &amp;&amp; ((String) kcontext.getVariable(\"name\")).length() > 0;</constraint>"
                        +
                        "      </constraints>" +
                        "    </split>" +
                        "    <end id=\"8\" name=\"End\" />" +
                        "    <actionNode id=\"6\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >list.add(kcontext.getProcessInstance().getStringId());</action>" +
                        "    </actionNode>" +
                        "    <start id=\"1\" name=\"Start\" />" +
                        "    <end id=\"3\" name=\"End\" />" +
                        "  </nodes>" +
                        "  <connections>" +
                        "    <connection from=\"1\" to=\"2\" />" +
                        "    <connection from=\"2\" to=\"4\" />" +
                        "    <connection from=\"4\" to=\"8\" />" +
                        "    <connection from=\"4\" to=\"6\" />" +
                        "    <connection from=\"6\" to=\"3\" />" +
                        "  </connections>" +
                        "" +
                        "</process>");
        builder.add(new ReaderResource(source), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();
        List<Long> list = new ArrayList<Long>();
        kruntime.getKieSession().setGlobal("list", list);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "John Doe");
        KogitoProcessInstance processInstance = kruntime.startProcess("org.jbpm.process-split", params);

        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance.getState());
        assertEquals(1, list.size());
    }

    @Test
    public void testSplitWithMVELVariableConstraint() {
        Reader source = new StringReader(
                "<process xmlns=\"http://drools.org/drools-5.0/process\"" +
                        "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"" +
                        "         type=\"RuleFlow\" name=\"ruleflow\" id=\"org.jbpm.process-split\" package-name=\"org.jbpm\" >" +
                        "" +
                        "  <header>" +
                        "    <imports>" +
                        "      <import name=\"org.jbpm.componenttests.test.Person\" />" +
                        "      <import name=\"org.jbpm.componenttests.ProcessSplitTest.ProcessUtils\" />" +
                        "    </imports>" +
                        "    <globals>" +
                        "      <global identifier=\"list\" type=\"java.util.List\" />" +
                        "    </globals>" +
                        "    <variables>\n" +
                        "      <variable name=\"name\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "      </variable>\n" +
                        "    </variables>\n" +
                        "  </header>" +
                        "" +
                        "  <nodes>" +
                        "    <actionNode id=\"2\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >insert(context.getProcessInstance());</action>" +
                        "    </actionNode>" +
                        "    <split id=\"4\" name=\"Split\" type=\"2\" >" +
                        "      <constraints>" +
                        "        <constraint toNodeId=\"8\" toType=\"DROOLS_DEFAULT\" priority=\"2\" type=\"code\" dialect=\"mvel\" >return true;</constraint>" +
                        "        <constraint toNodeId=\"6\" toType=\"DROOLS_DEFAULT\" priority=\"1\" type=\"code\" dialect=\"mvel\" >return name != null &amp;&amp; name.length > 0;</constraint>" +
                        "      </constraints>" +
                        "    </split>" +
                        "    <end id=\"8\" name=\"End\" />" +
                        "    <actionNode id=\"6\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >list.add(context.getProcessInstance().getStringId());</action>" +
                        "    </actionNode>" +
                        "    <start id=\"1\" name=\"Start\" />" +
                        "    <end id=\"3\" name=\"End\" />" +
                        "  </nodes>" +
                        "  <connections>" +
                        "    <connection from=\"1\" to=\"2\" />" +
                        "    <connection from=\"2\" to=\"4\" />" +
                        "    <connection from=\"4\" to=\"8\" />" +
                        "    <connection from=\"4\" to=\"6\" />" +
                        "    <connection from=\"6\" to=\"3\" />" +
                        "  </connections>" +
                        "" +
                        "</process>");
        builder.add(new ReaderResource(source), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();
        List<Long> list = new ArrayList<Long>();
        kruntime.getKieSession().setGlobal("list", list);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "John Doe");
        KogitoProcessInstance processInstance = kruntime.startProcess("org.jbpm.process-split", params);

        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance.getState());
        assertEquals(1, list.size());
    }

    @Test
    public void testSplitWithJavaVariableConstraint() {
        Reader source = new StringReader(
                "<process xmlns=\"http://drools.org/drools-5.0/process\"" +
                        "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"" +
                        "         type=\"RuleFlow\" name=\"ruleflow\" id=\"org.jbpm.process-split\" package-name=\"org.jbpm\" >" +
                        "" +
                        "  <header>" +
                        "    <imports>" +
                        "      <import name=\"org.jbpm.componenttests.test.Person\" />" +
                        "      <import name=\"org.jbpm.componenttests.ProcessSplitTest.ProcessUtils\" />" +
                        "    </imports>" +
                        "    <globals>" +
                        "      <global identifier=\"list\" type=\"java.util.List\" />" +
                        "    </globals>" +
                        "    <variables>\n" +
                        "      <variable name=\"name\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "      </variable>\n" +
                        "    </variables>\n" +
                        "  </header>" +
                        "" +
                        "  <nodes>" +
                        "    <actionNode id=\"2\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >insert(context.getProcessInstance());</action>" +
                        "    </actionNode>" +
                        "    <split id=\"4\" name=\"Split\" type=\"2\" >" +
                        "      <constraints>" +
                        "        <constraint toNodeId=\"8\" toType=\"DROOLS_DEFAULT\" priority=\"2\" type=\"code\" dialect=\"java\" >return true;</constraint>" +
                        "        <constraint toNodeId=\"6\" toType=\"DROOLS_DEFAULT\" priority=\"1\" type=\"code\" dialect=\"java\" >return name != null &amp;&amp; name.length() > 0;</constraint>" +
                        "      </constraints>" +
                        "    </split>" +
                        "    <end id=\"8\" name=\"End\" />" +
                        "    <actionNode id=\"6\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >list.add(context.getProcessInstance().getStringId());</action>" +
                        "    </actionNode>" +
                        "    <start id=\"1\" name=\"Start\" />" +
                        "    <end id=\"3\" name=\"End\" />" +
                        "  </nodes>" +
                        "  <connections>" +
                        "    <connection from=\"1\" to=\"2\" />" +
                        "    <connection from=\"2\" to=\"4\" />" +
                        "    <connection from=\"4\" to=\"8\" />" +
                        "    <connection from=\"4\" to=\"6\" />" +
                        "    <connection from=\"6\" to=\"3\" />" +
                        "  </connections>" +
                        "" +
                        "</process>");
        builder.add(new ReaderResource(source), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();
        List<Long> list = new ArrayList<Long>();
        kruntime.getKieSession().setGlobal("list", list);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "John Doe");
        KogitoProcessInstance processInstance = kruntime.startProcess("org.jbpm.process-split", params);

        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance.getState());
        assertEquals(1, list.size());
    }

    @Test
    public void testSplitWithMVELGlobalConstraint() {
        Reader source = new StringReader(
                "<process xmlns=\"http://drools.org/drools-5.0/process\"" +
                        "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"" +
                        "         type=\"RuleFlow\" name=\"ruleflow\" id=\"org.jbpm.process-split\" package-name=\"org.jbpm\" >" +
                        "" +
                        "  <header>" +
                        "    <imports>" +
                        "      <import name=\"org.jbpm.componenttests.test.Person\" />" +
                        "      <import name=\"org.jbpm.componenttests.ProcessSplitTest.ProcessUtils\" />" +
                        "    </imports>" +
                        "    <globals>" +
                        "      <global identifier=\"list\" type=\"java.util.List\" />" +
                        "    </globals>" +
                        "    <variables>\n" +
                        "      <variable name=\"name\" >\n" +
                        "        <type name=\"org.jbpm.process.core.datatype.impl.type.StringDataType\" />\n" +
                        "      </variable>\n" +
                        "    </variables>\n" +
                        "  </header>" +
                        "" +
                        "  <nodes>" +
                        "    <actionNode id=\"2\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >insert(context.getProcessInstance());</action>" +
                        "    </actionNode>" +
                        "    <split id=\"4\" name=\"Split\" type=\"2\" >" +
                        "      <constraints>" +
                        "        <constraint toNodeId=\"8\" toType=\"DROOLS_DEFAULT\" priority=\"2\" type=\"code\" dialect=\"mvel\" >return true;</constraint>" +
                        "        <constraint toNodeId=\"6\" toType=\"DROOLS_DEFAULT\" priority=\"1\" type=\"code\" dialect=\"mvel\" >return list != null &amp;&amp; list.size() >= 0;</constraint>" +
                        "      </constraints>" +
                        "    </split>" +
                        "    <end id=\"8\" name=\"End\" />" +
                        "    <actionNode id=\"6\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >list.add(context.getProcessInstance().getStringId());</action>" +
                        "    </actionNode>" +
                        "    <start id=\"1\" name=\"Start\" />" +
                        "    <end id=\"3\" name=\"End\" />" +
                        "  </nodes>" +
                        "  <connections>" +
                        "    <connection from=\"1\" to=\"2\" />" +
                        "    <connection from=\"2\" to=\"4\" />" +
                        "    <connection from=\"4\" to=\"8\" />" +
                        "    <connection from=\"4\" to=\"6\" />" +
                        "    <connection from=\"6\" to=\"3\" />" +
                        "  </connections>" +
                        "" +
                        "</process>");
        builder.add(new ReaderResource(source), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();
        List<Long> list = new ArrayList<Long>();
        kruntime.getKieSession().setGlobal("list", list);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "John Doe");
        KogitoProcessInstance processInstance = kruntime.startProcess("org.jbpm.process-split", params);

        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance.getState());
        assertEquals(1, list.size());
    }

    @Test
    public void testSplitWithJavaGlobalConstraint() {
        Reader source = new StringReader(
                "<process xmlns=\"http://drools.org/drools-5.0/process\"" +
                        "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"" +
                        "         type=\"RuleFlow\" name=\"ruleflow\" id=\"org.jbpm.process-split\" package-name=\"org.jbpm\" >" +
                        "" +
                        "  <header>" +
                        "    <imports>" +
                        "      <import name=\"org.jbpm.componenttests.test.Person\" />" +
                        "      <import name=\"org.jbpm.componenttests.ProcessSplitTest.ProcessUtils\" />" +
                        "    </imports>" +
                        "    <globals>" +
                        "      <global identifier=\"list\" type=\"java.util.List\" />" +
                        "    </globals>" +
                        "  </header>" +
                        "" +
                        "  <nodes>" +
                        "    <actionNode id=\"2\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >insert(context.getProcessInstance());</action>" +
                        "    </actionNode>" +
                        "    <split id=\"4\" name=\"Split\" type=\"2\" >" +
                        "      <constraints>" +
                        "        <constraint toNodeId=\"8\" toType=\"DROOLS_DEFAULT\" priority=\"2\" type=\"code\" dialect=\"java\" >return true;</constraint>" +
                        "        <constraint toNodeId=\"6\" toType=\"DROOLS_DEFAULT\" priority=\"1\" type=\"code\" dialect=\"java\" >return list != null &amp;&amp; list.size() >= 0;</constraint>" +
                        "      </constraints>" +
                        "    </split>" +
                        "    <end id=\"8\" name=\"End\" />" +
                        "    <actionNode id=\"6\" name=\"Action\" >" +
                        "        <action type=\"expression\" dialect=\"mvel\" >list.add(context.getProcessInstance().getStringId());</action>" +
                        "    </actionNode>" +
                        "    <start id=\"1\" name=\"Start\" />" +
                        "    <end id=\"3\" name=\"End\" />" +
                        "  </nodes>" +
                        "  <connections>" +
                        "    <connection from=\"1\" to=\"2\" />" +
                        "    <connection from=\"2\" to=\"4\" />" +
                        "    <connection from=\"4\" to=\"8\" />" +
                        "    <connection from=\"4\" to=\"6\" />" +
                        "    <connection from=\"6\" to=\"3\" />" +
                        "  </connections>" +
                        "" +
                        "</process>");
        builder.add(new ReaderResource(source), ResourceType.DRF);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();
        List<Long> list = new ArrayList<Long>();
        kruntime.getKieSession().setGlobal("list", list);

        KogitoProcessInstance processInstance = kruntime.startProcess("org.jbpm.process-split");

        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance.getState());
        assertEquals(1, list.size());
    }

    public static class ProcessUtils {

        public static Object getValue(RuleFlowProcessInstance processInstance, String name) {
            VariableScopeInstance scope = (VariableScopeInstance) processInstance.getContextInstance(VariableScope.VARIABLE_SCOPE);
            return scope.getVariable(name);
        }

    }

}
