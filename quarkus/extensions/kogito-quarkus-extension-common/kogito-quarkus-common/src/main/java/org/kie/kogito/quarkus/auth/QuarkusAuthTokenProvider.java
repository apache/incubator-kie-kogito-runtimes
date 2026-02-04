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
package org.kie.kogito.quarkus.auth;

import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.kogito.auth.AuthTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.SecurityIdentity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

/**
 * Quarkus implementation of AuthTokenProvider.
 * Reads authentication tokens from the Quarkus security context (SecurityIdentity)
 * and provides fallback to configured default token.
 */
@ApplicationScoped
public class QuarkusAuthTokenProvider implements AuthTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(QuarkusAuthTokenProvider.class);

    @Inject
    Instance<SecurityIdentity> identity;

    @Inject
    @ConfigProperty(name = BEARER_TOKEN_CONFIG_PROPERTY)
    Optional<String> defaultBearerToken;

    private Optional<SecurityIdentity> getIdentity() {
        return identity.isResolvable() ? Optional.of(identity.get()) : Optional.empty();
    }

    @Override
    public Optional<String> getActualToken() {
        try {
            return getIdentity()
                    .filter(securityIdentity -> !securityIdentity.isAnonymous())
                    .flatMap(securityIdentity -> {
                        TokenCredential credential = securityIdentity.getCredential(TokenCredential.class);
                        if (credential != null) {
                            String token = credential.getToken();
                            logger.debug("Retrieved token from security context for user: {}",
                                    securityIdentity.getPrincipal().getName());
                            return Optional.ofNullable(token);
                        }
                        return Optional.empty();
                    });
        } catch (Exception e) {
            logger.debug("Could not retrieve token from security context: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @ActivateRequestContext
    public Optional<String> getActualTokenOrDefault() {
        Optional<String> actualToken = getActualToken();
        if (actualToken.isPresent()) {
            logger.debug("Using token from security context");
            return actualToken;
        }

        if (defaultBearerToken.isPresent()) {
            logger.debug("Using default bearer token from Quarkus configuration property: {}", BEARER_TOKEN_CONFIG_PROPERTY);
            return defaultBearerToken;
        }

        logger.debug("No token available from security context or configuration");
        return Optional.empty();
    }
}
