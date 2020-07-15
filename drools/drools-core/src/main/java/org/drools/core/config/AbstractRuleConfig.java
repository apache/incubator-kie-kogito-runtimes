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

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kie.kogito.rules.RuleConfig;
import org.kie.kogito.rules.RuleEventListenerConfig;

public abstract class AbstractRuleConfig implements RuleConfig {

    private final RuleEventListenerConfig ruleEventListenerConfig;

    public AbstractRuleConfig(RuleEventListenerConfig ruleEventListenerConfig) {
        this.ruleEventListenerConfig = ruleEventListenerConfig;
    }

    public AbstractRuleConfig(
            Iterable<org.kie.kogito.rules.RuleEventListenerConfig> ruleEventListenerConfigs,
            Iterable<org.kie.api.event.rule.AgendaEventListener> agendaEventListeners,
            Iterable<org.kie.api.event.rule.RuleRuntimeEventListener> ruleRuntimeEventListeners) {
        this.ruleEventListenerConfig = extractRuleEventListenerConfig(ruleEventListenerConfigs, agendaEventListeners, ruleRuntimeEventListeners);
    }

    @Override
    public RuleEventListenerConfig ruleEventListeners() {
        return ruleEventListenerConfig;
    }

    private org.kie.kogito.rules.RuleEventListenerConfig extractRuleEventListenerConfig(
            Iterable<RuleEventListenerConfig> ruleEventListenerConfigs,
            Iterable<org.kie.api.event.rule.AgendaEventListener> agendaEventListeners,
            Iterable<org.kie.api.event.rule.RuleRuntimeEventListener> ruleRuntimeEventListeners) {
        return this.mergeRuleEventListenerConfig(java.util.stream.StreamSupport.stream(ruleEventListenerConfigs.spliterator(), false).collect(java.util.stream.Collectors.toList()), java.util.stream.StreamSupport.stream(agendaEventListeners.spliterator(), false).collect(java.util.stream.Collectors.toList()), java.util.stream.StreamSupport.stream(ruleRuntimeEventListeners.spliterator(), false).collect(java.util.stream.Collectors.toList()));
    }

    private org.kie.kogito.rules.RuleEventListenerConfig mergeRuleEventListenerConfig(java.util.Collection<org.kie.kogito.rules.RuleEventListenerConfig> ruleEventListenerConfigs, java.util.Collection<org.kie.api.event.rule.AgendaEventListener> agendaEventListeners, java.util.Collection<org.kie.api.event.rule.RuleRuntimeEventListener> ruleRuntimeEventListeners) {
        return new org.drools.core.config.CachedRuleEventListenerConfig(merge(ruleEventListenerConfigs, org.kie.kogito.rules.RuleEventListenerConfig::agendaListeners, agendaEventListeners), merge(ruleEventListenerConfigs, org.kie.kogito.rules.RuleEventListenerConfig::ruleRuntimeListeners, ruleRuntimeEventListeners));
    }

    private static <C, L> List<L> merge(Collection<C> configs, Function<C, Collection<L>> configToListeners, Collection<L> listeners) {
        return Stream.concat(configs.stream().flatMap(c -> configToListeners.apply(c).stream()), listeners.stream()).collect(Collectors.toList());
    }
}
