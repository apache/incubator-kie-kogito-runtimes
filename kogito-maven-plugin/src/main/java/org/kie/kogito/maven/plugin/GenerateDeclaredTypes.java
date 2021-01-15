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

import java.io.IOException;
import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.kie.kogito.codegen.ApplicationGenerator;
import org.kie.kogito.codegen.GeneratedFile;
import org.kie.kogito.codegen.context.KogitoBuildContext;
import org.kie.kogito.codegen.rules.DeclaredTypeCodegen;

@Mojo(name = "generateDeclaredTypes",
      requiresDependencyResolution = ResolutionScope.NONE,
      requiresProject = true,
      defaultPhase = LifecyclePhase.GENERATE_SOURCES,
      threadSafe = true)
public class GenerateDeclaredTypes extends AbstractKieMojo {

    @Parameter(property = "kogito.sources.keep", defaultValue = "false")
    private boolean keepSources;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            generateModel();
        } catch (IOException e) {
            throw new MojoExecutionException("An I/O error occurred", e);
        }
    }

    private void generateModel() throws MojoExecutionException, IOException {
        project.addCompileSourceRoot(generatedSources.getPath());

        setSystemProperties(properties);

        ApplicationGenerator appGen = createApplicationGenerator();

        Collection<GeneratedFile> generatedFiles = appGen.generateComponents();

        writeGeneratedFiles(generatedFiles);
    }

    private ApplicationGenerator createApplicationGenerator() throws MojoExecutionException {
        KogitoBuildContext context = discoverKogitoRuntimeContext(projectClassLoader());

        ApplicationGenerator appGen = new ApplicationGenerator(context);

        appGen.registerGeneratorIfEnabled(DeclaredTypeCodegen.fromContext(context));
        
        return appGen;
    }
}
