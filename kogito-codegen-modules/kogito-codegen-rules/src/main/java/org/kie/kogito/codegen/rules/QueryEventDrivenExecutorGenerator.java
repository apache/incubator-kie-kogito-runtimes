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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.drools.compiler.compiler.DroolsError;
import org.drools.modelcompiler.builder.QueryModel;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.GeneratedFileType;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.codegen.api.template.TemplatedGenerator;

import com.github.javaparser.ast.CompilationUnit;

import static org.kie.kogito.codegen.rules.IncrementalRuleCodegen.TEMPLATE_RULE_FOLDER;

public class QueryEventDrivenExecutorGenerator implements RuleFileGenerator {

    private final RuleUnitDescription ruleUnit;
    private final QueryModel query;
    private final KogitoBuildContext context;

    private final String queryName;
    private final String queryClassName;
    private final TemplatedGenerator generator;

    public QueryEventDrivenExecutorGenerator(RuleUnitDescription ruleUnit, QueryModel query, KogitoBuildContext context) {
        this.ruleUnit = ruleUnit;
        this.query = query;
        this.context = context;

        this.queryName = toCamelCase(query.getName());
        this.queryClassName = ruleUnit.getSimpleName() + "Query" + queryName;

        String targetClassName = queryClassName + "EventDrivenExecutor";
        this.generator = TemplatedGenerator.builder()
                .withPackageName(query.getNamespace())
                .withTemplateBasePath(TEMPLATE_RULE_FOLDER)
                .withTargetTypeName(targetClassName)
                .withFallbackContext(JavaKogitoBuildContext.CONTEXT_NAME)
                .build(context, "EventDrivenExecutor");
    }

    @Override
    public String generatedFilePath() {
        return generator.generatedFilePath();
    }

    @Override
    public boolean validate() {
        return !query.getBindings().isEmpty();
    }

    @Override
    public DroolsError getError() {
        if (query.getBindings().isEmpty()) {
            return new NoBindingQuery(query);
        }
        return null;
    }

    @Override
    public GeneratedFile generate() {
        CompilationUnit cu = generator.compilationUnitWithReplacementsOrThrow("Could not create CompilationUnit",
                t -> t.replace("$QueryType$", queryClassName)
                        .replace("$DataType$", ruleUnit.getCanonicalName())
                        .replace("$ReturnType$", String.format("java.util.List<%s>", getReturnType()))
                        .replace("$name$", queryName));

        return new GeneratedFile(GeneratedFileType.SOURCE, generatedFilePath(), cu.toString());
    }

    private String getDataType() {
        return ruleUnit.getCanonicalName() + (context.hasDI() ? "" : "DTO");
    }

    private String getReturnType() {
        if (query.getBindings().size() == 1) {
            Map.Entry<String, Class<?>> binding = query.getBindings().entrySet().iterator().next();
            return binding.getValue().getCanonicalName();
        }
        return queryClassName + ".Result";
    }

    private static String toCamelCase(String inputString) {
        return Stream.of(inputString.split(" "))
                .map(s -> s.length() > 1 ? s.substring(0, 1).toUpperCase() + s.substring(1) : s.substring(0, 1).toUpperCase())
                .collect(Collectors.joining());
    }

    private static class NoBindingQuery extends DroolsError {

        private static final int[] ERROR_LINES = new int[0];

        private final QueryModel query;

        public NoBindingQuery(QueryModel query) {
            this.query = query;
        }

        @Override
        public String getMessage() {
            return "Query " + query.getName() + " has no bound variable. At least one binding is required to determine the value returned by this query";
        }

        @Override
        public int[] getLines() {
            return ERROR_LINES;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            NoBindingQuery that = (NoBindingQuery) o;
            return Objects.equals(query, that.query);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), query);
        }
    }
}
