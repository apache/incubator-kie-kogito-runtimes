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
package org.kie.kogito.spring;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.Application;
import org.kie.kogito.decision.DecisionModels;
import org.kie.kogito.prediction.PredictionModels;
import org.kie.kogito.process.Processes;
import org.kie.kogito.rules.RuleUnits;
import org.kie.kogito.examples.KogitoSpringbootApplication;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KogitoSpringbootApplication.class)
public class NoRuntimeTest {

    @Autowired
    Collection<DecisionModels> decisionModels;

    @Autowired
    Collection<RuleUnits> ruleUnits;

    @Autowired
    Collection<Processes> processes;

    @Autowired
    Collection<PredictionModels> predictionModels;

    @Autowired
    Application application;

    @Test
    public void testEvaluateTrafficViolation() {
        assertThat(decisionModels).hasSize(0);
        assertThat(application.get(DecisionModels.class)).isNull();

        assertThat(ruleUnits).hasSize(0);
        assertThat(application.get(RuleUnits.class)).isNull();

        assertThat(processes).hasSize(0);
        assertThat(application.get(Processes.class)).isNull();

        assertThat(predictionModels).hasSize(0);
        assertThat(application.get(PredictionModels.class)).isNull();
    }
}
