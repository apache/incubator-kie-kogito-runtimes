import java.util.Map;
import java.util.Objects;

import org.kie.api.io.Resource;

import static org.kie.pmml.commons.utils.KiePMMLModelUtils.getSanitizedPackageName;

public class PredictionModels implements org.kie.kogito.prediction.PredictionModels {

    public final static java.util.Map<java.lang.String, org.kie.pmml.evaluator.api.executor.PMMLRuntime> pmmlRuntimes = org.kie.kogito.pmml.PMMLKogito.createPMMLRuntimes();
    private final static org.kie.kogito.ExecutionIdSupplier execIdSupplier = null;

    public void init(org.kie.kogito.Application app) {
    }

    public org.kie.kogito.prediction.PredictionModel getPredictionModel(java.lang.String modelName) {
        return new org.kie.kogito.pmml.PmmlPredictionModel(getPMMLRuntime(modelName), modelName, execIdSupplier);
    }

    private org.kie.pmml.evaluator.api.executor.PMMLRuntime getPMMLRuntime(java.lang.String modelName) {
        return pmmlRuntimes.values().stream()
                .filter(pmmlRuntime ->  pmmlRuntime.getModels().stream().anyMatch(kiePMMLModel -> {
                    String originalSanitizedModelName = org.kie.pmml.commons.utils.KiePMMLModelUtils.getSanitizedPackageName(kiePMMLModel.getName());
                    return java.util.Objects.equals(sanitizedModelName, originalSanitizedModelName);
                }))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Failed to find a PMMLRuntime for %s", sanitizedModelName)));
    }
}

