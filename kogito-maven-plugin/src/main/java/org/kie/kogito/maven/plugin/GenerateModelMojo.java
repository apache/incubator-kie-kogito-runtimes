/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kie.kogito.maven.plugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.drools.compiler.builder.impl.KogitoKieModuleModelImpl;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.kogito.codegen.AddonsConfig;
import org.kie.kogito.codegen.ApplicationGenerator;
import org.kie.kogito.codegen.GeneratedFile;
import org.kie.kogito.codegen.DashboardGeneratedFileUtils;
import org.kie.kogito.codegen.context.KogitoBuildContext;
import org.kie.kogito.codegen.decision.DecisionCodegen;
import org.kie.kogito.codegen.io.CollectedResource;
import org.kie.kogito.codegen.prediction.PredictionCodegen;
import org.kie.kogito.codegen.process.ProcessCodegen;
import org.kie.kogito.codegen.rules.IncrementalRuleCodegen;
import org.kie.kogito.maven.plugin.util.MojoUtil;

@Mojo(name = "generateModel",
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE,
        threadSafe = true)
public class GenerateModelMojo extends AbstractKieMojo {

    public static final List<String> DROOLS_EXTENSIONS = Arrays.asList(".drl", ".xls", ".xlsx", ".csv");

    public static final PathMatcher drlFileMatcher = FileSystems.getDefault().getPathMatcher("glob:**.drl");

    @Parameter(property = "kogito.codegen.sources.directory", defaultValue = "${project.build.directory}/generated-sources/kogito")
    private File customizableSourcesPath;

    // due to a limitation of the injector, the following 2 params have to be Strings
    // otherwise we cannot get the default value to null
    // when the value is null, the semantics is to enable the corresponding
    // codegen backend only if at least one file of the given type exist

    @Parameter(property = "kogito.codegen.rules", defaultValue = "")
    private String generateRules; // defaults to true iff there exist DRL files

    @Parameter(property = "kogito.codegen.processes", defaultValue = "")
    private String generateProcesses; // defaults to true iff there exist BPMN files

    @Parameter(property = "kogito.codegen.decisions", defaultValue = "")
    private String generateDecisions; // defaults to true iff there exist DMN files

    @Parameter(property = "kogito.codegen.predictions", defaultValue = "")
    private String generatePredictions; // defaults to true iff there exist PMML files

    /**
     * Partial generation can be used when reprocessing a pre-compiled project
     * for faster code-generation. It only generates code for rules and processes,
     * and does not generate extra meta-classes (etc. Application).
     * Use only when doing recompilation and for development purposes
     */
    @Parameter(property = "kogito.codegen.partial", defaultValue = "false")
    private boolean generatePartial;

    @Parameter(property = "kogito.codegen.ondemand", defaultValue = "false")
    private boolean onDemand;

    @Parameter(property = "kogito.sources.keep", defaultValue = "false")
    private boolean keepSources;

