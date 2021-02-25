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
package org.kie.kogito.pmml;

import java.util.Map;

import org.kie.api.pmml.PMML4Result;
import org.kie.kogito.Application;

public abstract class AbstractPMMLRestResource {

    protected Object result(Application application, String modelName, Map<String, Object> variables) {
        org.kie.kogito.prediction.PredictionModel prediction = application.get(org.kie.kogito.prediction.PredictionModels.class).getPredictionModel(modelName);
        org.kie.api.pmml.PMML4Result pmml4Result = prediction.evaluateAll(prediction.newContext(variables));
        return java.util.Collections.singletonMap(pmml4Result.getResultObjectName(), pmml4Result.getResultVariables().get(pmml4Result.getResultObjectName()));
    }

    protected PMML4Result descriptive(Application application, String modelName, Map<String, Object> variables) {
        org.kie.kogito.prediction.PredictionModel prediction = application.get(org.kie.kogito.prediction.PredictionModels.class).getPredictionModel(modelName);
        return prediction.evaluateAll(prediction.newContext(variables));
    }

    public static String getJsonErrorMessage(Exception e) {
        String errorMessage = String.format("%1$s: %2$s", e.getClass().getName(), e.getMessage() != null ? e.getMessage() : "");
        return String.format("{\"exception\" : \"%s\"}", errorMessage);
    }
}
