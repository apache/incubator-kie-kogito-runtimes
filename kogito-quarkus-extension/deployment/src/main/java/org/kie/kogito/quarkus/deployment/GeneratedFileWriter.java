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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.kie.kogito.codegen.GeneratedFile;

public class GeneratedFileWriter {

    public static class Builder {

        private final String classesDir;
        private final String sourcesDir;
        private final String resourcePath;
        private final String scaffoldedSourcesDir;

        public Builder(String classesDir, String sourcesDir, String resourcesDir, String scaffoldedSourcesDir) {
            this.classesDir = classesDir;
            this.sourcesDir = sourcesDir;
            this.resourcePath = resourcesDir;
            this.scaffoldedSourcesDir = scaffoldedSourcesDir;
        }

        public GeneratedFileWriter build(Path basePath) {
            return new GeneratedFileWriter(
                    basePath.resolve(classesDir),
                    basePath.resolve(sourcesDir),
                    basePath.resolve(resourcePath),
                    basePath.resolve(scaffoldedSourcesDir));
        }
    }

    private final Path classesDir;
    private final Path sourcesDir;
    private final Path resourcePath;
    private final Path scaffoldedSourcesDir;

    public GeneratedFileWriter(Path classesDir, Path sourcesDir, Path resourcePath, Path scaffoldedSourcesDir) {
        this.classesDir = classesDir;
        this.sourcesDir = sourcesDir;
        this.resourcePath = resourcePath;
        this.scaffoldedSourcesDir = scaffoldedSourcesDir;
    }

    public void writeAll(Collection<GeneratedFile> generatedFiles) {
        generatedFiles.forEach(this::write);
    }

    public void write(GeneratedFile f) throws UncheckedIOException {
        try {
            GeneratedFile.Type type = f.getType();
            switch (type) {
                case RESOURCE:
                    writeGeneratedFile(f, resourcePath);
                    break;
                case GENERATED_CP_RESOURCE:
                    writeGeneratedFile(f, classesDir);
                    break;
                default:
                    if (type.isCustomizable()) {
                        writeGeneratedFile(f, scaffoldedSourcesDir);
                    } else {
                        writeGeneratedFile(f, sourcesDir);
                    }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeGeneratedFile(GeneratedFile f, Path location) throws IOException {
        if (location == null) {
            return;
        }
        String generatedClassFile = f.relativePath().replace("src/main/java", "");
        Files.write(
                pathOf(location, generatedClassFile),
                f.contents());
    }

    private Path pathOf(Path location, String end) {
        Path path = location.resolve(end);
        path.getParent().toFile().mkdirs();
        return path;
    }
}
