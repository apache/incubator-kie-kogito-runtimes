/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.codegen.process;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.validation.ProcessValidationError;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.validation.RuleFlowProcessValidator;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.ActionNode;
import org.kie.api.definition.process.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.printer.DefaultPrettyPrinterVisitor;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

/**
 * Java rule validator.
 */
class JavaRuleFlowProcessValidator extends RuleFlowProcessValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleFlowProcessValidator.class);

    private static JavaRuleFlowProcessValidator INSTANCE = new JavaRuleFlowProcessValidator();

    public static JavaRuleFlowProcessValidator getInstance() {
        return INSTANCE;
    }

    private JavaRuleFlowProcessValidator() {
    }

    private static final String KCONTEXT = "kcontext";

    @Override
    protected void validateNodes(Node[] nodes, List<ProcessValidationError> errors, RuleFlowProcess process) {
        super.validateNodes(nodes, errors, process);

        for (int i = 0; i < nodes.length; i++) {
            final org.kie.api.definition.process.Node node = nodes[i];
            if (node instanceof ActionNode) {
                final ActionNode actionNode = (ActionNode) node;
                if (actionNode.getAction() instanceof DroolsConsequenceAction) {
                    DroolsConsequenceAction droolsAction = (DroolsConsequenceAction) actionNode.getAction();
                    String actionString = droolsAction.getConsequence();

                    if (!"java".equals(droolsAction.getDialect())) {
                        addErrorMessage(process,
                                node,
                                errors,
                                droolsAction.getDialect() + " script language is not supported in Kogito.");
                    }

                    TypeSolver typeSolver = new ReflectionTypeSolver();
                    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
                    JavaParser parser = new JavaParser(new ParserConfiguration().setSymbolResolver(symbolSolver));

                    ParseResult<CompilationUnit> parse = parser.parse("import org.kie.kogito.internal.process.runtime.KogitoProcessContext;\n" +
                            "import org.jbpm.process.instance.impl.Action;\n" +
                            " class Test {\n" +
                            "    Action action = kcontext -> {" + actionString + "};\n" +
                            "}");

                    if (parse.isSuccessful()) {
                        CompilationUnit unit = parse.getResult().get();

                        //Check local variables declaration
                        Set<String> knownVariables = unit.findAll(VariableDeclarationExpr.class).stream().flatMap(v -> v.getVariables().stream()).map(v -> v.getNameAsString()).collect(toSet());

                        knownVariables.add(KCONTEXT);
                        knownVariables.addAll(Arrays.stream(process.getVariableScope().getVariableNames()).collect(toSet()));
                        knownVariables.addAll(Arrays.asList(process.getGlobalNames()));

                        if (actionNode.getParentContainer() instanceof ContextContainer) {
                            ContextContainer contextContainer = (ContextContainer) actionNode.getParentContainer();
                            VariableScope variableScope = (VariableScope) contextContainer.getDefaultContext(VariableScope.VARIABLE_SCOPE);
                            if (variableScope != null) {
                                knownVariables.addAll(Arrays.stream(variableScope.getVariableNames()).collect(toSet()));
                            }
                        }

                        BlockStmt blockStmt = unit.findFirst(BlockStmt.class).get();
                        try {
                            resolveVariablesType(unit, knownVariables);
                        } catch (UnsolvedSymbolException ex) {
                            DefaultPrettyPrinterVisitor v1 = new DefaultPrettyPrinterVisitor(new DefaultPrinterConfiguration());
                            blockStmt.accept(v1, null);
                            LOGGER.error("\n" + v1);
                            //Small hack to extract the variable name causing the issue
                            //Name comes as "Solving x" where x is the variable name
                            final String[] solving = ex.getName().split(" ");
                            final String var = solving.length == 2 ? solving[1] : solving[0];
                            addErrorMessage(process,
                                    node,
                                    errors,
                                    format("uses unknown variable in the script: %s", var));
                        }
                    } else {
                        addErrorMessage(process,
                                node,
                                errors,
                                format("unable to parse Java content: %s", parse.getProblems().get(0).getMessage()));
                    }

                    validateCompensationIntermediateOrEndEvent(actionNode,
                            process,
                            errors);
                }
            }
        }
    }

    private void resolveVariablesType(com.github.javaparser.ast.Node node, Set<String> knownVariables) {
        node.findAll(MethodCallExpr.class).stream()
                .filter(m -> m.getScope().isPresent())
                .forEach(m -> {
                    Expression expression = m.getScope().get();
                    if (expression.isNameExpr() && !knownVariables.contains(expression.asNameExpr().getNameAsString())) {
                        expression.calculateResolvedType();
                    }
                });
        node.findAll(AssignExpr.class).stream()
                .forEach(m -> {
                    Expression expression = m.getTarget();
                    if (expression.isNameExpr() && !knownVariables.contains(expression.asNameExpr().getNameAsString())) {
                        expression.calculateResolvedType();
                    }
                });
        resolveVariablesTypes(node, knownVariables);
    }

    private void resolveVariablesTypes(com.github.javaparser.ast.Node node, Set<String> knownVariables) {
        node.findAll(MethodCallExpr.class).stream()
                .flatMap(m -> m.getArguments().stream())
                .forEach(arg -> {
                    if (arg.isMethodCallExpr() || arg.isBinaryExpr()) {
                        resolveVariablesTypes(arg, knownVariables);
                    } else {
                        arg.findAll(NameExpr.class).stream().filter(ex -> !knownVariables.contains(ex.getNameAsString())).forEach(ex -> ex.calculateResolvedType());
                    }
                });
        node.findAll(BinaryExpr.class).stream()
                .map(bex -> bex.asBinaryExpr())
                .forEach(bex -> {
                    if (bex.getLeft().isNameExpr()) {
                        if (!knownVariables.contains(bex.getLeft().asNameExpr().getNameAsString())) {
                            bex.getLeft().calculateResolvedType();
                        }
                    } else {
                        resolveVariablesTypes(bex.getLeft(), knownVariables);
                    }
                    if (bex.getRight().isNameExpr()) {
                        if (!knownVariables.contains(bex.getRight().asNameExpr().getNameAsString())) {
                            bex.getRight().calculateResolvedType();
                        }
                    } else {
                        resolveVariablesTypes(bex.getRight(), knownVariables);
                    }
                });
    }

}
