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
package org.kie.kogito.codegen.prediction;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.drools.codegen.common.GeneratedFile;
import org.drools.codegen.common.GeneratedFileType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.api.io.ResourceType;
import org.kie.kogito.codegen.api.ApplicationSection;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.core.io.CollectedResourceProducer;

import com.github.javaparser.ast.CompilationUnit;

import static java.util.stream.Collectors.toList;
import static org.drools.codegen.common.GeneratedFileType.COMPILED_CLASS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.efesto.common.api.constants.Constants.INDEXFILE_DIRECTORY_PROPERTY;
import static org.kie.kogito.codegen.prediction.PredictionCodegenFactory.parsePredictions;
import static org.kie.kogito.codegen.prediction.PredictionCodegenFactoryTest.BASE_PATH;
import static org.kie.kogito.codegen.prediction.PredictionCodegenFactoryTest.MINING_FULL_SOURCE;
import static org.kie.kogito.codegen.prediction.PredictionCodegenFactoryTest.MULTIPLE_FULL_SOURCE;
import static org.kie.kogito.codegen.prediction.PredictionCodegenFactoryTest.REFLECT_JSON;
import static org.kie.kogito.codegen.prediction.PredictionCodegenFactoryTest.REGRESSION_FULL_SOURCE;
import static org.kie.kogito.codegen.prediction.PredictionCodegenFactoryTest.SCORECARD_FULL_SOURCE;
import static org.kie.kogito.codegen.prediction.PredictionCodegenFactoryTest.assertEndpoints;

class PredictionCodegenTest {

    @BeforeAll
    public static void setup() {
        System.setProperty(INDEXFILE_DIRECTORY_PROPERTY, "target/test-classes");
    }

    @AfterAll
    public static void cleanup() {
        System.clearProperty(INDEXFILE_DIRECTORY_PROPERTY);
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void internalGenerateAllFilesRegression(KogitoBuildContext.Builder contextBuilder) {
        KogitoBuildContext context = contextBuilder.build();
        Collection<CollectedResource> resources = CollectedResourceProducer.fromFiles(BASE_PATH,
                REGRESSION_FULL_SOURCE.toFile());
        Collection<PMMLResource> pmmlResources = resources.stream()
                .filter(r -> r.resource().getResourceType() == ResourceType.PMML)
                .flatMap(r -> parsePredictions(Thread.currentThread().getContextClassLoader(), r.basePath(),
                        Collections.singletonList(r.resource())).stream())
                .collect(toList());
        PredictionCodegen codeGenerator = new PredictionCodegen(context, pmmlResources);
        internalGenerateAllFiles(codeGenerator, 5, 3, 1, false);
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void internalGenerateAllFilesScorecard(KogitoBuildContext.Builder contextBuilder) {
        KogitoBuildContext context = contextBuilder.build();
        Collection<CollectedResource> resources = CollectedResourceProducer.fromFiles(BASE_PATH,
                SCORECARD_FULL_SOURCE.toFile());
        Collection<PMMLResource> pmmlResources = resources.stream()
                .filter(r -> r.resource().getResourceType() == ResourceType.PMML)
                .flatMap(r -> parsePredictions(Thread.currentThread().getContextClassLoader(), r.basePath(),
                        Collections.singletonList(r.resource())).stream())
                .collect(toList());
        PredictionCodegen codeGenerator = new PredictionCodegen(context, pmmlResources);
        internalGenerateAllFiles(codeGenerator, 36, 34, 1, false);
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void internalGenerateAllFilesMining(KogitoBuildContext.Builder contextBuilder) {
        KogitoBuildContext context = contextBuilder.build();
        Collection<CollectedResource> resources = CollectedResourceProducer.fromFiles(BASE_PATH,
                MINING_FULL_SOURCE.toFile());
        Collection<PMMLResource> pmmlResources = resources.stream()
                .filter(r -> r.resource().getResourceType() == ResourceType.PMML)
                .flatMap(r -> parsePredictions(Thread.currentThread().getContextClassLoader(), r.basePath(),
                        Collections.singletonList(r.resource())).stream())
                .collect(toList());
        PredictionCodegen codeGenerator = new PredictionCodegen(context, pmmlResources);
        internalGenerateAllFiles(codeGenerator, 79, 77, 1, false);
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void internalGenerateAllFilesMultiple(KogitoBuildContext.Builder contextBuilder) {
        KogitoBuildContext context = contextBuilder.build();
        Collection<CollectedResource> resources = CollectedResourceProducer.fromFiles(BASE_PATH,
                MULTIPLE_FULL_SOURCE.toFile());
        Collection<PMMLResource> pmmlResources = resources.stream()
                .filter(r -> r.resource().getResourceType() == ResourceType.PMML)
                .flatMap(r -> parsePredictions(Thread.currentThread().getContextClassLoader(), r.basePath(),
                        Collections.singletonList(r.resource())).stream())
                .collect(toList());
        PredictionCodegen codeGenerator = new PredictionCodegen(context, pmmlResources);
        internalGenerateAllFiles(codeGenerator, 88, 84, 2, false);
    }

    static void internalGenerateAllFiles(PredictionCodegen codeGenerator,
            int expectedTotalFiles, int expectedCompiledClasses, int expectedRestEndpoints,
            boolean assertReflect) {
        KogitoBuildContext context = codeGenerator.context();
        Collection<GeneratedFile> generatedFiles = codeGenerator.internalGenerate();

        int expectedGeneratedFilesSize = expectedTotalFiles - (context.hasRESTForGenerator(codeGenerator) ? 0 : expectedRestEndpoints * 2);
        assertEquals(expectedGeneratedFilesSize, generatedFiles.size());

        assertEquals(expectedCompiledClasses, generatedFiles.stream()
                .filter(generatedFile -> generatedFile.category().equals(GeneratedFileType.Category.COMPILED_CLASS) &&
                        generatedFile.type().equals(COMPILED_CLASS))
                .count());

        int expectedReflectResource = assertReflect ? 1 : 0;
        assertEquals(expectedReflectResource, generatedFiles.stream()
                .filter(generatedFile -> generatedFile.category().equals(GeneratedFileType.Category.INTERNAL_RESOURCE) &&
                        generatedFile.type().name().equals(GeneratedFileType.INTERNAL_RESOURCE.name()) &&
                        generatedFile.relativePath().endsWith(REFLECT_JSON))
                .count());

        assertEndpoints(context, generatedFiles, expectedRestEndpoints, codeGenerator);

        Optional<ApplicationSection> optionalApplicationSection = codeGenerator.section();
        assertTrue(optionalApplicationSection.isPresent());

        CompilationUnit compilationUnit = optionalApplicationSection.get().compilationUnit();
        assertNotNull(compilationUnit);
    }
}
