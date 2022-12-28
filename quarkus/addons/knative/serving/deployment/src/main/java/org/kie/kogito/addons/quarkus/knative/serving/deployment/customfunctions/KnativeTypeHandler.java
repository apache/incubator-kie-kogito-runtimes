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
package org.kie.kogito.addons.quarkus.knative.serving.deployment.customfunctions;

import org.jbpm.ruleflow.core.RuleFlowNodeContainerFactory;
import org.jbpm.ruleflow.core.factory.WorkItemNodeFactory;
import org.kie.kogito.addons.quarkus.knative.serving.customfunctions.KnativeWorkItemHandler;
import org.kie.kogito.addons.quarkus.knative.serving.customfunctions.KnativeWorkItemHandlerConstants;
import org.kie.kogito.addons.quarkus.knative.serving.customfunctions.ServiceDiscoveryException;
import org.kie.kogito.jackson.utils.ObjectMapperFactory;
import org.kie.kogito.serverless.workflow.parser.ParserContext;
import org.kie.kogito.serverless.workflow.parser.types.WorkItemTypeHandler;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.functions.FunctionDefinition;

import static org.kie.kogito.serverless.workflow.parser.FunctionTypeHandlerFactory.trimCustomOperation;

public class KnativeTypeHandler extends WorkItemTypeHandler {

    @Override
    protected <T extends RuleFlowNodeContainerFactory<T, ?>> WorkItemNodeFactory<T> fillWorkItemHandler(Workflow workflow,
            ParserContext context,
            WorkItemNodeFactory<T> node,
            FunctionDefinition functionDef) {
        String functionMetadata;
        try {
            functionMetadata = ObjectMapperFactory.get().writeValueAsString(functionDef.getMetadata());
        } catch (JsonProcessingException e) {
            throw new ServiceDiscoveryException("Error while reading function metadata.", e);
        }

        return node.workName(KnativeWorkItemHandler.NAME)
                .metaData(KnativeWorkItemHandlerConstants.OPERATION_PROPERTY_NAME, trimCustomOperation(functionDef))
                .metaData(KnativeWorkItemHandlerConstants.FUNCTION_METADATA_PROPERTY_NAME, functionMetadata);
    }

    @Override
    public String type() {
        return "knative";
    }
}
