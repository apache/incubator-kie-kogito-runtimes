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
package org.kie.kogito.codegen.rules;

import java.util.*;

import org.kie.kogito.codegen.api.*;
import org.kie.kogito.codegen.api.context.*;

public class RuleUnitExtendedCodegen {
    private final Collection<QueryEndpointGenerator> validQueries;

    // phases
    private final RuleUnitQueryDashboardCodegen dashboards;
    private final RuleUnitQueryEventCodegen events;
    private final RuleUnitQueryRestCodegen rest;
    private final RuleObjectMapperCodegen objectMapper;

    public RuleUnitExtendedCodegen(
            KogitoBuildContext context,
            Collection<RuleUnitGenerator> ruleUnitGenerators,
            Collection<QueryEndpointGenerator> validQueries) {
        this.validQueries = validQueries;
        this.dashboards = new RuleUnitQueryDashboardCodegen(context, ruleUnitGenerators);
        this.events = new RuleUnitQueryEventCodegen(context, ruleUnitGenerators);
        this.rest = new RuleUnitQueryRestCodegen();
        this.objectMapper = new RuleObjectMapperCodegen(context);
    }

    Collection<GeneratedFile> generate() {
        List<GeneratedFile> generatedFiles = new ArrayList<>();

        generatedFiles.addAll(events.generate(/* should use validQueries */));
        generatedFiles.addAll(rest.generate(validQueries));
        generatedFiles.addAll(dashboards.generate(validQueries));
        generatedFiles.add(objectMapper.generate());

        return generatedFiles;
    }

}
