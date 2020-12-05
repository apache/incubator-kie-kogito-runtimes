package $Package$;

import org.kie.kogito.Config;
import org.kie.kogito.StaticApplication;
import org.kie.kogito.process.Processes;
import org.kie.kogito.rules.RuleUnits;

@javax.inject.Singleton
public class Application extends StaticApplication {

    @javax.inject.Inject
    public Application(
            Config config,
            javax.enterprise.inject.Instance<Processes> processes,
            javax.enterprise.inject.Instance<RuleUnits> ruleUnits,
            javax.enterprise.inject.Instance<DecisionModels> decisionModels,
            java.util.Collection<PredictionModels> predictionModels) {
        super(config,
                orNull(processes),
                orNull(ruleUnits),
                orNull(decisionModels),
                orNull(predictionModels);
    }

    private static <T> T orNull(javax.enterprise.inject.Instance<T> instance) {
        if (instance.isUnsatisfied()) {
            return null;
        } else {
            return instance.get();
        }
    }

}
