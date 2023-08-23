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
package org.kie.kogito.quarkus.serverless.workflow.openapi;

import java.util.Map;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.kie.kogito.serverless.workflow.parser.schema.OpenApiModelSchemaGenerator;

public final class ServerlessWorkflowOASFilter implements OASFilter {

    private final Map<String, Schema> schemasInfo;
    private final Map<String, Schema> defsSchemas;

    public ServerlessWorkflowOASFilter(Map<String, Schema> schemasInfo, Map<String, Schema> defsSchemas) {
        this.schemasInfo = schemasInfo;
        this.defsSchemas = defsSchemas;
    }

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        OpenApiModelSchemaGenerator.mergeSchemas(openAPI, schemasInfo, defsSchemas);
    }
}
