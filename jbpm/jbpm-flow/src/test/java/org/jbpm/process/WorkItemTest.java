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
package org.jbpm.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.process.core.ParameterDefinition;
import org.jbpm.process.core.Work;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.datatype.impl.type.IntegerDataType;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.process.core.datatype.impl.type.StringDataType;
import org.jbpm.process.core.impl.ParameterDefinitionImpl;
import org.jbpm.process.core.impl.WorkImpl;
import org.jbpm.process.test.Person;
import org.jbpm.process.workitem.builtin.DoNothingWorkItemHandler;
import org.jbpm.process.workitem.builtin.MockDataWorkItemHandler;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.WorkflowElementIdentifierFactory;
import org.jbpm.test.util.AbstractBaseTest;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.junit.jupiter.api.Test;
import org.kie.api.definition.process.WorkflowElementIdentifier;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessRuntime;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkItemTest extends AbstractBaseTest {

    private static WorkflowElementIdentifier one = WorkflowElementIdentifierFactory.fromExternalFormat("one");
    private static WorkflowElementIdentifier two = WorkflowElementIdentifierFactory.fromExternalFormat("two");
    private static WorkflowElementIdentifier three = WorkflowElementIdentifierFactory.fromExternalFormat("three");

    public void addLogger() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Test
    public void testReachNonRegisteredWorkItemHandler() {
        String processId = "org.drools.actions";
        String workName = "Unnexistent Task";
        RuleFlowProcess process = getWorkItemProcess(processId,
                workName);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime(process);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("UserName",
                "John Doe");
        parameters.put("Person",
                new Person("John Doe"));

        KogitoProcessInstance processInstance = null;
        processInstance = kruntime.startProcess("org.drools.actions", parameters);
        assertThat(processInstance.getState()).isEqualTo(KogitoProcessInstance.STATE_ERROR);
    }

    @Test
    public void testCancelNonRegisteredWorkItemHandler() {
        String processId = "org.drools.actions";
        String workName = "Unnexistent Task";
        RuleFlowProcess process = getWorkItemProcess(processId,
                workName);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime(process);

        kruntime.getKogitoWorkItemManager().registerWorkItemHandler(workName,
                new DoNothingWorkItemHandler());

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("UserName",
                "John Doe");
        parameters.put("Person",
                new Person("John Doe"));

        KogitoProcessInstance processInstance = kruntime.startProcess("org.drools.actions", parameters);
        String processInstanceId = processInstance.getStringId();
        assertThat(processInstance.getState()).isEqualTo(KogitoProcessInstance.STATE_ACTIVE);
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler(workName,
                null);

        kruntime.abortProcessInstance(processInstanceId);

        assertThat(processInstance.getState()).isEqualTo(KogitoProcessInstance.STATE_ABORTED);
    }

    @Test
    public void testMockDataWorkItemHandler() {
        String processId = "org.drools.actions";
        String workName = "Unnexistent Task";
        RuleFlowProcess process = getWorkItemProcess(processId,
                workName);
        KogitoProcessRuntime kruntime = createKogitoProcessRuntime(process);
        Map<String, Object> output = new HashMap<String, Object>();
        output.put("Result", "test");

        kruntime.getKogitoWorkItemManager().registerWorkItemHandler(workName,
                new MockDataWorkItemHandler(output));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("UserName",
                "John Doe");
        parameters.put("Person",
                new Person("John Doe"));

        KogitoProcessInstance processInstance = kruntime.startProcess("org.drools.actions",
                parameters);

        Object numberVariable = ((WorkflowProcessInstance) processInstance).getVariable("MyObject");
        assertThat(numberVariable).isNotNull().isEqualTo("test");

        assertThat(processInstance.getState()).isEqualTo(KogitoProcessInstance.STATE_COMPLETED);
    }

    @Test
    public void testMockDataWorkItemHandlerCustomFunction() {
        String processId = "org.drools.actions";
        String workName = "Unnexistent Task";
        RuleFlowProcess process = getWorkItemProcess(processId,
                workName);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime(process);

        kruntime.getKogitoWorkItemManager().registerWorkItemHandler(workName,
                new MockDataWorkItemHandler((input) -> {
                    Map<String, Object> output = new HashMap<String, Object>();
                    if ("John Doe".equals(input.get("Comment"))) {
                        output.put("Result", "one");
                    } else {
                        output.put("Result", "two");

                    }
                    return output;
                }));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("UserName",
                "John Doe");
        parameters.put("Person",
                new Person("John Doe"));

        KogitoProcessInstance processInstance = kruntime.startProcess("org.drools.actions",
                parameters);

        Object numberVariable = ((WorkflowProcessInstance) processInstance).getVariable("MyObject");
        assertThat(numberVariable).isNotNull().isEqualTo("one");

        assertThat(processInstance.getState()).isEqualTo(KogitoProcessInstance.STATE_COMPLETED);
        parameters = new HashMap<String, Object>();
        parameters.put("UserName",
                "John Doe");
        parameters.put("Person",
                new Person("John Deen"));

        processInstance = kruntime.startProcess("org.drools.actions",
                parameters);

        numberVariable = ((WorkflowProcessInstance) processInstance).getVariable("MyObject");
        assertThat(numberVariable).isNotNull().isEqualTo("two");

        assertThat(processInstance.getState()).isEqualTo(KogitoProcessInstance.STATE_COMPLETED);
    }

    private RuleFlowProcess getWorkItemProcess(String processId,
            String workName) {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId(processId);

        List<Variable> variables = new ArrayList<Variable>();
        Variable variable = new Variable();
        variable.setName("UserName");
        variable.setType(new StringDataType());
        variables.add(variable);
        variable = new Variable();
        variable.setName("Person");
        variable.setType(new ObjectDataType(Person.class.getName()));
        variables.add(variable);
        variable = new Variable();
        variable.setName("MyObject");
        variable.setType(new ObjectDataType());
        variables.add(variable);
        variable = new Variable();
        variable.setName("Number");
        variable.setType(new IntegerDataType());
        variables.add(variable);
        process.getVariableScope().setVariables(variables);

        StartNode startNode = new StartNode();
        startNode.setName("Start");
        startNode.setId(one);

        WorkItemNode workItemNode = new WorkItemNode();
        workItemNode.setName("workItemNode");
        workItemNode.setId(two);
        workItemNode.getIoSpecification().addInputMapping("#{Person.name}", "Comment");
        workItemNode.getIoSpecification().addInputMapping("MyObject", "Attachment");
        workItemNode.getIoSpecification().addOutputMapping("Result", "MyObject");
        workItemNode.getIoSpecification().addOutputMapping("Result.length()", "Number");
        Work work = new WorkImpl();
        work.setName(workName);
        Set<ParameterDefinition> parameterDefinitions = new HashSet<ParameterDefinition>();
        ParameterDefinition parameterDefinition = new ParameterDefinitionImpl("ActorId",
                new StringDataType());
        parameterDefinitions.add(parameterDefinition);
        parameterDefinition = new ParameterDefinitionImpl("Content",
                new StringDataType());
        parameterDefinitions.add(parameterDefinition);
        parameterDefinition = new ParameterDefinitionImpl("Comment",
                new StringDataType());
        parameterDefinitions.add(parameterDefinition);
        work.setParameterDefinitions(parameterDefinitions);
        work.setParameter("ActorId", "#{UserName}");
        work.setParameter("Content", "#{Person.name}");
        workItemNode.setWork(work);

        EndNode endNode = new EndNode();
        endNode.setName("End");
        endNode.setId(three);

        connect(startNode,
                workItemNode);
        connect(workItemNode,
                endNode);

        process.addNode(startNode);
        process.addNode(workItemNode);
        process.addNode(endNode);

        return process;
    }

    private void connect(Node sourceNode,
            Node targetNode) {
        new ConnectionImpl(sourceNode,
                Node.CONNECTION_DEFAULT_TYPE,
                targetNode,
                Node.CONNECTION_DEFAULT_TYPE);
    }

}
