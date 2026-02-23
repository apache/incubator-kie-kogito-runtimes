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
package org.jbpm.compiler.canonical.descriptors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.compiler.canonical.ProcessMetaData;
import org.jbpm.process.core.Work;
import org.jbpm.workflow.core.impl.DataAssociation;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.kie.kogito.internal.utils.ConversionUtils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

import static com.github.javaparser.StaticJavaParser.parse;
import static com.github.javaparser.StaticJavaParser.parseClassOrInterfaceType;

public class RestTaskDescriptor implements TaskDescriptor {

    public static final String TYPE = "Rest";
    private static final String ACCESS_TOKEN_PROPERTY_TEMPLATE = "kogito.processes.%s.%s.access_token";

    private final ProcessMetaData processMetadata;
    private final WorkItemNode workItemNode;
    public static final String ACCESS_TOKEN_ACQUISITION_STRATEGY = "AccessTokenAcquisitionStrategy";
    public static final String REST_SERVICE_CALL_TASK_ID = "RestServiceCallTaskId";

    protected RestTaskDescriptor(final ProcessMetaData processMetadata, final WorkItemNode workItemNode) {
        this.processMetadata = processMetadata;
        this.workItemNode = workItemNode;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return ConversionUtils.sanitizeClassName(processMetadata.getProcessId()) + "RestWorkItemHandler";
    }

    @Override
    public CompilationUnit generateHandlerClassForService() {
        final String className = this.getName();
        CompilationUnit compilationUnit =
                parse(RestTaskDescriptor.class.getResourceAsStream("/class-templates/RestWorkItemHandlerTemplate.java"));
        compilationUnit.setPackageDeclaration("org.kie.kogito.handlers");
        compilationUnit.findFirst(ClassOrInterfaceDeclaration.class).ifPresent(c -> c.setName(className));
        compilationUnit.findAll(ConstructorDeclaration.class).forEach(c -> c.setName(className));
        compilationUnit.findAll(MethodDeclaration.class, m -> m.getNameAsString().equals("getName"))
                .forEach(m -> m.setBody(new BlockStmt(NodeList.nodeList(new ReturnStmt(new StringLiteralExpr(className))))));
        return compilationUnit;
    }

    @Override
    public Map<String, Expression> getCustomParams() {
        Work work = workItemNode.getWork();
        String strategyParam = extractRestTokenPropagationValues(workItemNode, ACCESS_TOKEN_ACQUISITION_STRATEGY);
        if (strategyParam == null || "none".equalsIgnoreCase(strategyParam.toString())) {
            return Collections.emptyMap();
        }

        String taskId;
        String taskIdParam = extractRestTokenPropagationValues(workItemNode, REST_SERVICE_CALL_TASK_ID);
        if (taskIdParam != null) {
            taskId = taskIdParam.toString();
        } else {
            taskId = String.valueOf(workItemNode.getId());
        }

        String processId = processMetadata.getProcessId();
        String configProperty = String.format(ACCESS_TOKEN_PROPERTY_TEMPLATE, processId, taskId);

        Expression resolverExpr = ExpressionUtils.getObjectCreationExpr(
                parseClassOrInterfaceType("org.kie.kogito.process.workitems.impl.ConfigWorkItemResolver")
                        .setTypeArguments(parseClassOrInterfaceType(String.class.getCanonicalName())),
                configProperty,
                String.class,
                null);

        Map<String, Expression> customParams = new HashMap<>();
        customParams.put("propagateToken", resolverExpr);
        return customParams;
    }

    static String extractRestTokenPropagationValues(WorkItemNode workItemNode, String paramName) {
        List<String> vals = extractValueFromWorkItemNode(workItemNode, paramName);
        return vals.isEmpty() ? null : vals.get(0);
    }

    static List<String> extractValueFromWorkItemNode(WorkItemNode workItemNode, String paramName) {
        return resolveInputValuesFromInAssociations(workItemNode, paramName);
    }

    static List<String> resolveInputValuesFromInAssociations(WorkItemNode win, String parameterName) {
        if (win == null || parameterName == null || parameterName.isEmpty()) {
            return Collections.emptyList();
        }
        List<DataAssociation> inAssocs = win.getInAssociations();
        if (inAssocs == null || inAssocs.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>();

        for (DataAssociation da : inAssocs) {
            if (!parameterName.equals(da.getTarget())) {
                continue;
            }

             List<?> assigns = da.getAssignments();
            if (assigns != null && !assigns.isEmpty()) {
                for (Object a : assigns) {
                    String val = tryGetAssignmentFrom(a);
                    if (val != null && !val.isBlank()) {
                        results.add(val.trim());
                    }
                }
            }

            break;
        }

        return results;
    }

    private static String tryGetAssignmentFrom(Object assignment) {
        if (assignment == null)
            return null;

        try {
            Method m = assignment.getClass().getMethod("getFrom");
            Object from = m.invoke(assignment);
            if (from != null) {
                String s = String.valueOf(from).trim();
                if (!s.isEmpty())
                    return s;
            }
        } catch (NoSuchMethodException e) {
            return null;
        } catch (Exception ignore) {
            return null;
        }

        try {
            Method m = assignment.getClass().getMethod("getExpression");
            Object expr = m.invoke(assignment);
            if (expr != null) {
                String s = String.valueOf(expr).trim();
                if (!s.isEmpty())
                    return s;
            }
        } catch (Exception ignore) {
            return null;
        }
        return null;
    }

}
