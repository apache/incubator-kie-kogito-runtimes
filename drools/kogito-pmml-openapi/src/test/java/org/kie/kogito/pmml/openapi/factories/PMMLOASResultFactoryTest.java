package org.kie.kogito.pmml.openapi.factories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.kie.kogito.pmml.openapi.api.PMMLOASResult;
import org.kie.kogito.pmml.openapi.impl.PMMLOASResultImpl;
import org.kie.pmml.api.enums.DATA_TYPE;
import org.kie.pmml.api.enums.FIELD_USAGE_TYPE;
import org.kie.pmml.api.enums.OP_TYPE;
import org.kie.pmml.api.enums.RESULT_FEATURE;
import org.kie.pmml.api.models.MiningField;
import org.kie.pmml.api.models.OutputField;
import org.kie.pmml.commons.model.KiePMMLModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.DEFINITIONS;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.INPUT_SET;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.OBJECT;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.OUTPUT_SET;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.PROPERTIES;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.REQUIRED;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.RESULT_SET;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.RESULT_VARIABLES;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.TYPE;

class PMMLOASResultFactoryTest {

    @Test
    void getPMMLOASResultNoMiningFieldsNoOutputFields() {
        final List<MiningField> miningFields = Collections.emptyList();
        final List<OutputField> outputFields = Collections.emptyList();
        final KiePMMLModel kiePMMLModel = getKiePMMLModelInternal(miningFields, outputFields);
        final PMMLOASResult retrieved = PMMLOASResultFactory.getPMMLOASResult(kiePMMLModel);
        assertNotNull(retrieved);
        final ObjectNode jsonNodes = retrieved.jsonSchemaNode();
        assertNotNull(jsonNodes);
        assertFalse(jsonNodes.isEmpty());
        assertNotNull(jsonNodes.get(DEFINITIONS));
        final JsonNode definitionsNode = jsonNodes.get(DEFINITIONS);
        assertFalse(definitionsNode.isEmpty());
        commonValidateInputSet(definitionsNode.get(INPUT_SET), miningFields);
        assertNull(definitionsNode.get(RESULT_SET));
        assertNotNull(definitionsNode.get(OUTPUT_SET));
        assertNull(definitionsNode.get(OUTPUT_SET).get(RESULT_VARIABLES));
    }

    @Test
    void getPMMLOASResultMiningFieldsNoOutputFields() {
        final List<MiningField> miningFields = getRandomMiningFields();
        final List<MiningField> predictedFields = miningFields.stream().filter(PMMLOASResultImpl::isPredicted).collect(Collectors.toList());
        final List<OutputField> outputFields = Collections.emptyList();
        final KiePMMLModel kiePMMLModel = getKiePMMLModelInternal(miningFields, outputFields);
        final PMMLOASResult retrieved = PMMLOASResultFactory.getPMMLOASResult(kiePMMLModel);
        assertNotNull(retrieved);
        final ObjectNode jsonNodes = retrieved.jsonSchemaNode();
        assertNotNull(jsonNodes);
        assertFalse(jsonNodes.isEmpty());
        assertNotNull(jsonNodes.get(DEFINITIONS));
        final JsonNode definitionsNode = jsonNodes.get(DEFINITIONS);
        assertFalse(definitionsNode.isEmpty());
        commonValidateInputSet(definitionsNode.get(INPUT_SET), miningFields);
        commonValidateResultSet(definitionsNode.get(RESULT_SET), predictedFields);
        assertNotNull(definitionsNode.get(OUTPUT_SET));
        assertNotNull(definitionsNode.get(OUTPUT_SET).get(PROPERTIES));
        assertNotNull(definitionsNode.get(OUTPUT_SET).get(PROPERTIES).get(RESULT_VARIABLES));
    }

