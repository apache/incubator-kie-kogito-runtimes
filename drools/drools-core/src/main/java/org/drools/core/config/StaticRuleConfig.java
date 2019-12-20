/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.drools.core.config;

import org.kie.kogito.rules.ClockType;
import org.kie.kogito.rules.EventProcessingMode;
import org.kie.kogito.rules.RuleConfig;
import org.kie.kogito.rules.RuleEventListenerConfig;

public class StaticRuleConfig implements RuleConfig {
    
    private final RuleEventListenerConfig ruleEventListenerConfig;
    private final EventProcessingMode eventProcessing;
    private final ClockType clockType;

    public StaticRuleConfig(RuleEventListenerConfig ruleEventListenerConfig, EventProcessingMode eventProcessing, ClockType clockType) {
        this.ruleEventListenerConfig = ruleEventListenerConfig;
        this.eventProcessing = eventProcessing;
        this.clockType = clockType;
    }

    public StaticRuleConfig(RuleEventListenerConfig ruleEventListenerConfig) {
        this(ruleEventListenerConfig, EventProcessingMode.Cloud, ClockType.RealTime);
    }

    @Override
    public RuleEventListenerConfig ruleEventListeners() {
        return ruleEventListenerConfig;
    }

    @Override
    public EventProcessingMode eventProcessingMode() {
        return eventProcessing;
    }

    @Override
    public ClockType clockType() {
        return clockType;
    }
}
