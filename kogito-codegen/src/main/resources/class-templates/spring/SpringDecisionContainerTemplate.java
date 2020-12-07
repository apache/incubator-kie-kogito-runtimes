@org.springframework.stereotype.Component
@org.springframework.web.context.annotation.ApplicationScope
public class DecisionModels extends org.kie.kogito.dmn.AbstractDecisionModels {

    private final static java.util.function.Function<java.lang.String, org.kie.api.runtime.KieRuntimeFactory> sKieRuntimeFactoryFunction = PredictionModels.sKieRuntimeFactoryFunction;
    private final static org.kie.dmn.api.core.DMNRuntime sDmnRuntime = org.kie.kogito.dmn.DMNKogito.createGenericDMNRuntime(sKieRuntimeFactoryFunction);
    private final static org.kie.kogito.ExecutionIdSupplier sExecIdSupplier = null;

    public DecisionModels(org.kie.kogito.Application app) {
        super();
        setApplication(app);
        setDmnRuntime(sDmnRuntime);
        setExecutionIdSupplier(sExecIdSupplier);
    }
}