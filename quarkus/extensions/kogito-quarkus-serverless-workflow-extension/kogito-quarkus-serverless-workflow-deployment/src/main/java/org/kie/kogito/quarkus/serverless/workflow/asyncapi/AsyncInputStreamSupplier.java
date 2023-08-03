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
package org.kie.kogito.quarkus.serverless.workflow.asyncapi;

import java.io.InputStream;

import org.kie.kogito.quarkus.serverless.workflow.WorkflowOperationResource;

import io.quarkiverse.asyncapi.generator.input.InputStreamSupplier;

class AsyncInputStreamSupplier implements InputStreamSupplier {

    private final WorkflowOperationResource resource;

    public AsyncInputStreamSupplier(WorkflowOperationResource resource) {
        this.resource = resource;
    }

    @Override
    public InputStream get() {
        return resource.getContentLoader().getInputStream();
    }
}
