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

import java.util.Map.Entry;

import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.factory.SubProcessNodeFactory;
import org.kie.api.definition.process.Node;
import org.jbpm.workflow.core.node.SubProcessNode;

import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

@Deprecated
public class SubProcessNodeVisitor extends AbstractNodeVisitor {
    
    private static final String NODE_NAME = "subProcessNode";

    @Override
    protected String getNodeKey() {
        return NODE_NAME;
    }
    
    @Override
    public void visitNode(String factoryField, Node node, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {
        SubProcessNode subProcessNode = (SubProcessNode) node;
        addFactoryMethodWithArgsWithAssignment(factoryField, body, SubProcessNodeFactory.class, getNodeId(node), "subProcessNode", new LongLiteralExpr(subProcessNode.getId()));
        addFactoryMethodWithArgs(body, getNodeId(node), "name", new StringLiteralExpr(getOrDefault(subProcessNode.getName(), "Call Activity")));
        addFactoryMethodWithArgs(body, getNodeId(node), "processId", new StringLiteralExpr(subProcessNode.getProcessId()));
        addFactoryMethodWithArgs(body, getNodeId(node), "processName", new StringLiteralExpr(getOrDefault(subProcessNode.getProcessName(), "")));
        addFactoryMethodWithArgs(body, getNodeId(node), "waitForCompletion", new BooleanLiteralExpr(subProcessNode.isWaitForCompletion()));
        addFactoryMethodWithArgs(body, getNodeId(node), "independent", new BooleanLiteralExpr(subProcessNode.isIndependent()));

        for (Entry<String, String> entry : subProcessNode.getInMappings().entrySet()) {
            addFactoryMethodWithArgs(body, getNodeId(node), "inMapping", new StringLiteralExpr(entry.getKey()), new StringLiteralExpr(entry.getValue()));
        }
        for (Entry<String, String> entry : subProcessNode.getOutMappings().entrySet()) {
            addFactoryMethodWithArgs(body, getNodeId(node), "outMapping", new StringLiteralExpr(entry.getKey()), new StringLiteralExpr(entry.getValue()));
        }
        
        visitMetaData(subProcessNode.getMetaData(), body, getNodeId(node));

        addFactoryDoneMethod(body, getNodeId(node));
    }
}
