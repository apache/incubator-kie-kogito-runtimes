
public class DecisionModels implements org.kie.kogito.decision.DecisionModels {

    private final static org.kie.dmn.api.core.DMNRuntime dmnRuntime = org.kie.kogito.dmn.DMNKogito.createGenericDMNRuntime();
    private org.kie.kogito.ExecutionIdSupplier execIdSupplier = null;

    public void init(org.kie.kogito.Application app) {
        app.config().decision().decisionEventListeners().listeners().forEach(dmnRuntime::addListener);
        if (requiresExecutionIdSupplier(app)) {
            execIdSupplier = new org.kie.kogito.dmn.DmnExecutionIdSupplier();
        }
    }

    private boolean requiresExecutionIdSupplier(org.kie.kogito.Application app) {
        return app.config().addons().availableAddons().contains("tracing-decision");
    }

    public org.kie.kogito.decision.DecisionModel getDecisionModel(java.lang.String namespace, java.lang.String name) {
        return new org.kie.kogito.dmn.DmnDecisionModel(dmnRuntime, namespace, name, execIdSupplier);
    }

}
