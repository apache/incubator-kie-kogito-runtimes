package org.jbpm.compiler.canonical;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.drools.core.util.StringUtils;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.kogito.rules.DataObserver;
import org.kie.kogito.rules.DataStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.javaparser.StaticJavaParser.parse;
import static com.github.javaparser.StaticJavaParser.parseClassOrInterfaceType;
import static com.github.javaparser.StaticJavaParser.parseExpression;
import static com.github.javaparser.StaticJavaParser.parseStatement;

public class RuleUnitHandler {

    public static final Logger logger = LoggerFactory.getLogger(ProcessToExecModelGenerator.class);
    private ClassLoader contextClassLoader;

    RuleUnitDescription ruleUnit;
    ProcessContextMetaModel variableScope;
    RuleSetNode ruleSetNode;

    public RuleUnitHandler(ClassLoader contextClassLoader, RuleUnitDescription ruleUnit, ProcessContextMetaModel variableScope, RuleSetNode ruleSetNode) {
        this.contextClassLoader = contextClassLoader;
        this.ruleUnit = ruleUnit;
        this.variableScope = variableScope;
        this.ruleSetNode = ruleSetNode;
    }

    public Optional<Expression> invoke() {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/class-templates/RuleUnitFactoryTemplate.java");
        Optional<Expression> ruleUnitFactory = parse(resourceAsStream).findFirst(Expression.class);

        String unitName = ruleUnit.getCanonicalName();

        ruleUnitFactory.ifPresent(factory -> {
            factory.findAll(ClassOrInterfaceType.class)
                    .stream()
                    .filter(t -> t.getNameAsString().equals("$Type$"))
                    .forEach(t -> t.setName(unitName));

            factory.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals("bind"))
                    .ifPresent(m -> m.setBody(bind(variableScope, ruleSetNode, ruleUnit)));
            factory.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals("unit"))
                    .ifPresent(m -> m.setBody(unit(unitName)));
            factory.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals("unbind"))
                    .ifPresent(m -> m.setBody(unbind(variableScope, ruleSetNode, ruleUnit)));
        });

        return ruleUnitFactory;
    }

    private BlockStmt unit(String unitName) {
        MethodCallExpr ruleUnit = new MethodCallExpr(
                new MethodCallExpr(new NameExpr("app"), "ruleUnits"), "create")
                .addArgument(new ClassExpr().setType(unitName));
        return new BlockStmt().addStatement(new ReturnStmt(ruleUnit));
    }

    /*
     * bind data to the rule unit POJO
     */
    private BlockStmt bind(ProcessContextMetaModel variableScope, RuleSetNode node, RuleUnitDescription unitDescription) {
        RuleUnitDescriptionCodeHelper unit =
                new RuleUnitDescriptionCodeHelper(unitDescription, "unit");

        BlockStmt actionBody = new BlockStmt();

        // create the RuleUnitData instance
        actionBody.addStatement(unit.newInstance());

        for (Map.Entry<String, String> e : getInputMappings(variableScope, node).entrySet()) {
            String procVar = e.getValue();
            String unitVar = e.getKey();

            if (!variableScope.hasVariable(procVar)) {
                continue;
            }

            boolean procVarIsCollection = variableScope.isCollectionType(procVar);
            boolean unitVarIsDataSource = unitDescription.hasDataSource(unitVar);

            // we assign procVars to unitVars, and subscribe unitVars for changes
            // subscription forward changes directly to the procVars
            if (procVarIsCollection && unitVarIsDataSource) {
                Expression expression = variableScope.getVariable(procVar);
                actionBody.addStatement(
                        unit.injectCollection(unitVar, "Object", expression));
            } else if (procVarIsCollection /* && !unitVarIsDataSource */) {
                Expression expression = variableScope.getVariable(procVar);
                actionBody.addStatement(unit.set(unitVar, expression));
            } else if (/* !procVarIsCollection && */ unitVarIsDataSource) {
                // set data source to variable
                Expression expression = variableScope.getVariable(procVar);
                actionBody.addStatement(
                        unit.injectScalar(unitVar, expression));
                // subscribe to updates to that data source
                actionBody.addStatement(
                        variableScope.assignVariable(procVar));
                actionBody.addStatement(
                        unit.extractIntoScalar(unitVar, procVar));
            } else {
                Expression expression = variableScope.getVariable(procVar);
                actionBody.addStatement(unit.set(unitVar, expression));
            }
        }

        actionBody.addStatement(new ReturnStmt(new NameExpr(unit.instanceVarName())));

        return actionBody;
    }

    private Map<String, String> getInputMappings(ProcessContextMetaModel variableScope, RuleSetNode node) {
        Map<String, String> entries = node.getInMappings();
        if (entries.isEmpty()) {
            entries = new HashMap<>();
            for (String varName : variableScope.getVariableNames()) {
                entries.put(varName, varName);
            }
        }
        return entries;
    }

    private BlockStmt unbind(ProcessContextMetaModel variableScope, RuleSetNode node, RuleUnitDescription unitDescription) {
        RuleUnitDescriptionCodeHelper unit =
                new RuleUnitDescriptionCodeHelper(unitDescription, "unit");

        BlockStmt actionBody = new BlockStmt();

        Map<String, String> mappings = getOutputMappings(variableScope, node);
        for (Map.Entry<String, String> e : mappings.entrySet()) {
            String targetUnitVar = e.getKey();
            String srcProcessVar = e.getValue();
            boolean procVarIsCollection = variableScope.isCollectionType(srcProcessVar);
            boolean unitVarIsDataSource = unitDescription.hasDataSource(targetUnitVar);
            if (procVarIsCollection && unitVarIsDataSource) {
                actionBody.addStatement(variableScope.assignVariable(srcProcessVar));
                actionBody.addStatement(unit.extractIntoCollection(targetUnitVar, srcProcessVar));
            } else if (procVarIsCollection /* && !unitVarIsDataSource */) {
                actionBody.addStatement(variableScope.assignVariable(srcProcessVar));
                actionBody.addStatement(unit.extractIntoScalar(targetUnitVar, srcProcessVar));
            } else if (/* !procVarIsCollection && */ unitVarIsDataSource) {
                actionBody.addStatement(variableScope.assignVariable(srcProcessVar));
                actionBody.addStatement(unit.extractIntoScalar(targetUnitVar, srcProcessVar));

            } else /* !procVarIsCollection && !unitVarIsDataSource */ {
                MethodCallExpr setterCall = variableScope.setVariable(srcProcessVar);
                actionBody.addStatement(
                        setterCall.addArgument(unit.get(targetUnitVar)));

            }
        }

        return actionBody;
    }

    private Map<String, String> getOutputMappings(ProcessContextMetaModel variableScope, RuleSetNode node) {
        Map<String, String> entries = node.getOutMappings();
        if (entries.isEmpty()) {
            entries = new HashMap<>();
            for (String varName : variableScope.getVariableNames()) {
                entries.put(varName, varName);
            }
        }
        return entries;
    }

}
