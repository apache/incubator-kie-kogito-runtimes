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
package org.kie.kogito.addons.quarkus.knative.serving.customfunctions;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.kie.kogito.addons.k8s.resource.catalog.KubernetesServiceCatalog;
import org.kogito.workitem.rest.RestWorkItemHandlerUtils;

import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

public final class KnativeWorkItemHandlerProducer {

    @Inject
    Vertx vertx;

    @Inject
    KubernetesServiceCatalog kubernetesServiceCatalog;

    @Produces
    KnativeWorkItemHandler createKnativeWorkItemHandler() {
        return new KnativeWorkItemHandler(getHttpClient(), getHttpsClient(), kubernetesServiceCatalog);
    }

    private WebClient getHttpsClient() {
        return WebClient.create(vertx, RestWorkItemHandlerUtils.sslWebClientOptions());
    }

    private WebClient getHttpClient() {
        return WebClient.create(vertx, new WebClientOptions());
    }
}
