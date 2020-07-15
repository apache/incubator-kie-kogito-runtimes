import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;

import org.kie.kogito.decision.DecisionEventListenerConfig;
import org.kie.kogito.dmn.config.AbstractDecisionConfig;
import org.kie.kogito.rules.RuleEventListenerConfig;

@javax.inject.Singleton
class DecisionConfig extends AbstractDecisionConfig {

    @javax.inject.Inject
    public DecisionConfig(
            Instance<org.kie.kogito.decision.DecisionEventListenerConfig> decisionEventListenerConfigs,
            Instance<org.kie.dmn.api.core.event.DMNRuntimeEventListener> dmnRuntimeEventListeners) {
        super(decisionEventListenerConfigs, dmnRuntimeEventListeners);
    }

}
