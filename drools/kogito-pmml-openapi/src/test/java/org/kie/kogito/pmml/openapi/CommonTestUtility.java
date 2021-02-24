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
package org.kie.kogito.pmml.openapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.kie.pmml.api.enums.DATA_TYPE;
import org.kie.pmml.api.enums.FIELD_USAGE_TYPE;
import org.kie.pmml.api.enums.OP_TYPE;
import org.kie.pmml.api.enums.RESULT_FEATURE;
import org.kie.pmml.api.models.Interval;
import org.kie.pmml.api.models.MiningField;
import org.kie.pmml.api.models.OutputField;
import org.kie.pmml.commons.model.KiePMMLModel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class CommonTestUtility {

    private CommonTestUtility() {
    }

    public static List<JsonNode> getFromArrayNode(ArrayNode source) {
        final List<JsonNode> toReturn = new ArrayList<>();
        final Iterator<JsonNode> elements = source.elements();
        while (elements.hasNext()) {
            toReturn.add(elements.next());
        }
        return toReturn;
    }

    public static JsonNode getFromJsonNodeList(List<JsonNode> source, String toLook) {
        return source.stream().filter(jsonNode -> toLook.equals(jsonNode.asText())).findFirst().orElse(null);
    }

    public static KiePMMLModel getKiePMMLModelInternal(List<MiningField> miningFields, List<OutputField> outputFields) {
        String modelName = "MODEL_NAME";
        KiePMMLModel toReturn = new KiePMMLModel(modelName, Collections.emptyList()) {
            @Override
            public Object evaluate(Object o, Map<String, Object> map) {
                return null;
            }
        };
        toReturn.setMiningFields(miningFields);
        toReturn.setOutputFields(outputFields);
        return toReturn;
    }

    public static List<MiningField> getRandomMiningFields() {
        List<MiningField> toReturn = IntStream.range(0, 4).mapToObj(i -> getRandomMiningField()).collect(Collectors.toList());
        toReturn.add(getRandomMiningFieldTarget());
        return toReturn;
    }

    public static MiningField getRandomMiningField() {
        Random random = new Random();
        String fieldName = RandomStringUtils.random(6, true, false);
        FIELD_USAGE_TYPE fieldUsageType = FIELD_USAGE_TYPE.values()[random.nextInt(FIELD_USAGE_TYPE.values().length)];
        OP_TYPE opType = OP_TYPE.values()[random.nextInt(OP_TYPE.values().length)];
        DATA_TYPE dataType = DATA_TYPE.values()[random.nextInt(DATA_TYPE.values().length)];
        List<Interval> intervals = IntStream.range(0, 3)
                .mapToObj(i -> new Interval(i * 2 + 3, i * 3 + 4))
                .collect(Collectors.toList());
        return new MiningField(fieldName, fieldUsageType, opType, dataType, null, null, intervals);
    }

    public static MiningField getRandomMiningFieldTarget() {
        Random random = new Random();
        String fieldName = RandomStringUtils.random(6, true, false);
        FIELD_USAGE_TYPE fieldUsageType = FIELD_USAGE_TYPE.TARGET;
        OP_TYPE opType = OP_TYPE.values()[random.nextInt(OP_TYPE.values().length)];
        DATA_TYPE dataType = DATA_TYPE.values()[random.nextInt(DATA_TYPE.values().length)];
        return new MiningField(fieldName, fieldUsageType, opType, dataType, null, null, null);
    }

    public static List<OutputField> getRandomOutputFields() {
        return IntStream.range(0, 4).mapToObj(i -> getRandomOutputField(RandomStringUtils.random(6, true, false))).collect(Collectors.toList());
    }

    public static OutputField getRandomOutputField(String targetField) {
        Random random = new Random();
        String fieldName = RandomStringUtils.random(6, true, false);
        OP_TYPE opType = OP_TYPE.values()[random.nextInt(OP_TYPE.values().length)];
        DATA_TYPE dataType = DATA_TYPE.values()[random.nextInt(DATA_TYPE.values().length)];
        RESULT_FEATURE resultFeature = RESULT_FEATURE.values()[random.nextInt(RESULT_FEATURE.values().length)];
        return new OutputField(fieldName, opType, dataType, targetField, resultFeature, Arrays.asList("A", "B", "C"));
    }
}
