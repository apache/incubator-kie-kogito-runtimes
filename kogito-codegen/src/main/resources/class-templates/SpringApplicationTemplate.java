package $Package$;

import org.kie.kogito.Config;
import org.kie.kogito.decision.DecisionModels;
import org.kie.kogito.process.Processes;
import org.kie.kogito.rules.RuleUnits;

@org.springframework.stereotype.Component
@org.springframework.web.context.annotation.ApplicationScope
public class Application extends org.kie.kogito.AbstractApplication {

    @org.springframework.beans.factory.annotation.Autowired()
    public Application(
            Config config,
            java.util.Collection<Processes> processes/*,
            java.util.Collection<RuleUnits> ruleUnits,
            java.util.Collection<DecisionModels> decisionModels*/) {
        this.config = config;
        this.processes = orNull(processes);
        this.ruleUnits = null /* $RuleUnits$ */;
        this.decisionModels = null /* $DecisionModels$ */;

        if (config().process() != null) {
            unitOfWorkManager().eventManager().setAddons(config().addons());
        }
    }

    private static <T> T orNull(java.util.Collection<T> collection) {
        if (collection.isEmpty()) {
            return null;
        } else {
            return collection.iterator().next();
        }
    }

}
