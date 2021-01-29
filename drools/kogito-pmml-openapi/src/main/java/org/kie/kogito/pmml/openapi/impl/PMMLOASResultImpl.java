/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.pmml.openapi.impl;

import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.smallrye.openapi.runtime.io.JsonUtil;
import org.kie.kogito.pmml.openapi.api.PMMLOASResult;
import org.kie.pmml.api.enums.DATA_TYPE;
import org.kie.pmml.api.enums.FIELD_USAGE_TYPE;
import org.kie.pmml.api.models.MiningField;
import org.kie.pmml.api.models.OutputField;

/**
 * Concrete implementation of <code>PMMLOASResult</code>
 */
public class PMMLOASResultImpl implements PMMLOASResult {

    private final ObjectNode jsonNodes;

    private PMMLOASResultImpl() {
        jsonNodes = JsonUtil.objectNode();
        ObjectNode definitions = JsonUtil.objectNode();
        jsonNodes.set(DEFINITIONS, definitions);
    }

    public static boolean isRequired(MiningField toVerify) {
        if (FIELD_USAGE_TYPE.PREDICTED.equals(toVerify.getUsageType()) ||
                FIELD_USAGE_TYPE.TARGET.equals(toVerify.getUsageType())) {
            return false;
        }
        return toVerify.getMissingValueReplacement() == null;
    }

    public static boolean isPredicted(MiningField toVerify) {
        return FIELD_USAGE_TYPE.PREDICTED.equals(toVerify.getUsageType()) ||
                FIELD_USAGE_TYPE.TARGET.equals(toVerify.getUsageType());
    }

    public static String getMappedType(DATA_TYPE toMap) {
        switch (toMap) {
            case DATE:
            case DATE_TIME:
            case STRING:
                return "string";
            case BOOLEAN:
                return "boolean";
            case INTEGER:
                return "integer";
            default:
                return "number";
        }
    }

    @Override
    public ObjectNode jsonSchemaNode() {
        return jsonNodes;
    }

    protected void addMiningFields(List<MiningField> toAdd) {
        final ObjectNode definitionsNode = (ObjectNode) jsonNodes.get(DEFINITIONS);
        final ObjectNode inputSetNode = JsonUtil.objectNode();
        definitionsNode.set(INPUT_SET, inputSetNode);
        inputSetNode.set(TYPE, new TextNode(OBJECT));
        final ArrayNode requiredNode = JsonUtil.arrayNode();
        inputSetNode.set(REQUIRED, requiredNode);
        final ObjectNode propertiesNode = JsonUtil.objectNode();
        inputSetNode.set(PROPERTIES, propertiesNode);
        toAdd.forEach(miningField -> {
            if (isRequired(miningField)) { // A MiningField may be not predicted AND not required if it has a missingValueReplacement
                requiredNode.add(miningField.getName());
            }
            if (isPredicted(miningField)) {
                addToOutputSet(miningField.getName(), miningField.getDataType(), miningField.getAllowedValues());
            } else {
                final ObjectNode typeFieldNode = JsonUtil.objectNode();
                String mappedType = getMappedType(miningField.getDataType());
                typeFieldNode.set(TYPE, new TextNode(mappedType));
                if (miningField.getMissingValueReplacement() != null && !miningField.getMissingValueReplacement().isEmpty()) {
                    typeFieldNode.set(DEFAULT, new TextNode(miningField.getMissingValueReplacement()));
                }
                if (miningField.getAllowedValues() != null && !miningField.getAllowedValues().isEmpty()) {
                    ArrayNode availableValues = JsonUtil.arrayNode();
                    miningField.getAllowedValues().forEach(availableValues::add);
                    typeFieldNode.set(ENUM, availableValues);
                }
                if (miningField.getIntervals() != null && !miningField.getIntervals().isEmpty()) {
                    ArrayNode intervals = JsonUtil.arrayNode();
                    miningField.getIntervals().forEach(intervals::add);
                    typeFieldNode.set(INTERVALS, intervals);
                }
                propertiesNode.set(miningField.getName(), typeFieldNode);
            }
        });
    }

    protected void addOutputFields(List<OutputField> toAdd) {
        toAdd.forEach(outputField -> addToOutputSet(outputField.getName(), outputField.getDataType(), outputField.getAllowedValues()));
    }

    protected void addToOutputSet(String fieldName, DATA_TYPE dataType, List<String> allowedValues) {
        final ObjectNode outputSetNode = conditionallyCreateOutputSetNode();
        final ObjectNode propertiesNode = (ObjectNode) outputSetNode.get(PROPERTIES);
        final ObjectNode typeFieldNode = JsonUtil.objectNode();
        String mappedType = getMappedType(dataType);
        typeFieldNode.set(TYPE, new TextNode(mappedType));
        propertiesNode.set(fieldName, typeFieldNode);
        if (allowedValues != null && !allowedValues.isEmpty()) {
            ArrayNode availableValues = conditionallyCreateEnumNode(typeFieldNode);
            allowedValues.forEach(availableValues::add);
        }
    }

    protected ArrayNode conditionallyCreateEnumNode(final ObjectNode parent) {
        if (parent.get(ENUM) == null) {
            ArrayNode availableValues = JsonUtil.arrayNode();
            parent.set(ENUM, availableValues);
        }
        return (ArrayNode) parent.get(ENUM);
    }

    protected ObjectNode conditionallyCreateOutputSetNode() {
        final ObjectNode definitionsNode = (ObjectNode) jsonNodes.get(DEFINITIONS);
        if (definitionsNode.get(OUTPUT_SET) == null) {
            final ObjectNode outputSetNode = JsonUtil.objectNode();
            definitionsNode.set(OUTPUT_SET, outputSetNode);
            outputSetNode.set(TYPE, new TextNode(OBJECT));
            final ObjectNode propertiesNode = JsonUtil.objectNode();
            outputSetNode.set(PROPERTIES, propertiesNode);
        }
        return (ObjectNode) definitionsNode.get(OUTPUT_SET);
    }

    public static class Builder {

        private final PMMLOASResultImpl toBuild;

        public Builder() {
            this.toBuild = new PMMLOASResultImpl();
        }

        public Builder withMiningFields(List<MiningField> miningFields) {
            toBuild.addMiningFields(miningFields);
            return this;
        }

        public Builder withOutputFields(List<OutputField> outputFields) {
            toBuild.addOutputFields(outputFields);
            return this;
        }

        public PMMLOASResult build() {
            return toBuild;
        }
    }
}
