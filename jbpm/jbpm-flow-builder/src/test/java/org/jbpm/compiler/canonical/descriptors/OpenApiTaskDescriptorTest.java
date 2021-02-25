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
package org.jbpm.compiler.canonical.descriptors;

import org.jbpm.process.core.impl.WorkImpl;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.junit.jupiter.api.Test;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

import static org.jbpm.compiler.canonical.descriptors.OpenApiTaskDescriptor.PARAM_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiTaskDescriptorTest {

    @Test
    void addParametersToServiceCall() {
        final BlockStmt execWorkItem = new BlockStmt();
        final MethodCallExpr serviceCallMethod = new MethodCallExpr("doCall");
        final WorkItemNode workItemNode = new WorkItemNode();
        workItemNode.setMetaData(OpenApiTaskDescriptor.PARAM_META_RESOLVER_TYPE, "org.jbpm.serverless.workflow.function.JsonNodeParameterResolver");
        workItemNode.setWork(new WorkImpl());
        workItemNode.getWork().setParameter(AbstractServiceTaskDescriptor.PARAM_INTERFACE, "http://myspec.com");
        workItemNode.getWork().setParameter(AbstractServiceTaskDescriptor.PARAM_OPERATION, "add");
        workItemNode.getWork().setParameter(PARAM_PREFIX + "add", null);
        final OpenApiTaskDescriptor taskDescriptor = new OpenApiTaskDescriptor(workItemNode);
        taskDescriptor.handleParametersForServiceCall(execWorkItem, serviceCallMethod);
        assertNotNull(serviceCallMethod);
        assertEquals(1, serviceCallMethod.getArguments().size());
    }

    @Test
    void handleResultHandler() {
        final BlockStmt execWorkItem = new BlockStmt();
        final MethodCallExpr serviceCallMethod = new MethodCallExpr("doCall");
        final WorkItemNode workItemNode = new WorkItemNode();
        workItemNode.setMetaData(OpenApiTaskDescriptor.PARAM_META_RESOLVER_TYPE, "org.jbpm.serverless.workflow.function.JsonNodeParameterResolver");
        workItemNode.setMetaData(OpenApiTaskDescriptor.PARAM_META_RESULT_HANDLER_TYPE, "org.jbpm.serverless.workflow.function.JsonNodeResultHandler");
        workItemNode.setWork(new WorkImpl());
        workItemNode.getWork().setParameter(AbstractServiceTaskDescriptor.PARAM_INTERFACE, "http://myspec.com");
        workItemNode.getWork().setParameter(AbstractServiceTaskDescriptor.PARAM_OPERATION, "add");
        workItemNode.getWork().setParameter(PARAM_PREFIX + "add", null);
        final OpenApiTaskDescriptor taskDescriptor = new OpenApiTaskDescriptor(workItemNode);
        taskDescriptor.handleParametersForServiceCall(execWorkItem, serviceCallMethod);
        final Expression decoratedServiceCall = taskDescriptor.handleServiceCallResult(execWorkItem, serviceCallMethod);
        assertNotNull(decoratedServiceCall);
        assertTrue(decoratedServiceCall instanceof MethodCallExpr);
        assertEquals(2, ((MethodCallExpr) decoratedServiceCall).getArguments().size());
    }
}
