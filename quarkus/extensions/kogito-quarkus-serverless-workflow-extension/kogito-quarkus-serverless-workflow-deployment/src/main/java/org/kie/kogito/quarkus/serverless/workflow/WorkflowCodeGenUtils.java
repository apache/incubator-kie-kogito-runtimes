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
package org.kie.kogito.quarkus.serverless.workflow;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.drools.codegen.common.GeneratedFile;
import org.drools.codegen.common.GeneratedFileType;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.process.ProcessCodegen;
import org.kie.kogito.process.impl.CachedWorkItemHandlerConfig;
import org.kie.kogito.serverless.workflow.io.URIContentLoaderFactory;
import org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils;
import org.kie.kogito.serverless.workflow.utils.WorkflowOperationId;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.functions.FunctionDefinition;

public class WorkflowCodeGenUtils {

    private final static String WORKITEMCONFIG_CLASSNAME = "GeneratedWorkItemHandlerConfig";

    private WorkflowCodeGenUtils() {
    }

    public static Stream<WorkflowOperationResource> operationResources(Path inputDir, Predicate<FunctionDefinition> predicate) {
        return getWorkflows(inputDir).map(w -> processFunction(w, predicate)).flatMap(x -> x);
    }

    public static Stream<Workflow> getWorkflows(Path inputDir) {
        try (Stream<Path> openApiFilesPaths = Files.walk(inputDir)) {
            return openApiFilesPaths
                    .filter(Files::isRegularFile)
                    .map(WorkflowCodeGenUtils::getWorkflow)
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static GeneratedFile generateWorkItemHandlerConfig(KogitoBuildContext context, Collection<GeneratedFile> generatedFiles) {
        CompilationUnit unit = new CompilationUnit(context.getPackageName());
        ClassOrInterfaceDeclaration clazz = unit.addClass(WORKITEMCONFIG_CLASSNAME);
        clazz.addExtendedType(CachedWorkItemHandlerConfig.class);
        clazz.addAnnotation(ApplicationScoped.class);
        BlockStmt body = clazz.addMethod("init").addAnnotation(PostConstruct.class).createBody();
        for (GeneratedFile generatedFile : generatedFiles) {
            final String refHandler = getRefHandler(generatedFile);
            final String fieldName = refHandler.toLowerCase();
            context.addGeneratedHandler(refHandler);
            clazz.addField(context.getPackageName() + "." + refHandler, fieldName).addAnnotation(Inject.class);
            body.addStatement(new MethodCallExpr(new SuperExpr(), "register").addArgument(new StringLiteralExpr(refHandler))
                    .addArgument(new NameExpr(fieldName)));
        }
        return fromCompilationUnit(context, unit, WORKITEMCONFIG_CLASSNAME);
    }

    private static String getRefHandler(GeneratedFile generatedFile) {
        String fileName = generatedFile.path().getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public static GeneratedFile fromCompilationUnit(KogitoBuildContext context, CompilationUnit unit, String className) {
        return new GeneratedFile(GeneratedFileType.SOURCE, Path.of("", context.getPackageName().split("\\.")).resolve(className + ".java"),
                unit.toString());
    }

    private static Stream<WorkflowOperationResource> processFunction(Workflow workflow, Predicate<FunctionDefinition> predicate) {
        return workflow.getFunctions().getFunctionDefs().stream().filter(predicate).map(f -> getResource(workflow, f));
    }

    private static WorkflowOperationResource getResource(Workflow workflow, FunctionDefinition function) {
        WorkflowOperationId operationId = WorkflowOperationId.fromOperation(function.getOperation());
        URI uri = operationId.getUri();
        return new WorkflowOperationResource(operationId,
                URIContentLoaderFactory.buildLoader(uri, Thread.currentThread().getContextClassLoader(), workflow, function.getAuthRef()));

    }

    private static Optional<Workflow> getWorkflow(Path p) {
        return ProcessCodegen.SUPPORTED_SW_EXTENSIONS.entrySet()
                .stream()
                .filter(e -> p.getFileName().toString().endsWith(e.getKey()))
                .map(e -> {
                    try (Reader r = Files.newBufferedReader(p)) {
                        return ServerlessWorkflowUtils.getWorkflow(r, e.getValue());
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }).findFirst();
    }

}
