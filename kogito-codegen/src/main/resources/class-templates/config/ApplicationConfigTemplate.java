import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.drools.core.config.StaticRuleConfig;
import org.kie.kogito.dmn.config.StaticDecisionConfig;
import org.kie.kogito.process.impl.StaticProcessConfig;

public class ApplicationConfig implements org.kie.kogito.Config {

    protected org.kie.kogito.process.ProcessConfig processConfig;
    protected org.kie.kogito.rules.RuleConfig ruleConfig;
    protected org.kie.kogito.decision.DecisionConfig decisionConfig;

    public ApplicationConfig() {
        this(new StaticProcessConfig(),
             new StaticRuleConfig(),
             new StaticDecisionConfig());
    }

    public ApplicationConfig(
            org.kie.kogito.process.ProcessConfig processConfig,
            org.kie.kogito.rules.RuleConfig ruleConfig,
            org.kie.kogito.decision.DecisionConfig decisionConfig) {
        this.processConfig = processConfig;
        this.ruleConfig = ruleConfig;
        this.decisionConfig = decisionConfig;
    }

    @javax.inject.Inject
    public ApplicationConfig(
            javax.enterprise.inject.Instance<org.kie.kogito.process.ProcessConfig> processConfig,
            javax.enterprise.inject.Instance<org.kie.kogito.rules.RuleConfig> ruleConfig,
            javax.enterprise.inject.Instance<org.kie.kogito.decision.DecisionConfig> decisionConfig) {
        this(orDefault(processConfig, StaticProcessConfig::new),
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


    @Override
    public org.kie.kogito.process.ProcessConfig process() {
        return processConfig;
    }

    @Override
    public org.kie.kogito.rules.RuleConfig rule() {
        return ruleConfig;
    }

    @Override
    public org.kie.kogito.decision.DecisionConfig decision() {
        return decisionConfig;
    }

}
