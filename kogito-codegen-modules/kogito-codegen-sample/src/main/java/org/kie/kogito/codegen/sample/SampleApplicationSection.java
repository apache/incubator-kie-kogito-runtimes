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
import org.kie.kogito.codegen.api.ApplicationSection;
import org.kie.kogito.codegen.api.ConfigGenerator;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.Generator;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;

import java.util.*;
import java.util.stream.Collectors;

public class SampleApplicationSection implements ApplicationSection {

    private KogitoBuildContext context;
    private List<String> filteredParsedResources;

    public SampleApplicationSection(KogitoBuildContext context, List<String> filteredParsedResources) {
        this.context = context;
        this.filteredParsedResources = filteredParsedResources;
    }

    @Override
    public String sectionClassName() {
        return "SampleEngine";
    }

    @Override
    public CompilationUnit compilationUnit() {

        return null;
    }
}
