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
package org.jbpm.compiler.canonical;

import java.text.MessageFormat;

import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.factory.EventNodeFactory;
import org.jbpm.workflow.core.node.EventNode;

import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

import static org.jbpm.ruleflow.core.Metadata.EVENT_TYPE;
import static org.jbpm.ruleflow.core.Metadata.EVENT_TYPE_MESSAGE;
import static org.jbpm.ruleflow.core.Metadata.EVENT_TYPE_SIGNAL;
import static org.jbpm.ruleflow.core.factory.EventNodeFactory.METHOD_EVENT_TYPE;
import static org.jbpm.ruleflow.core.factory.EventNodeFactory.METHOD_INPUT_VARIABLE_NAME;
import static org.jbpm.ruleflow.core.factory.EventNodeFactory.METHOD_VARIABLE_NAME;

public class EventNodeVisitor extends AbstractNodeVisitor<EventNode> {

    public EventNodeVisitor(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    protected String getNodeKey() {
        return "eventNode";
    }

    @Override
    public void visitNode(String factoryField, EventNode node, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {
        body.addStatement(getAssignedFactoryMethod(factoryField, EventNodeFactory.class, getNodeId(node), getNodeKey(), getWorkflowElementConstructor(node.getId())));
        visitMetaData(node.getMetaData(), body, getNodeId(node));
        body.addStatement(getNameMethod(node, "Event"))
                .addStatement(getFactoryMethod(getNodeId(node), METHOD_EVENT_TYPE, new StringLiteralExpr(node.getType())));

        Variable variable = null;
        if (node.getVariableName() != null) {
            body.addStatement(getFactoryMethod(getNodeId(node), METHOD_VARIABLE_NAME, new StringLiteralExpr(node.getVariableName())));
            variable = variableScope.findVariable(node.getVariableName());
        }
        if (node.getInputVariableName() != null) {
            body.addStatement(getFactoryMethod(getNodeId(node), METHOD_INPUT_VARIABLE_NAME, new StringLiteralExpr(node.getInputVariableName())));
        }

        if (EVENT_TYPE_SIGNAL.equals(node.getMetaData(EVENT_TYPE))) {
            metadata.addSignal(node.getType(), variable != null ? variable.getType().getStringType() : null);
        } else if (EVENT_TYPE_MESSAGE.equals(node.getMetaData(EVENT_TYPE))) {
            try {
                metadata.addTrigger(TriggerMetaData.of(node, node.getVariableName()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        MessageFormat.format(
                                "Invalid parameters for event node \"{0}\": {1}",
                                node.getName(),
                                e.getMessage()),
                        e);
            }
        }
        addNodeMappings(node, body, getNodeId(node));

        body.addStatement(getDoneMethod(getNodeId(node)));
    }

}
