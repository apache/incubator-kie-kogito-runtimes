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

import java.util.Map.Entry;
import java.util.Optional;

import org.jbpm.ruleflow.core.RuleFlowNodeContainerFactory;
import org.jbpm.ruleflow.core.factory.NodeFactory;
import org.kie.kogito.serverless.workflow.operationid.WorkflowOperationId;
import org.kie.kogito.serverless.workflow.parser.FunctionTypeHandler;
import org.kie.kogito.serverless.workflow.parser.ParserContext;
import org.kie.kogito.serverless.workflow.parser.VariableInfo;
import org.kie.kogito.serverless.workflow.utils.AsyncApiResolverHolder;

import com.asyncapi.v2.model.AsyncAPI;
import com.asyncapi.v2.model.channel.ChannelItem;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.functions.FunctionDefinition;
import io.serverlessworkflow.api.functions.FunctionRef;

public class AsyncAPITypeHandler implements FunctionTypeHandler {

    @Override
    public String type() {
        return FunctionDefinition.Type.ASYNCAPI.toString();
    }

    @Override
    public boolean isCustom() {
        return false;
    }

    @Override
    public NodeFactory<?, ?> getActionNode(Workflow workflow, ParserContext context,
            RuleFlowNodeContainerFactory<?, ?> embeddedSubProcess, FunctionDefinition functionDef,
            FunctionRef functionRef, VariableInfo varInfo) {
        WorkflowOperationId operationId = context.operationIdFactory().from(workflow, functionDef, Optional.of(context));
        return AsyncApiResolverHolder.get().flatMap(resolver -> resolver.getAsyncAPI(operationId.getFileName())).flatMap(asyncAPI -> buildNode(asyncAPI, operationId.getOperation()))
                .orElseThrow(() -> new IllegalArgumentException("Cannot find an async api with operation " + operationId.getOperation()));
    }

    private Optional<NodeFactory<?, ?>> buildNode(AsyncAPI asyncAPI, String operationId) {
        for (Entry<String, ChannelItem> entry : asyncAPI.getChannels().entrySet()) {
            if (entry.getValue().getPublish() != null) {

            } else if (entry.getValue().getSubscribe() != null) {

            }
        }
        return Optional.empty();
    }
}
