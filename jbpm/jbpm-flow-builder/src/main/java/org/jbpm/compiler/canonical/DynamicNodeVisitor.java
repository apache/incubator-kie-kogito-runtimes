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

package org.jbpm.compiler.canonical;

import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.utils.StringEscapeUtils;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.factory.DynamicNodeFactory;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.DynamicNode;
import org.kie.api.definition.process.Node;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

public class DynamicNodeVisitor extends AbstractCompositeNodeVisitor {

    private static final String NODE_KEY = "dynamicNode";

    public DynamicNodeVisitor(Map<Class<?>, AbstractNodeVisitor> nodesVisitors) {
        super(nodesVisitors);
    }

    protected String getNodeKey() {
        return NODE_KEY;
    }

    @Override
    public void visitNode(String factoryField, Node node, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {
        DynamicNode dynamicNode = (DynamicNode) node;

        addFactoryMethodWithArgsWithAssignment(factoryField, body, DynamicNodeFactory.class, getNodeId(node), NODE_KEY, new LongLiteralExpr(dynamicNode.getId()));
        addFactoryMethodWithArgs(body, getNodeId(node), "name", new StringLiteralExpr(getOrDefault(node.getName(), "Dynamic")));

        addActions(body, dynamicNode);

        visitMetaData(dynamicNode.getMetaData(), body, getNodeId(node));
        VariableScope variableScopeNode = (VariableScope) dynamicNode.getDefaultContext(VariableScope.VARIABLE_SCOPE);

        if (variableScope != null) {
            visitVariableScope(getNodeId(node), variableScopeNode, body, new HashSet<>());
        }
        visitExtendedFields(body, dynamicNode);

        // visit nodes
        visitNodes(getNodeId(node), dynamicNode.getNodes(), body, ((VariableScope) dynamicNode.getDefaultContext(VariableScope.VARIABLE_SCOPE)), metadata);
        visitConnections(getNodeId(node), dynamicNode.getNodes(), body);
        addFactoryDoneMethod(body, getNodeId(node));
    }

    protected void visitExtendedFields(BlockStmt body, CompositeContextNode node) {
    }
}
