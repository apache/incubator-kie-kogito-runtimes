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
package org.kie.addons.springboot.auth;

import java.util.Optional;

import org.kie.kogito.auth.AuthTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Spring Boot implementation of AuthTokenProvider.
 * Reads authentication tokens from the Spring Security context
 * and provides fallback to configured default token.
 */
@Component
@ConditionalOnClass({ SecurityContextHolder.class })
public class SpringBootAuthTokenProvider implements AuthTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootAuthTokenProvider.class);

    private final SpringBootAuthTokenHelper authTokenHelper;

    @Value("${" + BEARER_TOKEN_CONFIG_PROPERTY + ":#{null}}")
    private String defaultBearerToken;

    public SpringBootAuthTokenProvider(@Autowired SpringBootAuthTokenHelper authTokenHelper) {
        this.authTokenHelper = authTokenHelper;
    }

    @Override
    public Optional<String> getActualToken() {
        try {
            Optional<String> token = authTokenHelper.getAuthToken();
            if (token.isPresent()) {
                logger.debug("Retrieved token from Spring Security context");
                // Remove "Bearer " prefix if present, as SpringBootAuthTokenHelper adds it
                String tokenValue = token.get();
                if (tokenValue.startsWith("Bearer ")) {
                    return Optional.of(tokenValue.substring(7).trim());
                }
                return token;
            }
        } catch (Exception e) {
            logger.debug("Could not retrieve token from security context: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getActualTokenOrDefault() {
        Optional<String> actualToken = getActualToken();
        if (actualToken.isPresent()) {
            return actualToken;
        }

        if (defaultBearerToken != null && !defaultBearerToken.trim().isEmpty()) {
            logger.debug("Using default bearer token from configuration");
            return Optional.of(defaultBearerToken);
        }

        logger.debug("No token available from security context or configuration");
        return Optional.empty();
    }
}
