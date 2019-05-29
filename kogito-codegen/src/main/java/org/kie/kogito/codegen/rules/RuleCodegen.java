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

package org.kie.kogito.codegen.rules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.drools.compiler.compiler.io.memory.MemoryFile;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.drools.modelcompiler.CanonicalKieModule;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.kogito.codegen.ApplicationGenerator;
import org.kie.kogito.codegen.ConfigGenerator;
import org.kie.kogito.codegen.GeneratedFile;
import org.kie.kogito.codegen.Generator;
import org.kie.kogito.codegen.rules.config.RuleConfigGenerator;
import org.kie.kogito.codegen.rules.legacy.KogitoBuilder;

public class RuleCodegen implements Generator {

    private String packageName;
    private ModuleSourceClass moduleGenerator;

    public static RuleCodegen ofPath(Path path) throws IOException {
        return ofPath(path, false);
    }

    public static RuleCodegen ofPath(Path path, boolean oneClassPerRule) throws IOException {
        KogitoBuilder kieBuilder = new KogitoBuilder(path.toFile());
        return new RuleCodegen(path, kieBuilder, oneClassPerRule, Collections.emptyList());
    }

    public static RuleCodegen ofFiles(Path basePath, Collection<File> files) throws IOException {
        KogitoBuilder kieBuilder = new KogitoBuilder(basePath.toFile());
        return new RuleCodegen(basePath, kieBuilder, true, files);
    }

    private final boolean oneClassPerRule;

    private final Path basePath;
    private final KogitoBuilder kieBuilder;
    private KieModuleModel kieModuleModel = new KieModuleModelImpl();

    /**
     * will compile iff returns true for the given file
     */
    private final Predicate<String> fileFilter;

    private boolean dependencyInjection;

    private String ruleEventListenersConfigClass = null;

    public RuleCodegen(Path basePath, KogitoBuilder kieBuilder, boolean oneClassPerRule, Collection<File> files) {
        this.basePath = basePath;
        this.kieBuilder = kieBuilder;
        this.oneClassPerRule = oneClassPerRule;
        if (files.isEmpty()) {
            this.fileFilter = f -> true;
        } else {
            this.fileFilter = fname ->
                    files.stream()
                            .map(f -> f.getPath())
                            .anyMatch(f -> f.contains(fname));
        }
        // set default package name
        setPackageName(ApplicationGenerator.DEFAULT_PACKAGE_NAME);
    }

    public static String defaultRuleEventListenerConfigClass(String packageName) {
        return packageName + ".RuleEventListenerConfig";
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
        this.moduleGenerator = new ModuleSourceClass(packageName);
    }

    private MemoryFileSystem getMemoryFileSystem(InternalKieModule kieModule) {
        return kieModule instanceof CanonicalKieModule ?
                ((MemoryKieModule) ((CanonicalKieModule) kieModule).getInternalKieModule()).getMemoryFileSystem() :
                ((MemoryKieModule) kieModule).getMemoryFileSystem();
    }

    @Override
    public Collection<MethodDeclaration> factoryMethods() {
        return moduleGenerator.factoryMethods();
    }

    public List<GeneratedFile> generate() {
        if (ruleEventListenersConfigClass != null) {
            moduleGenerator.setRuleEventListenersConfigClass(ruleEventListenersConfigClass);
        }

        kieBuilder.buildAll(
                (km, cl) ->
                        new RuleCodegenProject(km, cl)
                                .withModuleGenerator(moduleGenerator)
                                .withOneClassPerRule(oneClassPerRule)
                                .withCdi(dependencyInjection),
                kieModuleModel
        );

        MemoryKieModule kieModule = kieBuilder.getMemoryKieModule();
        MemoryFileSystem mfs = getMemoryFileSystem(kieModule);

        return kieModule.getFileNames()
                .stream()
                .filter(f -> f.endsWith("java"))
                .map(mfs::getFile)
                .map(MemoryFile.class::cast)
                .map(f -> new GeneratedFile(GeneratedFile.Type.RULE, f.getPath().toPortableString(), mfs.getFileContents(f)))
                .collect(Collectors.toList());
    }

    @Override
    public void updateConfig(ConfigGenerator cfg) {
        // no config yet
        cfg.withRuleConfig(new RuleConfigGenerator().ruleEventListenersConfig(moduleGenerator.ruleEventListenersConfigClass()));
    }

    public ModuleSourceClass moduleGenerator() {
        return moduleGenerator;
    }

    public RuleCodegen withRuleEventListenersConfig(String ruleEventListenersConfigClass) {
        this.ruleEventListenersConfigClass = ruleEventListenersConfigClass;
        return this;
    }

    public RuleCodegen withKModule(KieModuleModel model) {
        kieModuleModel = model;
        return this;
    }

    public void setDependencyInjection(boolean di) {
        this.dependencyInjection = di;
    }

    @Override
    public Collection<BodyDeclaration<?>> applicationBodyDeclaration() {
        return moduleGenerator.getApplicationBodyDeclaration();
    }
}
