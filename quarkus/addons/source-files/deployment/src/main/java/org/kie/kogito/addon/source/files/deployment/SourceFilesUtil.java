/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addon.source.files.deployment;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static org.kie.kogito.codegen.process.ProcessCodegen.SUPPORTED_BPMN_EXTENSIONS;
import static org.kie.kogito.codegen.process.ProcessCodegen.SUPPORTED_SW_EXTENSIONS;

final class SourceFilesUtil {

    private SourceFilesUtil() {
    }

    static List<String> getSourceFiles(List<Path> paths) {
        List<String> sourceFiles = new ArrayList<>();

        for (Path path : paths) {
            try (Stream<Path> walkedPaths = Files.walk(path)) {
                walkedPaths.filter(not(Files::isDirectory))
                        .filter(SourceFilesUtil::isSourceFile)
                        .map(path::relativize)
                        .map(Path::toString)
                        .forEach(sourceFiles::add);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return sourceFiles;
    }

    private static boolean isSourceFile(Path file) {
        return SUPPORTED_BPMN_EXTENSIONS.stream().anyMatch(extension -> file.toString().endsWith(extension))
                || SUPPORTED_SW_EXTENSIONS.keySet().stream().anyMatch(extension -> file.toString().endsWith(extension));
    }
}
