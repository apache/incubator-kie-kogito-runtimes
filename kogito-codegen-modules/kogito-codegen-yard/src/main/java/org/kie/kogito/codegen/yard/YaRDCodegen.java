/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.codegen.yard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.drools.codegen.common.GeneratedFile;
import org.drools.codegen.common.GeneratedFileType;
import org.kie.api.io.Resource;
import org.kie.dmn.feel.codegen.feel11.CodegenStringUtil;
import org.kie.kogito.codegen.api.ApplicationSection;
import org.kie.kogito.codegen.api.ConfigGenerator;
import org.kie.kogito.codegen.api.Generator;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.QuarkusKogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.api.template.InvalidTemplateException;
import org.kie.kogito.codegen.api.template.TemplatedGenerator;
import org.kie.yard.core.YaRDParser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import static java.util.stream.Collectors.toList;

public class YaRDCodegen implements Generator {

    public static final String GENERATOR_NAME = "yard";
    private static final String REST_RESOURCE = "YaRDRestResource";
    private static final Set<String> EXTENSIONS = Set.of(".yard.yaml", ".yard.yml");
    private final List<YaRDResource> resources = new ArrayList<>();
    private final KogitoBuildContext context;

    public YaRDCodegen(KogitoBuildContext context, List<CollectedResource> rawSampleResources) {
        this.context = context;
        resources.addAll(parseResources(rawSampleResources));
    }

    public static YaRDCodegen ofCollectedResources(KogitoBuildContext context, Collection<CollectedResource> resources) {
        List<CollectedResource> yardResources = resources.stream()
                .filter(r -> EXTENSIONS.stream().anyMatch(r.resource().getSourcePath()::endsWith))
                .collect(toList());
        return new YaRDCodegen(context, yardResources);
    }

    private static Collection<? extends YaRDResource> parseResources(List<CollectedResource> rawSampleResources) {
        return rawSampleResources.stream()
                .map(cr -> {
                    final String content = getContent(cr.resource());
                    try {
                        final YaRDParser yaRDParser = new YaRDParser(content);
                        return new YaRDResource(yaRDParser.getModel(), yaRDParser.getYaml());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(toList());
    }

    private static String getContent(Resource resource) {
        try {
            return new BufferedReader(new InputStreamReader(
                    resource.getInputStream(), StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible to read resource " + resource.getSourcePath(), e);
        }
    }

    private static String encodeName(YaRDResource resource) {
        try {
            String encode = URLEncoder.encode(resource.getModel().getName(), StandardCharsets.UTF_8.name());
            return encode
                    .replace("+", " ");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Model name was not valid for the template.", e);
        }
    }

    @Override
    public boolean isEmpty() {
        return resources.isEmpty();
    }

    @Override
    public Optional<ApplicationSection> section() {
        return Optional.of(new YaRDContainerGenerator(context(), resources));
    }

    @Override
    public Collection<GeneratedFile> generate() {
        if (isEmpty()) {
            return Collections.emptyList();
        }

        final List<GeneratedFile> result = new ArrayList<>(
                resources.stream().map(r -> {
                    final String path = "org/kie/kogito/app/" + CodegenStringUtil.escapeIdentifier(r.getModel().getName()) + ".yard.yml";
                    return new GeneratedFile(GeneratedFileType.INTERNAL_RESOURCE, path, r.getYaml());
                }).toList());

        final TemplatedGenerator generator = TemplatedGenerator.builder()
                .withFallbackContext(QuarkusKogitoBuildContext.CONTEXT_NAME)
                .build(context(), REST_RESOURCE);

        for (YaRDResource resource : resources) {

            final CompilationUnit compilationUnit = generator.compilationUnitOrThrow();
            final ClassOrInterfaceDeclaration classDefinition = compilationUnit.findFirst(ClassOrInterfaceDeclaration.class)
                    .orElseThrow(() -> new InvalidTemplateException(generator, "Compilation unit doesn't contain a class or interface declaration!"));

            classDefinition.findAll(StringLiteralExpr.class)
                    .forEach(stringLiteralExpr -> interpolateStrings(stringLiteralExpr, encodeName(resource)));

            classDefinition.findAll(ClassOrInterfaceDeclaration.class)
                    .forEach(
                            type -> type.getName().setIdentifier(formDeclaration(resource.getModel().getName())));

            if (context.hasRESTForGenerator(this)) {
                final String relativePath = generator.generatedFilePath();
                final String path = "%s%s.java".formatted(
                        relativePath.substring(0, relativePath.lastIndexOf("/") + 1),
                        formDeclaration(resource.getModel().getName()));
                result.add(new GeneratedFile(REST_TYPE, path, compilationUnit.toString()));
            }
        }

        return result;
    }

    private static String formDeclaration(String identifier) {
        return identifier
                .replaceAll("\\s+", "") + YaRDCodegen.REST_RESOURCE;
    }

    private void interpolateStrings(final StringLiteralExpr expr,
            final String name) {
        final String value = expr.getValue();
        final String interpolated = value.replace("$name$", name);
        expr.setString(interpolated);
    }

    @Override
    public Optional<ConfigGenerator> configGenerator() {
        return Optional.empty(); // TODO
    }

    @Override
    public KogitoBuildContext context() {
        return context;
    }

    @Override
    public String name() {
        return GENERATOR_NAME;
    }

    @Override
    public int priority() {
        return 30;
    }
}
