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
package org.kie.kogito.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.kie.kogito.gradle.plugin.GenerateModelTask.GENERATE_MODEL_TASK_NAME;
import static org.kie.kogito.gradle.plugin.KogitoGradlePlugin.PLUGIN_ID;

class GenerateModelTaskTest {

    private Project project;
    private GenerateModelTask generateModelTask;

    @BeforeEach
    void setup() {
        // Create an in-memory project and apply the plugin
        project = ProjectBuilder.builder().build();
        project.getRepositories().mavenCentral();
        project.getRepositories().mavenLocal();
        Configuration compileConfiguration = project.getConfigurations().create("compileClasspath");
        Dependency compileDependency = project.getDependencies().create("org.kie.kogito:kogito-codegen-api:999-SNAPSHOT");
        compileConfiguration.getDependencies().add(compileDependency);
        Configuration runtimeConfigureation = project.getConfigurations().create("runtimeClasspath");
        Dependency runtimeDependency = project.getDependencies().create("org.webjars:bootstrap:4.5.3");
        runtimeConfigureation.getDependencies().add(runtimeDependency);
        project.getPlugins().apply(PLUGIN_ID);
        generateModelTask = (GenerateModelTask) project.getTasks().findByName(GENERATE_MODEL_TASK_NAME);
    }

    @Test
    void run() {
        assertThatNoException().isThrownBy(() -> generateModelTask.run());
    }

}   