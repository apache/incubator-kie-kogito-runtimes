/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.compiler.canonical;

import java.util.Map;

import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.factory.ForEachNodeFactory;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.DataDefinition;
import org.jbpm.workflow.core.node.ForEachNode;

import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

import static org.jbpm.ruleflow.core.factory.CompositeContextNodeFactory.METHOD_LINK_INCOMING_CONNECTIONS;
import static org.jbpm.ruleflow.core.factory.CompositeContextNodeFactory.METHOD_LINK_OUTGOING_CONNECTIONS;
import static org.jbpm.ruleflow.core.factory.ForEachNodeFactory.METHOD_COLLECTION_EXPRESSION;
import static org.jbpm.ruleflow.core.factory.ForEachNodeFactory.METHOD_INPUT_VARIABLE;
import static org.jbpm.ruleflow.core.factory.ForEachNodeFactory.METHOD_OUTPUT_COLLECTION_EXPRESSION;
import static org.jbpm.ruleflow.core.factory.ForEachNodeFactory.METHOD_OUTPUT_VARIABLE;

public class ForEachNodeVisitor extends AbstractCompositeNodeVisitor<ForEachNode> {

    public ForEachNodeVisitor(Map<Class<?>, AbstractNodeVisitor<? extends org.kie.api.definition.process.Node>> nodesVisitors) {
        super(nodesVisitors);
    }

    @Override
    protected String getNodeKey() {
        return "forEachNode";
    }

    @Override
    public void visitNode(String factoryField, ForEachNode node, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {
        body.addStatement(getAssignedFactoryMethod(factoryField, ForEachNodeFactory.class, getNodeId(node), getNodeKey(), new LongLiteralExpr(node.getId())))
                .addStatement(getNameMethod(node, "ForEach"));
        visitMetaData(node.getMetaData(), body, getNodeId(node));

        body.addStatement(getFactoryMethod(getNodeId(node), METHOD_INPUT_VARIABLE,
                new StringLiteralExpr("foreach_output"),
                buildDataResolver("java.util.Collection")));

        DataDefinition output = node.getMultiInstanceSpecification().getOutputDataItem();

        if (output != null) {
            body.addStatement(getFactoryMethod(getNodeId(node), METHOD_OUTPUT_VARIABLE,
                    new StringLiteralExpr(output.getId()),
                    new StringLiteralExpr(output.getLabel()),
                    buildDataResolver(output.getType())));
        }

        DataDefinition input = node.getMultiInstanceSpecification().getInputDataItem();
        if (input != null) {
            body.addStatement(getFactoryMethod(getNodeId(node), METHOD_INPUT_VARIABLE,
                    new StringLiteralExpr(input.getId()),
                    new StringLiteralExpr(input.getLabel()),
                    buildDataResolver(input.getType())));
        }

        body.addStatement(getFactoryMethod(getNodeId(node), METHOD_COLLECTION_EXPRESSION, new StringLiteralExpr(stripExpression(node.getCollectionExpression()))));

        if (node.getOutputCollectionExpression() != null) {
            body.addStatement(getFactoryMethod(getNodeId(node), METHOD_OUTPUT_COLLECTION_EXPRESSION, new StringLiteralExpr(stripExpression(node.getOutputCollectionExpression()))));
        }
        // visit nodes
        visitNodes(getNodeId(node), node.getNodes(), body, ((VariableScope) node.getCompositeNode().getDefaultContext(VariableScope.VARIABLE_SCOPE)), metadata);
        body.addStatement(getFactoryMethod(getNodeId(node), METHOD_LINK_INCOMING_CONNECTIONS, new LongLiteralExpr(node.getLinkedIncomingNode(Node.CONNECTION_DEFAULT_TYPE).getNodeId())))
                .addStatement(getFactoryMethod(getNodeId(node), METHOD_LINK_OUTGOING_CONNECTIONS, new LongLiteralExpr(node.getLinkedOutgoingNode(Node.CONNECTION_DEFAULT_TYPE).getNodeId())))
                .addStatement(getDoneMethod(getNodeId(node)));

    }

}
