package org.jbpm.compiler.canonical;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.drools.core.util.StringUtils;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.internal.ruleunit.RuleUnitVariable;

public class RuleUnitDescriptionCodeHelper {

    private final RuleUnitDescription ruleUnitDescription;
    private final String modelClassName;

    private final String instanceVarName;

    public RuleUnitDescriptionCodeHelper(RuleUnitDescription ruleUnitDescription, String instanceVarName) {
        this.ruleUnitDescription = ruleUnitDescription;
        this.modelClassName = ruleUnitDescription.getCanonicalName();
        this.instanceVarName = instanceVarName;
    }

    public String instanceVarName() {
        return instanceVarName;
    }

    public AssignExpr newInstance() {
        ClassOrInterfaceType type = new ClassOrInterfaceType(null, modelClassName);
        return new AssignExpr(
                new VariableDeclarationExpr(type, instanceVarName),
                new ObjectCreationExpr().setType(type),
                AssignExpr.Operator.ASSIGN);
    }

    public NodeList<Statement> hoistVars() {
        NodeList<Statement> statements = new NodeList<>();
        for (RuleUnitVariable v : ruleUnitDescription.getUnitVarDeclarations()) {
            statements.add(new ExpressionStmt(assignVar(v)));
        }
        return statements;
    }

    public MethodCallExpr get(String unitVar) {
        RuleUnitVariable v = ruleUnitDescription.getVar(unitVar);
        return get(v);
    }

    private MethodCallExpr get(RuleUnitVariable v) {
        String getter = v.getter();
        return new MethodCallExpr(new NameExpr(instanceVarName), getter);
    }

    private MethodCallExpr set(RuleUnitVariable targetUnitVar, Expression sourceExpr) {
        String setter = targetUnitVar.setter();
        return new MethodCallExpr(new NameExpr(instanceVarName), setter)
                .addArgument(sourceExpr);
    }

    public AssignExpr assignVar(RuleUnitVariable v) {
        ClassOrInterfaceType type = new ClassOrInterfaceType(null, v.getType().getCanonicalName());
        return new AssignExpr(
                new VariableDeclarationExpr(type, localVarName(v)),
                get(v),
                AssignExpr.Operator.ASSIGN);
    }

    private String localVarName(RuleUnitVariable v) {
        return String.format("%s_%s", instanceVarName, v.getName());
    }

    public Statement injectCollection(
            String targetUnitVar, String collectionTypeVar, Expression sourceExpression) {
        BlockStmt blockStmt = new BlockStmt();
        RuleUnitVariable v = ruleUnitDescription.getVar(targetUnitVar);
        if (v.isDataSource()) {
            blockStmt.addStatement(assignVar(v));
            blockStmt.addStatement(
                    iterate(new VariableDeclarator()
                                    .setType(collectionTypeVar).setName("it"),
                            sourceExpression)
                            .setBody(new ExpressionStmt(
                                    new MethodCallExpr()
                                            .setScope(new NameExpr(localVarName(v)))
                                            .setName("add")
                                            .addArgument(new NameExpr("it")))));
        } else {
            blockStmt.addStatement(set(v, sourceExpression));
        }
        return blockStmt;
    }

    private ForEachStmt iterate(VariableDeclarator iterVar, Expression sourceExpression) {
        return new ForEachStmt()
                .setVariable(new VariableDeclarationExpr(iterVar))
                .setIterable(sourceExpression);
    }

    public Statement injectScalar(String targetUnitVar, Expression sourceExpression) {
        BlockStmt blockStmt = new BlockStmt();
        RuleUnitVariable v = ruleUnitDescription.getVar(targetUnitVar);
        if (v.isDataSource()) {
            blockStmt.addStatement(assignVar(v));
            blockStmt.addStatement(
                    new MethodCallExpr()
                            .setScope(new NameExpr(localVarName(v)))
                            .setName("add")
                            .addArgument(sourceExpression));
        } else {
            blockStmt.addStatement(set(v, sourceExpression));
        }
        return blockStmt;
    }
}
