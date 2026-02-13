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

import org.kogito.workitem.rest.auth.AuthTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Spring Boot implementation of AuthTokenProvider.
 * Provides bearer tokens with the following priority:
 * 1. Security Context (authenticated user's JWT token)
 * 2. Process-specific configuration with taskName (kogito.processes.<processId>.<taskName>.access_token)
 * 3. Process-specific configuration with taskId (kogito.processes.<processId>.<taskId>.access_token)
 */
@Component
@ConditionalOnClass({ SecurityContextHolder.class })
public class SpringBootAuthTokenProvider implements AuthTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootAuthTokenProvider.class);

    private final SpringBootAuthTokenHelper authTokenHelper;
    private final Environment environment;

    public SpringBootAuthTokenProvider(@Autowired SpringBootAuthTokenHelper authTokenHelper,
            @Autowired Environment environment) {
        this.authTokenHelper = authTokenHelper;
        this.environment = environment;
    }

    @Override
    public Optional<String> getToken(String processId, String taskName, String taskId) {
        logger.debug("Resolving token for process '{}', task '{}', taskId '{}'", processId, taskName, taskId);

        // Priority 1: Security Context (authenticated user's token)
        Optional<String> securityContextToken = getTokenFromSecurityContext();
        if (securityContextToken.isPresent()) {
            logger.debug("Using token from security context for process '{}', task '{}', taskId '{}'",
                    processId, taskName, taskId);
            return securityContextToken;
        }

        // Priority 2: Process-specific configuration with taskName (if present in .wid file)
        if (taskName != null && !taskName.isEmpty()) {
            String configKey = String.format("kogito.processes.%s.%s.access_token", processId, taskName);
            String configToken = environment.getProperty(configKey);
            if (configToken != null && !configToken.trim().isEmpty()) {
                logger.debug("Using token from configuration '{}' for process '{}', task '{}'",
                        configKey, processId, taskName);
                return Optional.of(configToken);
            }
        }

        // Priority 3: Process-specific configuration with taskId (node ID)
        if (taskId != null && !taskId.isEmpty()) {
            String configKey = String.format("kogito.processes.%s.%s.access_token", processId, taskId);
            String configToken = environment.getProperty(configKey);
            if (configToken != null && !configToken.trim().isEmpty()) {
                logger.debug("Using token from configuration '{}' for process '{}', taskId '{}'",
                        configKey, processId, taskId);
                return Optional.of(configToken);
            }
        }

        logger.debug("No token found for process '{}', task '{}', taskId '{}'", processId, taskName, taskId);
        return Optional.empty();
    }

    /**
     * Retrieves the token from the Spring Security context.
     * 
     * @return Optional containing the token if available, empty otherwise
     */
    private Optional<String> getTokenFromSecurityContext() {
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
    public boolean isAvailable() {
        try {
            // Check if Spring Security context is available
            return SecurityContextHolder.getContext() != null;
        } catch (Exception e) {
            logger.debug("SpringBootAuthTokenProvider is not available: {}", e.getMessage());
            return false;
        }
    }
}
