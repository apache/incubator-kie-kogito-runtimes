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
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.drools.core.util.StringUtils;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.internal.ruleunit.RuleUnitVariable;
import org.kie.kogito.rules.DataObserver;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.DataStream;

import static com.github.javaparser.StaticJavaParser.parseExpression;
import static com.github.javaparser.StaticJavaParser.parseStatement;

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
            String appendMethod = appendMethodOf(v.getType());
            blockStmt.addStatement(assignVar(v));
            blockStmt.addStatement(
                    iterate(new VariableDeclarator()
                                    .setType(collectionTypeVar).setName("it"),
                            sourceExpression)
                            .setBody(new ExpressionStmt(
                                    new MethodCallExpr()
                                            .setScope(new NameExpr(localVarName(v)))
                                            .setName(appendMethod)
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
            String appendMethod = appendMethodOf(v.getType());
            blockStmt.addStatement(assignVar(v));
            blockStmt.addStatement(
                    new MethodCallExpr()
                            .setScope(new NameExpr(localVarName(v)))
                            .setName(appendMethod)
                            .addArgument(sourceExpression));
        } else {
            blockStmt.addStatement(set(v, sourceExpression));
        }
        return blockStmt;
    }

    private String appendMethodOf(Class<?> type) {
        String appendMethod;
        if (type.isAssignableFrom(DataStream.class)) {
            appendMethod = "append";
        } else if (type.isAssignableFrom(DataStore.class)) {
            appendMethod = "add";
        } else {
            throw new IllegalArgumentException("Unknown data source type " + type.getCanonicalName());
        }
        return appendMethod;
    }

    public Statement extractIntoCollection(String sourceUnitVar, String targetProcessVar) {
        BlockStmt blockStmt = new BlockStmt();
        RuleUnitVariable v = ruleUnitDescription.getVar(sourceUnitVar);
        if (v.isDataSource()) {
            String localVarName = localVarName(v);
            blockStmt.addStatement(assignVar(v))
                    .addStatement(parseStatement("java.util.Objects.requireNonNull(" + localVarName + ", \"Null collection variable used as an output variable: "
                                                         + sourceUnitVar + ". Initialize this variable to get the contents or the data source, " +
                                                         "or use a non-collection data type to extract one value.\");"))

                    .addStatement(new ExpressionStmt(
                            new MethodCallExpr(new NameExpr(localVarName), "subscribe")
                                    .addArgument(new MethodCallExpr(
                                            new NameExpr(DataObserver.class.getCanonicalName()), "of")
                                                         .addArgument(parseExpression(targetProcessVar + "::add")))));
        } else {
            throw new UnsupportedOperationException();
        }
        return blockStmt;
    }

    public Statement extractIntoScalar(String sourceUnitVar, String targetProcessVar) {
        BlockStmt blockStmt = new BlockStmt();
        RuleUnitVariable v = ruleUnitDescription.getVar(sourceUnitVar);
        if (v.isDataSource()) {
            String localVarName = localVarName(v);
            blockStmt.addStatement(assignVar(v))
                    .addStatement(parseStatement("java.util.Objects.requireNonNull(" + localVarName + ", \"Null collection variable used as an output variable: "
                                                         + sourceUnitVar + ". Initialize this variable to get the contents or the data source, " +
                                                         "or use a non-collection data type to extract one value.\");"))

                    .addStatement(new ExpressionStmt(
                            new MethodCallExpr(new NameExpr(localVarName), "subscribe")
                                    .addArgument(new MethodCallExpr(
                                            new NameExpr(DataObserver.class.getCanonicalName()), "ofUpdatable")
                                                         .addArgument(parseExpression("o -> kcontext.setVariable(\"" + targetProcessVar + "\", o)")))));
        } else {
            return parseStatement("kcontext.setVariable(\"" + targetProcessVar + "\", \"" + sourceUnitVar + "\");");
        }

        return blockStmt;
    }
}
