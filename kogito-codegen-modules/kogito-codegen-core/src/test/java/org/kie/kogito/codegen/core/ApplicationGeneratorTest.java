/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.codegen.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.kogito.codegen.api.ApplicationSection;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.codegen.api.Generator.REST_TYPE;

public class ApplicationGeneratorTest {

    private static final String EXPECTED_APPLICATION_NAME = KogitoBuildContext.DEFAULT_PACKAGE_NAME + "." + ApplicationGenerator.APPLICATION_CLASS_NAME;

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    public void targetCanonicalName(KogitoBuildContext.Builder contextBuilder) {
        final ApplicationGenerator appGenerator = new ApplicationGenerator(contextBuilder.build());
        assertThat(appGenerator.targetCanonicalName()).isNotNull();
        assertThat(appGenerator.targetCanonicalName()).isEqualTo(EXPECTED_APPLICATION_NAME);
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    public void targetCanonicalNameDifferentPackage(KogitoBuildContext.Builder contextBuilder) {
        final String differentPackageName = "org.drools.test";
        final String differentPackageExpectedApplicationName = differentPackageName + "." + ApplicationGenerator.APPLICATION_CLASS_NAME;
        contextBuilder.withPackageName(differentPackageName);
        final ApplicationGenerator appGenerator = new ApplicationGenerator(contextBuilder.build());
        assertThat(appGenerator.targetCanonicalName()).isNotNull();
        assertThat(appGenerator.targetCanonicalName()).isEqualTo(differentPackageExpectedApplicationName);
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    public void generatedFilePath(KogitoBuildContext.Builder contextBuilder) {
        final ApplicationGenerator appGenerator = new ApplicationGenerator(contextBuilder.build());
        String path = appGenerator.generateApplicationDescriptor().relativePath();
        assertThat(path)
                .isNotNull()
                .isEqualTo(EXPECTED_APPLICATION_NAME.replace(".", "/") + ".java");
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    public void compilationUnit(KogitoBuildContext.Builder contextBuilder) {
        final KogitoBuildContext context = contextBuilder.build();
        final ApplicationContainerGenerator appGenerator = new ApplicationContainerGenerator(context);
        assertCompilationUnit(appGenerator.getCompilationUnitOrThrow(), context.hasDI());
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    public void compilationUnitWithCDI(KogitoBuildContext.Builder contextBuilder) {
        final KogitoBuildContext context = contextBuilder.build();
        final ApplicationContainerGenerator appGenerator = new ApplicationContainerGenerator(context);
        assertCompilationUnit(appGenerator.getCompilationUnitOrThrow(), context.hasDI());
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    public void applicationSectionReplace(KogitoBuildContext.Builder contextBuilder) {
        final KogitoBuildContext context = contextBuilder.build();
        final ApplicationContainerGenerator appGenerator = new ApplicationContainerGenerator(context);
        assertApplicationPlaceholderReplace(appGenerator, context.hasDI(), 0);

        appGenerator.withSections(Arrays.asList("Processes", "DecisionModels"));
        assertApplicationPlaceholderReplace(appGenerator, context.hasDI(), 2);
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    public void registerGeneratorIfEnabled(KogitoBuildContext.Builder contextBuilder) {
        final KogitoBuildContext context = contextBuilder.build();
        final ApplicationGenerator appGenerator = new ApplicationGenerator(context);
        final MockGenerator disabledGenerator = new MockGenerator(context, false);
        final MockGenerator enabledGenerator = new MockGenerator(context, true);
        assertThat(appGenerator.registerGeneratorIfEnabled(disabledGenerator))
                .isEmpty();
        assertThat(appGenerator.getGenerators()).isEmpty();

        assertThat(appGenerator.registerGeneratorIfEnabled(enabledGenerator))
                .isNotEmpty();
        assertThat(appGenerator.getGenerators()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    public void filterRestIfDisabled(KogitoBuildContext.Builder contextBuilder) {
        final KogitoBuildContext context = contextBuilder.build();
        final ApplicationGenerator appGenerator = new ApplicationGenerator(context);
        final MockGenerator restGenerator = new MockGenerator(context, true, true);

        assertThat(appGenerator.registerGeneratorIfEnabled(restGenerator))
                .isNotEmpty();
        assertThat(appGenerator.getGenerators()).hasSize(1);

        if (context.hasREST()) {
            // disable REST
            context.setApplicationProperty(KogitoBuildContext.KOGITO_GENERATE_REST, "false");
            assertThat(appGenerator.generateComponents()).isEmpty();

            // enable REST
            context.setApplicationProperty(KogitoBuildContext.KOGITO_GENERATE_REST, "true");
            assertThat(appGenerator.generateComponents())
                    .isNotEmpty()
                    .hasSize(1)
                    .matches(files -> files.stream().anyMatch(gf -> REST_TYPE.equals(gf.type())));
        } else {
            assertThat(appGenerator.generateComponents()).isEmpty();
        }
    }

    private void assertCompilationUnit(final CompilationUnit compilationUnit, final boolean checkCDI) {
        assertThat(compilationUnit).isNotNull();

        assertThat(compilationUnit.getPackageDeclaration()).isPresent();
        assertThat(compilationUnit.getPackageDeclaration().get().getName()).hasToString(KogitoBuildContext.DEFAULT_PACKAGE_NAME);

        assertThat(compilationUnit.getTypes()).isNotNull();
        assertThat(compilationUnit.getTypes()).hasSize(1);

        final TypeDeclaration<?> mainAppClass = compilationUnit.getTypes().get(0);
        assertThat(mainAppClass).isNotNull();
        assertThat(mainAppClass.getName()).hasToString("Application");

        if (checkCDI) {
            assertThat(mainAppClass.getAnnotations()).isNotEmpty();
        } else {
            assertThat(mainAppClass.getAnnotations()).isEmpty();
        }

        assertThat(mainAppClass.getMembers()).isNotNull();
    }

    private void assertApplicationPlaceholderReplace(ApplicationContainerGenerator appGenerator, boolean hasDI, long expectedParams) {
        CompilationUnit compilationUnit = appGenerator.getCompilationUnitOrThrow();

        Optional<NodeList<Expression>> expressions = compilationUnit.findFirst(MethodCallExpr.class, mtd -> "loadEngines".equals(mtd.getNameAsString()))
                .map(MethodCallExpr::getArguments);

        if (hasDI) {
            assertThat(expressions).isEmpty();
        } else {
            assertThat(expressions).isPresent();

            expressions.get()
                    .forEach(expression -> assertThat(expression.toString()).doesNotContain("$"));

            long numberOfNull = expressions.get().stream()
                    .filter(Expression::isNullLiteralExpr)
                    .count();

            assertThat(numberOfNull).isZero();

            assertThat(expressions.get().size()).isEqualTo(expectedParams);
        }
    }

    static class MockGenerator extends AbstractGenerator {

        private final boolean enabled;
        private final boolean produceREST;

        protected MockGenerator(KogitoBuildContext context, boolean enabled) {
            this(context, enabled, false);
        }

        protected MockGenerator(KogitoBuildContext context, boolean enabled, boolean produceREST) {
            super(context, "mockGenerator");
            this.enabled = enabled;
            this.produceREST = produceREST;
        }

        @Override
        public Optional<ApplicationSection> section() {
            return Optional.empty();
        }

        @Override
        public Collection<GeneratedFile> generate() {
            if (produceREST) {
                return Collections.singleton(new GeneratedFile(REST_TYPE, "my/path", ""));
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}
