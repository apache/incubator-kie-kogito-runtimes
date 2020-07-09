
public class PredictionModels implements org.kie.kogito.prediction.PredictionModels {

    private final static org.kie.pmml.evaluator.api.executor.PMMLRuntime pmmlRuntime = org.kie.kogito.pmml.PMMLKogito.createGenericPMMLRuntime();
    private final static org.kie.kogito.ExecutionIdSupplier execIdSupplier = null;

    public void init(org.kie.kogito.Application app) {
//        app.config().prediction().predictionventListeners().listeners().forEach(dmnRuntime::addListener);
    }

    public org.kie.kogito.prediction.PredictionModel getPredictionModel(java.lang.String modelName) {
        return new org.kie.kogito.pmml.PmmlDecisionModel(pmmlRuntime, modelName, execIdSupplier);
    }

}
