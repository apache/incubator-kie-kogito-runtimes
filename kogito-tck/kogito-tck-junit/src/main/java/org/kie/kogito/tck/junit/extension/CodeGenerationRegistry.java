/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.tck.junit.extension;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.kie.kogito.codegen.api.Generator;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.core.io.CollectedResourceProducer;
import org.kie.kogito.codegen.decision.DecisionCodegen;
import org.kie.kogito.codegen.prediction.PredictionCodegen;
import org.kie.kogito.codegen.process.ProcessCodegen;
import org.kie.kogito.codegen.rules.IncrementalRuleCodegen;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResourceType;

public class CodeGenerationRegistry {

    private Map<KogitoUnitTestResourceType, BiFunction<KogitoBuildContext, List<String>, Generator>> generatorTypeMap =
            new HashMap<>();

    public CodeGenerationRegistry(String pathTestJava, String pathTestResources) {
        generatorTypeMap.put(KogitoUnitTestResourceType.PROCESS, (context, strings) -> ProcessCodegen.ofCollectedResources(context, toCollectedResources(pathTestResources, strings)));
        generatorTypeMap.put(KogitoUnitTestResourceType.RULES, (context, strings) -> IncrementalRuleCodegen.ofCollectedResources(context, toCollectedResources(pathTestResources, strings)));
        generatorTypeMap.put(KogitoUnitTestResourceType.DECISION, (context, strings) -> DecisionCodegen.ofCollectedResources(context, toCollectedResources(pathTestResources, strings)));

        generatorTypeMap.put(KogitoUnitTestResourceType.JAVA, (context, strings) -> IncrementalRuleCodegen.ofJavaResources(context, toCollectedResources(pathTestJava, strings)));
        generatorTypeMap.put(KogitoUnitTestResourceType.PREDICTION, (context, strings) -> PredictionCodegen.ofCollectedResources(context, toCollectedResources(pathTestResources, strings)));
    }

    private static Collection<CollectedResource> toCollectedResources(String basePath, List<String> strings) {
        File[] files = strings
                .stream()
                .map(resource -> new File(basePath, resource))
                .toArray(File[]::new);
        return CollectedResourceProducer.fromFiles(Paths.get(basePath), files);
    }

    public BiFunction<KogitoBuildContext, List<String>, Generator> get(KogitoUnitTestResourceType type) {
        if(!generatorTypeMap.containsKey(type)) {
            throw new RuntimeException ("No code generato registered for type " + type);
        }
        return generatorTypeMap.get(type);
    }
}
