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

package org.kie.kogito.maven.plugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.kie.kogito.codegen.CodegenUtils;
import org.kie.kogito.codegen.GeneratedFile;
import org.kie.kogito.codegen.JsonSchemaGenerator;
import org.kie.kogito.maven.plugin.util.MojoUtil;


@Mojo(name = "generate-json-schema",
      requiresDependencyResolution = ResolutionScope.COMPILE,
      requiresProject = true,
      defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class GenerateJsonSchemaMojo extends AbstractKieMojo {

    @Parameter(required = true, defaultValue = "${project}")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Path path = Paths.get(project.getBuild().getOutputDirectory());
            ClassLoader cl = MojoUtil
                                     .createProjectClassLoader(this
                                                                   .getClass()
                                                                   .getClassLoader(),
                                                               project,
                                                               path.toFile(),
                                                               null);
            Collection<GeneratedFile> files;
            try (Stream<Class<?>> stream = CodegenUtils.getGeneratedClassesStream(path, cl)) {
                files = new JsonSchemaGenerator(stream).generate();
            }
            Path parentPath = path.resolve("META-INF").resolve("jsonSchema");
            Files.createDirectories(parentPath);
            for (GeneratedFile file : files) {
                if (getLog().isInfoEnabled()) {
                    getLog().info("Creating JSON schema file " + file.relativePath());
                }
                Files.write(parentPath.resolve(file.relativePath()), file.contents());
            }
        } catch (Exception e) {
            // since the json schema generation step is "optional" MojoFailureException (which can be ignored using -fn), is thrown for any error type
            throw new MojoFailureException("Error generating JSON Schema  for tasks", e);
        }
    }
}
