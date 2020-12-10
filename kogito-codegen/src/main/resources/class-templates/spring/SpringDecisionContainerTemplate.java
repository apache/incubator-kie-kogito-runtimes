@org.springframework.stereotype.Component
@org.springframework.web.context.annotation.ApplicationScope
public class DecisionModels extends org.kie.kogito.dmn.AbstractDecisionModels {

    static {
        init(
                PredictionModels.kieRuntimeFactoryFunction
                /* arguments provided during codegen */);
    }

    public DecisionModels(org.kie.kogito.Application app) {
        super(app);
    }
}