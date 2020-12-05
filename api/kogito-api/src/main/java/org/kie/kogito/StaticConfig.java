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

package org.kie.kogito;

import org.kie.kogito.decision.DecisionConfig;
import org.kie.kogito.prediction.PredictionConfig;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.rules.RuleConfig;

import java.util.HashMap;
import java.util.Map;

public class StaticConfig implements Config {

    private final Addons addons;
    private final Map<String, KogitoConfig> configMap = new HashMap<>();

    public StaticConfig(Addons addons, ProcessConfig processConfig, RuleConfig ruleConfig, DecisionConfig decisionConfig, PredictionConfig predictionConfig) {
        this.addons = addons;
        loadConfig(processConfig);
        loadConfig(ruleConfig);
        loadConfig(decisionConfig);
        loadConfig(predictionConfig);

        if (processConfig != null) {
            processConfig.unitOfWorkManager().eventManager().setAddons(addons);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends KogitoConfig> T get(Class<T> clazz) {
        return (T) configMap.get(clazz.getCanonicalName());
    }

    @Override
    public Addons addons() {
        return addons;
    }

    private void loadConfig(KogitoConfig config) {
        if(config != null) {
            configMap.put(config.getClass().getCanonicalName(), config);
        }
    }
}
