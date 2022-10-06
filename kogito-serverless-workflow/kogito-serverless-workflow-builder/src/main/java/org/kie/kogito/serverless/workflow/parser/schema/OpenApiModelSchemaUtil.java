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

import org.kie.kogito.serverless.workflow.SWFConstants;

public final class OpenApiModelSchemaUtil {

    private OpenApiModelSchemaUtil() {
    }

    /**
     * Path to save the partial OpenAPI file with the additional model provided by the Workflow definition
     *
     * @see <a href="https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.asciidoc#location-and-formats">MicroProfile OpenAPI Specification -
     *      Location And Formats</a>
     */
    public static String getInputModelRef(String workflowId) {
        return "#/components/schemas/" + workflowId + '_' + SWFConstants.DEFAULT_WORKFLOW_VAR;
    }
}
