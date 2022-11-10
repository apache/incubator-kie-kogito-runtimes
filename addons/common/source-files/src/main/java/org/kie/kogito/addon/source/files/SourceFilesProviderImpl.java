/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addon.source.files;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.kie.kogito.codegen.process.ProcessCodegen;

public final class SourceFilesProviderImpl implements SourceFilesProvider {

    private final Map<String, Collection<SourceFile>> sourceFiles = new HashMap<>();

    public void addSourceFile(String id, SourceFile sourceFile) {
        sourceFiles.computeIfAbsent(id, k -> new HashSet<>()).add(sourceFile);
    }

    @Override
    public Optional<SourceFile> getSourceFilesByUri(String uri) {
        List<SourceFile> allSourceFiles = new ArrayList<>();

        sourceFiles.values().forEach(allSourceFiles::addAll);

        allSourceFiles.removeIf(sourceFile -> !Objects.equals(sourceFile.getUri(), uri));

        switch (allSourceFiles.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(allSourceFiles.get(0));
            default:
                throw new IllegalStateException("Found more than one source file with the same URI.");
        }
    }

    @Override
    public Collection<SourceFile> getProcessSourceFiles(String processId) {
        return sourceFiles.getOrDefault(processId, Set.of());
    }

    @Override
    public Optional<String> getProcessSourceFile(String processId) throws SourceFilesException {
        return getProcessSourceFiles(processId).stream()
                .filter(this::isValidDefinitionSource)
                .findFirst()
                .map(SourceFile::getContents);
    }

    private boolean isValidDefinitionSource(SourceFile sourceFile) {
        if (ProcessCodegen.SUPPORTED_BPMN_EXTENSIONS.stream().noneMatch(sourceFile.getUri()::endsWith)) {
            return ProcessCodegen.SUPPORTED_SW_EXTENSIONS.keySet().stream().anyMatch(sourceFile.getUri()::endsWith);
        }
        return true;
    }
}
