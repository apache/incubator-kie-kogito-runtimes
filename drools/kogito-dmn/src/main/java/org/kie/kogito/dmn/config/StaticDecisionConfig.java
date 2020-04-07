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

package org.kie.kogito.dmn.config;

import org.kie.kogito.decision.DecisionConfig;
import org.kie.kogito.decision.DecisionEventListenerConfig;

public class StaticDecisionConfig implements DecisionConfig {

    private final DecisionEventListenerConfig decisionEventListenerConfig;

    public StaticDecisionConfig(DecisionEventListenerConfig decisionEventListenerConfig) {
        this.decisionEventListenerConfig = decisionEventListenerConfig;
    }

    @Override
    public DecisionEventListenerConfig decisionEventListeners() {
        return decisionEventListenerConfig;
    }

}
