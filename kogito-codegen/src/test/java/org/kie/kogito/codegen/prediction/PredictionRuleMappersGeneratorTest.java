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
package org.kie.kogito.codegen.prediction;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PredictionRuleMappersGeneratorTest {

    @Test
    void getPredictionRuleMapperSource() {
        final String predictionRuleMapper = "PredictionRuleMapperImpl";
        final String packageName = "PACKAGE";
        final List<String> generatedRuleMappers = IntStream.range(0, 4).mapToObj(index -> packageName + "." +
                "subPack" + index + "." + predictionRuleMapper).collect(Collectors.toList());
        String retrieved = PredictionRuleMappersGenerator.getPredictionRuleMappersSource(packageName,
                                                                                        generatedRuleMappers);
        assertNotNull(retrieved);
        String expected = String.format("package %s;", packageName);
        assertTrue(retrieved.contains(expected));
        List<String> mod = generatedRuleMappers.stream().map(gen -> "new " + gen + "()").collect(Collectors.toList());
        expected = "Arrays.asList(" + String.join(", ", mod) + ");";
        assertTrue(retrieved.contains(expected));
    }
}
