/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.maven.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.kie.kogito.KogitoDrools;
import org.kie.kogito.KogitoGAV;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.Generator;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.QuarkusKogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.SpringBootKogitoBuildContext;
import org.kie.kogito.codegen.api.utils.AddonsConfigDiscovery;
import org.kie.kogito.codegen.api.utils.AppPaths;
import org.kie.kogito.codegen.core.utils.GeneratedFileWriter;
import org.kie.kogito.codegen.decision.DecisionCodegen;
import org.kie.kogito.codegen.prediction.PredictionCodegen;
import org.kie.kogito.codegen.process.ProcessCodegen;
import org.kie.kogito.codegen.process.persistence.PersistenceGenerator;
import org.kie.kogito.codegen.rules.IncrementalRuleCodegen;
import org.kie.kogito.dmn.KogitoDecisions;
import org.kie.kogito.maven.plugin.util.MojoUtil;
import org.kie.kogito.pmml.KogitoPredictions;
import org.kie.kogito.process.KogitoProcesses;
import org.kie.kogito.serverless.workflow.KogitoServerlessWorkflow;

public abstract class AbstractKieMojo extends AbstractMojo {

    protected static final Map<String, Collection<String>> engineToRuntimeClass = new HashMap<>();

    static {
        engineToRuntimeClass.put(IncrementalRuleCodegen.GENERATOR_NAME,
                Collections.singletonList(KogitoDrools.class.getCanonicalName()));
        engineToRuntimeClass.put(ProcessCodegen.GENERATOR_NAME,
                Arrays.asList(KogitoProcesses.class.getCanonicalName(), KogitoServerlessWorkflow.class.getCanonicalName()));
        engineToRuntimeClass.put(PredictionCodegen.GENERATOR_NAME,
                Collections.singletonList(KogitoPredictions.class.getCanonicalName()));
        engineToRuntimeClass.put(DecisionCodegen.GENERATOR_NAME,
                Collections.singletonList(KogitoDecisions.class.getCanonicalName()));
    }

    @Parameter(required = true, defaultValue = "${project.basedir}")
    protected File projectDir;

    @Parameter
    protected Map<String, String> properties;

    @Parameter(required = true, defaultValue = "${project}")
    protected MavenProject project;

    @Parameter(required = true, defaultValue = "${project.build.outputDirectory}")
    protected File outputDirectory;

    @Parameter(defaultValue = "${project.build.directory}/" + GeneratedFileWriter.DEFAULT_SOURCES_DIR)
    protected File generatedSources;

    @Parameter(defaultValue = "${project.build.directory}/" + GeneratedFileWriter.DEFAULT_RESOURCE_PATH)
    protected File generatedResources;

    @Parameter(property = "kogito.codegen.persistence", defaultValue = "true")
    protected String persistence;

    @Parameter(property = "kogito.codegen.rules", defaultValue = "true")
    protected String generateRules;

    @Parameter(property = "kogito.codegen.processes", defaultValue = "true")
    protected String generateProcesses;

    @Parameter(property = "kogito.codegen.decisions", defaultValue = "true")
    protected String generateDecisions;

    @Parameter(property = "kogito.codegen.predictions", defaultValue = "true")
    protected String generatePredictions;

    protected void setSystemProperties(Map<String, String> properties) {

        if (properties != null) {
            getLog().debug("Additional system properties: " + properties);
            for (Map.Entry<String, String> property : properties.entrySet()) {
                System.setProperty(property.getKey(), property.getValue());
            }
            getLog().debug("Configured system properties were successfully set.");
        }
    }

    protected KogitoBuildContext discoverKogitoRuntimeContext(ClassLoader classLoader) {
        AppPaths appPaths = AppPaths.fromProjectDir(projectDir.toPath());
        KogitoBuildContext context = contextBuilder()
                .withClassAvailabilityResolver(this::hasClassOnClasspath)
                .withApplicationProperties(appPaths.getResourceFiles())
                .withPackageName(appPackageName())
                .withAddonsConfig(AddonsConfigDiscovery.discover(this::hasClassOnClasspath))
                .withClassLoader(classLoader)
                .withAppPaths(appPaths)
                .withGAV(new KogitoGAV(project.getGroupId(), project.getArtifactId(), project.getVersion()))
                .build();

        additionalProperties(context);
        return context;
    }

    protected ClassLoader projectClassLoader() throws MojoExecutionException {
        return MojoUtil.createProjectClassLoader(this.getClass().getClassLoader(),
                project,
                outputDirectory,
                null);
    }

    protected String appPackageName() {
        return KogitoBuildContext.DEFAULT_PACKAGE_NAME;
    }

