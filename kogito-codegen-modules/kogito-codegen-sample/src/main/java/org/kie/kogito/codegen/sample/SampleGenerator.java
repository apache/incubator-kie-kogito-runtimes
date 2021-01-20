/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.codegen.sample;

import com.github.javaparser.ast.CompilationUnit;
import org.kie.kogito.codegen.api.*;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.api.template.TemplatedGenerator;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SampleGenerator implements Generator {

    public static final String TEMPLATE_NAME = "SampleRest";
    public static final Set<String> EXTENSIONS = Collections.singleton(".txt");
    private final KogitoBuildContext context;
    private final List<String> filteredParsedResources;

    public static Generator ofCollectedResource(KogitoBuildContext context, Collection<CollectedResource> collectedResources) {
        List<String> filteredResources = collectedResources.stream()
                .filter(resource -> EXTENSIONS.stream().anyMatch(ex -> resource.basePath().endsWith(ex)))
                .map(SampleGenerator::parseData)
                .collect(Collectors.toList());
        return new SampleGenerator(context, filteredResources);
    }

    protected static String parseData(CollectedResource collectedResource) {
        StringBuilder parsedData = new StringBuilder();
        try (Reader reader = collectedResource.resource().getReader()) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                parsedData.append((char) c);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error during conversion", e);
        }
        return parsedData.toString();
    }

    private SampleGenerator(KogitoBuildContext context, List<String> filteredParsedResources) {
        this.context = context;
        this.filteredParsedResources = filteredParsedResources;
    }

    @Override
    public Optional<ApplicationSection> section() {
        return Optional.of(new SampleApplicationSection(context, filteredParsedResources));
    }

    @Override
    public Collection<GeneratedFile> generate() {
        TemplatedGenerator generator = TemplatedGenerator.builder()
                .build(context(), TEMPLATE_NAME);

        CompilationUnit compilationUnit = generator.compilationUnitOrThrow();

        return Collections.singleton(new GeneratedFile(
                GeneratedFileType.SOURCE,
                generator.generatedFilePath(),
                compilationUnit.toString()));
    }

    @Override
    public Optional<ConfigGenerator> configGenerator() {
        return Optional.of(new SampleConfigGenerator());
    }

    @Override
    public KogitoBuildContext context() {
        return context;
    }

    @Override
    public String name() {
        return "sample";
    }
}
