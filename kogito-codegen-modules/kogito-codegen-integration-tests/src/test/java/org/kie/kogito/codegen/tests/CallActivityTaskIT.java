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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.instance.impl.humantask.HumanTaskTransition;
import org.jbpm.process.instance.impl.workitem.Active;
import org.jbpm.process.instance.impl.workitem.Complete;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.auth.SecurityPolicy;
import org.kie.kogito.codegen.AbstractCodegenIT;
import org.kie.kogito.codegen.data.Address;
import org.kie.kogito.codegen.data.Person;
import org.kie.kogito.codegen.data.PersonWithAddress;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.workitem.Policy;
import org.kie.kogito.services.identity.StaticIdentityProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CallActivityTaskIT extends AbstractCodegenIT {

    private Policy<?> securityPolicy = SecurityPolicy.of(new StaticIdentityProvider("john"));

    @Test
    public void testBasicCallActivityTask() throws Exception {

        Application app = generateCodeProcessesOnly("subprocess/CallActivity.bpmn2", "subprocess/CallActivitySubProcess.bpmn2");
        assertThat(app).isNotNull();

        Process p = app.get(Processes.class).processById("ParentProcess");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("x", "a");
        parameters.put("y", "b");
        m.fromMap(parameters);

        ProcessInstance processInstance = p.createInstance(m);
        processInstance.start();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        Model result = (Model) processInstance.variables();
        assertThat(result.toMap()).hasSize(2).containsKeys("x", "y");
        assertThat(result.toMap().get("y")).isNotNull().isEqualTo("new value");
        assertThat(result.toMap().get("x")).isNotNull().isEqualTo("a");
    }

    @Test
    public void testBasicCallActivityTaskWithTypeInfo() throws Exception {

        Application app = generateCodeProcessesOnly("subprocess/CallActivityWithTypeInfo.bpmn2", "subprocess/CallActivitySubProcess.bpmn2");
        assertThat(app).isNotNull();

        Process p = app.get(Processes.class).processById("ParentProcess");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("x", "a");
        parameters.put("y", "b");
        m.fromMap(parameters);

        ProcessInstance processInstance = p.createInstance(m);
        processInstance.start();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        Model result = (Model) processInstance.variables();
        assertThat(result.toMap()).hasSize(2).containsKeys("x", "y");
        assertThat(result.toMap().get("y")).isNotNull().isEqualTo("new value");
        assertThat(result.toMap().get("x")).isNotNull().isEqualTo("a");
    }

    @Test
    public void testCallActivityTaskMultiInstance() throws Exception {

        Application app = generateCodeProcessesOnly("subprocess/CallActivityMI.bpmn2", "subprocess/CallActivitySubProcess.bpmn2");
        assertThat(app).isNotNull();

        Process p = app.get(Processes.class).processById("ParentProcess");

        List<String> list = new ArrayList<String>();
        list.add("first");
        list.add("second");
        List<String> listOut = new ArrayList<String>();

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("list", list);
        parameters.put("listOut", listOut);
        m.fromMap(parameters);

        ProcessInstance processInstance = p.createInstance(m);
        processInstance.start();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        Model result = (Model) processInstance.variables();
        assertThat(result.toMap()).hasSize(4).containsKeys("x", "y", "list", "listOut");
        assertThat((List<?>) result.toMap().get("listOut")).isNotNull().hasSize(2);

    }

    @Test
    public void testCallActivityTaskWithExpressionsForIO() throws Exception {

        Application app = generateCodeProcessesOnly("subprocess/CallActivityWithIOexpression.bpmn2", "subprocess/CallActivitySubProcess.bpmn2");
        assertThat(app).isNotNull();

        Process p = app.get(Processes.class).processById("ParentProcess");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("person", new Person("john", 0));
        m.fromMap(parameters);

        ProcessInstance processInstance = p.createInstance(m);
        processInstance.start();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        Model result = (Model) processInstance.variables();
        assertThat(result.toMap()).hasSize(1).containsKeys("person");

        Person person = (Person) result.toMap().get("person");
        assertEquals("new value", person.getName());

        List<WorkItem> workItems = processInstance.workItems(securityPolicy);
        assertEquals(1, workItems.size());
        WorkItem wi = workItems.get(0);
        assertEquals("MyTask", wi.getName());
        assertEquals(Active.ID, wi.getPhase());
        assertEquals(Active.STATUS, wi.getPhaseStatus());

        processInstance.transitionWorkItem(workItems.get(0).getId(), new HumanTaskTransition(Complete.ID, null, securityPolicy));

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
    }

    @Test
    public void testCallActivityTaskWithExpressionsForIONested() throws Exception {

        Application app = generateCodeProcessesOnly("subprocess/CallActivityWithIOexpressionNested.bpmn2", "subprocess/CallActivitySubProcess.bpmn2");
        assertThat(app).isNotNull();

        Process p = app.get(Processes.class).processById("ParentProcess");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        PersonWithAddress pa = new PersonWithAddress("john", 0);
        pa.setAddress(new Address("test", null, null, null));
        parameters.put("person", pa);
        m.fromMap(parameters);

        ProcessInstance processInstance = p.createInstance(m);
        processInstance.start();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        Model result = (Model) processInstance.variables();
        assertThat(result.toMap()).hasSize(1).containsKeys("person");

        PersonWithAddress person = (PersonWithAddress) result.toMap().get("person");
        assertEquals("john", person.getName());
        assertEquals("test", person.getAddress().getStreet());
        assertEquals("new value", person.getAddress().getCity());

        List<WorkItem> workItems = processInstance.workItems(securityPolicy);
        assertEquals(1, workItems.size());
        WorkItem wi = workItems.get(0);
        assertEquals("MyTask", wi.getName());
        assertEquals(Active.ID, wi.getPhase());
        assertEquals(Active.STATUS, wi.getPhaseStatus());

        processInstance.transitionWorkItem(workItems.get(0).getId(), new HumanTaskTransition(Complete.ID, null, securityPolicy));

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
    }

    @Test
    public void testBasicCallActivityTaskWithSingleVarExpression() throws Exception {

        Application app = generateCodeProcessesOnly("subprocess/CallActivityVarIOExpression.bpmn2", "subprocess/CallActivitySubProcess.bpmn2");
        assertThat(app).isNotNull();

        Process p = app.get(Processes.class).processById("ParentProcess");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("x", "a");
        parameters.put("y", "b");
        m.fromMap(parameters);

        ProcessInstance processInstance = p.createInstance(m);
        processInstance.start();

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        Model result = (Model) processInstance.variables();
        assertThat(result.toMap()).hasSize(2).containsKeys("x", "y");
        assertThat(result.toMap().get("y")).isNotNull().isEqualTo("new value");
        assertThat(result.toMap().get("x")).isNotNull().isEqualTo("a");
    }
}
