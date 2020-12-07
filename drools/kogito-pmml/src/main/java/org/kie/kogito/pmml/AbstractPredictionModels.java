/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.pmml;

import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.kogito.prediction.PredictionModels;

import java.util.function.Function;

public abstract class AbstractPredictionModels implements PredictionModels {

    private java.util.function.Function<String, org.kie.api.runtime.KieRuntimeFactory> kieRuntimeFactoryFunction;

    public org.kie.kogito.prediction.PredictionModel getPredictionModel(java.lang.String modelName) {
        return new org.kie.kogito.pmml.PmmlPredictionModel(getPMMLRuntime(modelName), modelName);
    }

    protected Function<String, KieRuntimeFactory> getKieRuntimeFactoryFunction() {
        return kieRuntimeFactoryFunction;
    }

    protected void setKieRuntimeFactoryFunction(Function<String, KieRuntimeFactory> kieRuntimeFactoryFunction) {
        this.kieRuntimeFactoryFunction = kieRuntimeFactoryFunction;
    }

    private org.kie.pmml.api.runtime.PMMLRuntime getPMMLRuntime(java.lang.String modelName) {
        return getKieRuntimeFactoryFunction().apply(modelName).get(org.kie.pmml.api.runtime.PMMLRuntime.class);
    }
}
