/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.compiler.canonical;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.jbpm.process.core.Work;
import org.jbpm.process.core.context.variable.Mappable;

import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractVisitor {

    protected static final String FACTORY_FIELD_NAME = "factory";
    protected static final String KCONTEXT_VAR = "kcontext";

    protected MethodCallExpr addFactoryMethodWithArgs(String factoryField, BlockStmt body, String methodName, Expression... args) {
        return addFactoryMethodWithArgs(body, factoryField, methodName, args);
    }

    protected MethodCallExpr addFactoryMethodWithArgs(BlockStmt body, String object, String methodName, Expression... args) {
        MethodCallExpr variableMethod = new MethodCallExpr(new NameExpr(object), methodName);

        for (Expression arg : args) {
            variableMethod.addArgument(arg);
        }
        body.addStatement(variableMethod);

        return variableMethod;
    }

    protected String getOrDefault(String value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    protected Expression getOrNullExpr(String value) {
        if (value == null) {
            return new NullLiteralExpr();
        }

        return new StringLiteralExpr(value);
    }

    protected void addWorkItemParameters(Work work, BlockStmt body, String variableName) {

        for (Entry<String, Object> entry : work.getParameters().entrySet()) {
            if (entry.getValue() == null) {
                continue; // interfaceImplementationRef ?
            }
            addFactoryMethodWithArgs(body, variableName, "workParameter", new StringLiteralExpr(entry.getKey()), new StringLiteralExpr(entry.getValue().toString()));
        }
    }

    protected void addNodeMappings(Mappable node, BlockStmt body, String variableName) {
        for (Entry<String, String> entry : node.getInMappings().entrySet()) {
            addFactoryMethodWithArgs(body, variableName, "inMapping", new StringLiteralExpr(entry.getKey()), new StringLiteralExpr(entry.getValue()));
        }
        for (Entry<String, String> entry : node.getOutMappings().entrySet()) {
            addFactoryMethodWithArgs(body, variableName, "outMapping", new StringLiteralExpr(entry.getKey()), new StringLiteralExpr(entry.getValue()));
        }
    }

    protected void visitMetaData(Map<String, Object> metadata, BlockStmt body, String variableName) {
        for (Entry<String, Object> entry : metadata.entrySet()) {
            Expression value = null;

            if (entry.getValue() instanceof Boolean) {
                value = new BooleanLiteralExpr((Boolean) entry.getValue());
            } else if (entry.getValue() instanceof Integer) {
                value = new IntegerLiteralExpr((Integer) entry.getValue());
            } else if (entry.getValue() instanceof Long) {
                value = new LongLiteralExpr((Long) entry.getValue());
            } else if (entry.getValue() instanceof String) {
                value = new StringLiteralExpr(entry.getValue().toString());
            }
            if (value != null) {
                addFactoryMethodWithArgs(body, variableName, "metaData", new StringLiteralExpr(entry.getKey()), value);
            }
        }
    }
}
