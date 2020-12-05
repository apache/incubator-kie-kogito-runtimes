package $Package$;

import org.kie.kogito.StaticApplication;

public class Application extends StaticApplication {

    public Application() {
        super(new ApplicationConfig(),
                null /* $Processes$ */,
                null /* $RuleUnits$ */,
                null /* $DecisionModels$ */,
                null /* $PredictionModels$ */;
    }
}
