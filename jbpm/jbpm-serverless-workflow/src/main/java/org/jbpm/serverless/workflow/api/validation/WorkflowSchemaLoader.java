/*
 * Copyright 2020-Present The Serverless Workflow Specification Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.jbpm.serverless.workflow.api.validation;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.jbpm.serverless.workflow.api.schemaclient.ResourceSchemaClient;
import org.json.JSONObject;
import org.json.JSONTokener;

public class WorkflowSchemaLoader {
    private static final JSONObject workflowSchema = new JSONObject(new JSONTokener(
            WorkflowSchemaLoader.class.getResourceAsStream("/schema/workflow.json")));

    public static Schema getWorkflowSchema() {
        SchemaLoader schemaLoader = SchemaLoader.builder()
                .schemaClient(new ResourceSchemaClient(new DefaultSchemaClient()))
                .schemaJson(workflowSchema)
                .resolutionScope("classpath:schema")
                .draftV7Support()
                .build();
        return schemaLoader.load().build();
    }
}
