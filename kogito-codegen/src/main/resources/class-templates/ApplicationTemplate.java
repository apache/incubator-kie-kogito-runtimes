package $Package$;

import org.kie.kogito.Config;
import org.kie.kogito.uow.UnitOfWorkManager;

public class Application extends org.kie.kogito.AbstractApplication {

    public Application() {
        this.config = new ApplicationConfig();
        this.processes = null /* $Processes$ */;
        this.ruleUnits = null /* $RuleUnits$ */;
        this.decisionModels = null /* $DecisionModels$ */;
        this.predictionModels = null /* $PredictionModels$ */;

        if (config().process() != null) {
            unitOfWorkManager().eventManager().setAddons(config().addons());
        }
    }
}
