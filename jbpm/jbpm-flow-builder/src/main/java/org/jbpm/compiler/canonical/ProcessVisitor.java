package org.jbpm.compiler.canonical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.Work;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.BoundaryEventNode;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.DynamicNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.EventNode;
import org.jbpm.workflow.core.node.EventSubProcessNode;
import org.jbpm.workflow.core.node.FaultNode;
import org.jbpm.workflow.core.node.ForEachNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.MilestoneNode;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.jbpm.workflow.core.node.TimerNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.NodeContainer;
import org.kie.api.definition.process.WorkflowProcess;

public class ProcessVisitor extends AbstractVisitor {

    public static final String DEFAULT_VERSION = "1.0";
    public static final String METHOD_NAME = "name";
    public static final String METHOD_PACKAGE_NAME = "packageName";
    public static final String METHOD_DYNAMIC = "dynamic";
    public static final String METHOD_VERSION = "version";
    public static final String METHOD_VISIBILITY = "visibility";
    public static final String METHOD_VALIDATE = "validate";
    private Map<Class<?>, AbstractNodeVisitor> nodesVisitors = new HashMap<>();

    public ProcessVisitor(ClassLoader contextClassLoader) {
        this.nodesVisitors.put(StartNode.class, new StartNodeVisitor());
        this.nodesVisitors.put(ActionNode.class, new ActionNodeVisitor());
        this.nodesVisitors.put(EndNode.class, new EndNodeVisitor());
        this.nodesVisitors.put(HumanTaskNode.class, new HumanTaskNodeVisitor());
        this.nodesVisitors.put(WorkItemNode.class, new WorkItemNodeVisitor(contextClassLoader));
        this.nodesVisitors.put(SubProcessNode.class, new LambdaSubProcessNodeVisitor());
        this.nodesVisitors.put(Split.class, new SplitNodeVisitor());
        this.nodesVisitors.put(Join.class, new JoinNodeVisitor());
        this.nodesVisitors.put(FaultNode.class, new FaultNodeVisitor());
        this.nodesVisitors.put(RuleSetNode.class, new RuleSetNodeVisitor(contextClassLoader));
        this.nodesVisitors.put(BoundaryEventNode.class, new BoundaryEventNodeVisitor());
        this.nodesVisitors.put(EventNode.class, new EventNodeVisitor());
        this.nodesVisitors.put(ForEachNode.class, new ForEachNodeVisitor(nodesVisitors));
        this.nodesVisitors.put(CompositeContextNode.class, new CompositeContextNodeVisitor(nodesVisitors));
        this.nodesVisitors.put(EventSubProcessNode.class, new EventSubprocessNodeVisitor(nodesVisitors));
        this.nodesVisitors.put(TimerNode.class, new TimerNodeVisitor());
        this.nodesVisitors.put(MilestoneNode.class, new MilestoneNodeVisitor());
        this.nodesVisitors.put(DynamicNode.class, new DynamicNodeVisitor(nodesVisitors));
    }
    public void visitProcess(WorkflowProcess process, MethodDeclaration processMethod, ProcessMetaData metadata) {
        BlockStmt body = new BlockStmt();

        ClassOrInterfaceType processFactoryType = new ClassOrInterfaceType(null, RuleFlowProcessFactory.class.getSimpleName());

        // create local variable factory and assign new fluent process to it
        VariableDeclarationExpr factoryField = new VariableDeclarationExpr(processFactoryType, FACTORY_FIELD_NAME);
        MethodCallExpr assignFactoryMethod = new MethodCallExpr(new NameExpr(processFactoryType.getName().asString()), "createProcess");
        assignFactoryMethod.addArgument(new StringLiteralExpr(process.getId()));
        body.addStatement(new AssignExpr(factoryField, assignFactoryMethod, AssignExpr.Operator.ASSIGN));

        // item definitions
        Set<String> visitedVariables = new HashSet<>();
        VariableScope variableScope = (VariableScope) ((org.jbpm.process.core.Process) process).getDefaultContext(VariableScope.VARIABLE_SCOPE);

        visitVariableScope(variableScope, body, visitedVariables);
        visitSubVariableScopes(process.getNodes(), body, visitedVariables);

        visitInterfaces(process.getNodes(), body);

        // the process itself
        addFactoryMethodWithArgs(FACTORY_FIELD_NAME, body, METHOD_NAME, new StringLiteralExpr(process.getName()));
        addFactoryMethodWithArgs(FACTORY_FIELD_NAME, body, METHOD_PACKAGE_NAME, new StringLiteralExpr(process.getPackageName()));
        addFactoryMethodWithArgs(FACTORY_FIELD_NAME, body, METHOD_DYNAMIC, new BooleanLiteralExpr(((org.jbpm.workflow.core.WorkflowProcess) process).isDynamic()));
        addFactoryMethodWithArgs(FACTORY_FIELD_NAME, body, METHOD_VERSION, new StringLiteralExpr(getOrDefault(process.getVersion(), DEFAULT_VERSION)));
        addFactoryMethodWithArgs(FACTORY_FIELD_NAME, body, METHOD_VISIBILITY, new StringLiteralExpr(getOrDefault(process.getVisibility(), WorkflowProcess.PUBLIC_VISIBILITY)));

        visitMetaData(process.getMetaData(), body, FACTORY_FIELD_NAME);

        visitHeader(process, body);

        List<Node> processNodes = new ArrayList<>();
        for (org.kie.api.definition.process.Node procNode : process.getNodes()) {
            processNodes.add((org.jbpm.workflow.core.Node) procNode);
        }
        visitNodes(processNodes, body, variableScope, metadata);
        visitConnections(process.getNodes(), body);

        addFactoryMethodWithArgs(FACTORY_FIELD_NAME, body, METHOD_VALIDATE);

        MethodCallExpr getProcessMethod = new MethodCallExpr(new NameExpr(FACTORY_FIELD_NAME), "getProcess");
        body.addStatement(new ReturnStmt(getProcessMethod));
        processMethod.setBody(body);
    }

