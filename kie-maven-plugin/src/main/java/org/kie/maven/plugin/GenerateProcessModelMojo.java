/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.kie.submarine.codegen.GeneratedFile;
import org.kie.submarine.codegen.process.ProcessCodegen;

@Mojo(name = "generateProcessModel",
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class GenerateProcessModelMojo extends AbstractKieMojo {

    public static final String BOOTSTRAP_PACKAGE = "org.kie.bootstrap.process";

    @Parameter(required = true, defaultValue = "${project.basedir}/src")
    private File sourceDir;

    @Parameter(required = true, defaultValue = "${project.build.directory}")
    private File targetDirectory;

    @Parameter(required = true, defaultValue = "${project.basedir}")
    private File projectDir;

    @Parameter
    private Map<String, String> properties;

    @Parameter(required = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(property = "generateProcessModel", defaultValue = "yes")
    private String generateProcessModel;

    @Parameter(property = "dependencyInjection", defaultValue = "true")
    private boolean dependencyInjection;

    private final String additionalCompilerPath = "/generated-sources/process/main/java";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (BPMNModelMode.shouldGenerateBPMNModel(generateProcessModel)) {
            generateProcessModel();
        }
    }

    private void generateProcessModel() throws MojoExecutionException {
        project.addCompileSourceRoot(
                Paths.get(
                        targetDirectory.getPath(),
                        additionalCompilerPath).toString());

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            setSystemProperties(properties);
            ProcessCodegen generator =
                    ProcessCodegen.ofPath(sourceDir.toPath())
                            .withPackageName(project.getGroupId())
                            .withDependencyInjection(dependencyInjection)
                            .checkWorkItemHandlerConfig(sourceDir.getPath());

            List<GeneratedFile> generatedFiles = generator.generate();
            for (GeneratedFile f : generatedFiles) {
                Files.write(
                        pathOf(f.relativePath()),
                        f.contents());
            }

            getLog().info("Process Model successfully generated");

            writeLabelsImageMetadata(targetDirectory.toString(), generator.getLabels());
        } catch (IOException e) {
            throw new MojoExecutionException("An error was caught during process generation", e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private List<File> getBPMNFiles() throws IOException {
        return Files.walk(sourceDir.toPath())
                .filter(p -> p.toString().endsWith(".bpmn") || p.toString().endsWith(".bpmn2"))
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    private Path pathOf(String end) {
        Path path = Paths.get(targetDirectory.getPath(), additionalCompilerPath, end);
        path.getParent().toFile().mkdirs();
        return path;
    }
}

