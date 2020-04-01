package org.jbpm.compiler.canonical;

import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.utils.StringEscapeUtils;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.factory.MilestoneNodeFactory;
import org.jbpm.workflow.core.node.MilestoneNode;
import org.kie.api.definition.process.Node;

public class MilestoneNodeVisitor extends AbstractNodeVisitor {

    private static final String NODE_NAME = "milestoneNode";

    @Override
    protected String getNodeKey() {
        return NODE_NAME;
    }

    @Override
    public void visitNode(String factoryField, Node node, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {
        MilestoneNode milestoneNode = (MilestoneNode) node;

        addFactoryMethodWithArgsWithAssignment(factoryField, body, MilestoneNodeFactory.class, getNodeId(node), NODE_NAME, new LongLiteralExpr(milestoneNode.getId()));
        addFactoryMethodWithArgs(body, getNodeId(node), "name", new StringLiteralExpr(getOrDefault(milestoneNode.getName(), "Milestone")));
        addFactoryMethodWithArgs(body, getNodeId(node), "constraint", new StringLiteralExpr(StringEscapeUtils.escapeJava(milestoneNode.getConstraint())));
        if(milestoneNode.getMatchVariable() != null) {
            addFactoryMethodWithArgs(body, getNodeId(node), "matchVariable", new StringLiteralExpr(milestoneNode.getMatchVariable()));
        }
        addFactoryDoneMethod(body, getNodeId(node));

        addActions(body, milestoneNode);
        visitMetaData(milestoneNode.getMetaData(), body, getNodeId(node));
    }
}
