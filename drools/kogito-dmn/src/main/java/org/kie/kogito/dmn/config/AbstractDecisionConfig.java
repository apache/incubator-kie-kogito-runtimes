package org.kie.kogito.dmn.config;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kie.kogito.decision.DecisionEventListenerConfig;

public class AbstractDecisionConfig implements org.kie.kogito.decision.DecisionConfig {

    private final DecisionEventListenerConfig decisionEventListener;

    protected AbstractDecisionConfig(
            Iterable<org.kie.kogito.decision.DecisionEventListenerConfig> decisionEventListenerConfigs,
            Iterable<org.kie.dmn.api.core.event.DMNRuntimeEventListener> dmnRuntimeEventListeners) {
        this.decisionEventListener = extractDecisionEventListenerConfig(decisionEventListenerConfigs, dmnRuntimeEventListeners);
    }

    @Override
    public DecisionEventListenerConfig decisionEventListeners() {
        return decisionEventListener;
    }

    private org.kie.kogito.decision.DecisionEventListenerConfig extractDecisionEventListenerConfig(
            Iterable<org.kie.kogito.decision.DecisionEventListenerConfig> decisionEventListenerConfigs,
            Iterable<org.kie.dmn.api.core.event.DMNRuntimeEventListener> dmnRuntimeEventListeners) {
        return this.mergeDecisionEventListenerConfig(java.util.stream.StreamSupport.stream(decisionEventListenerConfigs.spliterator(), false).collect(java.util.stream.Collectors.toList()), java.util.stream.StreamSupport.stream(dmnRuntimeEventListeners.spliterator(), false).collect(java.util.stream.Collectors.toList()));
    }

    private org.kie.kogito.decision.DecisionEventListenerConfig mergeDecisionEventListenerConfig(
            java.util.Collection<org.kie.kogito.decision.DecisionEventListenerConfig> decisionEventListenerConfigs,
            java.util.Collection<org.kie.dmn.api.core.event.DMNRuntimeEventListener> dmnRuntimeEventListeners) {
        return new org.kie.kogito.dmn.config.CachedDecisionEventListenerConfig(merge(decisionEventListenerConfigs, org.kie.kogito.decision.DecisionEventListenerConfig::listeners, dmnRuntimeEventListeners));
    }

    private static <C, L> List<L> merge(Collection<C> configs, Function<C, Collection<L>> configToListeners, Collection<L> listeners) {
        return Stream.concat(configs.stream().flatMap(c -> configToListeners.apply(c).stream()), listeners.stream()).collect(Collectors.toList());
    }
}
