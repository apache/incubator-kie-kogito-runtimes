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
package org.kie.kogito.quarkus.runtime.decisions.graal.graal;

import java.io.Reader;
import java.util.function.Function;

import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.kogito.ExecutionIdSupplier;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.dmn.DMNKogitoCallbacks;
import org.slf4j.Logger;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * In a Kogito Quarkus application, we rely on the behaviour that in a Native Image build:
 * "By default, Quarkus initializes all classes at build time"
 *  ref: https://quarkus.io/guides/writing-native-applications-tips#delaying-class-initialization
 * These are internal callbacks that should occur only at Build (init) time,
 * so these substitutions might help mitigate by detecting early any potential change in expectations
 * (e.g.: a customized, forced NI configuration change).
 */
@TargetClass(DMNKogitoCallbacks.class)
final class DMNKogitoCallbacksSubstitutions {

    @Alias
    private static Logger LOG;

    private DMNKogitoCallbacksSubstitutions() {
        // intentionally private.
    }

    @Substitute
    public static void beforeCreateGenericDMNRuntime(Function<String, KieRuntimeFactory> kiePMMLRuntimeFactoryFunction, Reader[] readers) {
        LOG.warn("createGenericDMNRuntime with {} model(s) for DMNRuntime initialization...", readers.length);
    }

    @Substitute
    public static void afterCreateGenericDMNRuntime(DMNRuntime dmnRuntime) {
        LOG.warn("createGenericDMNRuntime done. DMNRuntime contains {} DMNModel(s).", dmnRuntime.getModels().size());
    }

    @Substitute
    public static void beforeAbstractDecisionModelsInit(Function<String, KieRuntimeFactory> sKieRuntimeFactoryFunction,
            ExecutionIdSupplier executionIdSupplier,
            Function<DecisionModel, DecisionModel> decisionModelTransformerInit,
            Reader[] readers) {
        LOG.warn("AbstractDecisionModels.init() called.");
    }

    @Substitute
    public static void afterAbstractDecisionModelsInit(DMNRuntime dmnRuntime) {
        LOG.warn("AbstractDecisionModels.init() done.");
    }
}
