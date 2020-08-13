/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.quarkus.deployment;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.quarkus.bootstrap.model.AppDependency;
import org.drools.compiler.commons.jci.compilers.CompilationResult;
import org.drools.compiler.commons.jci.compilers.JavaCompiler;
import org.drools.compiler.commons.jci.compilers.JavaCompilerSettings;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.modelcompiler.builder.JavaParserCompiler;
import org.kie.internal.jci.CompilationProblem;
import org.kie.kogito.codegen.GeneratedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryCompiler {

    private static final Logger logger = LoggerFactory.getLogger(KogitoAssetsProcessor.class);


    private final JavaCompiler javaCompiler;
    private final JavaCompilerSettings compilerSettings;
    private final MemoryFileSystem trgMfs = new MemoryFileSystem();

    InMemoryCompiler(
            List<Path> classesPaths,
            List<AppDependency> userDependencies) {
        javaCompiler = JavaParserCompiler.getCompiler();
        compilerSettings = javaCompiler.createDefaultSettings();
        compilerSettings.addOption("-proc:none"); // force disable annotation processing
        for (Path classPath : classesPaths) {
            compilerSettings.addClasspath(classPath.toString());
        }
        for (AppDependency i : userDependencies) {
            compilerSettings.addClasspath(i.getArtifact().getPaths().getSinglePath().toAbsolutePath().toString());
        }
    }

    CompilationResult compile(Collection<GeneratedFile> generatedFiles) {
        MemoryFileSystem srcMfs = new MemoryFileSystem();

        String[] sources = new String[generatedFiles.size()];
        int index = 0;
        for (GeneratedFile entry : generatedFiles) {
            String generatedClassFile = entry.relativePath().replace("src/main/java/", "");
            String fileName = toRuntimeSource(toClassName(generatedClassFile));
            sources[index++] = fileName;

            srcMfs.write(fileName, entry.contents());
        }

        CompilationResult result = javaCompiler.compile(
                sources,
                srcMfs,
                trgMfs,
                Thread.currentThread().getContextClassLoader(),
                compilerSettings);

        if (result.getErrors().length > 0) {
            StringBuilder errorInfo = new StringBuilder();
            for (CompilationProblem compilationProblem : result.getErrors()) {
                errorInfo.append(compilationProblem.toString());
                errorInfo.append("\n");
                logger.error(compilationProblem.toString());
            }
            Arrays.stream(result.getErrors()).forEach(cp -> errorInfo.append(cp.toString()));
            throw new IllegalStateException(errorInfo.toString());
        }

        return result;
    }

    public MemoryFileSystem getTargetFileSystem() {
        return trgMfs;
    }

    private String toClassName(String sourceName) {
        if (sourceName.startsWith("./")) {
            sourceName = sourceName.substring(2);
        }
        if (sourceName.endsWith(".java")) {
            sourceName = sourceName.substring(0, sourceName.length() - 5);
        } else if (sourceName.endsWith(".class")) {
            sourceName = sourceName.substring(0, sourceName.length() - 6);
        }
        return sourceName.replace('/', '.');
    }

    private String toRuntimeSource(String className) {
        return "src/main/java/" + className.replace('.', '/') + ".java";
    }
}
