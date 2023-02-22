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
package org.kie.kogito.codegen.process;

import java.util.Optional;

import org.jbpm.compiler.canonical.ModelMetaData;
import org.jbpm.compiler.canonical.ProcessToExecModelGenerator;
import org.jbpm.workflow.core.WorkflowModelValidator;
import org.kie.api.definition.process.WorkflowProcess;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcess;
import org.kie.kogito.serverless.workflow.parser.SwaggerSchemaGenerator;
import org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils;



public class ModelClassGenerator {

    private final String modelFileName;
    private final ModelMetaData modelMetaData;
    private final String modelClassName;
    private final Optional<SwaggerSchemaGenerator> inputSchemaSupplier;
    private final Optional<SwaggerSchemaGenerator> outputSchemaSupplier;

    public ModelClassGenerator(KogitoBuildContext context, WorkflowProcess workFlowProcess) {
        if (workFlowProcess.getType().equals(KogitoWorkflowProcess.SW_TYPE)) {
            modelMetaData = ServerlessWorkflowUtils.getModelMetadata(workFlowProcess);
            inputSchemaSupplier = getSchemaSupplier(getProcess(workFlowProcess).flatMap(org.jbpm.workflow.core.WorkflowProcess::getInputValidator));
            outputSchemaSupplier = getSchemaSupplier(getProcess(workFlowProcess).flatMap(org.jbpm.workflow.core.WorkflowProcess::getOutputValidator));
        } else {
            modelMetaData = ProcessToExecModelGenerator.INSTANCE.generateModel(workFlowProcess);
            inputSchemaSupplier = Optional.empty();
            outputSchemaSupplier = Optional.empty();
        }
        modelClassName = modelMetaData.getModelClassName();
        modelFileName = modelMetaData.getModelClassName().replace('.', '/') + ".java";
        modelMetaData.setSupportsValidation(context.isValidationSupported());
        modelMetaData.setSupportsOpenApiGeneration(context.isOpenApiSpecSupported());
    }

    private static final Optional<SwaggerSchemaGenerator> getSchemaSupplier(Optional<WorkflowModelValidator> validator) {
        return validator.filter(SwaggerSchemaGenerator.class::isInstance).map(SwaggerSchemaGenerator.class::cast);
    }

    private static final Optional<org.jbpm.workflow.core.WorkflowProcess> getProcess(WorkflowProcess workFlowProcess) {
        return workFlowProcess instanceof org.jbpm.workflow.core.WorkflowProcess ? Optional.of((org.jbpm.workflow.core.WorkflowProcess) workFlowProcess) : Optional.empty();
    }

    public ModelMetaData generate() {
        return modelMetaData;
    }

    public String generatedFilePath() {
        return modelFileName;
    }

    public String simpleName() {
        return modelMetaData.getModelClassSimpleName();
    }

    public String className() {
        return modelClassName;
    }
}
