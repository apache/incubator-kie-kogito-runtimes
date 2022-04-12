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

import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Optional;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.kie.kogito.jackson.utils.ObjectMapperFactory;
import org.kie.kogito.serverless.workflow.parser.ParserContext;
import org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.serverlessworkflow.api.Workflow;

import static org.kie.kogito.serverless.workflow.SWFConstants.DEFAULT_WORKFLOW_VAR;

public final class SchemaUtils {

    private SchemaUtils() {
    }

    public static Schema fromWorkflowDataInputToOpenApiSchema(Workflow workflow, ParserContext parserContext, String uriStr, String authRef) {
        final Schema inputSchema = OASFactory.createObject(Schema.class)
                .description("Data input schema for the workflow definition")
                .type(Schema.SchemaType.OBJECT)
                .properties(Collections.singletonMap(DEFAULT_WORKFLOW_VAR, OASFactory.createObject(Schema.class).type(Schema.SchemaType.OBJECT)))
                .required(Collections.singletonList(DEFAULT_WORKFLOW_VAR));
        if (uriStr != null) {
            final Optional<byte[]> bytes = ServerlessWorkflowUtils.loadResourceFile(workflow, parserContext, uriStr, authRef);
            if (bytes.isPresent()) {
                // SchemaLoader will load all the references from other files into the schema
                final JSONObject rawSchema = new JSONObject(new JSONTokener(new String(bytes.get())));
                final SchemaLoader schemaLoader = SchemaLoader.builder()
                        .schemaJson(rawSchema)
                        .resolutionScope(uriStr)
                        .schemaClient(SchemaClient.classPathAwareClient())
                        .build();
                try {
                    // create the "workflowdata" complex schema
                    final ObjectMapper objectMapper = ObjectMapperFactory.get();
                    inputSchema.properties(Collections.singletonMap(DEFAULT_WORKFLOW_VAR, objectMapper.readValue(schemaLoader.load().build().toString(), JsonSchemaImpl.class)));
                } catch (JsonProcessingException e) {
                    throw new UncheckedIOException("Error deserializing JSON Schema " + uriStr + " for workflow " + workflow.getId(), e);
                }
            }
        }
        return inputSchema;
    }
}
