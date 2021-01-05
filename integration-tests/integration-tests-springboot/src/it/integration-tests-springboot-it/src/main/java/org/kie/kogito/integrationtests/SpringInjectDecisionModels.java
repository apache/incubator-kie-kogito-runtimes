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

package org.kie.kogito.integrationtests;

import org.springframework.beans.factory.annotation.Autowired;
import org.kie.kogito.Application;
import org.kie.kogito.decision.DecisionConfig;
import org.kie.kogito.decision.DecisionModels;

public class SpringInjectDecisionModels {

    @Autowired
    public SpringInjectDecisionModels(DecisionModels decisionModels, Application application) {
        if(decisionModels != application.get(DecisionModels.class)) {
            throw new IllegalStateException("DecisionModels should be injectable and same as instance application.get(DecisionModels.class)");
        }
        if(application.config().get(DecisionConfig.class) == null) {
            throw new IllegalStateException("DecisionConfig not available");
        }
    }
}
