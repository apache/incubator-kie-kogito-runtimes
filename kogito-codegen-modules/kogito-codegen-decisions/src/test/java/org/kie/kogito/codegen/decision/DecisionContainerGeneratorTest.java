/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.kie.kogito.codegen.decision;

import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.api.io.Resource;
import org.kie.dmn.core.compiler.DMNProfile;
import org.kie.kogito.codegen.api.AddonsConfig;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.dmn.DmnExecutionIdSupplier;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.codegen.core.CodegenUtils.newObject;
import static org.kie.kogito.codegen.decision.DecisionCodegen.getCustomDMNProfiles;
import static org.kie.kogito.codegen.decision.DecisionCodegenTest.CUSTOM_PROFILES_PACKAGE;
import static org.kie.kogito.codegen.decision.DecisionContainerGenerator.MONITORED_DECISIONMODEL_TRANSFORMER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DecisionContainerGeneratorTest {

    private MethodCallExpr initMethod;
    private KogitoBuildContext context;

    @BeforeEach
    void setUp() {
        initMethod = new MethodCallExpr();
        initMethod.setName("init");

        context = mock(KogitoBuildContext.class);
        AddonsConfig addonsConfig = AddonsConfig.builder().build();
        when(context.getAddonsConfig()).thenReturn(addonsConfig);
        when(context.getPackageName()).thenReturn("org.kie.kogito.test");
        when(context.name()).thenReturn("Java");
    }

    @Test
    void setupExecIdSupplierVariableWithTracing() {
        assertThat(initMethod.getArguments()).isEmpty();
        DecisionContainerGenerator.setupExecIdSupplierVariable(initMethod, true);
        assertThat(initMethod.getArguments()).hasSize(1);
        Expression expected = newObject(DmnExecutionIdSupplier.class);
        assertThat(initMethod.getArguments().get(0)).isEqualTo(expected);
    }

    @Test
    void setupExecIdSupplierVariableWithoutTracing() {
        assertThat(initMethod.getArguments()).isEmpty();
        DecisionContainerGenerator.setupExecIdSupplierVariable(initMethod, false);
        assertThat(initMethod.getArguments()).hasSize(1);
        Expression expected = new NullLiteralExpr();
        assertThat(initMethod.getArguments().get(0)).isEqualTo(expected);
    }

    @Test
    void setupDecisionModelTransformerVariableWithMonitoring() {
        assertThat(initMethod.getArguments()).isEmpty();
        DecisionContainerGenerator.setupDecisionModelTransformerVariable(initMethod, true);
        assertThat(initMethod.getArguments()).hasSize(1);
        Expression expected = newObject(MONITORED_DECISIONMODEL_TRANSFORMER);
        assertThat(initMethod.getArguments().get(0)).isEqualTo(expected);
    }

    @Test
    void setupDecisionModelTransformerVariableWithoutMonitoring() {
        assertThat(initMethod.getArguments()).isEmpty();
        DecisionContainerGenerator.setupDecisionModelTransformerVariable(initMethod, false);
        assertThat(initMethod.getArguments()).hasSize(1);
        Expression expected = new NullLiteralExpr();
        assertThat(initMethod.getArguments().get(0)).isEqualTo(expected);
    }

    @Test
    void setupCustomDMNProfiles() {
        NodeList<Expression> arguments = initMethod.getArguments();
        assertThat(initMethod.getArguments()).isEmpty();
        Set<String> customDMNProfileStrings = IntStream.range(0, 3)
                .mapToObj(index -> String.format("%s.Profile_%d", CUSTOM_PROFILES_PACKAGE, index))
                .collect(Collectors.toSet());
        Set<DMNProfile> customDMNProfiles = getCustomDMNProfiles(customDMNProfileStrings, Thread.currentThread().getContextClassLoader());
        DecisionContainerGenerator.setupCustomDMNProfiles(initMethod, customDMNProfiles);
        assertThat(initMethod.getArguments()).hasSize(1);
        Expression retrieved = arguments.get(0);
        assertThat(retrieved).isInstanceOf(MethodCallExpr.class);
        MethodCallExpr methodCallExpr = (MethodCallExpr) retrieved;
        assertThat(methodCallExpr.getScope()).isNotEmpty();
        assertThat(methodCallExpr.getScope().get()).isEqualTo(new NameExpr(Set.class.getCanonicalName()));
        assertThat(methodCallExpr.getName().getIdentifier()).isEqualTo("of");
        NodeList<Expression> retrievedArguments = methodCallExpr.getArguments();
        assertThat(retrievedArguments).hasSize(customDMNProfileStrings.size());
        customDMNProfileStrings.forEach(profileString -> {
            Expression expectedArgument = newObject(profileString);
            assertThat(retrievedArguments).contains(expectedArgument);
        });
    }

    @ParameterizedTest
    @MethodSource("booleans")
    void setupEnableRuntimeTypeCheckOptionFalse(boolean enableRuntimeTypeCheckOption) {
        NodeList<Expression> arguments = initMethod.getArguments();
        assertThat(initMethod.getArguments()).isEmpty();
        DecisionContainerGenerator.setupEnableRuntimeTypeCheckOption(initMethod, enableRuntimeTypeCheckOption);
        assertThat(initMethod.getArguments()).hasSize(1);
        Expression retrieved = arguments.get(0);
        assertThat(retrieved).isInstanceOf(BooleanLiteralExpr.class);
        assertThat(((BooleanLiteralExpr) retrieved).getValue()).isEqualTo(enableRuntimeTypeCheckOption);
    }

    static Stream<Boolean> booleans() {
        return Stream.of(true, false);
    }

    @Test
    void testChunkingWithFewResources() throws Exception {
        Collection<CollectedResource> resources = createMockResources(5);

        DecisionContainerGenerator generator = new DecisionContainerGenerator(
                context, "org.kie.kogito.Application", resources, Collections.emptyList(), Collections.emptySet(), false);

        CompilationUnit cu = generator.compilationUnit();
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("DecisionModels").orElseThrow();

        List<MethodDeclaration> loadMethods0 = clazz.getMethodsByName("loadDmnResources_0");
        assertThat(loadMethods0).hasSize(1);

        assertThat(loadMethods0.get(0).getBody().get().getStatements()).hasSize(7);

        assertThat(clazz.getMethodsByName("loadDmnResources_1")).isEmpty();
    }

    @Test
    void testChunkingWithManyResources() throws Exception {
        Collection<CollectedResource> resources = createMockResources(2005);

        DecisionContainerGenerator generator = new DecisionContainerGenerator(
                context, "org.kie.kogito.Application", resources, Collections.emptyList(), Collections.emptySet(), false);

        CompilationUnit cu = generator.compilationUnit();
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("DecisionModels").orElseThrow();

        List<MethodDeclaration> loadMethods0 = clazz.getMethodsByName("loadDmnResources_0");
        assertThat(loadMethods0).hasSize(1);
        assertThat(loadMethods0.get(0).getBody().get().getStatements()).hasSize(1002);

        List<MethodDeclaration> loadMethods1 = clazz.getMethodsByName("loadDmnResources_1");
        assertThat(loadMethods1).hasSize(1);
        assertThat(loadMethods1.get(0).getBody().get().getStatements()).hasSize(1002);

        List<MethodDeclaration> loadMethods2 = clazz.getMethodsByName("loadDmnResources_2");
        assertThat(loadMethods2).hasSize(1);
        assertThat(loadMethods2.get(0).getBody().get().getStatements()).hasSize(7);

        assertThat(clazz.getMethodsByName("loadDmnResources_3")).isEmpty();
    }

    private Collection<CollectedResource> createMockResources(int count) throws Exception {
        List<CollectedResource> resources = new ArrayList<>();
        // using a .jar base path bypasses the ReadResourceUtil file system checks
        Path basePath = Paths.get("dummy.jar");

        for (int i = 0; i < count; i++) {
            Resource resource = mock(Resource.class);
            when(resource.getSourcePath()).thenReturn("dmn_" + i + ".dmn");
            when(resource.getReader()).thenReturn(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<definitions/>"));

            CollectedResource collected = mock(CollectedResource.class);
            when(collected.basePath()).thenReturn(basePath);
            when(collected.resource()).thenReturn(resource);

            resources.add(collected);
        }
        return resources;
    }

}
