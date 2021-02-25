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

import java.util.Collection;
import java.util.Collections;

import org.jbpm.workflow.core.node.WorkItemNode;
import org.kie.kogito.process.workitem.WorkItemExecutionException;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class OpenApiTaskDescriptor extends AbstractServiceTaskDescriptor {

    public static final String TYPE = "OpenApi Task";
    public static final String PARAM_PREFIX = "ServiceParam_";
    public static final String PARAM_META_RESOLVER_TYPE = "ParamResolverType";
    public static final String PARAM_META_RESULT_HANDLER = "ResultHandler";
    public static final String PARAM_META_RESULT_HANDLER_TYPE = "ResultHandlerType";
    private static final String VAR_INPUT_MODEL = "inputModel";

    // TODO: create a static method to create an WorkItem for OpenApi Tasks.

    OpenApiTaskDescriptor(WorkItemNode workItemNode) {
        super(workItemNode);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return String.format("%s_%s_%s_Handler", interfaceName, operationName, workItemNode.getName()).replaceAll("\\s+", "");
    }

    @Override
    public CompilationUnit generateHandlerClassForService() {
        CompilationUnit compilationUnit = new CompilationUnit("org.kie.kogito.handlers");
        compilationUnit.getTypes().add(classDeclaration());
        compilationUnit.addImport(WorkItemExecutionException.class);
        return compilationUnit;
    }

    @Override
    protected Collection<Class<?>> getCompleteWorkItemExceptionTypes() {
        return Collections.emptyList();
    }

    @Override
    protected void handleParametersForServiceCall(final BlockStmt executeWorkItemBody, final MethodCallExpr callServiceMethod) {
        // declare the input model
        final MethodCallExpr getInputModel = new MethodCallExpr(new NameExpr("workItem"), "getParameter").addArgument(new StringLiteralExpr("Parameter"));
        final VariableDeclarationExpr inputModel =
                new VariableDeclarationExpr(new VariableDeclarator(new ClassOrInterfaceType(null, Object.class.getCanonicalName()), VAR_INPUT_MODEL, getInputModel));
        executeWorkItemBody.addStatement(inputModel);
        final ClassOrInterfaceType resolverType = new ClassOrInterfaceType(null, workItemNode.getMetaData(PARAM_META_RESOLVER_TYPE).toString());

        workItemNode.getWork().getParameters().entrySet()
                .stream()
                .filter(p -> p.getKey().startsWith(PARAM_PREFIX))
                .forEach(p -> {
                    // method to get the param resolver instance
                    final MethodCallExpr getParamMethod = new MethodCallExpr(new NameExpr("workItem"), "getParameter").addArgument(new StringLiteralExpr(p.getKey()));
                    // cast to the given param resolver type
                    final CastExpr castToResolver = new CastExpr(resolverType, getParamMethod);
                    // temp to hold the param resolver with the correct cast
                    final VariableDeclarationExpr paramResolver =
                            new VariableDeclarationExpr(new VariableDeclarator(castToResolver.getType(), "resolver" + p.getKey(), castToResolver));
                    executeWorkItemBody.addStatement(paramResolver);
                    // param resolver apply method
                    final MethodCallExpr paramResolverApplyMethod =
                            new MethodCallExpr(paramResolver.getVariable(0).getNameAsExpression(), "apply").addArgument(inputModel.getVariable(0).getNameAsExpression());
                    callServiceMethod.addArgument(paramResolverApplyMethod);
                });
    }

    @Override
    protected Expression handleServiceCallResult(final BlockStmt executeWorkItemBody, final MethodCallExpr callService) {
        // fetch the handler type
        final ClassOrInterfaceType resultHandlerType = new ClassOrInterfaceType(null, workItemNode.getMetaData(PARAM_META_RESULT_HANDLER_TYPE).toString());
        // get the handler
        final MethodCallExpr getResultHandler = new MethodCallExpr(new NameExpr("workItem"), "getParameter").addArgument(new StringLiteralExpr(PARAM_META_RESULT_HANDLER));
        // convert the result into the given type
        final CastExpr castToHandler = new CastExpr(resultHandlerType, getResultHandler);
        // temp to hold the result handler with the correct cast
        // Type resultHandler = (Type) workItem.getParameter("ResultHandler");
        final VariableDeclarationExpr resultHandler =
                new VariableDeclarationExpr(new VariableDeclarator(castToHandler.getType(), "resultHandler", castToHandler));
        executeWorkItemBody.addStatement(resultHandler);
        // resultHandler.apply(inputModel, serviceCall(...));
        return new MethodCallExpr(resultHandler.getVariable(0).getNameAsExpression(), "apply")
                .addArgument(new NameExpr(VAR_INPUT_MODEL))
                .addArgument(callService);
    }
}
