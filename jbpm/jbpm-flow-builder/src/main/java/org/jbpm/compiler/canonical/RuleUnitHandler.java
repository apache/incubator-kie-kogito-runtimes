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
            String srcProcessVar = e.getValue();
            String targetUnitVar = e.getKey();

            boolean procVarIsCollection = variableScope.isCollectionType(srcProcessVar);
            boolean unitVarIsDataSource = unitDescription.hasDataSource(targetUnitVar);
            if (procVarIsCollection && unitVarIsDataSource) {
                variableScope.getVariable(srcProcessVar).ifPresent(expression -> {
                    Statement stmt = unit.injectCollection(targetUnitVar, "Object", expression);
                    actionBody.addStatement(stmt);
                });
            } else if (procVarIsCollection /* && !unitVarIsDataSource */) {
                variableScope.getVariable(srcProcessVar).ifPresent(expression -> {
                    Statement stmt = unit.injectCollection(targetUnitVar, "Object", expression);
                    actionBody.addStatement(stmt);
                });
            } else if (/* !procVarIsCollection && */ unitVarIsDataSource) {
                // set data source to variable
                variableScope.getVariable(srcProcessVar).ifPresent(expression -> {
                    Statement stmt = unit.injectScalar(targetUnitVar, expression);
                    actionBody.addStatement(stmt);
                });
                // subscribe to updates to that data source
                variableScope.assignVariable(srcProcessVar).ifPresent(assignExpr -> {
                    actionBody.addStatement(assignExpr);
                    actionBody.addStatement(unit.extractIntoScalar(targetUnitVar, srcProcessVar));
                });
            } else {
                variableScope.getVariable(srcProcessVar).ifPresent(expression -> {
                    Statement stmt = unit.injectScalar(targetUnitVar, expression);
                    actionBody.addStatement(stmt);
                });
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
                variableScope.assignVariable(srcProcessVar).ifPresent(assignExpr -> {
                    actionBody.addStatement(assignExpr);
                    actionBody.addStatement(unit.extractIntoCollection(targetUnitVar, srcProcessVar));
                });
            } else if (procVarIsCollection /* && !unitVarIsDataSource */) {
                variableScope.assignVariable(srcProcessVar).ifPresent(assignExpr -> {
                    actionBody.addStatement(assignExpr);
                    actionBody.addStatement(unit.extractIntoScalar(targetUnitVar, srcProcessVar));
                });
            } else if (/* !procVarIsCollection && */ unitVarIsDataSource) {
                variableScope.assignVariable(srcProcessVar).ifPresent(assignExpr -> {
                    actionBody.addStatement(assignExpr);
                    actionBody.addStatement(unit.extractIntoScalar(targetUnitVar, srcProcessVar));
                });
            } else /* !procVarIsCollection && !unitVarIsDataSource */ {
                variableScope.setVariable(srcProcessVar).ifPresent(setterCall -> {
                    actionBody.addStatement(
                            setterCall.addArgument(unit.get(targetUnitVar)));
                });
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

    private void injectDataFromModel(BlockStmt stmts, String target, String source) {
        stmts.addStatement(new ExpressionStmt(
                new MethodCallExpr(new MethodCallExpr(
                        new NameExpr("model"),
                        "get" + StringUtils.capitalize(target)), "subscribe")
                        .addArgument(new MethodCallExpr(
                                new NameExpr(DataObserver.class.getCanonicalName()), "ofUpdatable")
                                             .addArgument(parseExpression("o -> kcontext.setVariable(\"" + source + "\", o)")))));
    }

    protected Statement makeAssignmentFromModel(Variable v, String name, RuleUnitDescription unit) {
        String vname = v.getName();
        ClassOrInterfaceType type = parseClassOrInterfaceType(v.getType().getStringType());

        if (unit.hasDataSource(name)) {
            Expression fieldAccessor =
                    new MethodCallExpr(new NameExpr("model"), unit.getVar(name).getter());

            if (isCollectionType(v)) {
                VariableDeclarationExpr varDecl = declareErasedDataSource(fieldAccessor);

                return new BlockStmt()
                        .addStatement(varDecl)
                        .addStatement(parseStatement("java.util.Collection c = (java.util.Collection) kcontext.getVariable(\"" + vname + "\");"))
                        .addStatement(parseStatement("java.util.Objects.requireNonNull(c, \"Null collection variable used as an output variable: "
                                                             + vname + ". Initialize this variable to get the contents or the data source, " +
                                                             "or use a non-collection data type to extract one value.\");"))
                        .addStatement(new ExpressionStmt(
                                new MethodCallExpr(new NameExpr("ds"), "subscribe")
                                        .addArgument(new MethodCallExpr(
                                                new NameExpr(DataObserver.class.getCanonicalName()), "of")
                                                             .addArgument(parseExpression("c::add")))));
            } else {
                return new ExpressionStmt(
                        new MethodCallExpr(fieldAccessor, "subscribe")
                                .addArgument(new MethodCallExpr(
                                        new NameExpr(DataObserver.class.getCanonicalName()), "of")
                                                     .addArgument(parseExpression("o -> kcontext.setVariable(\"" + vname + "\", o)"))));
            }
        }

        // `type` `name` = (`type`) `model.get<Name>
        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(new AssignExpr(
                new VariableDeclarationExpr(type, name),
                new CastExpr(
                        type,
                        new MethodCallExpr(
                                new NameExpr("model"),
                                "get" + StringUtils.capitalize(name))),
                AssignExpr.Operator.ASSIGN));
        blockStmt.addStatement(new MethodCallExpr()
                                       .setScope(new NameExpr("kcontext"))
                                       .setName("setVariable")
                                       .addArgument(new StringLiteralExpr(vname))
                                       .addArgument(name));

        return blockStmt;
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

    private VariableDeclarationExpr declareErasedDataSource(Expression fieldAccessor) {
        return new VariableDeclarationExpr(new VariableDeclarator()
                                                   .setType(DataStream.class.getCanonicalName())
                                                   .setName("ds")
                                                   .setInitializer(fieldAccessor));
    }

    private Class<?> loadUnitClass(String nodeName, String unitName, String packageName) throws ClassNotFoundException {
        ClassNotFoundException ex;
        try {
            return contextClassLoader.loadClass(unitName);
        } catch (ClassNotFoundException e) {
            ex = e;
        }
        // maybe the name is not qualified. Let's try with tacking the packageName at the front
        try {
            return contextClassLoader.loadClass(packageName + "." + unitName);
        } catch (ClassNotFoundException e) {
            // throw the original error
            throw ex;
        }
    }
}