    @Test
    void getPMMLOASResultMiningFieldsOutputFields() {
        final List<MiningField> miningFields = getRandomMiningFields();
        final List<MiningField> predictedFields = miningFields.stream().filter(PMMLOASResultImpl::isPredicted).collect(Collectors.toList());
        final List<OutputField> outputFields = getRandomOutputFields();
        outputFields.add(getRandomOutputField(miningFields.get(miningFields.size() - 1).getName()));
        final KiePMMLModel kiePMMLModel = getKiePMMLModelInternal(miningFields, outputFields);
        final PMMLOASResult retrieved = PMMLOASResultFactory.getPMMLOASResult(kiePMMLModel);
        assertNotNull(retrieved);
        final ObjectNode jsonNodes = retrieved.jsonSchemaNode();
        assertNotNull(jsonNodes);
        assertFalse(jsonNodes.isEmpty());
        assertNotNull(jsonNodes.get(DEFINITIONS));
        final JsonNode definitionsNode = jsonNodes.get(DEFINITIONS);
        assertFalse(definitionsNode.isEmpty());
        commonValidateInputSet(definitionsNode.get(INPUT_SET), miningFields);
        commonValidateResultSet(definitionsNode.get(RESULT_SET), predictedFields);
        assertNotNull(definitionsNode.get(OUTPUT_SET));
        assertNotNull(definitionsNode.get(OUTPUT_SET).get(PROPERTIES));
        assertNotNull(definitionsNode.get(OUTPUT_SET).get(PROPERTIES).get(RESULT_VARIABLES));
        commonValidateOutputSet(definitionsNode.get(OUTPUT_SET).get(PROPERTIES).get(RESULT_VARIABLES), outputFields);
    }

    private void commonValidateInputSet(final JsonNode toValidate, final List<MiningField> miningFields) {
        assertNotNull(toValidate);
        assertEquals(OBJECT, toValidate.get(TYPE).asText());
        assertNotNull(toValidate.get(REQUIRED));
        assertNotNull(toValidate.get(PROPERTIES));
        final ArrayNode requiredNode = (ArrayNode) toValidate.get(REQUIRED);
        List<MiningField> requiredMiningFields = miningFields.stream().filter(PMMLOASResultImpl::isRequired).collect(Collectors.toList());
        assertEquals(requiredMiningFields.size(), requiredNode.size());
        final List<JsonNode> requiredJsonNodes = getFromArrayNode(requiredNode);
        final ObjectNode propertiesNode = (ObjectNode) toValidate.get(PROPERTIES);
        List<MiningField> active = miningFields.stream().filter(miningField -> !PMMLOASResultImpl.isPredicted(miningField)).collect(Collectors.toList());
        assertEquals(active.size(), propertiesNode.size());
        for (MiningField miningField : requiredMiningFields) {
            JsonNode required = getFromJsonNodeList(requiredJsonNodes, miningField.getName());
            assertNotNull(required);
        }
        for (MiningField miningField : active) {
            JsonNode property = propertiesNode.get(miningField.getName());
            assertNotNull(property);
            final ObjectNode typeFieldNode = (ObjectNode) property;
            assertNotNull(typeFieldNode.get(TYPE));
            final TextNode typeNode = (TextNode) typeFieldNode.get(TYPE);
            String mappedType = PMMLOASResultImpl.getMappedType(miningField.getDataType());
            assertEquals(mappedType, typeNode.asText());
        }
    }

    private void commonValidateResultSet(final JsonNode toValidate, final List<MiningField> miningFields) {
        assertNotNull(toValidate);
        assertEquals(OBJECT, toValidate.get(TYPE).asText());
        assertNotNull(toValidate.get(PROPERTIES));
        final ObjectNode propertiesNode = (ObjectNode) toValidate.get(PROPERTIES);
        for (MiningField miningField : miningFields) {
            JsonNode property = propertiesNode.get(miningField.getName());
            assertNotNull(property);
            final ObjectNode typeFieldNode = (ObjectNode) property;
            assertNotNull(typeFieldNode.get(TYPE));
            final TextNode typeNode = (TextNode) typeFieldNode.get(TYPE);
            String mappedType = PMMLOASResultImpl.getMappedType(miningField.getDataType());
            assertEquals(mappedType, typeNode.asText());
        }
    }

