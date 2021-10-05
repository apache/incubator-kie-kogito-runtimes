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

package org.kie.kogito.junit.deployment.spi.impl;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.kie.kogito.Model;
import org.kie.kogito.codegen.api.Generator;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.process.persistence.PersistenceGenerator;
import org.kie.kogito.codegen.process.persistence.proto.ReflectionProtoGenerator;
import org.kie.kogito.junit.deployment.spi.RuntimeBuildGeneratorProvider;
import org.kie.kogito.process.ProcessInstancesFactory;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

@SuppressWarnings({ "unchecked" })
public class PersistenceRuntimeBuildGeneratorProvider implements RuntimeBuildGeneratorProvider {

    @Override
    public Optional<Generator> buildCodegenGenerator(KogitoBuildContext context, Collection<CollectedResource> resources) {
        if (!context.getAddonsConfig().usePersistence()) {
            return Optional.empty();
        }

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addClassLoader(PersistenceRuntimeBuildGeneratorProvider.class.getClassLoader());

        Reflections reflections = new Reflections(builder);

        Set<Class<?>> modelClasses = (Set) reflections.getSubTypesOf(Model.class);

        // collect constructor parameters so the generated class can create constructor with injection
        Class<?> persistenceClass = reflections.getSubTypesOf(ProcessInstancesFactory.class)
                .stream()
                .filter(c -> !c.isInterface())
                .findFirst()
                .orElse(null);

        ReflectionProtoGenerator protoGenerator = ReflectionProtoGenerator.builder()
                .withPersistenceClass(persistenceClass)
                .build(modelClasses);
        return Optional.of(new PersistenceGenerator(context, protoGenerator));
    }

}
