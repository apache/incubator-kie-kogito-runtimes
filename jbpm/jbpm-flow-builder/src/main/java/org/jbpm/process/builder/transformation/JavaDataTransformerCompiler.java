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
package org.jbpm.process.builder.transformation;

import java.util.List;

import org.jbpm.workflow.core.impl.DataDefinition;
import org.jbpm.workflow.core.node.Transformation;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class JavaDataTransformerCompiler implements DataTransformerCompiler {

    @Override
    public String[] dialects() {
        return new String[] { "http://www.java.com/java" };
    }

    @Override
    public Expression compile(List<DataDefinition> inputs, List<DataDefinition> outputs, Transformation transformation) {
        // build lambda function
        String expression = transformation.getExpression();
        BlockStmt body = parseExpression(expression);

        if (body == null) {
            body = parseStatement(expression);
        }

        if (body == null) {
            body = StaticJavaParser.parseBlock("{" + expression + "}");
        }

        for (DataDefinition input : inputs) {
            ClassOrInterfaceType type = StaticJavaParser.parseClassOrInterfaceType(input.getType());
            VariableDeclarationExpr target = new VariableDeclarationExpr(type, input.getLabel());
            Expression source = new CastExpr(type, new MethodCallExpr(new NameExpr("parameters"), "get", NodeList.nodeList(new StringLiteralExpr(input.getLabel()))));
            AssignExpr assignment = new AssignExpr(target, source, AssignExpr.Operator.ASSIGN);
            body.addStatement(0, assignment);
        }

        Expression lambda = new LambdaExpr(NodeList.nodeList(new Parameter(StaticJavaParser.parseClassOrInterfaceType("java.util.Map<String,Object>"), "parameters")), body, true);
        ClassOrInterfaceType type = StaticJavaParser.parseClassOrInterfaceType("java.util.function.Function<java.util.Map<String, Object>, Object>");
        return new CastExpr(type, lambda);
    }

    private BlockStmt parseStatement(String expression) {
        try {
            BlockStmt block = new BlockStmt();
            block.addStatement(StaticJavaParser.parseStatement(expression));
            return block;
        } catch (ParseProblemException e) {
            return null;
        }
    }

    private BlockStmt parseExpression(String expression) {
        try {
            BlockStmt block = new BlockStmt();
            block.addStatement(new ReturnStmt(StaticJavaParser.parseExpression(expression)));
            return block;
        } catch (ParseProblemException e) {
            return null;
        }
    }
}
