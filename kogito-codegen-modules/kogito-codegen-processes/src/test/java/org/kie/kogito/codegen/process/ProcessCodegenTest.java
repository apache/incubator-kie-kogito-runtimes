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
package org.kie.kogito.codegen.process;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.drools.codegen.common.GeneratedFile;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.kogito.codegen.api.AddonsConfig;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.core.DashboardGeneratedFileUtils;
import org.kie.kogito.codegen.core.io.CollectedResourceProducer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.grafana.utils.GrafanaDashboardUtils.DISABLED_OPERATIONAL_DASHBOARDS;

class ProcessCodegenTest {

    private static final Path BASE_PATH = Paths.get("src/test/resources/").toAbsolutePath();
    private static final String MESSAGE_USERTASK_SOURCE = "usertask/UserTasksProcess.bpmn2";
    private static final Path MESSAGE_USERTASK_SOURCE_FULL_SOURCE = BASE_PATH.resolve(MESSAGE_USERTASK_SOURCE);

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    public void isEmpty(KogitoBuildContext.Builder contextBuilder) {
        KogitoBuildContext context = contextBuilder.build();
        ProcessCodegen emptyCodeGenerator = ProcessCodegen.ofCollectedResources(context, Collections.emptyList());

        assertThat(emptyCodeGenerator.isEmpty()).isTrue();
        assertThat(emptyCodeGenerator.isEnabled()).isFalse();

        Collection<GeneratedFile> emptyGeneratedFiles = emptyCodeGenerator.generate();
        assertThat(emptyGeneratedFiles).isEmpty();

        ProcessCodegen codeGenerator = ProcessCodegen.ofCollectedResources(
                context,
                CollectedResourceProducer.fromFiles(BASE_PATH, MESSAGE_USERTASK_SOURCE_FULL_SOURCE.toFile()));

        assertThat(codeGenerator.isEmpty()).isFalse();
        assertThat(codeGenerator.isEnabled()).isTrue();

        Collection<GeneratedFile> generatedFiles = codeGenerator.generate();
        assertThat(generatedFiles).hasSizeGreaterThanOrEqualTo(10);
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    public void whenMonitoringAndPrometheusEnabledGrafanaDashboardsAreGenerated(KogitoBuildContext.Builder contextBuilder) throws Exception {

        AddonsConfig addonsConfig = AddonsConfig.builder().withMonitoring(true).withPrometheusMonitoring(true).build();

        KogitoBuildContext context = contextBuilder
                .withAddonsConfig(addonsConfig)
                .build();
        ProcessCodegen codeGenerator = ProcessCodegen.ofCollectedResources(
                context,
                CollectedResourceProducer.fromFiles(BASE_PATH, MESSAGE_USERTASK_SOURCE_FULL_SOURCE.toFile()));

        generateTestDashboards(codeGenerator, 2);
    }

    @ParameterizedTest
    @MethodSource("org.kie.kogito.codegen.api.utils.KogitoContextTestUtils#contextBuilders")
    public void whenMonitoringAndPrometheusEnabledGrafanaDashboardsAreNotGenerated(KogitoBuildContext.Builder contextBuilder) throws Exception {

        AddonsConfig addonsConfig = AddonsConfig.builder().withMonitoring(true).withPrometheusMonitoring(true).build();

        KogitoBuildContext context = contextBuilder
                .withAddonsConfig(addonsConfig)
                .build();
        ProcessCodegen codeGenerator = ProcessCodegen.ofCollectedResources(
                context,
                CollectedResourceProducer.fromFiles(BASE_PATH, MESSAGE_USERTASK_SOURCE_FULL_SOURCE.toFile()));

        KogitoBuildContext build = contextBuilder.build();
        build.setApplicationProperty(DISABLED_OPERATIONAL_DASHBOARDS, "Global,UserTasksProcess");
        generateTestDashboards(codeGenerator, 0);
    }

    private List<GeneratedFile> generateTestDashboards(ProcessCodegen codeGenerator, int expectedDashboards) {

        Collection<GeneratedFile> generatedFiles = codeGenerator.generate();

        List<GeneratedFile> dashboards = generatedFiles.stream()
                .filter(x -> x.type().equals(DashboardGeneratedFileUtils.DASHBOARD_TYPE))
                .collect(Collectors.toList());

        assertThat(dashboards).hasSize(expectedDashboards);

        return dashboards;
    }
}
