/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.drools.core.config.StaticRuleConfig;
import org.kie.kogito.dmn.config.StaticDecisionConfig;
import org.kie.kogito.pmml.config.StaticPredictionConfig;
import org.kie.kogito.process.impl.StaticProcessConfig;

import javax.enterprise.inject.Instance;

@javax.inject.Singleton
public class ApplicationConfig extends org.kie.kogito.StaticConfig {

    @javax.inject.Inject
    public ApplicationConfig(
            Instance<org.kie.kogito.process.ProcessConfig> processConfig,
            Instance<org.kie.kogito.rules.RuleConfig> ruleConfig,
            Instance<org.kie.kogito.decision.DecisionConfig> decisionConfig,
            Instance<org.kie.kogito.prediction.PredictionConfig> predictionConfig) {
        super($Addons$,
              orDefault(processConfig, StaticProcessConfig::new),
              orDefault(ruleConfig, StaticRuleConfig::new),
              orDefault(decisionConfig, StaticDecisionConfig::new),
              orDefault(predictionConfig, StaticPredictionConfig::new));
    }

    private static <T> T orDefault(javax.enterprise.inject.Instance<T> instance, Supplier<T> supplier) {
        if (instance.isUnsatisfied()) {
            return supplier.get();
        } else {
            return instance.get();
        }
    }
}
