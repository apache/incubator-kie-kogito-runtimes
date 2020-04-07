import org.kie.kogito.decision.DecisionConfig;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.rules.RuleConfig;


public class ApplicationConfig implements org.kie.kogito.Config {
  
    protected ProcessConfig processConfig;
    protected RuleConfig ruleConfig;
    protected DecisionConfig decisionConfig;

    @Override
    public ProcessConfig process() {
        return processConfig;
    }

    @Override
    public RuleConfig rule() {
        return ruleConfig;
    }

    @Override
    public DecisionConfig decision() {
        return decisionConfig;
    }

}
