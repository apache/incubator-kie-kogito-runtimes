/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

import org.eclipse.microprofile.openapi.models.OpenAPI;

public class SchemaInfo {

    private final String workflowId;

    private final OpenAPI openAPI;

    private final String targetModelRef;

    public SchemaInfo(String workflowId, OpenAPI openAPI, String modelRef) {
        this.workflowId = workflowId;
        this.openAPI = openAPI;
        this.targetModelRef = modelRef;
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    public String getTargetModelRef() {
        return targetModelRef;
    }

    public String getWorkflowId() {
        return workflowId;
    }
}
