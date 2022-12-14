/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addons.quarkus.camel.deployment;

import java.util.Objects;

import org.kie.kogito.addons.quarkus.camel.runtime.CamelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.actions.Action;
import io.serverlessworkflow.api.functions.FunctionRef;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.api.states.CallbackState;
import io.serverlessworkflow.api.states.ForEachState;
import io.serverlessworkflow.api.states.OperationState;

/**
 * Static validation for Workflow DSL in build time.
 */
public final class CamelFunctionStaticValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelFunctionStaticValidator.class);

    private CamelFunctionStaticValidator() {
    }

    /**
     * Validation for the Function Definition within the given Workflow
     *
     * @param workflow        the workflow reference
     * @param functionDefName the name of the function definition
     * @throws IllegalArgumentException if the referenced function has more than one argument in the call
     */
    public static void validateFunctionDefinition(final Workflow workflow, final String functionDefName) {
        for (State state : workflow.getStates()) {
            switch (state.getType()) {
                case OPERATION:
                    ((OperationState) state).getActions().forEach(action -> validateAction(action, functionDefName));
                    break;
                case FOREACH:
                    ((ForEachState) state).getActions().forEach(action -> validateAction(action, functionDefName));
                    break;
                case CALLBACK:
                    validateAction(((CallbackState) state).getAction(), functionDefName);
                    break;
                // Sonar compliance...
                default:
                    break;
            }
        }
    }

    /**
     * Validation for the Function Reference
     *
     * @param functionRef the given Function Reference
     * @throws IllegalArgumentException if the there's more than one argument in the function call
     */
    public static void validateFunctionRef(final FunctionRef functionRef) {
        final JsonNode jsonNode = functionRef.getArguments();
        if (jsonNode == null) {
            return;
        }
        if (jsonNode.size() > 2) {
            throw new IllegalArgumentException("Camel functions only support 'body', 'header', or no arguments. Please review the arguments: \n" + jsonNode.asText());
        }
        final JsonNode headers = jsonNode.get(CamelConstants.HEADERS);
        if (headers != null && (headers.isArray() || !headers.isObject())) {
            throw new IllegalArgumentException("Camel functions headers arguments must be a key/value object. Please review the arguments: \n" + headers.asText());
        }
        if (jsonNode.get(CamelConstants.BODY) == null) {
            LOGGER.warn("No body arguments found in the function reference '{}'. The first parameter will be used as the Camel message body. Please use 'body: { }'.", functionRef.getRefName());
        }
    }

    private static void validateAction(final Action action, final String functionDefName) {
        if (Objects.equals(action.getFunctionRef().getRefName(), functionDefName)) {
            validateFunctionRef(action.getFunctionRef());
        }
    }
}
