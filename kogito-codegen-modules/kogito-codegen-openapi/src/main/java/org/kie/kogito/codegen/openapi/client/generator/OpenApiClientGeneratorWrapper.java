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
package org.kie.kogito.codegen.openapi.client.generator;

import java.io.File;
import java.util.List;

import org.kie.kogito.codegen.openapi.client.OpenApiSpecDescriptor;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;
import org.openapitools.codegen.config.GlobalSettings;
import org.openapitools.codegen.languages.AbstractJavaCodegen;
import org.openapitools.codegen.languages.JavaClientCodegen;

/**
 * Wrapper for the OpenAPIGen tool.
 * This is the same as calling the Maven plugin or the CLI.
 * We are wrapping into a class to generate code that meet our requirements.
 * In the future we can consider exposing some of this properties for fine tune configuration.
 *
 * @see <a href="https://openapi-generator.tech/docs/generators/java">OpenAPI Generator Client for Java</a>
 */
public class OpenApiClientGeneratorWrapper {

    private static final String FALSE = "false";
    private static final String CUSTOM_TEMPLATES = "custom-templates/resteasy";

    private static final String MODEL_PACKAGE = "model";
    private static final String GENERATOR_NAME = "java";
    private static final String API_TESTS = "apiTests";
    private static final String MODEL_TESTS = "modelTests";
    private static final String MODEL_DOCS = "modelDocs";
    private static final String API_DOCS = "apiDocs";

    private final OpenApiClientGeneratorAdapter generatorAdapter;
    private final CodegenConfigurator configurator;
    private final DefaultGenerator generator;

    private OpenApiClientGeneratorWrapper(final String specFilePath, final String outputDir) {
        GlobalSettings.setProperty(API_TESTS, FALSE);
        GlobalSettings.setProperty(MODEL_TESTS, FALSE);
        GlobalSettings.setProperty(MODEL_DOCS, FALSE);
        GlobalSettings.setProperty(API_DOCS, FALSE);
        this.configurator = new CodegenConfigurator();
        this.configurator.setInputSpec(specFilePath);
        this.configurator.setGeneratorName(GENERATOR_NAME);
        this.generator = new DefaultGenerator();
        this.generatorAdapter = new OpenApiClientGeneratorAdapter(this.generator);
        // not working, see @OpenApiClientGeneratorAdapter
        this.generatorAdapter.setUseRuntimeException(true);
        this.generatorAdapter.setLibrary(JavaClientCodegen.RESTEASY);
        this.generatorAdapter.setOutputDir(outputDir);
        this.generatorAdapter.setTemplateDir(CUSTOM_TEMPLATES);
        this.generatorAdapter.setDateLibrary(AbstractJavaCodegen.JAVA8_MODE);
    }

    /**
     * Generates the OpenAPI project based on the given OpenAPI spec file
     *
     * @param specFilePath a valid path in the local system to the spec file
     * @param outputDir a valid path in the local system where the files will be generated
     * @return a new instance of {@link OpenApiClientGeneratorWrapper}
     */
    public static OpenApiClientGeneratorWrapper newInstance(final String specFilePath, final String outputDir) {
        return new OpenApiClientGeneratorWrapper(specFilePath, outputDir);
    }

    public OpenApiClientGeneratorWrapper withPackage(final String pkg) {
        this.generatorAdapter.setApiPackage(pkg);
        this.generatorAdapter.setInvokerPackage(pkg);
        this.generatorAdapter.setModelPackage(pkg + "." + MODEL_PACKAGE);
        return this;
    }

    public List<File> generate(final OpenApiSpecDescriptor descriptor) {
        final List<File> generatedFiles = this.generator.opts(
                this.configurator
                        .toClientOptInput()
                        .config(this.generatorAdapter))
                .generate();
        this.generatorAdapter.processGeneratedOperations(descriptor);
        return generatedFiles;
    }
}
