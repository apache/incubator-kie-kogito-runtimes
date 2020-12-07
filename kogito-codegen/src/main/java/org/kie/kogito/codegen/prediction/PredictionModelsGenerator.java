/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen.prediction;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.kie.kogito.codegen.AbstractApplicationSection;
import org.kie.kogito.codegen.AddonsConfig;
import org.kie.kogito.codegen.InvalidTemplateException;
import org.kie.kogito.codegen.TemplatedGenerator;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;
import org.kie.kogito.prediction.PredictionModels;

import java.util.List;

public class PredictionModelsGenerator extends AbstractApplicationSection {

    private static final String RESOURCE = "/class-templates/PredictionModelsTemplate.java";
    private static final String RESOURCE_CDI = "/class-templates/CdiPredictionModelsTemplate.java";
    private static final String RESOURCE_SPRING = "/class-templates/spring/SpringPredictionModelsTemplate.java";
    private static final String SECTION_CLASS_NAME = "PredictionModels";

    protected final List<PMMLResource> resources;
    protected final String applicationCanonicalName;
    protected AddonsConfig addonsConfig = AddonsConfig.DEFAULT;
    protected final TemplatedGenerator templatedGenerator;

    public PredictionModelsGenerator(String packageName, String applicationCanonicalName, List<PMMLResource> resources) {
        super(SECTION_CLASS_NAME, "predictionModels", PredictionModels.class);
        this.applicationCanonicalName = applicationCanonicalName;
        this.resources = resources;

        this.templatedGenerator = new TemplatedGenerator(
                packageName,
                SECTION_CLASS_NAME,
                RESOURCE_CDI,
                RESOURCE_SPRING,
                RESOURCE);
    }

    public PredictionModelsGenerator withAddons(AddonsConfig addonsConfig) {
        this.addonsConfig = addonsConfig;
        return this;
    }

    public PredictionModelsGenerator withDependencyInjection(DependencyInjectionAnnotator annotator) {
        this.templatedGenerator.withDependencyInjection(annotator);
        return this;
    }

    @Override
    public ClassOrInterfaceDeclaration classDeclaration() {
        CompilationUnit clazz = templatedGenerator.compilationUnit()
                .orElseThrow(() -> new InvalidTemplateException(
                        SECTION_CLASS_NAME,
                        templatedGenerator.templatePath(),
                        "Invalid Template: No CompilationUnit"));
        ClassOrInterfaceDeclaration typeDeclaration = (ClassOrInterfaceDeclaration) clazz.getTypes().get(0);
        populateStaticKieRuntimeFactoryFunctionInit(typeDeclaration);
        return typeDeclaration;
    }

    private void populateStaticKieRuntimeFactoryFunctionInit(ClassOrInterfaceDeclaration typeDeclaration) {
        final InitializerDeclaration staticDeclaration = typeDeclaration.getMembers()
                .stream()
                .filter(member -> member instanceof InitializerDeclaration)
                .findFirst()
                .map(member -> (InitializerDeclaration) member)
                .orElseThrow(() -> new InvalidTemplateException(
                        SECTION_CLASS_NAME,
                        templatedGenerator.templatePath(),
                        "Template has been modified."));
        final NodeList<Statement> statements = staticDeclaration.getBody().getStatements();
        final VariableDeclarationExpr kieRuntimeFactories = statements.stream()
                .filter(statement -> statement instanceof ExpressionStmt && ((ExpressionStmt) statement).getExpression() instanceof VariableDeclarationExpr)
                .map(statement -> (VariableDeclarationExpr) ((ExpressionStmt) statement).getExpression())
                .filter(expression -> expression.getVariable(0).getName().asString().equals("kieRuntimeFactories"))
                .findFirst()
                .orElseThrow(() -> new InvalidTemplateException(
                        SECTION_CLASS_NAME,
                        templatedGenerator.templatePath(),
                        "Template has been modified."));
        MethodCallExpr methodCallExpr = kieRuntimeFactories.getVariable(0)
                .getInitializer()
                .map(expression -> (MethodCallExpr) expression)
                .orElseThrow(() -> new InvalidTemplateException(
                        SECTION_CLASS_NAME,
                        templatedGenerator.templatePath(),
                        "Template has been modified."));
        for (PMMLResource resource : resources) {
            StringLiteralExpr getResAsStream = getReadResourceMethod(resource);
            methodCallExpr.addArgument(getResAsStream);
        }
    }

    private StringLiteralExpr getReadResourceMethod(PMMLResource resource) {
        String source = resource.getModelPath();
        return new StringLiteralExpr(source);
    }
}
