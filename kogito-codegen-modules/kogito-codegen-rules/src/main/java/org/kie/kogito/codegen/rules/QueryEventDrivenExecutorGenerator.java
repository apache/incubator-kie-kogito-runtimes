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
package org.kie.kogito.codegen.rules;

import java.util.Map;

import org.drools.modelcompiler.builder.QueryModel;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.GeneratedFileType;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;

import com.github.javaparser.ast.CompilationUnit;

public class QueryEventDrivenExecutorGenerator extends AbstractQueryEntrypointGenerator {

    public QueryEventDrivenExecutorGenerator(RuleUnitDescription ruleUnit, QueryModel query, KogitoBuildContext context) {
        super(ruleUnit, query, context, "EventDrivenExecutor", "EventDrivenExecutor");
    }

    @Override
    public GeneratedFile generate() {
        CompilationUnit cu = generator.compilationUnitWithReplacementsOrThrow("Could not create CompilationUnit",
                t -> t.replace("$QueryType$", queryClassName)
                        .replace("$DataType$", getDataType())
                        .replace("$ReturnType$", getReturnType())
                        .replace("$name$", queryName));

        return new GeneratedFile(GeneratedFileType.SOURCE, generatedFilePath(), cu.toString());
    }

    private String getDataType() {
        return ruleUnit.getCanonicalName() + (context.hasDI() ? "" : "DTO");
    }

    private String getReturnType() {
        Map<String, Class<?>> bindings = query.getBindings();

        String innerType = bindings.size() != 1
                ? queryClassName + ".Result"
                : bindings.values().iterator().next().getCanonicalName();

        return String.format("java.util.List<%s>", innerType);
    }
}
