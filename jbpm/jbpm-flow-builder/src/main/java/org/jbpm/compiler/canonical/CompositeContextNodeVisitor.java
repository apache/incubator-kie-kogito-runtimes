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

import java.util.HashSet;
import java.util.Map;

import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.factory.CompositeNodeFactory;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.kie.api.definition.process.Node;

public class CompositeContextNodeVisitor extends AbstractCompositeNodeVisitor {

    private static final String NODE_KEY = "compositeContextNode";
    private static final String FACTORY_METHOD_NAME = "compositeNode";

    public CompositeContextNodeVisitor(Map<Class<?>, AbstractNodeVisitor> nodesVisitors) {
        super(nodesVisitors);
    }

    protected String getNodeKey() {
        return NODE_KEY;
    }

    protected Class<? extends CompositeNodeFactory> factoryClass() {
        return CompositeNodeFactory.class;
    }

    protected String factoryMethod() {
        return FACTORY_METHOD_NAME;
    }

    @Override
    public void visitNode(String factoryField, Node node, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {
        CompositeContextNode compositeContextNode = (CompositeContextNode) node;

        addFactoryMethodWithArgsWithAssignment(factoryField, body, factoryClass(), getNodeId(node), factoryMethod(), new LongLiteralExpr(compositeContextNode.getId()));
        addFactoryMethodWithArgs(body, getNodeId(node), "name", new StringLiteralExpr(getOrDefault(node.getName(), "CompositeContext")));

        addActions(body, compositeContextNode);
        visitMetaData(compositeContextNode.getMetaData(), body, getNodeId(node));
        VariableScope variableScopeNode = (VariableScope) compositeContextNode.getDefaultContext(VariableScope.VARIABLE_SCOPE);

        if (variableScope != null) {
            visitVariableScope(getNodeId(node), variableScopeNode, body, new HashSet<>());
        }
        visitExtendedFields(body, compositeContextNode);

        // visit nodes
        visitNodes(getNodeId(node), compositeContextNode.getNodes(), body, ((VariableScope) compositeContextNode.getDefaultContext(VariableScope.VARIABLE_SCOPE)), metadata);
        visitConnections(getNodeId(node), compositeContextNode.getNodes(), body);
        addFactoryDoneMethod(body, getNodeId(node));
    }

    protected void visitExtendedFields(BlockStmt body, CompositeContextNode node) {
        //TODO
    }
}