    @Parameter(property = "kogito.persistence.enabled", defaultValue = "false")
    private boolean persistence;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            addCompileSourceRoots();
            if (isOnDemand()) {
                getLog().info("On-Demand Mode is On. Use mvn compile kogito:scaffold");
            } else {
                generateModel();
            }
        } catch (IOException e) {
            throw new MojoExecutionException("An I/O error occurred", e);
        }
    }

    protected boolean isOnDemand() {
        return onDemand;
    }

    @Override
    protected File getSourcesPath() {
        return customizableSourcesPath;
    }

    protected void addCompileSourceRoots() {
        project.addCompileSourceRoot(getSourcesPath().getPath());
        project.addCompileSourceRoot(generatedSources.getPath());
    }

    protected void generateModel() throws MojoExecutionException, IOException {

        setSystemProperties(properties);

        ApplicationGenerator appGen = createApplicationGenerator();

        Collection<GeneratedFile> generatedFiles;
        if (generatePartial) {
            generatedFiles = appGen.generateComponents();
        } else {
            generatedFiles = appGen.generate();
        }

        Optional<GeneratedFile> dashboardsListFile = DashboardGeneratedFileUtils.list(generatedFiles);
        dashboardsListFile.ifPresent(generatedFiles::add);

        writeGeneratedFiles(generatedFiles);

        if (!keepSources) {
            deleteDrlFiles();
        }
    }

    private boolean generateDecisions() throws IOException {
        return generateDecisions == null ? decisionsExist() : Boolean.parseBoolean(generateDecisions);
    }

    private boolean generatePredictions() throws IOException {
        return generatePredictions == null ? predictionsExist() : Boolean.parseBoolean(generatePredictions);
    }

    private boolean generateRules() throws IOException {
        return generateRules == null ? rulesExist() : Boolean.parseBoolean(generateRules);
    }

    private boolean generateProcesses() throws IOException {
        return generateProcesses == null ? processesExist() : Boolean.parseBoolean(generateProcesses);
    }

    private boolean decisionsExist() throws IOException {
        try (final Stream<Path> paths = Files.walk(projectDir.toPath())) {
            return paths.map(p -> p.toString().toLowerCase()).anyMatch(p -> p.endsWith(".dmn"));
        }
    }

    private boolean predictionsExist() throws IOException {
        try (final Stream<Path> paths = Files.walk(projectDir.toPath())) {
            return paths.map(p -> p.toString().toLowerCase()).anyMatch(p -> p.endsWith(".pmml"));
        }
    }

    private boolean processesExist() throws IOException {
        try (final Stream<Path> paths = Files.walk(projectDir.toPath())) {
            return paths.map(p -> p.toString().toLowerCase())
                    .anyMatch(p -> p.endsWith(".bpmn") || p.endsWith(".bpmn2") || p.endsWith(".sw.json") || p.endsWith(".sw.yml"));
        }
    }

    private boolean rulesExist() throws IOException {
        try (final Stream<Path> paths = Files.walk(projectDir.toPath())) {
            return paths.map(p -> p.toString().toLowerCase())
                    .map(p -> {
                        int dot = p.lastIndexOf('.');
                        return dot > 0 ? p.substring(dot) : "";
                    })
                    .anyMatch(DROOLS_EXTENSIONS::contains);
        }
    }

    private ApplicationGenerator createApplicationGenerator() throws IOException, MojoExecutionException {
        String appPackageName = project.getGroupId();

        // safe guard to not generate application classes that would clash with interfaces
        if (appPackageName.equals(ApplicationGenerator.DEFAULT_GROUP_ID)) {
            appPackageName = KogitoBuildContext.DEFAULT_PACKAGE_NAME;
        }


        ClassLoader projectClassLoader = MojoUtil.createProjectClassLoader(this.getClass().getClassLoader(),
                                                                           project,
                                                                           outputDirectory,
                                                                           null);

        AddonsConfig addonsConfig = loadAddonsConfig(persistence, project);

        KogitoBuildContext context = discoverKogitoRuntimeContext(project)
                .withApplicationProperties(kieSourcesDirectory)
                .withPackageName(appPackageName)
                .withTargetDirectory(targetDirectory)
                .withAddonsConfig(addonsConfig)
                .build();

        ApplicationGenerator appGen =
                new ApplicationGenerator(context)
                        .withClassLoader(projectClassLoader);

        // if unspecified, then default to checking for file type existence
        // if not null, the property has been overridden, and we should use the specified value

        if (generateProcesses()) {
            appGen.setupGenerator(ProcessCodegen.ofCollectedResources(context, CollectedResource.fromDirectory(kieSourcesDirectory.toPath())))
                    .withClassLoader(projectClassLoader);
        }

        if (generateRules()) {
            boolean useRestServices = hasClassOnClasspath(project, "javax.ws.rs.Path")
                    || hasClassOnClasspath(project, "org.springframework.web.bind.annotation.RestController");
            appGen.setupGenerator(IncrementalRuleCodegen.ofCollectedResources(context, CollectedResource.fromDirectory(kieSourcesDirectory.toPath())))
                    .withKModule(getKModuleModel())
                    .withClassLoader(projectClassLoader)
                    .withRestServices(useRestServices);
        }

        boolean isJPMMLAvailable = hasClassOnClasspath(project, "org.kie.dmn.jpmml.DMNjPMMLInvocationEvaluator");
        if(generatePredictions()) {
            appGen.setupGenerator(PredictionCodegen.ofCollectedResources(context, isJPMMLAvailable, CollectedResource.fromDirectory(kieSourcesDirectory.toPath())));
        }

        if (generateDecisions()) {
            appGen.setupGenerator(DecisionCodegen.ofCollectedResources(context, CollectedResource.fromDirectory(kieSourcesDirectory.toPath())))
                    .withClassLoader(projectClassLoader)
                    .withPCLResolverFn(x -> hasClassOnClasspath(project, x));
        }

        return appGen;
    }

    private KieModuleModel getKModuleModel() throws IOException {
        if (!project.getResources().isEmpty()) {
            Path moduleXmlPath = Paths.get(project.getResources().get(0).getDirectory()).resolve(KieModuleModelImpl.KMODULE_JAR_PATH);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(Files.readAllBytes(moduleXmlPath))) {
                return KogitoKieModuleModelImpl.fromXML(bais);
            } catch (NoSuchFileException e) {
                getLog().debug("kmodule.xml is missing. Returned the default value.", e);
                return new KogitoKieModuleModelImpl();
            }
        } else {
            getLog().debug("kmodule.xml is missing. Returned the default value.");
            return new KogitoKieModuleModelImpl();
        }
    }

    private void deleteDrlFiles() throws MojoExecutionException {
        // Remove drl files
        try (final Stream<Path> drlFiles = Files.find(outputDirectory.toPath(), Integer.MAX_VALUE,
                                                      (p, f) -> drlFileMatcher.matches(p))) {
            drlFiles.forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to find .drl files");
        }
    }
}
