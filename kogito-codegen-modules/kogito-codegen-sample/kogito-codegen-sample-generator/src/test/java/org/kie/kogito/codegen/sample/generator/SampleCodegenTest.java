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
package org.kie.kogito.codegen.sample.generator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.api.utils.CollectedResourcesTestUtils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.codegen.api.Generator.REST_TYPE;

class SampleCodegenTest {

    @Test
    void section() {
        SampleCodegen sampleCodegen = SampleCodegen.ofCollectedResources(JavaKogitoBuildContext.builder().build(), Collections.emptyList());
        assertThat(sampleCodegen.section())
                .isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void generate(KogitoBuildContext.Builder contextBuilder) {
        KogitoBuildContext context = contextBuilder.build();
        Collection<CollectedResource> resources = Arrays.asList(
                CollectedResourcesTestUtils.toCollectedResource("/sampleFile1.txt"),
                CollectedResourcesTestUtils.toCollectedResource("/sampleFile2.txt"));

        SampleCodegen codeGenerator = SampleCodegen.ofCollectedResources(context, resources);

        Collection<GeneratedFile> generatedFiles = codeGenerator.generate();

        if (contextBuilder.build().hasREST(codeGenerator)) {
            assertThat(generatedFiles).hasSize(1);
            List<GeneratedFile> generatedRests = generatedFiles.stream().filter(gf -> gf.type() == REST_TYPE).collect(Collectors.toList());
            assertThat(generatedRests).hasSize(1);

            CompilationUnit compilationUnit = StaticJavaParser.parse(new String(generatedRests.get(0).contents()));
            Optional<FieldDeclaration> optionalFieldDeclaration = compilationUnit.findFirst(FieldDeclaration.class, SampleCodegen::isSampleRuntimeField);

            assertThat(optionalFieldDeclaration).isNotEmpty();

            FieldDeclaration fieldDeclaration = optionalFieldDeclaration.get();

            if (context.hasDI()) {
                assertThat(fieldDeclaration.getAnnotations()).isNotEmpty();
            } else {
                assertThat(fieldDeclaration.getVariable(0).getInitializer()).isNotEmpty();
            }
        } else {
            assertThat(generatedFiles).isEmpty();
        }
    }

    @Test
    void configGenerator() {
        SampleCodegen sampleCodegen = SampleCodegen.ofCollectedResources(JavaKogitoBuildContext.builder().build(), Collections.emptyList());
        assertThat(sampleCodegen.configGenerator())
                .isNotEmpty();
    }
}
