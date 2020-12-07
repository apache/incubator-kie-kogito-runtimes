@javax.enterprise.context.ApplicationScoped()
public class PredictionModels extends org.kie.kogito.pmml.AbstractPredictionModels {

    public final static java.util.function.Function<String, org.kie.api.runtime.KieRuntimeFactory> sKieRuntimeFactoryFunction;

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

    public PredictionModels() {
        super();
        setKieRuntimeFactoryFunction(sKieRuntimeFactoryFunction);
    }
}