/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.compiler.canonical;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.utils.StringEscapeUtils;
import org.jbpm.ruleflow.core.factory.CompositeContextNodeFactory;
import org.jbpm.ruleflow.core.factory.DynamicNodeFactory;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.DynamicNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import static org.jbpm.ruleflow.core.factory.DynamicNodeFactory.METHOD_ACTIVATION_EXPRESSION;
import static org.jbpm.ruleflow.core.factory.DynamicNodeFactory.METHOD_COMPLETION_EXPRESSION;
import static org.jbpm.ruleflow.core.factory.DynamicNodeFactory.METHOD_LANGUAGE;

public class DynamicNodeVisitor extends CompositeContextNodeVisitor {

    private static final String FACTORY_METHOD_NAME = "dynamicNode";

    public DynamicNodeVisitor(Map<Class<?>, AbstractNodeVisitor> nodesVisitors) {
        super(nodesVisitors);
    }

    @Override
    protected Class<? extends CompositeContextNodeFactory> factoryClass() {
        return DynamicNodeFactory.class;
    }

    @Override
    protected String factoryMethod() {
        return FACTORY_METHOD_NAME;
    }

    @Override
    protected String getNodeKey() {
        return FACTORY_METHOD_NAME;
    }

    @Override
    protected String getDefaultName() {
        return "Dynamic";
    }

    @Override
    public Stream<MethodCallExpr> visitCustomFields(CompositeContextNode node) {
        Collection<MethodCallExpr> methods = new ArrayList<>();
        DynamicNode dynamicNode = (DynamicNode) node;
        methods.add(getFactoryMethod(getNodeId(node), METHOD_LANGUAGE, getOrNullExpr(dynamicNode.getLanguage())));
        if (dynamicNode.getActivationExpression() != null) {
            methods.add(getFactoryMethod(getNodeId(node), METHOD_ACTIVATION_EXPRESSION, getOrNullExpr(StringEscapeUtils.escapeJava(dynamicNode.getActivationExpression()))));
        }
        if (dynamicNode.getCompletionExpression() != null) {
            methods.add(getFactoryMethod(getNodeId(node), METHOD_COMPLETION_EXPRESSION, getOrNullExpr(StringEscapeUtils.escapeJava(dynamicNode.getCompletionExpression()))));
        }
        return methods.stream();
    }
}
