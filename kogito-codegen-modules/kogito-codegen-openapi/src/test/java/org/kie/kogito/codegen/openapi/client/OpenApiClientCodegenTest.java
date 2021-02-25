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
package org.kie.kogito.codegen.openapi.client;

import java.util.Collection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.codegen.api.utils.CollectedResourcesUtils.toCollectedResources;

class OpenApiClientCodegenTest {

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void verifyHTTPResource(final KogitoBuildContext.Builder contextBuilder) {
        final String workflowDefinitionFile = "/sendcloudeventonprovision.sw.json";
        final KogitoBuildContext context = contextBuilder.build();
        final OpenApiClientCodegen codegen =
                OpenApiClientCodegen.ofCollectedResources(context,
                        toCollectedResources(workflowDefinitionFile));
        assertThat(codegen).isNotNull();
        assertThat(codegen.getOpenAPISpecResources()).isNotEmpty();
        assertThat(codegen.getOpenAPISpecResources()).hasSize(1);
        assertThat(codegen.getOpenAPISpecResources().get(0).getResourceName()).isEqualTo("provisioning.json");
        assertThat(codegen.getOpenAPISpecResources().get(0).getId()).contains("provisioning");
        assertThat(codegen.getOpenAPISpecResources().get(0).getURI().getScheme()).isEqualTo("http");
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void verifyLocalMultipleResources(final KogitoBuildContext.Builder contextBuilder) {
        final String workflowDefinitionFile = "/sendcloudeventonprovision2.sw.json";
        final KogitoBuildContext context = contextBuilder.build();
        final OpenApiClientCodegen codegen =
                OpenApiClientCodegen.ofCollectedResources(context,
                        toCollectedResources(workflowDefinitionFile));
        assertThat(codegen).isNotNull();
        assertThat(codegen.getOpenAPISpecResources()).isNotEmpty();
        assertThat(codegen.getOpenAPISpecResources()).hasSize(1);
        assertThat(codegen.getOpenAPISpecResources().get(0).getResourceName()).isEqualTo("provisioning.json");
        assertThat(codegen.getOpenAPISpecResources().get(0).getId()).contains("provisioning");
        assertThat(codegen.getOpenAPISpecResources().get(0).getURI().getScheme()).isNull();
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    void verifyCodegenClasspath(final KogitoBuildContext.Builder contextBuilder) {
        final String workflowDefinitionFile = "/petstore-classpath.sw.json";
        final KogitoBuildContext context = contextBuilder.build();
        final OpenApiClientCodegen codegen =
                OpenApiClientCodegen.ofCollectedResources(context,
                        toCollectedResources(workflowDefinitionFile));
        assertCodeGen(codegen.generate());
        assertThat(codegen.getOpenAPISpecResources()).hasSize(1);
        assertThat(codegen.getOpenAPISpecResources().get(0).getRequiredOperations()).hasSize(2);
        assertThat(codegen.getOpenAPISpecResources().get(0).getRequiredOperations()
                .stream()
                .anyMatch(o -> o.getOperationId().equals("getInventory") &&
                        o.getParameters().size() == 0 &&
                        o.getGeneratedClass().endsWith("StoreApi"))).isTrue();
    }

    private void assertCodeGen(final Collection<GeneratedFile> generatedFiles) {
        assertThat(generatedFiles).isNotEmpty();
        boolean containsApiClient = false;
        for (GeneratedFile file : generatedFiles) {
            assertThat(file.relativePath()).endsWith(".java");
            if (file.relativePath().endsWith("ApiClient.java")) {
                containsApiClient = true;
            }
        }
        assertThat(containsApiClient).isTrue();
    }
}