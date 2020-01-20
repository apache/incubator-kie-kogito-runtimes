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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.junit.jupiter.api.Test;
import org.kie.api.definition.process.Node;

public class ServlerlessWorkflowParsingTest {

    @Test
    public void testSingleOperationWorkflow() throws Exception {

        ServerlessWorkflowParser parser = new ServerlessWorkflowParser();
        RuleFlowProcess process = (RuleFlowProcess) parser.parseWorkFlow(classpathResourceReader("/single-operation.sw.json"));
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

    @Test
    public void testSingleEventStateWorkflow() throws Exception {
        ServerlessWorkflowParser parser = new ServerlessWorkflowParser();
        RuleFlowProcess process = (RuleFlowProcess) parser.parseWorkFlow(classpathResourceReader("/single-eventstate.sw.json"));
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

    @Test
    public void testSingleOperationWithManyFunctionsWorkflow() throws Exception {

        ServerlessWorkflowParser parser = new ServerlessWorkflowParser();

        RuleFlowProcess process = (RuleFlowProcess) parser.parseWorkFlow(classpathResourceReader("/single-operation-many-functions.sw.json"));
        assertEquals("function", process.getId());
        assertEquals("test-wf", process.getName());
        assertNull(process.getVersion());
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

    @Test
    public void testMultipleOperationWorkflow() throws Exception {

        ServerlessWorkflowParser parser = new ServerlessWorkflowParser();

        RuleFlowProcess process = (RuleFlowProcess) parser.parseWorkFlow(classpathResourceReader("/multiple-operations.sw.json"));
        assertEquals("function", process.getId());
        assertEquals("test-wf", process.getName());
        assertNull(process.getVersion());
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
}