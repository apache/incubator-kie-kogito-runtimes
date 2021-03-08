/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.tck.junit.extension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.kie.kogito.codegen.api.AddonsConfig;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.codegen.core.ApplicationGenerator;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResourceType;
import org.kie.memorycompiler.CompilationProblem;
import org.kie.memorycompiler.CompilationResult;
import org.kie.memorycompiler.JavaCompiler;
import org.kie.memorycompiler.JavaCompilerFactory;
import org.kie.memorycompiler.JavaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KogitoInMemoryCompiler {
    private static final JavaCompiler JAVA_COMPILER = JavaCompilerFactory.loadCompiler(JavaConfiguration.CompilerType.NATIVE, "8");

    private static final Logger logger = LoggerFactory.getLogger(KogitoUnitTestDeploymentExtension.class);

    private static final String TEST_JAVA = "src/test/java/";
    private static final String TEST_RESOURCES = "src/test/resources";

    private static final String DUMMY_PROCESS_RUNTIME =
            "package org.drools.project.model;\n" +
                    "\n" +
                    "import org.kie.api.KieBase;\n" +
                    "import org.kie.api.builder.model.KieBaseModel;\n" +
                    "import org.kie.api.runtime.KieSession;\n" +
                    "import org.drools.modelcompiler.builder.KieBaseBuilder;\n" +
                    "\n" +
                    "\n" +
                    "public class ProjectRuntime implements org.kie.kogito.rules.KieRuntimeBuilder {\n" +
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

    private CodeGenerationRegistry codeGenerationRegistry;
    private AddonsConfig addonsConfig;



    
    public KogitoInMemoryCompiler() {
        addonsConfig = AddonsConfig.DEFAULT;
        codeGenerationRegistry = new CodeGenerationRegistry(TEST_JAVA, TEST_RESOURCES);
    }

    protected Map<String, byte[]> generateCode(String packageName, Map<KogitoUnitTestResourceType, List<String>> resourcesTypeMap) throws Exception {
        KogitoBuildContext context = JavaKogitoBuildContext.builder()
                .withApplicationProperties(new File(TEST_RESOURCES))
                .withPackageName(packageName)
                .withAddonsConfig(addonsConfig)
                .build();

        ApplicationGenerator appGen = new ApplicationGenerator(context);

        for (KogitoUnitTestResourceType type : KogitoUnitTestResourceType.values()) {
            if (resourcesTypeMap.containsKey(type) && !resourcesTypeMap.get(type).isEmpty()) {
                appGen.registerGeneratorIfEnabled(codeGenerationRegistry.get(type).apply(context, resourcesTypeMap.get(type)));
            }
        }

        Collection<GeneratedFile> generatedFiles = appGen.generate();

        MemoryFileSystem srcMfs = new MemoryFileSystem();
        MemoryFileSystem trgMfs = new MemoryFileSystem();

        List<String> sources = new ArrayList<>();
        for (GeneratedFile entry : generatedFiles) {
            String fileName = entry.relativePath();
            if (!fileName.endsWith(".java")) {
                continue;
            }
            sources.add(fileName);
            srcMfs.write(fileName, entry.contents());
            logger.debug(new String(entry.contents()));
        }

        if (resourcesTypeMap.containsKey(KogitoUnitTestResourceType.PROCESS)) {
            sources.add("org/drools/project/model/ProjectRuntime.java");
            srcMfs.write("org/drools/project/model/ProjectRuntime.java", DUMMY_PROCESS_RUNTIME.getBytes());
        }


        if (logger.isDebugEnabled()) {
            Path temp = Files.createTempDirectory("KOGITO_TESTS");
            logger.debug("Dumping generated files in " + temp);
            for (GeneratedFile entry : generatedFiles) {
                Path fpath = temp.resolve(entry.relativePath());
                fpath.getParent().toFile().mkdirs();
                Files.write(fpath, entry.contents());
            }
        }

        CompilationResult result = JAVA_COMPILER.compile(sources.toArray(new String[sources.size()]), srcMfs, trgMfs, this.getClass().getClassLoader());
        if(result.getErrors().length > 0) {
            for(CompilationProblem problem : result.getErrors()) {
                logger.error(problem.getMessage());
            }
            throw new RuntimeException("compilation errors");
        }

        return trgMfs.getMap();
    }
}
