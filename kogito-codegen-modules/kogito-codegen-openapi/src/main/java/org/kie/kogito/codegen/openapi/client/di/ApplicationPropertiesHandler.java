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
package org.kie.kogito.codegen.openapi.client.di;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.openapi.client.OpenApiSpecDescriptor;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.Expression;

public class ApplicationPropertiesHandler extends AbstractDependencyInjectionHandler {

    private static final String CONFIGURABLE_CLASS = "ApiClient";
    private static final String SUFFIX = "org.kogito.openapi.client";
    private static final String BASE_PATH_FIELD = "basePath";
    private static final String BASE_PATH_CONFIG = "base_path";
    private final Map<String, String> attributesAndKeys = new HashMap<>();

    ApplicationPropertiesHandler(KogitoBuildContext context) {
        super(context);
        this.attributesAndKeys.put("setPassword", "password");
        this.attributesAndKeys.put("setUsername", "username");
        this.attributesAndKeys.put("setApiKey", "api_key");
        this.attributesAndKeys.put("setApiKeyPrefix", "api_key_prefix");
    }

    @Override
    public ClassOrInterfaceDeclaration handle(ClassOrInterfaceDeclaration node, OpenApiSpecDescriptor descriptor, File originalGeneratedFile) {
        if (node.getNameAsString().equals(CONFIGURABLE_CLASS)) {
            final String openApiId = this.formatSpecId(descriptor);
            this.attributesAndKeys.forEach((key, value) -> node.getMethodsByName(key)
                    .stream()
                    .findFirst()
                    .ifPresent(m -> this.context.getDependencyInjectionAnnotator()
                            .withConfigInjection(m, SUFFIX + "." + openApiId + "." + value)));
            this.handleBasePathProperty(node, SUFFIX + "." + openApiId);
            this.context.getDependencyInjectionAnnotator().withApplicationComponent(node);
        }
        return node;
    }

    private String formatSpecId(final OpenApiSpecDescriptor descriptor) {
        if (descriptor.getResourceName() == null || descriptor.getResourceName().isEmpty()) {
            return "";
        }
        String id = descriptor.getResourceName().toLowerCase();
        final int dividerIdx = id.indexOf(".");
        if (dividerIdx >= 0) {
            id = descriptor.getResourceName().substring(0, dividerIdx);
        }
        return id;
    }

    // TODO: workaround for now, open a PR for OpenApi Generator Tool and fix ApiClient.
    // setBasePath does not respect POJO structure
    // after fixing this problem we can add it to #attributesAndKeys map and things will just work
    // we could modify their template, but this is just not right.
    private void handleBasePathProperty(final ClassOrInterfaceDeclaration node, final String configSuffix) {
        final FieldDeclaration basePathField = node.getFieldByName(BASE_PATH_FIELD).orElseThrow(() -> new IllegalArgumentException("basePath property not found on class " + CONFIGURABLE_CLASS));
        basePathField.setModifiers(Modifier.Keyword.PUBLIC);
        String defaultURL = "";
        if (!basePathField.getVariables().isEmpty()) {
            final Optional<Expression> initializer = basePathField.getVariables().get(0).getInitializer();
            if (initializer.isPresent()) {
                defaultURL = initializer.get().toString().replace("\"", "");
                basePathField.getVariables().get(0).removeInitializer();
            }
        }
        this.context.getDependencyInjectionAnnotator().withConfigInjection(basePathField, configSuffix + "." + BASE_PATH_CONFIG, defaultURL);
    }
}
