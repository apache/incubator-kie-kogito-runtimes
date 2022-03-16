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
import java.util.List;
import java.util.Map;

public final class SourceFilesProviderImpl implements SourceFilesProvider {

    private final Map<String, Collection<SourceFile>> sourceFiles = new HashMap<>();

    public void addSourceFile(String processId, SourceFile sourceFile) {
        sourceFiles.computeIfAbsent(processId, k -> new ArrayList<>()).add(sourceFile);
    }

    @Override
    public Collection<SourceFile> getSourceFiles(String processId) {
        Collection<SourceFile> foundSourceFiles = this.sourceFiles.get(processId);
        return foundSourceFiles != null ? List.copyOf(foundSourceFiles) : List.of();
    }

    @Override
    public Map<String, Collection<SourceFile>> getSourceFiles() {
        return Map.copyOf(sourceFiles);
    }

    @Override
    public boolean contains(String sourceFile) {
        for (Collection<SourceFile> sourceFileCollection : sourceFiles.values()) {
            for (SourceFile sf : sourceFileCollection) {
                if (sf.getUri().equals(sourceFile)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void clear() {
        sourceFiles.clear();
    }
}