    private void commonValidateOutputSet(final JsonNode toValidate, final List<OutputField> outputFields) {
        assertNotNull(toValidate);
        assertEquals(OBJECT, toValidate.get(TYPE).asText());
        assertNotNull(toValidate.get(PROPERTIES));
        final ObjectNode propertiesNode = (ObjectNode) toValidate.get(PROPERTIES);
        for (OutputField outputField : outputFields) {
            JsonNode property = propertiesNode.get(outputField.getName());
            assertNotNull(property);
            final ObjectNode typeFieldNode = (ObjectNode) property;
            assertNotNull(typeFieldNode.get(TYPE));
            final TextNode typeNode = (TextNode) typeFieldNode.get(TYPE);
            String mappedType = PMMLOASResultImpl.getMappedType(outputField.getDataType());
            assertEquals(mappedType, typeNode.asText());
        }
    }

    private List<JsonNode> getFromArrayNode(ArrayNode source) {
        final List<JsonNode> toReturn = new ArrayList<>();
        final Iterator<JsonNode> elements = source.elements();
        while (elements.hasNext()) {
            toReturn.add(elements.next());
        }
        return toReturn;
    }

    private JsonNode getFromJsonNodeList(List<JsonNode> source, String toLook) {
        return source.stream().filter(jsonNode -> toLook.equals(jsonNode.asText())).findFirst().orElse(null);
    }

    private KiePMMLModel getKiePMMLModelInternal(List<MiningField> miningFields, List<OutputField> outputFields) {
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

    private List<MiningField> getRandomMiningFields() {
        List<MiningField> toReturn = IntStream.range(0, 4).mapToObj(i -> getRandomMiningField()).collect(Collectors.toList());
        toReturn.add(getRandomMiningFieldTarget());
        return toReturn;
    }

    private MiningField getRandomMiningField() {
        Random random = new Random();
        String fieldName = RandomStringUtils.random(6, true, false);
        FIELD_USAGE_TYPE fieldUsageType = FIELD_USAGE_TYPE.values()[random.nextInt(FIELD_USAGE_TYPE.values().length)];
        OP_TYPE opType = OP_TYPE.values()[random.nextInt(OP_TYPE.values().length)];
        DATA_TYPE dataType = DATA_TYPE.values()[random.nextInt(DATA_TYPE.values().length)];
        return new MiningField(fieldName, fieldUsageType, opType, dataType, null, null, null);
    }

    private MiningField getRandomMiningFieldTarget() {
        Random random = new Random();
        String fieldName = RandomStringUtils.random(6, true, false);
        FIELD_USAGE_TYPE fieldUsageType = FIELD_USAGE_TYPE.TARGET;
        OP_TYPE opType = OP_TYPE.values()[random.nextInt(OP_TYPE.values().length)];
        DATA_TYPE dataType = DATA_TYPE.values()[random.nextInt(DATA_TYPE.values().length)];
        return new MiningField(fieldName, fieldUsageType, opType, dataType, null, null, null);
    }

    private List<OutputField> getRandomOutputFields() {
        return IntStream.range(0, 4).mapToObj(i -> getRandomOutputField(RandomStringUtils.random(6, true, false))).collect(Collectors.toList());
    }

    private OutputField getRandomOutputField(String targetField) {
        Random random = new Random();
        String fieldName = RandomStringUtils.random(6, true, false);
        OP_TYPE opType = OP_TYPE.values()[random.nextInt(OP_TYPE.values().length)];
        DATA_TYPE dataType = DATA_TYPE.values()[random.nextInt(DATA_TYPE.values().length)];
        RESULT_FEATURE resultFeature = RESULT_FEATURE.values()[random.nextInt(RESULT_FEATURE.values().length)];
        return new OutputField(fieldName, opType, dataType, targetField, resultFeature, Arrays.asList("A", "B", "C"));
    }
}