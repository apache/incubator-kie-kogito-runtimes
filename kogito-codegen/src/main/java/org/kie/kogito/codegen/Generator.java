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

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A code generator for a part of the platform, e.g. rules, processes, etc.
 */
public interface Generator {

    /**
     * Returns the "section" of the Application class corresponding to rules.
     * e.g the processes() method with processes().createMyProcess() etc.
     *
     */
    ApplicationSection section();

    /**
     * Returns the collection of all the files that have been generated/compiled
     *
     */
    Collection<GeneratedFile> generate();

    /**
     * Consumes the given ApplicationConfigGenerator so that it can enrich it with
     * further, Generator-specific details.
     *
     * This is automatically called by the ApplicationGenerator.
     */
    void updateConfig(ApplicationConfigGenerator cfg);

    void setPackageName(String packageName);

    void setProjectDirectory(Path projectDirectory);
    
    void setContext(GeneratorContext context);

    void setAddonsConfig(AddonsConfig addonsConfig);
    
    GeneratorContext context();
    
    default Map<String, String> getLabels() {
        return Collections.emptyMap();
    }
}