package org.kie.kogito.pmml.openapi.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import org.kie.pmml.api.enums.ResultCode;
import org.kie.pmml.api.models.MiningField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.kogito.pmml.openapi.CommonTestUtility.getRandomMiningFields;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.CORRELATION_ID;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.DEFINITIONS;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.ENUM;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.INPUT_SET;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.OBJECT;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.OUTPUT_SET;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.PROPERTIES;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.RESULT_CODE;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.RESULT_OBJECT_NAME;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.SEGMENTATION_ID;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.SEGMENT_ID;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.SEGMENT_INDEX;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.STRING;
import static org.kie.kogito.pmml.openapi.api.PMMLOASResult.TYPE;

class PMMLOASResultImplTest {


    @Test
    void constructor() {
        PMMLOASResultImpl retrieved = (PMMLOASResultImpl) new PMMLOASResultImpl.Builder().build();
        assertNotNull(retrieved);
        ObjectNode jsonNodes = retrieved.jsonNodes;
        assertNotNull(jsonNodes);
        ObjectNode definitions = (ObjectNode) jsonNodes.get(DEFINITIONS);
        assertNotNull(definitions);
        ObjectNode outputSet = (ObjectNode) definitions.get(OUTPUT_SET);
        assertNotNull(outputSet);
        commonValidateOutputSet(outputSet);
    }

    @Test
    void addMiningFields() {
        PMMLOASResultImpl retrieved = (PMMLOASResultImpl) new PMMLOASResultImpl.Builder().build();
        final List<MiningField> miningFields = getRandomMiningFields();
        retrieved.addMiningFields(miningFields);
        ObjectNode jsonNodes = retrieved.jsonNodes;
        assertNotNull(jsonNodes);
        ObjectNode definitions = (ObjectNode) jsonNodes.get(DEFINITIONS);
        assertNotNull(definitions);
        ObjectNode inputSet = (ObjectNode) definitions.get(INPUT_SET);
        assertNotNull(inputSet);
    }

    @Test
    void addIntervals() {
    }

    @Test
    void addOutputFields() {
    }

    @Test
    void addToResultSet() {
        // TODO KEEP GOING
    }

    @Test
    void addToResultVariables() {
        // TODO KEEP GOING
    }

    @Test
    void conditionallyCreateResultSetNode() {
        // TODO KEEP GOING
    }

    @Test
    void conditionallyCreateResultVariablesNode() {
        // TODO KEEP GOING
    }

    @Test
    void conditionallyCreateSetNode() {
        // TODO KEEP GOING 
    }

    private void commonValidateOutputSet(ObjectNode toValidate) {
        JsonNode typeNode = toValidate.get(TYPE);
        assertNotNull(typeNode);
        assertTrue(typeNode instanceof TextNode);
        assertEquals(OBJECT, ((TextNode) typeNode).asText());
        JsonNode propertiesNode = toValidate.get(PROPERTIES);
        assertNotNull(propertiesNode);
        assertTrue(propertiesNode instanceof ObjectNode);
        JsonNode correlationIdNode = propertiesNode.get(CORRELATION_ID);
        assertNotNull(correlationIdNode);
        assertTrue(correlationIdNode instanceof ObjectNode);
        typeNode = correlationIdNode.get(TYPE);
        assertNotNull(typeNode);
        assertTrue(typeNode instanceof TextNode);
        assertEquals(STRING, ((TextNode) typeNode).asText());
        JsonNode segmentationIdNode = propertiesNode.get(SEGMENTATION_ID);
        assertNotNull(segmentationIdNode);
        assertTrue(segmentationIdNode instanceof ObjectNode);
        typeNode = segmentationIdNode.get(TYPE);
        assertNotNull(typeNode);
        assertTrue(typeNode instanceof TextNode);
        assertEquals(STRING, ((TextNode) typeNode).asText());
        JsonNode segmentIdNode = propertiesNode.get(SEGMENT_ID);
        assertNotNull(segmentIdNode);
        assertTrue(segmentIdNode instanceof ObjectNode);
        typeNode = segmentIdNode.get(TYPE);
        assertNotNull(typeNode);
        assertTrue(typeNode instanceof TextNode);
        assertEquals(STRING, ((TextNode) typeNode).asText());
        JsonNode segmentIndexNode = propertiesNode.get(SEGMENT_INDEX);
        assertNotNull(segmentIndexNode);
        assertTrue(segmentIndexNode instanceof ObjectNode);
        typeNode = segmentIndexNode.get(TYPE);
        assertNotNull(typeNode);
        assertTrue(typeNode instanceof TextNode);
        assertEquals("integer", ((TextNode) typeNode).asText());
        JsonNode resultCodeNode = propertiesNode.get(RESULT_CODE);
        assertNotNull(resultCodeNode);
        assertTrue(resultCodeNode instanceof ObjectNode);
        typeNode = resultCodeNode.get(TYPE);
        assertNotNull(typeNode);
        assertTrue(typeNode instanceof TextNode);
        assertEquals(STRING, ((TextNode) typeNode).asText());
        JsonNode enumNode = resultCodeNode.get(ENUM);
        assertNotNull(enumNode);
        assertTrue(enumNode instanceof ArrayNode);
        assertEquals(ResultCode.values().length,  ((ArrayNode) enumNode).size());
        final Iterator<JsonNode> enumElements = enumNode.elements();
        Arrays.stream(ResultCode.values()).forEach(resultCode -> {
            boolean find = false;
            while (enumElements.hasNext()) {
                JsonNode node = enumElements.next();
                assertTrue(node instanceof TextNode);
                if (resultCode.getName().equals(((TextNode) node).asText())) {
                    find = true;
                    break;
                }
            }
            assertTrue(find);
        });
        JsonNode resultObjectNameNode = propertiesNode.get(RESULT_OBJECT_NAME);
        assertNotNull(resultObjectNameNode);
        assertTrue(resultObjectNameNode instanceof ObjectNode);
        typeNode = resultObjectNameNode.get(TYPE);
        assertNotNull(typeNode);
        assertTrue(typeNode instanceof TextNode);
        assertEquals(STRING, ((TextNode) typeNode).asText());
    }
}