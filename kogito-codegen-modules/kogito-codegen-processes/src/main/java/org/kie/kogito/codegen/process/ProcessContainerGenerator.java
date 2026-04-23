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
package org.kie.kogito.codegen.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.template.InvalidTemplateException;
import org.kie.kogito.codegen.api.template.TemplatedGenerator;
import org.kie.kogito.codegen.core.AbstractApplicationSection;
import org.kie.kogito.codegen.core.CodegenUtils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.UnknownType;

public class ProcessContainerGenerator extends AbstractApplicationSection {

    public static final String SECTION_CLASS_NAME = "Processes";
    private static final int CHUNK_SIZE = 1000;

    private final List<ProcessGenerator> processes;
    private final TemplatedGenerator templatedGenerator;

    public ProcessContainerGenerator(KogitoBuildContext context) {
        super(context, SECTION_CLASS_NAME);
        this.processes = new ArrayList<>();
        this.templatedGenerator = TemplatedGenerator.builder()
                .withTargetTypeName(SECTION_CLASS_NAME)
                .build(context, "ProcessContainer");
    }

    public void addProcess(ProcessGenerator p) {
        processes.add(p);
    }

    public List<ProcessGenerator> getProcesses() {
        return Collections.unmodifiableList(this.processes);
    }

    @Override
    public CompilationUnit compilationUnit() {
        CompilationUnit compilationUnit = templatedGenerator.compilationUnitOrThrow("Invalid Template: No CompilationUnit");
        if (!context.hasDI()) {
            setupStaticFactoryMap(compilationUnit);
            setupProcessById(compilationUnit);
            setupProcessIds(compilationUnit);
        }
        return compilationUnit;
    }

    private void setupStaticFactoryMap(CompilationUnit compilationUnit) {
        ClassOrInterfaceDeclaration clazz = compilationUnit.getClassByName(SECTION_CLASS_NAME)
                .orElseThrow(() -> new InvalidTemplateException(templatedGenerator, "Class not found"));

        clazz.addFieldWithInitializer(
                StaticJavaParser.parseType("java.util.Map<String, java.util.function.Function<org.kie.kogito.Application, org.kie.kogito.process.Process<? extends org.kie.kogito.Model>>>"),
                "factories",
                StaticJavaParser.parseExpression("new java.util.HashMap<>()"),
                Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);

        List<List<ProcessGenerator>> chunks = CodegenUtils.partitionList(processes, CHUNK_SIZE);
        BlockStmt staticInitBody = new BlockStmt();
        String localAppClass = context.getPackageName() + ".Application";

        for (int i = 0; i < chunks.size(); i++) {
            String methodName = "initFactories_" + i;

            MethodDeclaration chunkMethod = clazz.addMethod(methodName, Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
            BlockStmt body = new BlockStmt();

            for (ProcessGenerator p : chunks.get(i)) {
                Expression castedApp = new com.github.javaparser.ast.expr.CastExpr(
                        StaticJavaParser.parseType(localAppClass),
                        new NameExpr("app"));

                ObjectCreationExpr newProcess = new ObjectCreationExpr()
                        .setType(p.targetCanonicalName())
                        .addArgument(castedApp)
                        .addArgument(new NullLiteralExpr());

                MethodCallExpr configuredProcess = new MethodCallExpr(newProcess, "configure");

                LambdaExpr lambda = new LambdaExpr(new Parameter(new UnknownType(), "app"), configuredProcess);

                MethodCallExpr putCall = new MethodCallExpr(new NameExpr("factories"), "put")
                        .addArgument(new StringLiteralExpr(p.processId()))
                        .addArgument(lambda);
                body.addStatement(putCall);
            }
            chunkMethod.setBody(body);

            staticInitBody.addStatement(new MethodCallExpr(methodName));
        }

        clazz.addMember(new InitializerDeclaration(true, staticInitBody));
    }

    private void setupProcessIds(CompilationUnit compilationUnit) {
        MethodDeclaration processIds = compilationUnit.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals("processIds"))
                .orElseThrow(() -> new InvalidTemplateException(templatedGenerator, "Cannot find 'processIds' method body"));

        processIds.setBody(StaticJavaParser.parseBlock("{ return factories.keySet(); }"));
    }

    private void setupProcessById(CompilationUnit compilationUnit) {
        MethodDeclaration processById = compilationUnit.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals("processById"))
                .orElseThrow(() -> new InvalidTemplateException(templatedGenerator, "Cannot find 'processById' method body"));

        BlockStmt body = StaticJavaParser.parseBlock(
                "{ return mappedProcesses.computeIfAbsent(processId, k -> { " +
                        "    java.util.function.Function<org.kie.kogito.Application, org.kie.kogito.process.Process<? extends org.kie.kogito.Model>> factory = factories.get(k); " +
                        "    return factory != null ? factory.apply(application) : null; " +
                        "}); }");
        processById.setBody(body);
    }
}
