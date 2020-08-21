/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.explainability;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kie.kogito.explainability.model.ModelIdentifier;
import org.kie.kogito.explainability.model.PredictInput;
import org.kie.kogito.explainability.model.PredictOutput;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.kie.kogito.explainability.Constants.MODEL_NAME;
import static org.kie.kogito.explainability.Constants.MODEL_NAMESPACE;

class SpringBootExplainableResourceTest {

    SpringBootExplainableResource resource = new SpringBootExplainableResource(new ApplicationMock());

    @Test
    @SuppressWarnings("unchecked")
    void explainServiceTest() {
        List<PredictInput> inputs = singletonList(createInput());

        List<PredictOutput> outputs = (List<PredictOutput>) resource.predict(inputs).getBody();

        Assertions.assertNotNull(outputs);
        Assertions.assertEquals(1, outputs.size());

        PredictOutput output = outputs.get(0);

        Assertions.assertNotNull(output.getResult());
        Assertions.assertNotNull(output.getModelIdentifier());
        Map<String, Object> result = output.getResult();

        Assertions.assertTrue(result.containsKey("Should the driver be suspended?"));
        Assertions.assertEquals("Yes", result.get("Should the driver be suspended?"));
        Assertions.assertTrue(result.containsKey("Fine"));
        Map<String, Object> expectedFine = new HashMap<>();
        expectedFine.put("Points", BigDecimal.valueOf(7));
        expectedFine.put("Amount", BigDecimal.valueOf(1000));
        Assertions.assertEquals(expectedFine.get("Points"), ((Map<String, Object>)result.get("Fine")).get("Points"));
        Assertions.assertEquals(expectedFine.get("Amount"), ((Map<String, Object>)result.get("Fine")).get("Amount"));
    }

    private PredictInput createInput() {
        String resourceId = String.format("%s:%s", MODEL_NAMESPACE, MODEL_NAME);

        Map<String, Object> driver = new HashMap<>();
        driver.put("Age", 25);
        driver.put("Points", 100);

        Map<String, Object> violation = new HashMap<>();
        violation.put("Type", "speed");
        violation.put("Actual Speed", 120);
        violation.put("Speed Limit", 40);

        Map<String, Object> payload = new HashMap<>();
        payload.put("Driver", driver);
        payload.put("Violation", violation);

        ModelIdentifier modelIdentifier = new ModelIdentifier("dmn", resourceId);
        return new PredictInput(modelIdentifier, payload);
    }
}
