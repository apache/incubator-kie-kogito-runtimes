/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;

import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kie.api.definition.process.Node;

public class ServlerlessWorkflowParsingTest {

    @ParameterizedTest
    @ValueSource(strings = {"/single-operation.sw.json", "/single-operation.sw.yml"})
    public void testSingleOperationWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation).parseWorkFlow(classpathResourceReader(workflowLocation));
        assertEquals("function", process.getId());
        assertEquals("test-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(3, process.getNodes().length);

        Node node = process.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[1];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof EndNode);

        // now check the composite one to see what nodes it has
        CompositeContextNode compositeNode = (CompositeContextNode) process.getNodes()[1];

        assertEquals(3, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof EndNode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/single-eventstate.sw.json", "/single-eventstate.sw.yml"})
    public void testSingleEventStateWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation).parseWorkFlow(classpathResourceReader(workflowLocation));
        assertEquals("function", process.getId());
        assertEquals("test-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(3, process.getNodes().length);

        Node node = process.getNodes()[0];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[1];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof EndNode);

        // now check the composite one to see what nodes it has
        CompositeContextNode compositeNode = (CompositeContextNode) process.getNodes()[0];

        assertEquals(3, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof EndNode);

    }

    @ParameterizedTest
    @ValueSource(strings = {"/single-operation-many-functions.sw.json", "/single-operation-many-functions.sw.yml"})
    public void testSingleOperationWithManyFunctionsWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation).parseWorkFlow(classpathResourceReader(workflowLocation));
        assertEquals("function", process.getId());
        assertEquals("test-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(3, process.getNodes().length);

        Node node = process.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[1];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof EndNode);

        // now check the composite one to see what nodes it has
        CompositeContextNode compositeNode = (CompositeContextNode) process.getNodes()[1];

        assertEquals(4, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[3];
        assertTrue(node instanceof EndNode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/multiple-operations.sw.json", "/multiple-operations.sw.yml"})
    public void testMultipleOperationWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation).parseWorkFlow(classpathResourceReader(workflowLocation));
        assertEquals("function", process.getId());
        assertEquals("test-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(5, process.getNodes().length);

        Node node = process.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[1];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[3];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[4];
        assertTrue(node instanceof EndNode);

        // now check the composite one to see what nodes it has
        CompositeContextNode compositeNode = (CompositeContextNode) process.getNodes()[1];

        assertEquals(3, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof EndNode);

        compositeNode = (CompositeContextNode) process.getNodes()[2];

        assertEquals(3, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof EndNode);

        compositeNode = (CompositeContextNode) process.getNodes()[3];

        assertEquals(3, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof EndNode);
    }
    
    /*
     * Helper methods
     */
    protected Reader classpathResourceReader(String location) {
        return new InputStreamReader(this.getClass().getResourceAsStream(location));
    }

    protected ServerlessWorkflowParser getWorkflowParser(String workflowLocation) {
        ServerlessWorkflowParser parser;
        if(workflowLocation.endsWith(".sw.json")) {
            parser = new ServerlessWorkflowParser("json");
        } else {
            parser = new ServerlessWorkflowParser("yml");
        }
        return parser;
    }
}