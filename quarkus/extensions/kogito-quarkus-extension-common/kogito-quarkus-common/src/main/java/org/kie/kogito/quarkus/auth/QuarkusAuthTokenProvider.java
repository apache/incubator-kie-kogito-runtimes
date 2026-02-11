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

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.kogito.workitem.rest.auth.AuthTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.SecurityIdentity;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Quarkus implementation of AuthTokenProvider.
 * Provides bearer tokens with the following priority:
 * 1. Security Context (authenticated user's JWT token)
 * 2. Process-specific configuration (kogito.processes.<processId>.<TaskName>.access_token)
 *
 * This class is loaded via ServiceLoader and uses Arc.container() to access CDI beans.
 */
@ApplicationScoped
public class QuarkusAuthTokenProvider implements AuthTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(QuarkusAuthTokenProvider.class);

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
            Optional<String> configToken = getConfig().getOptionalValue(configKey, String.class);
            if (configToken.isPresent() && !configToken.get().isEmpty()) {
                logger.debug("Using token from configuration '{}' for process '{}', task '{}'",
                        configKey, processId, taskName);
                return configToken;
            }
        }

        // Priority 3: Process-specific configuration with taskId (node ID)
        if (taskId != null && !taskId.isEmpty()) {
            String configKey = String.format("kogito.processes.%s.%s.access_token", processId, taskId);
            Optional<String> configToken = getConfig().getOptionalValue(configKey, String.class);
            if (configToken.isPresent() && !configToken.get().isEmpty()) {
                logger.debug("Using token from configuration '{}' for process '{}', taskId '{}'",
                        configKey, processId, taskId);
                return configToken;
            }
        }

        logger.debug("No token found for process '{}', task '{}', taskId '{}'", processId, taskName, taskId);
        return Optional.empty();
    }

    /**
     * Retrieves the token from the Quarkus security context.
     *
     * @return Optional containing the token if available, empty otherwise
     */
    private Optional<String> getTokenFromSecurityContext() {
        try {
            return getSecurityIdentity()
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

    /**
     * Gets the SecurityIdentity from the CDI container.
     *
     * @return Optional containing the SecurityIdentity if available
     */
    private Optional<SecurityIdentity> getSecurityIdentity() {
        try {
            ArcContainer container = Arc.container();
            if (container == null) {
                logger.debug("Arc container is not available");
                return Optional.empty();
            }

            InstanceHandle<SecurityIdentity> handle = container.instance(SecurityIdentity.class);
            if (handle.isAvailable()) {
                return Optional.of(handle.get());
            }

            logger.debug("SecurityIdentity is not available in CDI container");
            return Optional.empty();
        } catch (Exception e) {
            logger.debug("Error getting SecurityIdentity from CDI container: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Gets the Config instance.
     *
     * @return the Config instance
     */
    private Config getConfig() {
        try {
            ArcContainer container = Arc.container();
            if (container != null) {
                InstanceHandle<Config> handle = container.instance(Config.class);
                if (handle.isAvailable()) {
                    return handle.get();
                }
            }
        } catch (Exception e) {
            logger.debug("Could not get Config from CDI container, falling back to ConfigProvider: {}", e.getMessage());
        }

        // Fallback to ConfigProvider if CDI is not available
        return ConfigProvider.getConfig();
    }

    @Override
    public boolean isAvailable() {
        try {
            // Check if Arc container is available (indicates Quarkus runtime)
            ArcContainer container = Arc.container();
            if (container == null) {
                logger.debug("QuarkusAuthTokenProvider is not available: Arc container is null");
                return false;
            }

            logger.debug("QuarkusAuthTokenProvider is available");
            return true;
        } catch (Exception e) {
            logger.debug("QuarkusAuthTokenProvider is not available: {}", e.getMessage());
            return false;
        }
    }
}
