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
package org.kie.kogito.maven.plugin;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.kie.kogito.codegen.api.Generator;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.process.ProcessCodegen;
import org.kie.kogito.codegen.process.persistence.PersistenceGenerator;

import static org.kie.kogito.maven.plugin.AbstractScaffoldMojo.NAME;

@Mojo(name = NAME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresProject = true,
        threadSafe = true)
public class ScaffoldMojo extends AbstractScaffoldMojo {

    @Parameter(property = "kogito.codegen.persistence", defaultValue = "true")
    protected boolean persistence;

    @Parameter(property = "kogito.codegen.processes", defaultValue = "true")
    protected String generateProcesses;

    @Override
    protected void additionalProperties(KogitoBuildContext context) {
        super.additionalProperties(context);
        context.setApplicationProperty(Generator.CONFIG_PREFIX + ProcessCodegen.GENERATOR_NAME, generateProcesses);
        context.setApplicationProperty(Generator.CONFIG_PREFIX + PersistenceGenerator.GENERATOR_NAME, persistence);
    }

}
