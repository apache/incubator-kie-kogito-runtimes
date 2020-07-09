package org.kie.kogito.pmml;

import java.util.Map;

import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.kogito.ExecutionIdSupplier;
import org.kie.kogito.prediction.PredictionModel;
import org.kie.pmml.commons.model.KiePMMLModel;
import org.kie.pmml.evaluator.api.executor.PMMLContext;
import org.kie.pmml.evaluator.api.executor.PMMLRuntime;
import org.kie.pmml.evaluator.core.PMMLContextImpl;
import org.kie.pmml.evaluator.core.utils.PMMLRequestDataBuilder;

public class PmmlDecisionModel implements PredictionModel {

    private final PMMLRuntime pmmlRuntime;
    private final ExecutionIdSupplier execIdSupplier;
    private final KiePMMLModel pmmlModel;

    public PmmlDecisionModel(PMMLRuntime pmmlRuntime, String modelName) {
        this(pmmlRuntime, modelName, null);
    }

    public PmmlDecisionModel(PMMLRuntime pmmlRuntime, String modelName, ExecutionIdSupplier execIdSupplier) {
        this.pmmlRuntime = pmmlRuntime;
        this.execIdSupplier = execIdSupplier;
        this.pmmlModel = pmmlRuntime.getModel(modelName).orElseThrow(() -> new IllegalStateException("PMML model '" + modelName + "' not found in the inherent PMMLRuntime."));
    }

    @Override
    public PMMLContext newContext(Map<String, Object> variables) {
        final PMMLRequestData pmmlRequestData = getPMMLRequestData(pmmlModel.getName(), variables);
        return new PMMLContextImpl(pmmlRequestData);
    }


    @Override
    public PMML4Result evaluateAll(PMMLContext context) {
        return pmmlRuntime.evaluate(pmmlModel.getName(), context);
    }

    @Override
    public KiePMMLModel getKiePMMLModel() {
        return pmmlModel;
    }

    private static PMMLRequestData getPMMLRequestData(String modelName, Map<String, Object> parameters) {
        String correlationId = "CORRELATION_ID";
        PMMLRequestDataBuilder pmmlRequestDataBuilder = new PMMLRequestDataBuilder(correlationId, modelName);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            Object pValue = entry.getValue();
            Class class1 = pValue.getClass();
            pmmlRequestDataBuilder.addParameter(entry.getKey(), pValue, class1);
        }
        return pmmlRequestDataBuilder.build();
    }

}
