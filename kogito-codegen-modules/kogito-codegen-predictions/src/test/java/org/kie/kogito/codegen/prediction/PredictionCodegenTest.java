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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.kogito.codegen.api.ApplicationSection;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.GeneratedFileType;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.core.io.CollectedResourceProducer;
import org.kie.kogito.codegen.prediction.mock.KiePMMLModelWithSourcesAndNestedModelsMock;
import org.kie.kogito.codegen.prediction.mock.KiePMMLModelWithSourcesMock;
import org.kie.pmml.commons.model.KiePMMLModel;

import com.github.javaparser.ast.CompilationUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.kogito.codegen.api.Generator.REST_TYPE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PredictionCodegenTest {

    private static final Path BASE_PATH = Paths.get("src/test/resources/").toAbsolutePath();
    private static final String REGRESSION_SOURCE = "prediction/test_regression.pmml";
    private static final Path REGRESSION_FULL_SOURCE = BASE_PATH.resolve(REGRESSION_SOURCE);
    private static final String SCORECARD_SOURCE = "prediction/test_scorecard.pmml";
    private static final Path SCORECARD_FULL_SOURCE = BASE_PATH.resolve(SCORECARD_SOURCE);
    private static final String MINING_SOURCE = "prediction/test_miningmodel.pmml";
    private static final Path MINING_FULL_SOURCE = BASE_PATH.resolve(MINING_SOURCE);
    private static final String MULTIPLE_SOURCE = "prediction/test_multiplemodels.pmml";
    private static final Path MULTIPLE_FULL_SOURCE = BASE_PATH.resolve(MULTIPLE_SOURCE);
    private static final String REFLECT_JSON = "reflect-config.json";

    private static final String CODE = "CODE";
    private static final String EMPTY = "";
    private static final String MOCK = "mock";
    private static final String NESTED_MOCK = "nestedMock";

    private static final String NAME_PMML = "PMML";
    private static final String NAME_RULE = "RULE";
    private static final String NAME_REST = "REST";
    private static final String NAME_RESOURCE = "RESOURCE";

    private static final String SRC_1_NAME = "com.example.model.Class";
    private static final String SRC_1_PATH = nameToPath(SRC_1_NAME, '.', "java");
    private static final String SRC_2_NAME = "com.example.model.rule.Rule1";
    private static final String SRC_2_PATH = nameToPath(SRC_2_NAME, '.', "java");
    private static final String SRC_3_NAME = "com.example.model.nested.Class";
    private static final String SRC_3_PATH = nameToPath(SRC_3_NAME, '.', "java");
    private static final String SRC_4_NAME = "com.example.model.nested.rule.Rule1";
    private static final String SRC_4_PATH = nameToPath(SRC_4_NAME, '.', "java");

    private static final String REST_PATH = nameToPath("org.kie.kogito.org_46kie_46kogito_46codegen_46prediction_46mock.MockResource", '.', "java");
    private static final String RESOURCE_PATH = nameToPath("META-INF/resources/Mock", '/', "json");

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void generateAllFilesRegression(KogitoBuildContext.Builder contextBuilder) {
        KogitoBuildContext context = contextBuilder.build();
        PredictionCodegen codeGenerator = PredictionCodegen.ofCollectedResources(
                context, CollectedResourceProducer.fromFiles(BASE_PATH, REGRESSION_FULL_SOURCE.toFile()));
        generateAllFiles(context, codeGenerator, 5, 3, 1, false);
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void generateAllFilesScorecard(KogitoBuildContext.Builder contextBuilder) {
        KogitoBuildContext context = contextBuilder.build();
        PredictionCodegen codeGenerator = PredictionCodegen.ofCollectedResources(
                context, CollectedResourceProducer.fromFiles(BASE_PATH, SCORECARD_FULL_SOURCE.toFile()));
        generateAllFiles(context, codeGenerator, 27, 4, 1, true);
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void generateAllFilesMining(KogitoBuildContext.Builder contextBuilder) {
        KogitoBuildContext context = contextBuilder.build();
        PredictionCodegen codeGenerator = PredictionCodegen.ofCollectedResources(
                context, CollectedResourceProducer.fromFiles(BASE_PATH, MINING_FULL_SOURCE.toFile()));
        generateAllFiles(context, codeGenerator, 67, 18, 1, true);
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void generateAllFilesMultiple(KogitoBuildContext.Builder contextBuilder) {
        KogitoBuildContext context = contextBuilder.build();
        PredictionCodegen codeGenerator = PredictionCodegen.ofCollectedResources(
                context, CollectedResourceProducer.fromFiles(BASE_PATH, MULTIPLE_FULL_SOURCE.toFile()));
        generateAllFiles(context, codeGenerator, 36, 6, 2, true);
    }

    private static void generateAllFiles(KogitoBuildContext context, PredictionCodegen codeGenerator, int expectedTotalFiles, int expectedJavaSources, int expectedRestEndpoints,
            boolean assertReflect) {
        List<GeneratedFile> generatedFiles = codeGenerator.generate();

        int expectedGeneratedFilesSize = expectedTotalFiles - (context.hasREST() ? 0 : expectedRestEndpoints * 2);
        assertEquals(expectedGeneratedFilesSize, generatedFiles.size());

        assertEquals(expectedJavaSources, generatedFiles.stream()
                .filter(generatedFile -> generatedFile.category().equals(GeneratedFileType.Category.SOURCE) &&
                        generatedFile.type().name().equals(NAME_PMML) &&
                        generatedFile.relativePath().endsWith(".java"))
                .count());

        int expectedReflectResource = assertReflect ? 1 : 0;
        assertEquals(expectedReflectResource, generatedFiles.stream()
                .filter(generatedFile -> generatedFile.category().equals(GeneratedFileType.Category.RESOURCE) &&
                        generatedFile.type().name().equals(GeneratedFileType.RESOURCE.name()) &&
                        generatedFile.relativePath().endsWith(REFLECT_JSON))
                .count());

        assertEndpoints(context, generatedFiles, expectedRestEndpoints);

        Optional<ApplicationSection> optionalApplicationSection = codeGenerator.section();
        assertTrue(optionalApplicationSection.isPresent());

        CompilationUnit compilationUnit = optionalApplicationSection.get().compilationUnit();
        assertNotNull(compilationUnit);
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void testGenerateModelBaseFilesMethodThrowsExceptionWithInvalidModel(KogitoBuildContext.Builder contextBuilder) {
        PredictionCodegen codeGenerator = PredictionCodegen.ofCollectedResources(contextBuilder.build(), Collections.emptyList());

        KiePMMLModel nullNameMock = buildInvalidMock(null);
        assertThrows(IllegalArgumentException.class, () -> codeGenerator.generateModelBaseFiles(new ArrayList<>(), nullNameMock, EMPTY));

        KiePMMLModel emptyNameMock = buildInvalidMock(EMPTY);
        assertThrows(IllegalArgumentException.class, () -> codeGenerator.generateModelBaseFiles(new ArrayList<>(), emptyNameMock, EMPTY));

        KiePMMLModel invalidClassMock = buildInvalidMock(MOCK);
        assertThrows(IllegalStateException.class, () -> codeGenerator.generateModelBaseFiles(new ArrayList<>(), invalidClassMock, EMPTY));
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void testGenerateModelBaseFilesMethodThrowsExceptionWithInvalidNestedModel(KogitoBuildContext.Builder contextBuilder) {
        PredictionCodegen codeGenerator = PredictionCodegen.ofCollectedResources(contextBuilder.build(), Collections.emptyList());

        KiePMMLModel nullNameMock = new KiePMMLModelWithSourcesAndNestedModelsMock(MOCK, buildInvalidMock(null));
        assertThrows(IllegalArgumentException.class, () -> codeGenerator.generateModelBaseFiles(new ArrayList<>(), nullNameMock, EMPTY));

        KiePMMLModel emptyNameMock = new KiePMMLModelWithSourcesAndNestedModelsMock(MOCK, buildInvalidMock(EMPTY));
        assertThrows(IllegalArgumentException.class, () -> codeGenerator.generateModelBaseFiles(new ArrayList<>(), emptyNameMock, EMPTY));

        KiePMMLModel invalidClassMock = new KiePMMLModelWithSourcesAndNestedModelsMock(MOCK, buildInvalidMock(NESTED_MOCK));
        assertThrows(IllegalStateException.class, () -> codeGenerator.generateModelBaseFiles(new ArrayList<>(), invalidClassMock, EMPTY));
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void testGenerateModelBaseFilesMethodWorkingWithValidModel(KogitoBuildContext.Builder contextBuilder) {
        PredictionCodegen codeGenerator = PredictionCodegen.ofCollectedResources(contextBuilder.build(), Collections.emptyList());

        List<KiePMMLModel> mocks = buildValidMocks();

        List<GeneratedFile> list0 = new ArrayList<>();
        codeGenerator.generateModelBaseFiles(list0, mocks.get(0), EMPTY);
        assertEquals(1, list0.size());
        assertGeneratedFile(SRC_1_PATH, NAME_PMML, GeneratedFileType.Category.SOURCE, list0.get(0));

        List<GeneratedFile> list1 = new ArrayList<>();
        codeGenerator.generateModelBaseFiles(list1, mocks.get(1), EMPTY);
        assertEquals(2, list1.size());
        assertGeneratedFile(SRC_1_PATH, NAME_PMML, GeneratedFileType.Category.SOURCE, list1.get(0));
        assertGeneratedFile(SRC_2_PATH, NAME_RULE, GeneratedFileType.Category.SOURCE, list1.get(1));

        List<GeneratedFile> list2 = new ArrayList<>();
        codeGenerator.generateModelBaseFiles(list2, mocks.get(2), EMPTY);
        assertEquals(2, list2.size());
        assertGeneratedFile(SRC_1_PATH, NAME_PMML, GeneratedFileType.Category.SOURCE, list2.get(0));
        assertGeneratedFile(SRC_3_PATH, NAME_PMML, GeneratedFileType.Category.SOURCE, list2.get(1));

        List<GeneratedFile> list3 = new ArrayList<>();
        codeGenerator.generateModelBaseFiles(list3, mocks.get(3), EMPTY);
        assertEquals(3, list3.size());
        assertGeneratedFile(SRC_1_PATH, NAME_PMML, GeneratedFileType.Category.SOURCE, list3.get(0));
        assertGeneratedFile(SRC_3_PATH, NAME_PMML, GeneratedFileType.Category.SOURCE, list3.get(1));
        assertGeneratedFile(SRC_4_PATH, NAME_RULE, GeneratedFileType.Category.SOURCE, list3.get(2));

        List<GeneratedFile> list4 = new ArrayList<>();
        codeGenerator.generateModelBaseFiles(list4, mocks.get(4), EMPTY);
        assertEquals(3, list4.size());
        assertGeneratedFile(SRC_1_PATH, NAME_PMML, GeneratedFileType.Category.SOURCE, list4.get(0));
        assertGeneratedFile(SRC_2_PATH, NAME_RULE, GeneratedFileType.Category.SOURCE, list4.get(1));
        assertGeneratedFile(SRC_3_PATH, NAME_PMML, GeneratedFileType.Category.SOURCE, list4.get(2));

        List<GeneratedFile> list5 = new ArrayList<>();
        codeGenerator.generateModelBaseFiles(list5, mocks.get(5), EMPTY);
        assertEquals(4, list5.size());
        assertGeneratedFile(SRC_1_PATH, NAME_PMML, GeneratedFileType.Category.SOURCE, list5.get(0));
        assertGeneratedFile(SRC_2_PATH, NAME_RULE, GeneratedFileType.Category.SOURCE, list5.get(1));
        assertGeneratedFile(SRC_3_PATH, NAME_PMML, GeneratedFileType.Category.SOURCE, list5.get(2));
        assertGeneratedFile(SRC_4_PATH, NAME_RULE, GeneratedFileType.Category.SOURCE, list5.get(3));
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void testGenerateModelRESTFilesMethodWorkingWithValidModel(KogitoBuildContext.Builder contextBuilder) {
        KogitoBuildContext context = contextBuilder.build();
        PredictionCodegen codeGenerator = PredictionCodegen.ofCollectedResources(context, Collections.emptyList());

        for (KiePMMLModel mock : buildValidMocks()) {
            List<GeneratedFile> list = new ArrayList<>();
            codeGenerator.generateModelRESTFiles(list, mock);

            if (context.hasREST()) {
                assertEquals(2, list.size());
                assertGeneratedFile(REST_PATH, NAME_REST, GeneratedFileType.Category.SOURCE, list.get(0));
                assertGeneratedFile(RESOURCE_PATH, NAME_RESOURCE, GeneratedFileType.Category.RESOURCE, list.get(1));
            } else {
                assertEquals(0, list.size());
            }
        }
    }

    private static void assertEndpoints(KogitoBuildContext context, Collection<GeneratedFile> generatedFiles, int expectedRestEndpoints) {
        if (context.hasREST()) {
            // REST resource
            assertEquals(expectedRestEndpoints, generatedFiles.stream()
                    .filter(generatedFile -> generatedFile.type().equals(REST_TYPE))
                    .count());
            // OpenAPI Json schema
            assertEquals(expectedRestEndpoints, generatedFiles.stream()
                    .filter(generatedFile -> generatedFile.category().equals(GeneratedFileType.Category.RESOURCE) &&
                            generatedFile.type().name().equals(GeneratedFileType.RESOURCE.name()) &&
                            !generatedFile.relativePath().endsWith(REFLECT_JSON))
                    .count());
        }
    }

    private static void assertGeneratedFile(String expectedRelativePath, String expectedName, GeneratedFileType.Category expectedCategory, GeneratedFile actual) {
        assertEquals(expectedRelativePath, actual.relativePath());
        assertEquals(expectedName, actual.type().name());
        assertEquals(expectedCategory, actual.type().category());
    }

    private static KiePMMLModel buildInvalidMock(String name) {
        KiePMMLModel mock = mock(KiePMMLModel.class);
        when(mock.getName()).thenReturn(name);
        return mock;
    }

    private static List<KiePMMLModel> buildValidMocks() {
        Map<String, String> sourcesMap = new HashMap<>();
        sourcesMap.put(SRC_1_NAME, CODE);
        Map<String, String> ruleSourcesMap = new HashMap<>();
        ruleSourcesMap.put(SRC_2_NAME, CODE);
        Map<String, String> nestedSourcesMap = new HashMap<>();
        nestedSourcesMap.put(SRC_3_NAME, CODE);
        Map<String, String> nestedRuleSourcesMap = new HashMap<>();
        nestedRuleSourcesMap.put(SRC_4_NAME, CODE);

        List<KiePMMLModel> mocks = new ArrayList<>(6);

        // Model with sources
        KiePMMLModel mock0 = new KiePMMLModelWithSourcesMock(MOCK, sourcesMap);
        mocks.add(mock0);

        // Model with sources and rules
        KiePMMLModel mock1 = new KiePMMLModelWithSourcesMock(MOCK, sourcesMap, ruleSourcesMap);
        mocks.add(mock1);

        // Model with sources + nested model with sources
        KiePMMLModel nested2 = new KiePMMLModelWithSourcesMock(NESTED_MOCK, nestedSourcesMap);
        KiePMMLModel mock2 = new KiePMMLModelWithSourcesAndNestedModelsMock(MOCK, sourcesMap, nested2);
        mocks.add(mock2);

        // Model with sources + nested model with sources and rules
        KiePMMLModel nested3 = new KiePMMLModelWithSourcesMock(NESTED_MOCK, nestedSourcesMap, nestedRuleSourcesMap);
        KiePMMLModel mock3 = new KiePMMLModelWithSourcesAndNestedModelsMock(MOCK, sourcesMap, nested3);
        mocks.add(mock3);

        // Model with sources and rules + nested model with sources
        KiePMMLModel nested4 = new KiePMMLModelWithSourcesMock(NESTED_MOCK, nestedSourcesMap);
        KiePMMLModel mock4 = new KiePMMLModelWithSourcesAndNestedModelsMock(MOCK, sourcesMap, ruleSourcesMap, nested4);
        mocks.add(mock4);

        // Model with sources and rules + nested model with sources and rules
        KiePMMLModel nested5 = new KiePMMLModelWithSourcesMock(NESTED_MOCK, nestedSourcesMap, nestedRuleSourcesMap);
        KiePMMLModel mock5 = new KiePMMLModelWithSourcesAndNestedModelsMock(MOCK, sourcesMap, ruleSourcesMap, nested5);
        mocks.add(mock5);

        return mocks;
    }

    private static String nameToPath(String name, char separator, String extension) {
        return name.replace(separator, File.separatorChar) + "." + extension;
    }
}
