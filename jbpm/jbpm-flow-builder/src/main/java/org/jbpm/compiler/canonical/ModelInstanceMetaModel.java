package org.jbpm.compiler.canonical;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.drools.core.util.StringUtils;
import org.kie.internal.ruleunit.RuleUnitVariable;

public class ModelInstanceMetaModel {

    ModelMetaData modelDescriptor;
    String instanceVarName;

    public AssignExpr newInstance() {
        ClassOrInterfaceType type = new ClassOrInterfaceType(null, modelDescriptor.getModelClassName());
        return new AssignExpr(
                new VariableDeclarationExpr(type, instanceVarName),
                new ObjectCreationExpr().setType(type),
                AssignExpr.Operator.ASSIGN);
    }

    public MethodCallExpr callGetter(String field) {
        String getter = "get" + StringUtils.capitalize(field);
        return new MethodCallExpr(new NameExpr(instanceVarName), getter);
    }

    private MethodCallExpr callSetter(RuleUnitVariable v) {
        String setter = v.setter();
        return new MethodCallExpr(new NameExpr(instanceVarName), setter);
    }

}
