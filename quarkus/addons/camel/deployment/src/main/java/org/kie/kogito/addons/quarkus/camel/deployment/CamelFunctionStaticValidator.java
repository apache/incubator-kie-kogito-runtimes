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
import org.kie.kogito.process.expr.ExpressionHandlerFactory;

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

    private CamelFunctionStaticValidator() {
    }

    /**
     * Validation for the Function Definition within the given Workflow
     *
     * @param workflow the workflow reference
     * @param functionDefName the name of the function definition
     * @throws IllegalArgumentException if the referenced function has more than one argument in the call
     */
    public static void validateFunctionDefinition(final Workflow workflow, final String functionDefName) {
        for (State state : workflow.getStates()) {
            // only states that have actions are valid
            switch (state.getType()) {
                case OPERATION:
                    ((OperationState) state).getActions().forEach(action -> validateAction(action, functionDefName, workflow));
                    break;
                case FOREACH:
                    ((ForEachState) state).getActions().forEach(action -> validateAction(action, functionDefName, workflow));
                    break;
                case CALLBACK:
                    validateAction(((CallbackState) state).getAction(), functionDefName, workflow);
                    break;
                // Sonar compliance
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
    public static void validateFunctionRef(final FunctionRef functionRef, final Workflow workflow) {
        final JsonNode jsonNode = functionRef.getArguments();
        if (jsonNode == null) {
            return;
        }
        switch (jsonNode.size()) {
            case 0:
                return;
            case 1:
                verifyBodyArguments(jsonNode, functionRef);
                break;
            case 2:
                verifyBodyArguments(jsonNode, functionRef);
                verifyHeaderArguments(jsonNode, functionRef, workflow);
                break;
            default:
                throw new IllegalArgumentException("Camel functions only support 'body', 'header', or no arguments. Please review the arguments: \n" + jsonNode.asText());
        }
    }

    private static void verifyHeaderArguments(final JsonNode jsonNode, final FunctionRef functionRef, final Workflow workflow) {
        final JsonNode headers = jsonNode.get(CamelConstants.HEADERS);
        if (headers == null) {
            throw new IllegalArgumentException(
                    "Camel functions only support 'body', 'header', or no arguments. "
                            + "Please review the arguments for the function '" + functionRef.getRefName() + "': \n" + jsonNode.asText());
        }
        if (headers.isTextual() && !ExpressionHandlerFactory.get(workflow.getExpressionLang(), headers.asText()).isValid()) {
            throw new IllegalArgumentException(
                    "Camel functions headers arguments must be a valid expression or a key/value object. "
                            + "Please review the arguments for the function '" + functionRef.getRefName() + "': \n"
                            + headers.asText());
        }
        if (!headers.isObject()) {
            throw new IllegalArgumentException(
                    "Camel functions headers arguments must be a valid expression or a key/value object. "
                            + "Please review the arguments for the function '" + functionRef.getRefName() + "': \n"
                            + headers.asText());
        }
    }

    private static void verifyBodyArguments(final JsonNode jsonNode, final FunctionRef functionRef) {
        if (jsonNode.get(CamelConstants.BODY) == null) {
            throw new IllegalArgumentException("No body arguments found in the function reference '" + functionRef.getRefName()
                    + "'. Please review the function arguments to include a '\"body\": {}' argument.");
        }
    }

    private static void validateAction(final Action action, final String functionDefName, final Workflow workflow) {
        if (Objects.equals(action.getFunctionRef().getRefName(), functionDefName)) {
            validateFunctionRef(action.getFunctionRef(), workflow);
        }
    }
}
