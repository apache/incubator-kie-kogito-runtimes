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

package org.kie.kogito.serverless.workflow.parser;

import java.net.URI;
import java.util.Collections;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.Schema;

import static org.kie.kogito.serverless.workflow.SWFConstants.DEFAULT_WORKFLOW_VAR;

public final class SchemaUtils {

    private SchemaUtils() {
    }

    public static Schema getDefaultWorkflowDataInputSchema(final URI reference) {
        final Schema inputSchema = OASFactory.createObject(Schema.class)
                .description("Data input schema for the workflow definition")
                .type(Schema.SchemaType.OBJECT)
                .properties(Collections.singletonMap(DEFAULT_WORKFLOW_VAR, OASFactory.createObject(Schema.class).type(Schema.SchemaType.OBJECT)))
                .required(Collections.singletonList(DEFAULT_WORKFLOW_VAR));
        if (reference != null) {
            inputSchema.getProperties().get(DEFAULT_WORKFLOW_VAR).ref(reference.toString());
        }
        return inputSchema;
    }

    public static Schema getDefaultWorkflowDataInputSchema() {
        return getDefaultWorkflowDataInputSchema(null);
    }

}
