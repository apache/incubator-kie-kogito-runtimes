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

package org.kie.kogito.serverless.workflow.parser.schema;

import java.util.Optional;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jbpm.workflow.core.WorkflowModelValidator;
import org.jbpm.workflow.core.WorkflowProcess;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcess;
import org.kie.kogito.serverless.workflow.parser.SwaggerSchemaProvider;

import io.serverlessworkflow.api.Workflow;

public final class OpenApiModelSchemaGenerator {

    private OpenApiModelSchemaGenerator() {
    }

    private static final Optional<SwaggerSchemaProvider> getSchemaSupplier(Optional<WorkflowModelValidator> validator) {
        return validator.filter(SwaggerSchemaProvider.class::isInstance).map(SwaggerSchemaProvider.class::cast);
    }

    public static Optional<SchemaInfo> generateOpenAPIModelSchema(KogitoWorkflowProcess workflow) {

        if (workflow instanceof WorkflowProcess) {
            WorkflowProcess workflowProcess = (WorkflowProcess) workflow;
            Optional<SwaggerSchemaProvider> inputSchemaSupplier = getSchemaSupplier(workflowProcess.getInputValidator());
            Optional<SwaggerSchemaProvider> outputSchemaSupplier = getSchemaSupplier(workflowProcess.getOutputValidator());
            if (inputSchemaSupplier.isEmpty() || outputSchemaSupplier.isEmpty()) {
                OpenAPI openAPI = OASFactory.createOpenAPI().openapi(workflow.getId() + '_' + "workflowmodelschema");
                inputSchemaSupplier.ifPresent(v -> openAPI.components(OASFactory.createComponents().addSchema(workflow.getId() + "_input", v.getSchema())));
                outputSchemaSupplier.ifPresent(v -> openAPI.components(OASFactory.createComponents().addSchema(workflow.getId() + "_output", v.getSchema())));
                return Optional.of(new SchemaInfo(workflow.getId(), openAPI, getInputModelRef(workflow.getId())));
            }
        }
        return Optional.empty();
    }

    /**
     * Path to save the partial OpenAPI file with the additional model provided by the Workflow definition
     *
     * @see <a href="https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.asciidoc#location-and-formats">MicroProfile OpenAPI Specification -
     *      Location And Formats</a>
     */
    private static String getInputModelRef(String workflowId) {
        return "#/components/schemas/" + workflowId + "_input";
    }

    public static String getOutputModelRef(String workflowId) {
        return "#/components/schemas/" + workflowId + "_output";
    }

}
