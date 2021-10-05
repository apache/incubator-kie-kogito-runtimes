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

package org.kie.kogito.junit.deployment.spi.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.kie.kogito.codegen.api.AddonsConfig;
import org.kie.kogito.codegen.api.AddonsConfig.AddonsConfigBuilder;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.core.ApplicationGenerator;
import org.kie.kogito.codegen.core.io.CollectedResourceProducer;
import org.kie.kogito.junit.deployment.Deployment;
import org.kie.kogito.junit.deployment.DeploymentInstance;
import org.kie.kogito.junit.deployment.spi.DeploymentInstanceBuilder;
import org.kie.kogito.junit.deployment.spi.RuntimeBuildContextProvider;
import org.kie.kogito.junit.deployment.spi.RuntimeBuildGeneratorProvider;
import org.kie.kogito.junit.deployment.spi.RuntimeBuildPropertiesProvider;
import org.kie.kogito.junit.deployment.spi.RuntimeTestPersistenceProvider;
import org.kie.memorycompiler.CompilationProblem;
import org.kie.memorycompiler.CompilationResult;
import org.kie.memorycompiler.JavaCompiler;
import org.kie.memorycompiler.JavaCompilerFactory;
import org.kie.memorycompiler.JavaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDeploymentInstanceBuilder implements DeploymentInstanceBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDeploymentInstanceBuilder.class);

    private static final JavaCompiler JAVA_COMPILER = JavaCompilerFactory.loadCompiler(JavaConfiguration.CompilerType.NATIVE, "8");

    private static final String DUMMY_PROCESS_RUNTIME =
            "package org.drools.project.model;\n" +
                    "\n" +
                    "import org.kie.api.KieBase;\n" +
                    "import org.kie.api.builder.model.KieBaseModel;\n" +
                    "import org.kie.api.runtime.KieSession;\n" +
                    "import org.drools.modelcompiler.builder.KieBaseBuilder;\n" +
                    "\n" +
                    "\n" +
                    "public class ProjectRuntime implements org.kie.kogito.legacy.rules.KieRuntimeBuilder {\n" +
                    "\n" +
                    "    public static final ProjectRuntime INSTANCE = new ProjectRuntime();\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public KieBase getKieBase() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public KieBase getKieBase(String name) {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public KieSession newKieSession() {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public KieSession newKieSession(String sessionName) {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public KieSession newKieSession(String sessionName, org.kie.kogito.rules.RuleConfig ruleConfig) {\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "\n" +
                    "}";

    public DeploymentInstance build(Deployment deployment) {
        try {
            Properties properties = new Properties();
            ServiceLoader.load(RuntimeBuildPropertiesProvider.class).forEach(propertiesProvider -> propertiesProvider.provide(properties));

            AddonsConfigBuilder addonsConfigBuilder = AddonsConfig.builder();
            addonsConfigBuilder.withPersistence(properties.containsKey("kogito.persistence.type"));
            AddonsConfig addonsConfig = addonsConfigBuilder.build();

            RuntimeBuildContextProvider loaderBuildContext = ServiceLoader.load(RuntimeBuildContextProvider.class).findFirst().orElseThrow();
            KogitoBuildContext.Builder kogitoBuildContextBuilder = loaderBuildContext.newBuildContext();

            KogitoBuildContext context = kogitoBuildContextBuilder
                    .withApplicationProperties(properties)
                    .withPackageName(deployment.namespace())
                    .withAddonsConfig(addonsConfig)
                    .build();

            List<String> resourcesPath = deployment.getResources().stream().map(r -> r.getRelativePath()).collect(Collectors.toList());
            Collection<CollectedResource> resources = toCollectedResources("src/test/resources", resourcesPath);

            ApplicationGenerator appGen = new ApplicationGenerator(context);
            ServiceLoader.load(RuntimeBuildGeneratorProvider.class).forEach(generatorProvider -> {
                generatorProvider.buildCodegenGenerator(context, resources).ifPresent(gen -> appGen.registerGeneratorIfEnabled(gen));
            });

            Collection<GeneratedFile> generatedFiles = appGen.generate();
            MemoryFileSystem srcMfs = new MemoryFileSystem();
            MemoryFileSystem trgMfs = new MemoryFileSystem();

            List<String> sources = new ArrayList<>();
            for (GeneratedFile entry : generatedFiles) {
                String fileName = entry.relativePath();
                logger.trace("Entry: {}\n{}", entry.path(), new String(entry.contents()));
                trgMfs.write(fileName, entry.contents());
                if (fileName.endsWith(".java")) {
                    sources.add(fileName);
                    srcMfs.write(fileName, entry.contents());
                }
            }

            // maybe there is a way to get rid of this
            sources.add("org/drools/project/model/ProjectRuntime.java");
            srcMfs.write("org/drools/project/model/ProjectRuntime.java", DUMMY_PROCESS_RUNTIME.getBytes());

            CompilationResult result = JAVA_COMPILER.compile(sources.toArray(new String[sources.size()]), srcMfs, trgMfs, AbstractDeploymentInstanceBuilder.class.getClassLoader());
            if (result.getErrors().length > 0) {
                for (CompilationProblem problem : result.getErrors()) {
                    logger.error(problem.toString());
                }
                throw new RuntimeException("compilation errors");
            }

            Path buildPath = Files.createTempDirectory("KOGITO_TESTS");
            logger.debug("Created working directory {} for namespace {}", buildPath, deployment.namespace());
            for (String fileName : trgMfs.getFileNames()) {
                Path fpath = buildPath.resolve(fileName);
                fpath.getParent().toFile().mkdirs();
                Files.write(fpath, trgMfs.getBytes(fileName));
            }

            return createDeploymentInstance(deployment, buildPath);
        } catch (Exception e) {
            throw new RuntimeException("could not create deployment instance", e);
        }
    }

    public Optional<RuntimeTestPersistenceProvider> getRuntimeTestPersistenceProvider() {
        return (Optional<RuntimeTestPersistenceProvider>) ServiceLoader.load(RuntimeTestPersistenceProvider.class).findFirst();
    }

    protected abstract DeploymentInstance createDeploymentInstance(Deployment deployment, Path path) throws Exception;

    private Collection<CollectedResource> toCollectedResources(String basePath, List<String> strings) {

        File[] files = strings
                .stream()
                .map(resource -> new File(basePath, resource))
                .toArray(File[]::new);
        for (File file : files) {
            logger.debug("Resource {} collected for deployment {}", file, file.isFile());
        }
        return CollectedResourceProducer.fromFiles(Paths.get(basePath), files);
    }

}
