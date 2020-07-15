import org.kie.api.io.Resource;

public class PredictionModels implements org.kie.kogito.prediction.PredictionModels {

    public final static java.util.Map<java.lang.String, org.kie.pmml.evaluator.api.executor.PMMLRuntime> pmmlRuntimes = org.kie.kogito.pmml.PMMLKogito.createPMMLRuntimes();
    private final static org.kie.kogito.ExecutionIdSupplier execIdSupplier = null;

    public void init(org.kie.kogito.Application app) {
    }

    public org.kie.kogito.prediction.PredictionModel getPredictionModel(java.lang.String modelName) {
        return new org.kie.kogito.pmml.PmmlPredictionModel(pmmlRuntimes.get(modelName), modelName, execIdSupplier);
    }
}

