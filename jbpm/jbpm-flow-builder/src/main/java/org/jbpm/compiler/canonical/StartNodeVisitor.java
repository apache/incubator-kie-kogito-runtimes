/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.compiler.canonical;

import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.ruleflow.core.factory.StartNodeFactory;
import org.jbpm.workflow.core.node.StartNode;
import org.kie.api.definition.process.Node;

import java.util.Map;
import java.util.Map.Entry;

public class StartNodeVisitor extends AbstractNodeVisitor {

    private static final String NODE_KEY = "startNode";

    private static final String TRIGGER_REF = "TriggerRef";
    private static final String MESSAGE_TYPE = "MessageType";
    private static final String TRIGGER_TYPE = "TriggerType";
    private static final String TRIGGER_MAPPING = "TriggerMapping";

    @Override
    public String getNodeKey() {
        return NODE_KEY;
    }

    @Override
    public void visitNode(String factoryField, Node node, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {
        StartNode startNode = (StartNode) node;

        addFactoryMethodWithArgsWithAssignment(factoryField, body, StartNodeFactory.class, getNodeId(node), "startNode", new LongLiteralExpr(startNode.getId()));
        addFactoryMethodWithArgs(body, getNodeId(node), "name", new StringLiteralExpr(getOrDefault(startNode.getName(), "Start")));

        addFactoryMethodWithArgs(body, getNodeId(node), "interrupting", new BooleanLiteralExpr(startNode.isInterrupting()));

        visitMetaData(startNode.getMetaData(), body, getNodeId(node));

        addFactoryDoneMethod(body, getNodeId(node));
        
        startNode.getOutMappings().forEach((k, v) -> addFactoryMethodWithArgs(body, getNodeId(node), "addOutMapping", new StringLiteralExpr(k), new StringLiteralExpr(v)));

        if (startNode.getTimer() != null) {
            Timer timer = startNode.getTimer();
            addFactoryMethodWithArgs(body, "startNode" + node.getId(), "timer", getOrNullExpr(timer.getDelay()),
                    getOrNullExpr(timer.getPeriod()),
                    getOrNullExpr(timer.getDate()),
                    new IntegerLiteralExpr(startNode.getTimer().getTimeType()));

        } else if (startNode.getTriggers() != null && !startNode.getTriggers().isEmpty()) {
            Map<String, Object> nodeMetaData = startNode.getMetaData();
            metadata.getTriggers().add(new TriggerMetaData((String) nodeMetaData.get(TRIGGER_REF),
                    (String) nodeMetaData.get(TRIGGER_TYPE),
                    (String) nodeMetaData.get(MESSAGE_TYPE),
                    (String) nodeMetaData.get(TRIGGER_MAPPING),
                    String.valueOf(node.getId())).validate());

            handleSignal(startNode, nodeMetaData, body, variableScope, metadata);
            if (nodeMetaData.get(TRIGGER_MAPPING) == null) {
                addFactoryMethodWithArgs(body, getNodeId(node), "trigger", new StringLiteralExpr((String) nodeMetaData.get(TRIGGER_REF)),
                        new StringLiteralExpr(""));
            }
        } else {
            // since there is start node without trigger then make sure it is startable
            metadata.setStartable(true);
        }

    }

    protected void handleSignal(StartNode startNode, Map<String, Object> nodeMetaData, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {
        if ("signal".equalsIgnoreCase((String) startNode.getMetaData(TRIGGER_TYPE))) {
            Variable variable = null;
            Map<String, String> variableMapping = startNode.getOutMappings();
            if (variableMapping != null && !variableMapping.isEmpty()) {
                Entry<String, String> varInfo = variableMapping.entrySet().iterator().next();

                addFactoryMethodWithArgs(body, getNodeId(startNode), "trigger", new StringLiteralExpr((String) nodeMetaData.get(MESSAGE_TYPE)), new StringLiteralExpr(varInfo.getKey()));
                variable = variableScope.findVariable(varInfo.getKey());

                if (variable == null) {
                    // check parent node container
                    VariableScope vscope = (VariableScope) startNode.resolveContext(VariableScope.VARIABLE_SCOPE, varInfo.getKey());
                    variable = vscope.findVariable(varInfo.getKey());
                }


            }

            metadata.getSignals().put((String) nodeMetaData.get(MESSAGE_TYPE), variable != null ? variable.getType().getStringType() : null);
        }
    }
}
