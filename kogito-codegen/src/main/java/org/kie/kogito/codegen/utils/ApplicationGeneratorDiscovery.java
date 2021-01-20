/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen.utils;

import org.kie.kogito.codegen.core.ApplicationGenerator;
import org.kie.kogito.codegen.api.Generator;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.decision.DecisionCodegen;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.core.io.CollectedResourceProducer;
import org.kie.kogito.codegen.decision.DecisionCodegenFactory;
import org.kie.kogito.codegen.prediction.PredictionCodegen;
import org.kie.kogito.codegen.prediction.PredictionCodegenFactory;
import org.kie.kogito.codegen.process.ProcessCodegen;
import org.kie.kogito.codegen.process.ProcessCodegenFactory;
import org.kie.kogito.codegen.rules.IncrementalRuleCodegen;
import org.kie.kogito.codegen.rules.IncrementalRuleCodegenFactory;

import java.util.Arrays;
import java.util.Collection;

/**
 * Utility class that performs automatic ApplicationGenerator discovery
 */
public class ApplicationGeneratorDiscovery {

    private ApplicationGeneratorDiscovery() {
        // utility class
    }

    public static ApplicationGenerator discover(KogitoBuildContext context) {
        ApplicationGenerator appGen = new ApplicationGenerator(context);

        loadGenerators(context)
                .forEach(appGen::registerGeneratorIfEnabled);

        return appGen;
    }

    protected static Collection<Generator> loadGenerators(KogitoBuildContext context) {
        Collection<CollectedResource> collectedResources = CollectedResourceProducer.fromPaths(context.getAppPaths().getPaths());

        // ordering is relevant.
        return Arrays.asList(
                new ProcessCodegenFactory().create(context, collectedResources),
                new IncrementalRuleCodegenFactory().create(context, collectedResources),
                new PredictionCodegenFactory().create(context, collectedResources),
                new DecisionCodegenFactory().create(context, collectedResources)
        );
    }
}
