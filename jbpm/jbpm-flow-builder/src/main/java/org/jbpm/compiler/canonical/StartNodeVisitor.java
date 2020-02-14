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

import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.factory.EventNodeFactory;
import org.jbpm.ruleflow.core.factory.StartNodeFactory;
import org.jbpm.workflow.core.node.StartNode;
import org.kie.api.definition.process.Node;

import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

public class StartNodeVisitor extends AbstractVisitor {

    @Override
    public void visitNode(String factoryField, Node node, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {
        StartNode startNode = (StartNode) node;

        if ("Signal".equals(startNode.getMetaData("TriggerType"))) {
            Map<String, Object> nodeMetaData = startNode.getMetaData();
            String variableName = "eventNode" + node.getId();
            String signalName = (String) nodeMetaData.get("MessageType");
            String triggerMapping = (String) nodeMetaData.get("TriggerMapping");

            addFactoryMethodWithArgsWithAssignment(factoryField, body, EventNodeFactory.class, variableName, "eventNode", new LongLiteralExpr(startNode.getId()));

            if (triggerMapping != null) {
                metadata.getSignals().put(signalName, variableScope.findVariable(triggerMapping).getType().getStringType());
                addFactoryMethodWithArgs(body, variableName, "variableName", new StringLiteralExpr(triggerMapping));
            } else {
                metadata.getSignals().put(signalName, null);
            }

            addFactoryMethodWithArgs(body, variableName, "name", new StringLiteralExpr(getOrDefault(startNode.getName(), "Event")));
            addFactoryMethodWithArgs(body, variableName, "eventType", new StringLiteralExpr(signalName));

            Map<String, Object> newMetaData = new HashMap<>(nodeMetaData);
            newMetaData.put("EventType", "signal");
            visitMetaData(newMetaData, body, variableName);
            addFactoryMethodWithArgs(body, variableName, "done");
        } else {
            addFactoryMethodWithArgsWithAssignment(factoryField, body, StartNodeFactory.class, "startNode" + node.getId(), "startNode", new LongLiteralExpr(startNode.getId()));
            addFactoryMethodWithArgs(body, "startNode" + node.getId(), "name", new StringLiteralExpr(getOrDefault(startNode.getName(), "Start")));

            visitMetaData(startNode.getMetaData(), body, "startNode" + node.getId());

            addFactoryMethodWithArgs(body, "startNode" + node.getId(), "done");

            if (startNode.getTriggers() != null && !startNode.getTriggers().isEmpty()) {
                Map<String, Object> nodeMetaData = startNode.getMetaData();
                String name = (String) nodeMetaData.get("TriggerRef");
                String type = (String) nodeMetaData.get("TriggerType");
                String messageType = (String) nodeMetaData.get("MessageType");
                String triggerMapping = (String) nodeMetaData.get("TriggerMapping");

                metadata.getTriggers().add(new TriggerMetaData(name,
                                                               type,
                                                               messageType,
                                                               triggerMapping,
                                                               String.valueOf(node.getId())).validate());
                addFactoryMethodWithArgs(body, "startNode" + node.getId(), "trigger", new StringLiteralExpr(name),
                                         new StringLiteralExpr(getOrDefault(triggerMapping, "")));
            } else {
                // since there is start node without trigger then make sure it is startable
                metadata.setStartable(true);
            }
        }

    }
}
