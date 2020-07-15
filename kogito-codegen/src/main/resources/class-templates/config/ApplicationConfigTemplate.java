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

    private org.kie.kogito.process.ProcessConfig processConfig = new StaticProcessConfig();
    private org.kie.kogito.rules.RuleConfig ruleConfig = new StaticRuleConfig();
    private org.kie.kogito.decision.DecisionConfig decisionConfig = new StaticDecisionConfig();

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
