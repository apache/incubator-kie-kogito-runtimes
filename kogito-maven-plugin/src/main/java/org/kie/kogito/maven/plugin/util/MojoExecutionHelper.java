/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.maven.plugin.util;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

/**
 * Class responsible of Mojo invocations
 */
public class MojoExecutionHelper {

    private MojoExecutionHelper() {
    }

    static final String ORG_APACHE_MAVEN_PLUGINS = "org.apache.maven.plugins";
    static final String MAVEN_COMPILER_PLUGIN = "maven-compiler-plugin";
    static final String COMPILE_GOAL = "compile";

    static final String ORG_KIE_KOGITO = "org.kie.kogito";
    static final String KOGITO_MAVEN_PLUGIN = "kogito-maven-plugin";
    static final String PROCESS_CLASSES_GOAL = "process-model-classes";

    public static void compile(final MavenProject mavenProject,
            final MavenSession mavenSession,
            final BuildPluginManager pluginManager,
            final String compilerPluginVersion,
            final Log log) throws MojoExecutionException {
        commonExecution(mavenProject, mavenSession, pluginManager, ORG_APACHE_MAVEN_PLUGINS, MAVEN_COMPILER_PLUGIN, compilerPluginVersion, COMPILE_GOAL, log);
    }

    public static void processClasses(final MavenProject mavenProject,
            final MavenSession mavenSession,
            final BuildPluginManager pluginManager,
            final String kogitoMavenPluginVersion,
            final Log log) throws MojoExecutionException {
        commonExecution(mavenProject, mavenSession, pluginManager, ORG_KIE_KOGITO, KOGITO_MAVEN_PLUGIN, kogitoMavenPluginVersion, PROCESS_CLASSES_GOAL, log);
    }

    static void commonExecution(final MavenProject mavenProject,
            final MavenSession mavenSession,
            final BuildPluginManager pluginManager,
            final String groupId,
            final String artifactId,
            final String versionId,
            final String goal,
            final Log log) throws MojoExecutionException {
        log.info(String.format("Invoking %s:%s:%s", groupId, artifactId, PROCESS_CLASSES_GOAL));
        executeMojo(plugin(groupId(groupId), artifactId(artifactId), version(versionId)),
                goal(goal), configuration(),
                executionEnvironment(mavenProject, mavenSession, pluginManager));
    }

}
