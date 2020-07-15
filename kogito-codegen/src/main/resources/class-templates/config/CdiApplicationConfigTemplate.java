import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.drools.core.config.StaticRuleConfig;
import org.kie.kogito.dmn.config.StaticDecisionConfig;
import org.kie.kogito.process.impl.StaticProcessConfig;

@javax.inject.Singleton
public class ApplicationConfig extends org.kie.kogito.StaticConfig {

    @javax.inject.Inject
    public ApplicationConfig(
            javax.enterprise.inject.Instance<org.kie.kogito.process.ProcessConfig> processConfig,
            javax.enterprise.inject.Instance<org.kie.kogito.rules.RuleConfig> ruleConfig,
            javax.enterprise.inject.Instance<org.kie.kogito.decision.DecisionConfig> decisionConfig) {
        super(orDefault(processConfig, StaticProcessConfig::new),
              orDefault(ruleConfig, StaticRuleConfig::new),
              orDefault(decisionConfig, StaticDecisionConfig::new));
    }

    private static <T> T orDefault(javax.enterprise.inject.Instance<T> instance, Supplier<T> supplier) {
        if (instance.isUnsatisfied()) {
            return supplier.get();
        } else {
            return instance.get();
        }
    }

}
