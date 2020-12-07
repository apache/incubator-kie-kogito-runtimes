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

package org.kie.kogito.codegen;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class ApplicationContainerGenerator extends TemplatedGenerator {

    public static final String APPLICATION_CLASS_NAME = "Application";
    private static final String RESOURCE_CDI = "/class-templates/CdiApplicationTemplate.java";
    private static final String RESOURCE_SPRING = "/class-templates/SpringApplicationTemplate.java";
    private static final String RESOURCE_DEFAULT = "/class-templates/ApplicationTemplate.java";

    private List<String> sections = new ArrayList<>();

    public ApplicationContainerGenerator(String packageName) {
        super(packageName,
              APPLICATION_CLASS_NAME,
              RESOURCE_CDI,
              RESOURCE_SPRING,
              RESOURCE_DEFAULT);
    }

    public ApplicationContainerGenerator withSections(List<String> sections) {
        this.sections = sections;
        return this;
    }

    public CompilationUnit getCompilationUnitOrThrow() {
        return compilationUnit()
                .orElseThrow(() -> new InvalidTemplateException(
                        APPLICATION_CLASS_NAME,
                        templatePath(),
                        "Cannot find template for " + super.typeName()));
    }

    @Override
    public Optional<CompilationUnit> compilationUnit() {
        Optional<CompilationUnit> optionalCompilationUnit = super.compilationUnit();
        CompilationUnit compilationUnit =
                optionalCompilationUnit
                        .orElseThrow(() -> new InvalidTemplateException(
                                APPLICATION_CLASS_NAME,
                                templatePath(),
                                "Cannot find template for " + super.typeName()));

        ClassOrInterfaceDeclaration cls = compilationUnit
                .findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new InvalidTemplateException(
                        APPLICATION_CLASS_NAME,
                        templatePath(),
                        "Compilation unit doesn't contain a class or interface declaration!"));

        if (annotator == null) {
            replacePlaceholder(getSuperArguments(cls), sections);
        }

        cls.getMembers().sort(new BodyDeclarationComparator());
        return optionalCompilationUnit;
    }

    private NodeList<Expression> getSuperArguments(ClassOrInterfaceDeclaration cls) {
        return cls.findFirst(ExplicitConstructorInvocationStmt.class)
                    .map(ExplicitConstructorInvocationStmt::getArguments)
                    .orElseThrow(() -> new InvalidTemplateException(
                            APPLICATION_CLASS_NAME,
                            templatePath(),
                            "Impossible to find super invocation"));
    }

    private void replacePlaceholder(NodeList<Expression> expressions, List<String> sections) {
        // look for expressions that contain $ and replace them with an initializer or null
        //      e.g.: $Processes$
        // is replaced with:
        //      e.g.: new Processes(this)
        // or null if Process section is not available

        Map<String, Expression> replacementMap = sections.stream()
                .collect(toMap(
                        identity(),
                        sectionClassName -> new ObjectCreationExpr()
                                .setType(sectionClassName)
                                .addArgument(new ThisExpr())
                ));

        expressions.stream()
                .filter(exp -> exp.toString().contains("$"))
                .forEach(argument -> replaceOrNull(argument, replacementMap));
    }

    private void replaceOrNull(Expression originalExpression, Map<String, Expression> expressionMap) {
        for (Map.Entry<String, Expression> entry : expressionMap.entrySet()) {
            if (originalExpression.toString().contains(entry.getKey())) {
                originalExpression.replace(entry.getValue());
                return;
            }
        }
        originalExpression.replace(new NullLiteralExpr());
    }
}
