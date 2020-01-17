package org.jbpm.compiler.canonical;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;

import static java.util.Arrays.asList;

public class ProcessContextMetaModel {

    private final String kcontext;
    private final VariableScope variableScope;
    private ClassLoader contextClassLoader;

    public ProcessContextMetaModel(VariableScope variableScope, ClassLoader contextClassLoader) {
        this("kcontext", variableScope, contextClassLoader);
    }

    public ProcessContextMetaModel(String kcontext, VariableScope variableScope, ClassLoader contextClassLoader) {
        this.kcontext = kcontext;
        this.variableScope = variableScope;
        this.contextClassLoader = contextClassLoader;
    }

    public boolean hasVariable(String srcProcessVar) {
        return variableScope.findVariable(srcProcessVar) != null;
    }

    public Expression getVariable(String procVar) {
        String interpolatedVar = extractVariableFromExpression(procVar);
        Variable v = variableScope.findVariable(interpolatedVar);
        if (v == null) {
            throw new IllegalArgumentException("No such variable " + procVar);
        }
        MethodCallExpr getter = new MethodCallExpr().setScope(new NameExpr(kcontext))
                .setName("getVariable")
                .addArgument(new StringLiteralExpr(interpolatedVar));
        CastExpr castExpr = new CastExpr()
                .setExpression(new EnclosedExpr(getter))
                .setType(v.getType().getStringType());
        return castExpr;
    }

    public AssignExpr assignVariable(String procVar) {
        Expression e = getVariable(procVar);
        return new AssignExpr()
                .setTarget(new VariableDeclarationExpr(
                        new VariableDeclarator()
                                .setType(variableScope.findVariable(procVar).getType().getStringType())
                                .setName(procVar)))
                .setOperator(AssignExpr.Operator.ASSIGN)
                .setValue(e);
    }

    public MethodCallExpr setVariable(String procVar) {
        Variable v = variableScope.findVariable(procVar);
        if (v == null) {
            throw new IllegalArgumentException("No such variable " + procVar);
        }
        MethodCallExpr setter = new MethodCallExpr().setScope(new NameExpr(kcontext))
                .setName("setVariable")
                .addArgument(new StringLiteralExpr(procVar));
        return setter;
    }

    public boolean isCollectionType(String procVar) {
        return isCollectionType(variableScope.findVariable(procVar));
    }

    private boolean isCollectionType(Variable v) {
        String stringType = v.getType().getStringType();
        Class<?> type;
        try {
            type = contextClassLoader.loadClass(stringType);
            return Collection.class.isAssignableFrom(type);
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    private String extractVariableFromExpression(String variableExpression) {
        if (variableExpression.startsWith("#{")) {
            return variableExpression.substring(2, variableExpression.indexOf("."));
        }
        return variableExpression;
    }

    public Collection<String> getVariableNames() {
        return asList(variableScope.getVariableNames());
    }

    public Collection<String> getVariableNames(Collection<String> usedVariables) {
        List<String> allVars = asList(variableScope.getVariableNames());
        List<String> vars = allVars.stream().filter(usedVariables::contains).collect(Collectors.toList());
        if (vars.isEmpty()) {
            return allVars;
        }
        return vars;
    }
}
