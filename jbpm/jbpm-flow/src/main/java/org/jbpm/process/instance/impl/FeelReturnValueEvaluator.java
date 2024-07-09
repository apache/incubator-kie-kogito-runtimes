/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jbpm.process.instance.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.kie.api.runtime.Globals;
import org.kie.dmn.api.feel.runtime.events.FEELEvent;
import org.kie.dmn.feel.FEEL;
import org.kie.dmn.feel.lang.impl.FEELBuilder;
import org.kie.dmn.feel.parser.feel11.profiles.KieExtendedFEELProfile;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;

public class FeelReturnValueEvaluator extends AbstractReturnValueEvaluator {

    public FeelReturnValueEvaluator() {
        super("FEEL", "true()");
    }

    public FeelReturnValueEvaluator(String expr) {
        super("FEEL", expr);
    }

    public Object evaluate(KogitoProcessContext context) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("kcontext", context);

        // insert globals into context
        Globals globals = context.getKieRuntime().getGlobals();

        if (globals != null && globals.getGlobalKeys() != null) {
            for (String gKey : globals.getGlobalKeys()) {
                variables.put(gKey, globals.get(gKey));
            }
        }
        if (context.getProcessInstance() != null && context.getProcessInstance().getProcess() != null) {
            // insert process variables
            VariableScopeInstance variableScope = (VariableScopeInstance) ((WorkflowProcessInstance) context.getProcessInstance())
                    .getContextInstance(VariableScope.VARIABLE_SCOPE);

            variables.putAll(variableScope.getVariables());
        }
        FEEL feel = FEELBuilder.builder().withProfiles(Collections.singletonList(new KieExtendedFEELProfile())).build();
        FeelErrorEvaluatorListener listener = new FeelErrorEvaluatorListener();
        feel.addListener(listener);

        Object value = feel.evaluate(expression(), variables);

        processErrorEvents(listener.getErrorEvents());
        if (!(value instanceof Boolean)) {
            throw new RuntimeException("Constraints must return boolean values: " +
                    expression() + " returns " + value +
                    (value == null ? "" : " (type=" + value.getClass()));
        }

        return ((Boolean) value).booleanValue();
    }

    private void processErrorEvents(List<FEELEvent> errorEvents) {
        if (errorEvents.isEmpty()) {
            return;
        }
        String exceptionMessage = errorEvents.stream().map(FeelReturnValueEvaluator::eventToMessage).collect(Collectors.joining(", "));
        throw new FeelReturnValueEvaluatorException(exceptionMessage);
    }

    public static String eventToMessage(FEELEvent event) {
        StringBuilder messageBuilder = new StringBuilder(event.getSeverity().toString()).append(" ").append(event.getMessage());
        if (event.getOffendingSymbol() != null) {
            messageBuilder.append(" ( offending symbol: '").append(event.getOffendingSymbol()).append("' )");
        }
        if (event.getSourceException() != null) {
            messageBuilder.append("  ").append(event.getSourceException().getMessage());
        }
        return messageBuilder.toString();
    }

}
