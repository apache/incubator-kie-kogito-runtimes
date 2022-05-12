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
package org.kie.kogito.quarkus.serverless.workflow.grpc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.kie.kogito.quarkus.serverless.workflow.WorkflowCodeGenUtils;
import org.kie.kogito.quarkus.serverless.workflow.WorkflowOperationResource;

import io.quarkus.bootstrap.prebuild.CodeGenException;
import io.quarkus.deployment.CodeGenContext;
import io.quarkus.deployment.CodeGenProvider;
import io.serverlessworkflow.api.functions.FunctionDefinition;
import io.serverlessworkflow.api.functions.FunctionDefinition.Type;

public class WorkflowGRPCGenerator implements CodeGenProvider {

    @Override
    public String providerId() {
        return "serverless-workflow-grpc";
    }

    @Override
    public String inputExtension() {
        return "json";
    }

    @Override
    public String inputDirectory() {
        return "resources";
    }

    @Override
    public boolean trigger(CodeGenContext context) throws CodeGenException {
        List<WorkflowOperationResource> resources = WorkflowCodeGenUtils.operationResources(context.inputDir(), this::isRPC).collect(Collectors.toList());
        boolean result = false;
        // write files to proto dir
        Path outputPath = context.inputDir().getParent().resolve("proto");
        for (WorkflowOperationResource resource : resources) {
            result |= writeResource(outputPath, resource);
        }
        return result;
    }

    private boolean isRPC(FunctionDefinition function) {
        return function.getType() == Type.RPC;
    }

    private boolean writeResource(Path outputPath, WorkflowOperationResource resource) throws CodeGenException {
        try (InputStream is = resource.getInputStream()) {
            Files.write(outputPath.resolve(resource.getOperationId().getFileName()), resource.getInputStream().readAllBytes());
            return true;
        } catch (IOException io) {
            throw new CodeGenException(io);
        }
    }
}
