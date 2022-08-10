/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.spring.pmml;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.pmml.PMML4Result;
import org.kie.kogito.examples.KogitoSpringbootApplication;
import org.kie.kogito.prediction.PredictionModel;
import org.kie.kogito.prediction.PredictionModels;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KogitoSpringbootApplication.class)
class PMMLRegressionTest {

    @Autowired
    PredictionModels predictionModels;

    @Test
    void testEvaluateLinRegResult() {
        PredictionModel linReg = predictionModels.getPredictionModel("PMMLRegression.pmml", "LinReg");

        Map<String, Object> context = new HashMap<>();
        context.put("fld1", 3.0);
        context.put("fld2", 2.0);
        context.put("fld3", "y");

        PMML4Result pmml4Result = linReg.evaluateAll(linReg.newContext(context));

        double fld4 = (double) pmml4Result.getResultVariables().get("fld4");
        assertThat(fld4).isEqualTo(52.5d, Offset.offset(0.1d));
    }
}
