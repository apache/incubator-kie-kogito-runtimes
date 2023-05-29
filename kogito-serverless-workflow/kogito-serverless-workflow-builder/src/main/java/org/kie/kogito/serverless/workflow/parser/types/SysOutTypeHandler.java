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
package org.kie.kogito.serverless.workflow.parser.types;

import org.jbpm.ruleflow.core.RuleFlowNodeContainerFactory;
import org.jbpm.ruleflow.core.factory.ActionNodeFactory;
import org.kie.kogito.serverless.workflow.parser.FunctionTypeHandlerFactory;
import org.kie.kogito.serverless.workflow.parser.VariableInfo;
import org.kie.kogito.serverless.workflow.suppliers.SysoutActionSupplier;

import com.fasterxml.jackson.databind.JsonNode;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.functions.FunctionDefinition;
import io.serverlessworkflow.api.functions.FunctionRef;

public class SysOutTypeHandler extends ActionTypeHandler {

    public static final String SYSOUT_TYPE = "sysout";
    public static final String SYSOUT_TYPE_PARAM = "message";

    @Override
    protected <T extends RuleFlowNodeContainerFactory<T, ?>> ActionNodeFactory<T> fillAction(Workflow workflow,
            ActionNodeFactory<T> node,
            FunctionDefinition functionDef,
            FunctionRef functionRef,
            VariableInfo varInfo) {
        JsonNode args = functionRef.getArguments();
        if (args == null) {
            throw new IllegalArgumentException("Arguments cannot be null for a sysout function");
        }
        JsonNode message = args.get(SYSOUT_TYPE_PARAM);
        if (message == null) {
            throw new IllegalArgumentException("Missing mandatory message argument in " + args);
        }
        return node.action(new SysoutActionSupplier(workflow.getExpressionLang(), message.asText(), varInfo.getInputVar(),
                FunctionTypeHandlerFactory.trimCustomOperation(functionDef)));
    }

    @Override
    public String type() {
        return SYSOUT_TYPE;
    }
}
