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
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.kie.kogito.codegen.process.ProcessCodegen;
import org.kie.kogito.serverless.workflow.io.URIContentLoaderFactory;
import org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils;
import org.kie.kogito.serverless.workflow.utils.WorkflowOperationId;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.functions.FunctionDefinition;

public class WorkflowCodeGenUtils {

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

    private static Stream<WorkflowOperationResource> processFunction(Workflow workflow, Predicate<FunctionDefinition> predicate) {
        return workflow.getFunctions().getFunctionDefs().stream().filter(predicate).map(f -> getResource(workflow, f));
    }

    private static WorkflowOperationResource getResource(Workflow workflow, FunctionDefinition function) {
        try {
            WorkflowOperationId operationId = WorkflowOperationId.fromOperation(function.getOperation());
            URI uri = operationId.getUri();
            return new WorkflowOperationResource(operationId,
                    URIContentLoaderFactory.buildLoader(uri, Thread.currentThread().getContextClassLoader(), workflow, function.getAuthRef()).getInputStream());
        } catch (IOException io) {
            throw new IllegalStateException(io);
        }
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
