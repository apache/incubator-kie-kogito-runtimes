package org.kie.kogito.pmml;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.pmml.evaluator.api.executor.PMMLRuntime;
import org.kie.pmml.evaluator.core.PMMLContextImpl;
import org.kie.pmml.evaluator.core.utils.PMMLRequestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PMMLKogitoTest {

    private static final String SOURCE = "test_regression.pmml";
    private static final String MODEL_NAME = "LinReg";

    @Test
    void createGenericPMMLRuntime() {
        PMMLRuntime pmmlRuntime = PMMLKogito.createGenericPMMLRuntime(new InputStreamReader(PMMLKogitoTest.class.getResourceAsStream(SOURCE)));
        assertEquals(1, pmmlRuntime.getModels().size());
    }

    @Test
    void modelByName() {
        PMMLRuntime pmmlRuntime = PMMLKogito.createGenericPMMLRuntime(new InputStreamReader(PMMLKogitoTest.class.getResourceAsStream(SOURCE)));
        assertTrue(pmmlRuntime.getModel(MODEL_NAME).isPresent());
    }

    @Test
    void evaluate() {
        PMMLRuntime pmmlRuntime = PMMLKogito.createGenericPMMLRuntime(new InputStreamReader(PMMLKogitoTest.class.getResourceAsStream(SOURCE)));
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("fld1", 35.3);
        inputData.put("fld2", 16.3);
        inputData.put("fld3", "y");
        final PMMLRequestData pmmlRequestData = getPMMLRequestData(MODEL_NAME, inputData);
        PMML4Result retrieved =  pmmlRuntime.evaluate(MODEL_NAME, new PMMLContextImpl(pmmlRequestData));
        assertNotNull(retrieved);
    }

    private PMMLRequestData getPMMLRequestData(String modelName, Map<String, Object> parameters) {
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