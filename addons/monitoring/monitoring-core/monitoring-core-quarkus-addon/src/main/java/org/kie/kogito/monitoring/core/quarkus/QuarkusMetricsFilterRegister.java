/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.monitoring.core.quarkus;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.kogito.monitoring.core.common.Constants;

@Provider
public class QuarkusMetricsFilterRegister implements DynamicFeature {

    @ConfigProperty(name = Constants.HTTP_INTERCEPTOR_USE_DEFAULT, defaultValue = "true")
    boolean httpInterceptorUseDefault;

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (httpInterceptorUseDefault) {
            context.register(new QuarkusMetricsInterceptor());
        }
    }

    // for testing purpose
    void setHttpInterceptorUseDefault(boolean httpInterceptorUseDefault) {
        this.httpInterceptorUseDefault = httpInterceptorUseDefault;
    }
}
