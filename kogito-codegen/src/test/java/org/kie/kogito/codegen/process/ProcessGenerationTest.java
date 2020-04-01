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

package org.kie.kogito.codegen.process;

import org.jbpm.process.core.timer.Timer;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.impl.ExtendedNodeImpl;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.*;
import org.junit.jupiter.api.Test;
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.Node;
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.codegen.AbstractCodegenTest;
import org.kie.kogito.process.impl.AbstractProcess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ProcessGenerationTest extends AbstractCodegenTest {

    private static final Set<String> IGNORED_PROCESS_META = Set.of("Definitions", "BPMN.Connections", "ItemDefinitions");

    @Test
    public void testProcessesGeneration() throws IOException {
        Files.lines(Paths.get("src/test/resources/org/kie/kogito/codegen/process/process-generation-test.txt"))
                .filter(f -> !f.startsWith("#"))
                .forEach(this::testProcessGereration);
    }

    private void testProcessGereration(String processFile) {
        try {
            List<org.kie.api.definition.process.Process> processes = ProcessCodegen.parseProcesses(Stream.of(processFile)
                    .map(resource -> new File("src/test/resources", resource))
                    .collect(Collectors.toList()));
            RuleFlowProcess expected = (RuleFlowProcess) processes.get(0);

            Application app = generateCodeProcessesOnly(processFile);
            AbstractProcess<? extends Model> process = (AbstractProcess<? extends Model>) app.processes().processById(expected.getId());
            RuleFlowProcess current = (RuleFlowProcess) process.legacyProcess();

            assertNotNull(current);
            assertEquals(expected.getId(), current.getId());
            assertEquals(expected.getName(), current.getName());
            assertEquals(expected.getPackageName(), current.getPackageName());
            assertEquals(expected.getVisibility(), current.getVisibility());
            assertEquals(expected.getType(), current.getType());
            assertEquals(expected.isAutoComplete(), current.isAutoComplete());
            assertEquals(expected.isDynamic(), current.isDynamic());
//        assertEquals(expected.getVersion(), current.getVersion());
            assertEquals(expected.getImports(), current.getImports());
            assertEquals(expected.getFunctionImports(), current.getFunctionImports());
            assertMetadata(expected.getMetaData(), current.getMetaData(), IGNORED_PROCESS_META);

            List<Node> expectedNodes = expected.getNodesRecursively();
            List<Node> currentNodes = current.getNodesRecursively();
            assertEquals(expectedNodes.size(), currentNodes.size());
            expectedNodes.forEach(eNode -> {
                Optional<Node> cNode = currentNodes.stream().filter(c -> eNode.getId() == c.getId()).findFirst();
                assertTrue(cNode.isPresent());
                assertNode(eNode, cNode.get());
            });
        } catch (Throwable e) {
            fail("Unable to validate process generation for: " + processFile, e);
        }
    }

    private static final BiConsumer<Node, Node> nodeAsserter = (expected, current) -> {
        assertEquals(expected.getId(), current.getId());
//        assertEquals(expected.getName(), current.getName());
        assertConnections(expected.getIncomingConnections(), current.getIncomingConnections());
        assertConnections(expected.getOutgoingConnections(), current.getOutgoingConnections());
//        assertEquals(((NodeImpl) eNode).getConstraints(), ((NodeImpl) cNode).getConstraints());
    };

    private static final BiConsumer<Node, Node> extendedNodeAsserter = (eNode, cNode) -> {
        assertTrue(ExtendedNodeImpl.class.isAssignableFrom(eNode.getClass()));
        assertTrue(ExtendedNodeImpl.class.isAssignableFrom(cNode.getClass()));
        ExtendedNodeImpl expected = (ExtendedNodeImpl) eNode;
        ExtendedNodeImpl current = (ExtendedNodeImpl) cNode;
        for (String actionType : expected.getActionTypes()) {
            if (expected.getActions(actionType) == null) {
                assertNull(current.getActions(actionType));
            } else {
                assertNotNull(current.getActions(actionType));
                assertEquals(expected.getActions(actionType).size(), current.getActions(actionType).size());
                assertActions(expected.getActions(actionType), current.getActions(actionType));
            }
        }
    };

    private static final BiConsumer<Node, Node> startNodeAsserter = (eNode, cNode) -> {
        assertEquals(StartNode.class, eNode.getClass());
        assertEquals(StartNode.class, cNode.getClass());
        StartNode expected = (StartNode) eNode;
        StartNode current = (StartNode) cNode;
        assertEquals(expected.isInterrupting(), current.isInterrupting());
        assertTriggers(expected.getTriggers(), current.getTriggers());
    };

    private static final BiConsumer<Node, Node> endNodeAsserter = (eNode, cNode) -> {
        assertEquals(EndNode.class, eNode.getClass());
        assertEquals(EndNode.class, cNode.getClass());
        EndNode expected = (EndNode) eNode;
        EndNode current = (EndNode) cNode;
        assertEquals(expected.isTerminate(), current.isTerminate());
    };

    private static final BiConsumer<Node, Node> stateBasedNodeAsserter = (eNode, cNode) -> {
        assertTrue(StateBasedNode.class.isAssignableFrom(eNode.getClass()));
        assertTrue(StateBasedNode.class.isAssignableFrom(cNode.getClass()));
        StateBasedNode expected = (StateBasedNode) eNode;
        StateBasedNode current = (StateBasedNode) cNode;
        assertEquals(expected.getBoundaryEvents(), current.getBoundaryEvents());
        assertTimers(expected.getTimers(), current.getTimers());
    };

    private static final BiConsumer<Node, Node> workItemNodeAsserter = (eNode, cNode) -> {
        assertTrue(WorkItemNode.class.isAssignableFrom(eNode.getClass()));
        assertTrue(WorkItemNode.class.isAssignableFrom(cNode.getClass()));
        WorkItemNode expected = (WorkItemNode) eNode;
        WorkItemNode current = (WorkItemNode) cNode;
        assertEquals(expected.isWaitForCompletion(), current.isWaitForCompletion());
        expected.getInMappings().forEach((k, v) -> assertEquals(v, current.getInMapping(k)));
        expected.getOutMappings().forEach((k, v) -> assertEquals(v, current.getOutMapping(k)));
    };

    private static final BiConsumer<Node, Node> humanTaskNodeAsserter = (eNode, cNode) -> {
        assertEquals(HumanTaskNode.class, eNode.getClass());
        assertEquals(HumanTaskNode.class, cNode.getClass());
        HumanTaskNode expected = (HumanTaskNode) eNode;
        HumanTaskNode current = (HumanTaskNode) cNode;
        assertEquals(expected.getSwimlane(), current.getSwimlane());
    };

    private static final BiConsumer<Node, Node> eventNodeAsserter = (eNode, cNode) -> {
        assertTrue(EventNode.class.isAssignableFrom(eNode.getClass()));
        assertTrue(EventNode.class.isAssignableFrom(cNode.getClass()));
        EventNode expected = (EventNode) eNode;
        EventNode current = (EventNode) cNode;
        assertEquals(expected.getScope(), current.getScope());
        assertEquals(expected.getType(), current.getType());
        assertEquals(expected.getVariableName(), current.getVariableName());
        assertEquals(expected.getEventFilters().size(), current.getEventFilters().size());
    };

    private static final BiConsumer<Node, Node> boundaryEventNodeAsserter = (eNode, cNode) -> {
        assertEquals(BoundaryEventNode.class, eNode.getClass());
        assertEquals(BoundaryEventNode.class, cNode.getClass());
        BoundaryEventNode expected = (BoundaryEventNode) eNode;
        BoundaryEventNode current = (BoundaryEventNode) cNode;
        assertEquals(expected.getAttachedToNodeId(), current.getAttachedToNodeId());
    };

    private static final BiConsumer<Node, Node> splitNodeAsserter = (eNode, cNode) -> {
        assertEquals(Split.class, eNode.getClass());
        assertEquals(Split.class, cNode.getClass());
        Split expected = (Split) eNode;
        Split current = (Split) cNode;
        assertEquals(expected.getType(), current.getType());
    };

    private static final BiConsumer<Node, Node> joinNodeAsserter = (eNode, cNode) -> {
        assertEquals(Join.class, eNode.getClass());
        assertEquals(Join.class, cNode.getClass());
        Join expected = (Join) eNode;
        Join current = (Join) cNode;
        assertEquals(expected.getType(), current.getType());
        assertEquals(expected.getN(), current.getN());
    };

    private static final BiConsumer<Node, Node> actionNodeAsserter = (eNode, cNode) -> {
        assertEquals(ActionNode.class, eNode.getClass());
        assertEquals(ActionNode.class, cNode.getClass());
        ActionNode expected = (ActionNode) eNode;
        ActionNode current = (ActionNode) cNode;
        if (expected.getAction() != null) {
            assertNotNull(current.getAction());
            assertEquals(expected.getAction().getName(), current.getAction().getName());
        }
    };

    private static final BiConsumer<Node, Node> milestoneNodeAsserter = (eNode, cNode) -> {
        assertEquals(MilestoneNode.class, eNode.getClass());
        assertEquals(MilestoneNode.class, cNode.getClass());
        MilestoneNode expected = (MilestoneNode) eNode;
        MilestoneNode current = (MilestoneNode) cNode;
        assertEquals(expected.getConstraint(), current.getConstraint());
        assertEquals(expected.getMatchVariable(), current.getMatchVariable());
    };

    private static final Map<Class<? extends Node>, BiConsumer<Node, Node>> nodeAsserters = new HashMap<>();

    static {
        nodeAsserters.put(NodeImpl.class, nodeAsserter);
        nodeAsserters.put(ExtendedNodeImpl.class, extendedNodeAsserter);
        nodeAsserters.put(StartNode.class, startNodeAsserter);
        nodeAsserters.put(EndNode.class, endNodeAsserter);
        nodeAsserters.put(Split.class, splitNodeAsserter);
        nodeAsserters.put(Join.class, joinNodeAsserter);
        nodeAsserters.put(StateBasedNode.class, stateBasedNodeAsserter);
        nodeAsserters.put(WorkItemNode.class, workItemNodeAsserter);
        nodeAsserters.put(HumanTaskNode.class, humanTaskNodeAsserter);
        nodeAsserters.put(EventNode.class, eventNodeAsserter);
        nodeAsserters.put(BoundaryEventNode.class, boundaryEventNodeAsserter);
        nodeAsserters.put(ActionNode.class, actionNodeAsserter);
        nodeAsserters.put(MilestoneNode.class, milestoneNodeAsserter);
    }

    private static void assertNode(Node expected, Node current) {
        nodeAsserters.keySet()
                .stream()
                .filter(clazz -> clazz.isAssignableFrom(expected.getClass()))
                .forEach(clazz -> nodeAsserters.get(clazz).accept(expected, current));
    }

    private static void assertMetadata(Map<String, Object> expected, Map<String, Object> current, Set<String> ignoredKeys) {
        if (expected == null) {
            assertNull(current);
            return;
        }
        assertNotNull(current);
        if(ignoredKeys == null) ignoredKeys = new HashSet<>();
        expected.keySet()
                .stream()
                .filter(Predicate.not(ignoredKeys::contains))
                .forEach(k -> assertEquals(expected.get(k), current.get(k)));
    }

    private static void assertConnections(Map<String, List<Connection>> expectedConnections, Map<String, List<Connection>> currentConnections) {
        assertEquals(expectedConnections.size(), currentConnections.size());
        expectedConnections.forEach((type, expectedByType) -> {
            assertTrue(currentConnections.containsKey(type), "Node does not have connections of type: " + type);
            List<Connection> currentByType = currentConnections.get(type);
            expectedByType.forEach(expected -> {
                Optional<Connection> current = currentByType
                        .stream()
                        .filter(c -> expected.getMetaData().get("UniqueId").equals(c.getMetaData().get("UniqueId"))).findFirst();
                assertTrue(current.isPresent());
                assertEquals(expected.getFromType(), current.get().getFromType());
                assertEquals(expected.getFrom().getId(), current.get().getFrom().getId());
                assertEquals(expected.getToType(), current.get().getToType());
                assertEquals(expected.getTo().getId(), current.get().getTo().getId());
            });
        });
    }

    private static void assertTriggers(List<Trigger> expected, List<Trigger> current) {
        if (expected == null) {
            assertNull(current);
            return;
        }
        assertNotNull(current);
        assertEquals(expected.size(), current.size());
        for (int i = 0; i < expected.size(); i++) {
            Trigger e = expected.get(i);
            Trigger c = current.get(i);
            e.getInMappings().forEach((k, v) -> assertEquals(v, c.getInMapping(k)));
            assertDataAssociations(e.getInAssociations(), c.getInAssociations());
        }
    }

    private static void assertDataAssociations(List<DataAssociation> expected, List<DataAssociation> current) {
        if (expected == null) {
            assertNull(current);
            return;
        }
        if (expected.isEmpty()) {
            assertEquals(1, current.size());
            assertEquals(1, current.get(0).getSources().size());
            assertEquals("", current.get(0).getSources().get(0));
        } else {
            assertEquals(expected.size(), current.size());
            for (int i = 0; i < expected.size(); i++) {
                assertEquals(expected.get(i).getSources(), current.get(i).getSources());
                assertEquals(expected.get(i).getTarget(), current.get(i).getTarget());
                assertEquals(expected.get(i).getTransformation(), current.get(i).getTransformation());
                assertAssignments(expected.get(i).getAssignments(), current.get(i).getAssignments());
            }
        }
    }

    private static void assertActions(List<DroolsAction> expected, List<DroolsAction> current) {
        assertEquals(expected.size(), current.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(((DroolsConsequenceAction) expected.get(i)).getDialect(), ((DroolsConsequenceAction) current.get(i)).getDialect());
            assertEquals(((DroolsConsequenceAction) expected.get(i)).getConsequence(), ((DroolsConsequenceAction) current.get(i)).getConsequence());
        }
    }

    private static void assertAssignments(List<Assignment> expected, List<Assignment> current) {
        if (expected == null) {
            assertNull(current);
            return;
        }
        assertEquals(expected.size(), current.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getFrom(), current.get(i).getFrom());
            assertEquals(expected.get(i).getDialect(), current.get(i).getDialect());
            assertEquals(expected.get(i).getTo(), current.get(i).getTo());
        }
    }

    private static void assertTimers(Map<Timer, DroolsAction> expected, Map<Timer, DroolsAction> current) {
        if(expected == null) {
            assertNull(current);
            return;
        }
        assertNotNull(current);
        assertEquals(expected.size(), current.size());
        expected.forEach((expectedTimer, expectedAction) -> {
            Optional<Timer> currentTimer = current.keySet().stream().filter(c -> c.getId() == expectedTimer.getId()).findFirst();
            assertTrue(currentTimer.isPresent());
            assertEquals(expectedTimer.getPeriod(), currentTimer.get().getPeriod());
            assertEquals(expectedTimer.getDate(), currentTimer.get().getDate());
            assertEquals(expectedTimer.getDelay(), currentTimer.get().getDelay());
            assertEquals(expectedTimer.getTimeType(), currentTimer.get().getTimeType());
            DroolsAction currentAction = current.get(currentTimer.get());
            if(expectedAction == null) {
                assertNull(currentAction);
                return;
            }
            assertNotNull(currentAction);
            assertEquals(expectedAction.getName(), currentAction.getName());
            //TODO: Is this expected? They are totally different objects. Expected DroolsConsequenceAction, Got lambda
            // assertEquals(expectedAction.getMetaData(DroolsAction.METADATA_ACTION), currentAction.getMetaData(DroolsAction.METADATA_ACTION));
        });
    }
}
