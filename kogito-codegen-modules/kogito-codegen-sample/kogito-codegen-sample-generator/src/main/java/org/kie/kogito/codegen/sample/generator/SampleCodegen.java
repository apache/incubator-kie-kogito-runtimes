/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.codegen.sample.generator;


import org.kie.kogito.codegen.api.ApplicationSection;
import org.kie.kogito.codegen.api.ConfigGenerator;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.Generator;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.sample.generator.config.SampleConfigGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;


public class SampleCodegen implements Generator {

    public static final Logger LOGGER = LoggerFactory.getLogger(SampleCodegen.class);

    public static String GENERATOR_NAME = "sample";
    public static Set<String> SUPPORTED_EXTENSIONS = Collections.singleton("txt");
    private final KogitoBuildContext context;
    private final List<CollectedResource> sampleResources;

    public static SampleCodegen ofCollectedResources(KogitoBuildContext context, Collection<CollectedResource> resources) {
        List<CollectedResource> sampleResources = resources.stream()
                .filter(resource -> SUPPORTED_EXTENSIONS.stream().anyMatch(resource.resource().getSourcePath()::endsWith))
                .collect(toList());
        return new SampleCodegen(context, sampleResources);
    }

    public SampleCodegen(KogitoBuildContext context, List<CollectedResource> sampleResources) {
        this.context = context;
        this.sampleResources = sampleResources;
    }

    @Override
    public Optional<ApplicationSection> section() {
        return Optional.of(new SampleContainerGenerator(context()));
    }

    @Override
    public Collection<GeneratedFile> generate() {
        // FIXME to fix
        return null;
    }

    @Override
    public Optional<ConfigGenerator> configGenerator() {
        return Optional.of(new SampleConfigGenerator(context()));
    }

    @Override
    public KogitoBuildContext context() {
        return context;
    }

    @Override
    public String name() {
        return GENERATOR_NAME;
    }
}