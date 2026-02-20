/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kogito.addons.quarkus.common.rest.workitem;

import org.kie.kogito.auth.AuthTokenProvider;
import org.kogito.workitem.rest.auth.AuthTokenProviderHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthTokenProviderRegistration {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenProviderRegistration.class);

    private final Instance<AuthTokenProvider> authTokenProvider;

    @Inject
    public AuthTokenProviderRegistration(Instance<AuthTokenProvider> authTokenProvider) {
        this.authTokenProvider = authTokenProvider;
    }

    void onStartUp(@Observes StartupEvent startupEvent) {
        if (authTokenProvider.isResolvable()) {
            AuthTokenProviderHolder.setProvider(authTokenProvider.get());
            logger.info("AuthTokenProvider registered: {}", authTokenProvider.get().getClass().getName());
        } else {
            logger.debug("No AuthTokenProvider bean found, token propagation from security context will not be available");
        }
    }
}
