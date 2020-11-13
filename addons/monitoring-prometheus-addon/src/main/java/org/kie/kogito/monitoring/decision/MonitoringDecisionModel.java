/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kie.kogito.monitoring.decision;

import java.util.Map;

import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.FEELPropertyAccessible;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.monitoring.system.metrics.DMNResultMetricsBuilder;

public class MonitoringDecisionModel implements DecisionModel {

    private final DecisionModel wrapped;

    public MonitoringDecisionModel(DecisionModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public DMNContext newContext(Map<String, Object> inputSet) {
        return wrapped.newContext(inputSet);
    }

    @Override
    public DMNContext newContext(FEELPropertyAccessible inputSet) {
        return wrapped.newContext(inputSet);
    }

    @Override
    public DMNResult evaluateAll(DMNContext context) {
        DMNResult result = wrapped.evaluateAll(context);
        DMNResultMetricsBuilder.generateMetrics(result, wrapped.getDMNModel().getName());
        return result;
    }

    @Override
    public DMNResult evaluateDecisionService(DMNContext context, String decisionServiceName) {
        DMNResult result = wrapped.evaluateDecisionService(context, decisionServiceName);
        DMNResultMetricsBuilder.generateMetrics(result, wrapped.getDMNModel().getName());
        return result;
    }

    @Override
    public DMNModel getDMNModel() {
        return wrapped.getDMNModel();
    }
}