    private void visitVariableScope(VariableScope variableScope, BlockStmt body, Set<String> visitedVariables) {
        if (variableScope != null && !variableScope.getVariables().isEmpty()) {
            for (Variable variable : variableScope.getVariables()) {

                if (!visitedVariables.add(variable.getName())) {
                    continue;
                }
                String tags = (String) variable.getMetaData(Variable.VARIABLE_TAGS);
                ClassOrInterfaceType variableType = new ClassOrInterfaceType(null, ObjectDataType.class.getSimpleName());
                ObjectCreationExpr variableValue = new ObjectCreationExpr(null, variableType, new NodeList<>(new StringLiteralExpr(variable.getType().getStringType())));
                addFactoryMethodWithArgs(FACTORY_FIELD_NAME, body, "variable", new StringLiteralExpr(variable.getName()), variableValue, new StringLiteralExpr(Variable.VARIABLE_TAGS), tags != null ? new StringLiteralExpr(tags) : new NullLiteralExpr());
            }
        }
    }

    private void visitSubVariableScopes(org.kie.api.definition.process.Node[] nodes, BlockStmt body, Set<String> visitedVariables) {
        for (org.kie.api.definition.process.Node node : nodes) {
            if (node instanceof ContextContainer) {
                VariableScope variableScope = (VariableScope)
                    ((ContextContainer) node).getDefaultContext(VariableScope.VARIABLE_SCOPE);
                if (variableScope != null) {
                    visitVariableScope(variableScope, body, visitedVariables);
                }
            }
            if (node instanceof NodeContainer) {
                visitSubVariableScopes(((NodeContainer) node).getNodes(), body, visitedVariables);
            }
        }
    }

    private void visitHeader(WorkflowProcess process, BlockStmt body) {
        Map<String, Object> metaData = getMetaData(process.getMetaData());
        Set<String> imports = ((org.jbpm.process.core.Process) process).getImports();
        Map<String, String> globals = ((org.jbpm.process.core.Process) process).getGlobals();
        if ((imports != null && !imports.isEmpty()) || (globals != null && globals.size() > 0) || !metaData.isEmpty()) {
            if (imports != null) {
                for (String s : imports) {
                    addFactoryMethodWithArgs(FACTORY_FIELD_NAME, body, "imports", new StringLiteralExpr(s));
                }
            }
            if (globals != null) {
                for (Map.Entry<String, String> global : globals.entrySet()) {
                    addFactoryMethodWithArgs(FACTORY_FIELD_NAME, body, "global", new StringLiteralExpr(global.getKey()), new StringLiteralExpr(global.getValue()));
                }
            }
        }
    }

    private Map<String, Object> getMetaData(Map<String, Object> input) {
        Map<String, Object> metaData = new HashMap<>();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String name = entry.getKey();
            if (entry.getKey().startsWith("custom")
                && entry.getValue() instanceof String) {
                metaData.put(name, entry.getValue());
            }
        }
        return metaData;
    }

    private void visitNodes(List<org.jbpm.workflow.core.Node> nodes, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {

        for (org.kie.api.definition.process.Node node : nodes) {
            AbstractNodeVisitor visitor = nodesVisitors.get(node.getClass());

            if (visitor == null) {
                throw new IllegalStateException("No visitor found for node " + node.getClass().getName());
            }

            visitor.visitNode(node, body, variableScope, metadata);
        }
    }

    private void visitConnections(org.kie.api.definition.process.Node[] nodes, BlockStmt body) {

        List<Connection> connections = new ArrayList<>();
        for (org.kie.api.definition.process.Node node : nodes) {
            for (List<Connection> connectionList : node.getIncomingConnections().values()) {
                connections.addAll(connectionList);
            }
        }
        for (Connection connection : connections) {
            visitConnection(connection, body);
        }
    }

    private void visitInterfaces(org.kie.api.definition.process.Node[] nodes, BlockStmt body) {
        for (org.kie.api.definition.process.Node node : nodes) {
            if (node instanceof WorkItemNode) {
                Work work = ((WorkItemNode) node).getWork();
                if (work != null) {
                    // TODO - finish this method
                }
            }
        }
    }

    private void visitConnection(Connection connection, BlockStmt body) {
        // if the connection was generated by a link event, don't dump.
        if (isConnectionRepresentingLinkEvent(connection)) {
            return;
        }
        // if the connection is a hidden one (compensations), don't dump
        Object hidden = ((ConnectionImpl) connection).getMetaData("hidden");
        if (hidden != null && ((Boolean) hidden)) {
            return;
        }

        addFactoryMethodWithArgs(FACTORY_FIELD_NAME, body, "connection", new LongLiteralExpr(connection.getFrom().getId()),
                                 new LongLiteralExpr(connection.getTo().getId()),
                                 new StringLiteralExpr(getOrDefault((String) connection.getMetaData().get("UniqueId"), "")));
    }

    private boolean isConnectionRepresentingLinkEvent(Connection connection) {
        return connection.getMetaData().get("linkNodeHidden") != null;
    }

}
