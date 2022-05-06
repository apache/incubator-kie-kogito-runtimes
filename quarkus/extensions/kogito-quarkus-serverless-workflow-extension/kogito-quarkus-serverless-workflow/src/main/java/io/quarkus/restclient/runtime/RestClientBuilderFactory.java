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
package io.quarkus.restclient.runtime;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;

public class RestClientBuilderFactory extends RestClientBase {

    public RestClientBuilderFactory(Class<?> proxyType, String baseUriFromAnnotation, String configKey) {
        super(proxyType, baseUriFromAnnotation, configKey, new Class[0]);
    }

    public static RestClientBuilder build(Class<?> restClass) {
        RegisterRestClient annotation = restClass.getAnnotation(RegisterRestClient.class);
        RestClientBuilderFactory instance = new RestClientBuilderFactory(restClass, annotation.baseUri(), annotation.configKey());
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        instance.configureBaseUrl(builder);
        instance.configureTimeouts(builder);
        instance.configureProviders(builder);
        instance.configureSsl(builder);
        instance.configureProxy(builder);
        instance.configureRedirects(builder);
        instance.configureQueryParamStyle(builder);
        instance.configureCustomProperties(builder);
        // If we have context propagation, then propagate context to the async client threads
        InstanceHandle<ManagedExecutor> managedExecutor = Arc.container().instance(ManagedExecutor.class);
        if (managedExecutor.isAvailable()) {
            builder.executorService(managedExecutor.get());
        }
        return builder;
    }

}
