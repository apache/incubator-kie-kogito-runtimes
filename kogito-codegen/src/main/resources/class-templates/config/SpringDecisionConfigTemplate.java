import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kie.kogito.decision.DecisionEventListenerConfig;
import org.kie.kogito.dmn.config.AbstractDecisionConfig;
import org.kie.kogito.rules.RuleEventListenerConfig;

@org.springframework.stereotype.Component
class DecisionConfig extends AbstractDecisionConfig {

    @org.springframework.beans.factory.annotation.Autowired
    public DecisionConfig(
            List<org.kie.kogito.decision.DecisionEventListenerConfig> decisionEventListenerConfigs,
            List<org.kie.dmn.api.core.event.DMNRuntimeEventListener> dmnRuntimeEventListeners) {
        super(decisionEventListenerConfigs, dmnRuntimeEventListeners);
    }

}
