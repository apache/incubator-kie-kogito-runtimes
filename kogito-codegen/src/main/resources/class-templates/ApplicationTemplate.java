package $Package$;

import org.kie.kogito.StaticApplication;

public class Application extends StaticApplication {

    public Application() {
        super(new ApplicationConfig(),
                $Processes$,
                $RuleUnits$,
                $DecisionModels$,
                $PredictionModels$);
    }
}