    private void additionalProperties(KogitoBuildContext context) {

        classToCheckForREST().ifPresent(restClass -> {
            if (!context.hasClassAvailable(restClass)) {
                getLog().info("Disabling REST generation because class '" + restClass + "' is not available");
                context.setApplicationProperty(KogitoBuildContext.KOGITO_GENERATE_REST, "false");
            }
        });
        classToCheckForDI().ifPresent(diClass -> {
            if (!context.hasClassAvailable(diClass)) {
                getLog().info("Disabling dependency injection generation because class '" + diClass + "' is not available");
                context.setApplicationProperty(KogitoBuildContext.KOGITO_GENERATE_DI, "false");
            }
        });

        disableGeneratorIfNecessary(context, IncrementalRuleCodegen.GENERATOR_NAME, generateRules);
        disableGeneratorIfNecessary(context, ProcessCodegen.GENERATOR_NAME, generateProcesses);
        disableGeneratorIfNecessary(context, PredictionCodegen.GENERATOR_NAME, generateProcesses);
        disableGeneratorIfNecessary(context, DecisionCodegen.GENERATOR_NAME, generateProcesses);
        disableGeneratorIfNecessary(context, PersistenceGenerator.GENERATOR_NAME, generateProcesses);
    }

    /**
     * A generator can be disabled explicitly with a property or if the corresponding runtime marker class is not available
     */
    private void disableGeneratorIfNecessary(KogitoBuildContext context, String generatorName, String enableProperty) {
        if ("false".equalsIgnoreCase(enableProperty) || !engineToRuntimeClass.containsKey(generatorName)) {
            context.setApplicationProperty(Generator.CONFIG_PREFIX + generatorName, enableProperty);
            return;
        }

        Collection<String> runtimeMarkerClasses = engineToRuntimeClass.get(generatorName);
        for (String runtimeMarkerClass : runtimeMarkerClasses) {
            if (context.hasClassAvailable(runtimeMarkerClass)) {
                context.setApplicationProperty(Generator.CONFIG_PREFIX + generatorName, "true");
                return;
            }
        }

        getLog().info(String.format("Disabling '%s' generator because runtime classes not available (%s)", generatorName, String.join(",", runtimeMarkerClasses)));
        context.setApplicationProperty(Generator.CONFIG_PREFIX + generatorName, "false");
    }

    private KogitoBuildContext.Builder contextBuilder() {
        switch (discoverFramework()) {
            case QUARKUS:
                return QuarkusKogitoBuildContext.builder();
            case SPRING:
                return SpringBootKogitoBuildContext.builder();
            default:
                return JavaKogitoBuildContext.builder();
        }
    }

    private Optional<String> classToCheckForREST() {
        switch (discoverFramework()) {
            case QUARKUS:
                return Optional.of(QuarkusKogitoBuildContext.QUARKUS_REST);
            case SPRING:
                return Optional.of(SpringBootKogitoBuildContext.SPRING_REST);
            default:
                return Optional.empty();
        }
    }

    private Optional<String> classToCheckForDI() {
        switch (discoverFramework()) {
            case QUARKUS:
                return Optional.of(QuarkusKogitoBuildContext.QUARKUS_DI);
            case SPRING:
                return Optional.of(SpringBootKogitoBuildContext.SPRING_DI);
            default:
                return Optional.empty();
        }
    }

    private enum Framework {
        QUARKUS,
        SPRING,
        NONE
    }

    private Framework discoverFramework() {
        if (hasDependency("quarkus")) {
            return Framework.QUARKUS;
        }

        if (hasDependency("spring")) {
            return Framework.SPRING;
        }

        return Framework.NONE;
    }

    private boolean hasDependency(String dependency) {
        return project.getDependencies().stream().anyMatch(d -> d.getArtifactId().contains(dependency));
    }

    private boolean hasClassOnClasspath(String className) {
        try {
            Set<Artifact> elements = project.getArtifacts();
            URL[] urls = new URL[elements.size()];

            int i = 0;
            Iterator<Artifact> it = elements.iterator();
            while (it.hasNext()) {
                Artifact artifact = it.next();

                urls[i] = artifact.getFile().toURI().toURL();
                i++;
            }
            try (URLClassLoader cl = new URLClassLoader(urls)) {
                cl.loadClass(className);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void writeGeneratedFiles(Collection<GeneratedFile> generatedFiles) {
        generatedFiles.forEach(this::writeGeneratedFile);
    }

    protected void writeGeneratedFile(GeneratedFile generatedFile) {
        GeneratedFileWriter writer = new GeneratedFileWriter(outputDirectory.toPath(),
                generatedSources.toPath(),
                generatedResources.toPath(),
                getSourcesPath().toPath());

        getLog().info("Generating: " + generatedFile.relativePath());
        writer.write(generatedFile);
    }

    protected File getSourcesPath() {
        return generatedSources;
    }
}
