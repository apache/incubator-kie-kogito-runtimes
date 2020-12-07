public class PredictionModels extends org.kie.kogito.pmml.AbstractPredictionModels {

    public final static java.util.function.Function<java.lang.String, org.kie.api.runtime.KieRuntimeFactory> sKieRuntimeFactoryFunction;

    static {
        final java.util.Map<org.kie.api.KieBase, org.kie.api.runtime.KieRuntimeFactory> kieRuntimeFactories = org.kie.kogito.pmml.PMMLKogito.createKieRuntimeFactories();
        sKieRuntimeFactoryFunction = new java.util.function.Function<java.lang.String, org.kie.api.runtime.KieRuntimeFactory>() {
            @Override
            public org.kie.api.runtime.KieRuntimeFactory apply(java.lang.String s) {
                return kieRuntimeFactories.keySet().stream()
                        .filter(kieBase -> org.kie.pmml.evaluator.core.utils.KnowledgeBaseUtils.getModel(kieBase, s).isPresent())
                        .map(kieBase ->  kieRuntimeFactories.get(kieBase))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Failed to find KieRuntimeFactory for model " +s));
            }
        };
    }

    // Application parameter is needed to match expected behavior of ApplicationTemplate:
    //       $PredictionModels$ is replaced with new PredictionModels(this)
    public PredictionModels(org.kie.kogito.Application application) {
        super();
        setKieRuntimeFactoryFunction(sKieRuntimeFactoryFunction);
    }
}