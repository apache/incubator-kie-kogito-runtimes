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
package org.kie.kogito.integrationtests;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.kie.kogito.addon.cloudevents.quarkus.http.AbstractQuarkusCloudEventResource;

import io.cloudevents.CloudEvent;

@Path("/")
public class QuarkusCloudEventResource extends AbstractQuarkusCloudEventResource {

    @Override
    public CompletionStage<Response> cloudEventListener(CloudEvent event) {
        return CompletableFuture.completedFuture(Response.ok(this.serialize(event)).build());
    }
}
