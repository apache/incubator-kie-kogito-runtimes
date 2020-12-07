@javax.enterprise.context.ApplicationScoped()
public class DecisionModels extends org.kie.kogito.dmn.AbstractDecisionModels {

    private final static java.util.function.Function<java.lang.String, org.kie.api.runtime.KieRuntimeFactory> sKieRuntimeFactoryFunction = PredictionModels.sKieRuntimeFactoryFunction;
    private final static org.kie.dmn.api.core.DMNRuntime sDmnRuntime = org.kie.kogito.dmn.DMNKogito.createGenericDMNRuntime(sKieRuntimeFactoryFunction);
    private final static org.kie.kogito.ExecutionIdSupplier sExecIdSupplier = null;

    @javax.inject.Inject
    protected org.kie.kogito.Application application;

    public DecisionModels() {
        super();
    }

    @javax.annotation.PostConstruct
    protected void init() {
        setApplication(application);
        setDmnRuntime(sDmnRuntime);
        setExecutionIdSupplier(sExecIdSupplier);
    }
}
