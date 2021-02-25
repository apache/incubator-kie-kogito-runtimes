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
package org.kie.kogito.codegen.process.openapi;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.compiler.canonical.descriptors.OpenApiTaskDescriptor;
import org.jbpm.serverless.workflow.parser.core.ServerlessWorkflowFactory;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.kie.api.definition.process.Node;
import org.kie.kogito.codegen.api.context.ContextAttributesConstants;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.openapi.client.OpenApiClientOperation;
import org.kie.kogito.codegen.openapi.client.OpenApiSpecDescriptor;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcess;

import static org.jbpm.serverless.workflow.parser.core.ServerlessWorkflowFactory.WORKITEM_INTERFACE;
import static org.jbpm.serverless.workflow.parser.core.ServerlessWorkflowFactory.WORKITEM_OPERATION;

public class OpenApiClientWorkItemIntrospector {

    private List<OpenApiSpecDescriptor> descriptors;

    @SuppressWarnings({ "unchecked" })
    public OpenApiClientWorkItemIntrospector(final KogitoBuildContext context) {
        if (context == null) {
            this.descriptors = new ArrayList<>();
        } else {
            this.descriptors = context.getContextAttribute(ContextAttributesConstants.OPENAPI_DESCRIPTORS, List.class);
            if (this.descriptors == null) {
                this.descriptors = new ArrayList<>();
            }
        }
    }

    public void introspect(KogitoWorkflowProcess workFlowProcess) {
        final List<Node> nodes = workFlowProcess.getNodesRecursively();
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.stream()
                .filter(node -> node instanceof WorkItemNode && OpenApiTaskDescriptor.TYPE.equals(((WorkItemNode) node).getWork().getName()))
                .forEach(node -> {
                    final OpenApiClientOperation operation =
                            discoverOperation(((WorkItemNode) node).getWork().getParameter(ServerlessWorkflowFactory.WORKITEM_INTERFACE).toString(),
                                    ((WorkItemNode) node).getWork().getParameter(ServerlessWorkflowFactory.WORKITEM_OPERATION).toString());
                    ((WorkItemNode) node).getWork().setParameter(WORKITEM_INTERFACE, operation.getGeneratedClass());
                    ((WorkItemNode) node).getWork().setParameter(WORKITEM_OPERATION, operation.getMethodName());
                });
    }

    private OpenApiClientOperation discoverOperation(String spec, String operationId) {
        return this.descriptors
                .stream()
                .filter(d -> spec.equals(d.getURI().toString()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No OpenApi descriptor found in the KogitoBuildContext for " + spec))
                .getRequiredOperations()
                .stream()
                .filter(o -> o.getOperationId().equals(operationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Required Operation " + operationId + " not found in the OpenApi Spec Descriptors in KogitoBuildContext"));
    }
}
