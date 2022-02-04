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
package org.kie.kogito.serverless.workflow.actions;

import org.jbpm.process.instance.impl.Action;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;
import org.kie.kogito.jackson.utils.ObjectMapperFactory;
import org.kie.kogito.serverless.workflow.io.URIContentLoaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import static org.kie.kogito.serverless.workflow.actions.ActionUtils.getWorkflowData;

public class DataInputSchemaAction implements Action {

    private static JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
    private static final Logger logger = LoggerFactory.getLogger(DataInputSchemaAction.class);

    protected String schema;
    protected boolean failOnValidationErrors;

    public DataInputSchemaAction(String schema, boolean failOnValidationErrors) {
        this.schema = schema;
        this.failOnValidationErrors = failOnValidationErrors;
    }

    @Override
    public void execute(KogitoProcessContext context) throws Exception {

        ProcessingReport report = factory.getJsonSchema(ObjectMapperFactory.get().readValue(URIContentLoaderFactory.loader(schema).toBytes(), JsonNode.class)).validate(getWorkflowData(context), true);
        logger.info("Validation report {}", report);
        if (!report.isSuccess() && failOnValidationErrors) {
            throw new IllegalArgumentException(report.toString());
        }
    }
}
