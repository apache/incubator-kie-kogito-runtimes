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

package org.kie.kogito.codegen;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.drools.compiler.commons.jci.compilers.CompilationResult;
import org.drools.compiler.commons.jci.compilers.JavaCompiler;
import org.drools.compiler.commons.jci.compilers.JavaCompilerFactory;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.rule.builder.dialect.java.JavaDialectConfiguration;
import org.kie.kogito.Application;
import org.kie.kogito.codegen.context.KogitoBuildContext;
import org.kie.kogito.codegen.context.QuarkusKogitoBuildContext;
import org.kie.kogito.codegen.context.SpringBootKogitoBuildContext;
import org.kie.kogito.codegen.decision.DecisionCodegen;
import org.kie.kogito.codegen.process.ProcessCodegen;
import org.kie.kogito.codegen.rules.IncrementalRuleCodegen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractCodegenTest {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractCodegenTest.class);
    
    private TestClassLoader classloader;

    private static final JavaCompiler JAVA_COMPILER = JavaCompilerFactory.INSTANCE.loadCompiler(JavaDialectConfiguration.CompilerType.NATIVE, "11");

    private boolean withSpringContext;

    public void withSpringContext(boolean withSpringContext){
        this.withSpringContext = withSpringContext;
    }

    protected Application generateCodeProcessesOnly(String... processes) throws Exception {
        return generateCode(Arrays.asList(processes), Collections.emptyList());
    }

    protected Application generateCodeRulesOnly(String... rules) throws Exception {
        return generateCode( Collections.emptyList(), Arrays.asList(rules), Collections.emptyList(), Collections.emptyList(), true );
    }

    protected Application generateRulesFromJava(String... javaSourceCode) throws Exception {
        return generateCode(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Arrays.asList(javaSourceCode), true);
    }

    protected Application generateCode(List<String> processResources, List<String> rulesResources ) throws Exception {
        return generateCode( processResources, rulesResources, Collections.emptyList(), Collections.emptyList(), false );
    }

    protected Application generateCode(
            List<String> processResources,
            List<String> rulesResources,
            List<String> decisionResources,
            List<String> javaRulesResources,
            boolean hasRuleUnit) throws Exception {
        GeneratorContext context = GeneratorContext.ofResourcePath(new File("src/test/resources"));

        //Testing based on Quarkus as Default
        context.withBuildContext(Optional.ofNullable(withSpringContext)
                                         .filter(Boolean.TRUE::equals)
                                         .<KogitoBuildContext>map(t -> new SpringBootKogitoBuildContext((className -> true)))
                                         .orElse(new QuarkusKogitoBuildContext((className -> true))));

        ApplicationGenerator appGen =
                new ApplicationGenerator(this.getClass().getPackage().getName(), new File("target/codegen-tests"))
                        .withGeneratorContext(context)
                        .withRuleUnits(hasRuleUnit)
                        .withDependencyInjection(null);

        if (!processResources.isEmpty()) {
            appGen.withGenerator(ProcessCodegen.ofFiles(processResources
                                                                .stream()
                                                                .map(resource -> new File("src/test/resources", resource))
                                                                .collect(Collectors.toList())));
        }

        if (!rulesResources.isEmpty()) {
            appGen.withGenerator(IncrementalRuleCodegen.ofFiles(rulesResources
                                                                   .stream()
                                                                   .map(resource -> new File("src/test/resources", resource))
                                                                   .collect(Collectors.toList())));
        }


        if (!decisionResources.isEmpty()) {
            appGen.withGenerator(DecisionCodegen.ofFiles(Paths.get("src/test/resources").toAbsolutePath(), decisionResources
                                                                    .stream()
                                                                    .map(resource -> new File("src/test/resources", resource))
                                                                    .collect(Collectors.toList())));
        }

        if (!javaRulesResources.isEmpty()) {
            appGen.withGenerator(IncrementalRuleCodegen.ofJavaFiles(javaRulesResources
                                                                            .stream()
                                                                            .map(resource -> new File("src/test/java/", resource))
                                                                            .collect(Collectors.toList())));
        }

        Collection<GeneratedFile> generatedFiles = appGen.generate();

        MemoryFileSystem srcMfs = new MemoryFileSystem();
        MemoryFileSystem trgMfs = new MemoryFileSystem();

        List<String> sources = new ArrayList<>();
        for (GeneratedFile entry : generatedFiles) {
            String fileName = entry.relativePath();
            if (!fileName.endsWith( ".java" )) {
                continue;
            }
            sources.add( fileName );
            srcMfs.write(fileName, entry.contents());
            log(new String(entry.contents()));
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

        CompilationResult result = JAVA_COMPILER.compile(sources.toArray( new String[sources.size()] ), srcMfs, trgMfs, this.getClass().getClassLoader());
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).as(Arrays.toString(result.getErrors())).hasSize(0);

        classloader = new TestClassLoader(this.getClass().getClassLoader(), trgMfs.getMap());

        @SuppressWarnings("unchecked")
        Class<Application> app = (Class<Application>) Class.forName(this.getClass().getPackage().getName() + ".Application", true, classloader);

        Application application = app.newInstance();
        app.getMethod("setup").invoke(application);
        return application;
    }
    
    protected ClassLoader testClassLoader() {
        return classloader;
    }
    
    protected void log(String content) {
        logger.debug(content);
    }

    private static class TestClassLoader extends URLClassLoader {

        private final Map<String, byte[]> extraClassDefs;

        public TestClassLoader(ClassLoader parent, Map<String, byte[]> extraClassDefs) {
            super(new URL[0], parent);
            this.extraClassDefs = new HashMap<>();

            for (Entry<String, byte[]> entry : extraClassDefs.entrySet()) {
                this.extraClassDefs.put(entry.getKey().replaceAll("/", ".").replaceFirst("\\.class", ""), entry.getValue());
            }
        }

        @Override
        protected Class<?> findClass(final String name) throws ClassNotFoundException {
            byte[] classBytes = this.extraClassDefs.remove(name);
            if (classBytes != null) {
                return defineClass(name, classBytes, 0, classBytes.length);
            }
            return super.findClass(name);
        }

    }
}
