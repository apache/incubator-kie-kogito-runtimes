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
package org.kie.kogito.codegen.api;

import java.util.Collection;
import java.util.Optional;

import org.kie.kogito.codegen.api.context.KogitoBuildContext;

/**
 * A code generator for a part of the platform, e.g. rules, processes, etc.
 */
public interface Generator {

    GeneratedFileType REST_TYPE = GeneratedFileType.of("REST", GeneratedFileType.Category.SOURCE, true, true);
    GeneratedFileType MODEL_TYPE = GeneratedFileType.of("MODEL", GeneratedFileType.Category.SOURCE, true, true);

    String CONFIG_PREFIX = "kogito.codegen.";

    /**
     * Returns the "section" of the Application class corresponding to rules.
     * e.g the processes() method with processes().createMyProcess() etc.
     *
     */
    Optional<ApplicationSection> section();

    /**
     * Returns the collection of all the files that have been generated/compiled
     *
     */
    Collection<GeneratedFile> generate();

    Optional<ConfigGenerator> configGenerator();

    KogitoBuildContext context();

    String name();

    /**
     * Override this method to specify an order of execution
     * 
     * @return
     */
    default int priority() {
        return Integer.MAX_VALUE;
    }

    default boolean isEnabled() {
        return context().getApplicationProperty(CONFIG_PREFIX + name())
                .map("true"::equalsIgnoreCase)
                .orElse(true);
    }
}
