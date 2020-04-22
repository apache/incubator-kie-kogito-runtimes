/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.utils.StringEscapeUtils;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.factory.MilestoneNodeFactory;
import org.jbpm.workflow.core.node.MilestoneNode;
import org.kie.api.definition.process.Node;

import static org.jbpm.ruleflow.core.factory.MilestoneNodeFactory.METHOD_CONSTRAINT;
import static org.jbpm.ruleflow.core.factory.MilestoneNodeFactory.METHOD_MATCH_VARIABLE;

public class MilestoneNodeVisitor extends AbstractNodeVisitor {

    private static final String NODE_KEY = "milestoneNode";

    @Override
    protected String getNodeKey() {
        return NODE_KEY;
    }

    @Override
    public void visitNode(String factoryField, Node node, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {
        MilestoneNode milestoneNode = (MilestoneNode) node;
        body.addStatement(getAssignedFactoryMethod(factoryField, MilestoneNodeFactory.class, getNodeId(node), NODE_KEY,new LongLiteralExpr(milestoneNode.getId())))
                .addStatement(getNameMethod(node, "Milestone"))
                .addStatement(getFactoryMethod(getNodeId(node), METHOD_CONSTRAINT, new StringLiteralExpr(StringEscapeUtils.escapeJava(milestoneNode.getConstraint()))));
        if (milestoneNode.getMatchVariable() != null) {
            body.addStatement(getFactoryMethod(getNodeId(node), METHOD_MATCH_VARIABLE, new StringLiteralExpr(milestoneNode.getMatchVariable())));
        }
        body.addStatement(getDoneMethod(getNodeId(node)));
        visitMetaData(milestoneNode.getMetaData(), body, getNodeId(node));
    }

}
