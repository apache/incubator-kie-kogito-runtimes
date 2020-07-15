/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.tracing.decision.explainability;

import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.kogito.Application;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.decision.DecisionModels;
import org.kie.kogito.tracing.decision.explainability.model.ModelIdentifier;
import org.kie.kogito.tracing.decision.explainability.model.PredictInput;
import org.kie.kogito.tracing.decision.explainability.model.PredictOutput;

import java.util.Collections;
import java.util.Map;

import static org.kie.kogito.tracing.decision.DecisionTracingListener.SKIP_TRACING;
import static org.kie.kogito.tracing.decision.explainability.model.ModelIdentifier.RESOURCE_ID_SEPARATOR;

public class DecisionExplainabilityResourceExecutor implements ExplainabilityResourceExecutor {

    @Override
    public boolean acceptRequest(PredictInput predictInput) {
        return "dmn".equalsIgnoreCase(predictInput.getModelIdentifier().getResourceType());
    }

    @Override
    public PredictOutput processRequest(Application application, PredictInput predictInput) {
        DecisionModel decisionModel = getDecisionModel(application.decisionModels(), predictInput.getModelIdentifier());
        DMNContext dmnContext = decisionModel.newContext(convertDMNInput(predictInput, decisionModel.getDMNModel()));
        dmnContext.getMetadata().set(SKIP_TRACING, true);
        return convertDMNOutput(decisionModel.evaluateAll(dmnContext), predictInput);
    }

    public DecisionModel getDecisionModel(DecisionModels decisionModels, ModelIdentifier modelIdentifier) {
        String[] modelIdentifierParts = modelIdentifier.getResourceId().split(RESOURCE_ID_SEPARATOR);
        if(modelIdentifierParts.length != 2) {
            throw new IllegalArgumentException("Malformed resourceId " + modelIdentifier.getResourceId());
        }
        return decisionModels.getDecisionModel(modelIdentifierParts[0], modelIdentifierParts[1]);
    }

    public Map<String, Object> convertDMNInput(PredictInput predictInput, DMNModel model) {
        // FIXME to implement
        return Collections.emptyMap();
    }

    public PredictOutput convertDMNOutput(DMNResult dmnResult, PredictInput predictInput) {
        // FIXME to implement
        return null;
    }


}
