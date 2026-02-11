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
package org.kogito.workitem.rest.auth;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.WorkItemExecutionException;
import org.kie.kogito.process.workitems.impl.WorkItemParamResolver;
import org.kie.kogito.serverless.workflow.utils.ConfigResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete resolver for bearer tokens in REST work items.
 * Uses AuthTokenProviderHolder to access framework-specific token providers.
 * Implements both WorkItemParamResolver for runtime parameter resolution
 * and ConfigResolver for configuration management.
 *
 * Token resolution priority:
 * 1. Security Context (authenticated user's JWT)
 * 2. Process-specific configuration with taskName (kogito.processes.<processId>.<taskName>.access_token)
 * 3. Process-specific configuration with taskId (kogito.processes.<processId>.<taskId>.access_token)
 */
public class BearerTokenResolver implements WorkItemParamResolver<String>, ConfigResolver {

    private static final Logger logger = LoggerFactory.getLogger(BearerTokenResolver.class);

    private final String processId;
    private final String taskName;

    public BearerTokenResolver(String processId, String taskName) {
        this.processId = processId;
        this.taskName = taskName;
        logger.debug("Created BearerTokenResolver for process '{}', task '{}'", processId, taskName);
    }

    @Override
    public String apply(KogitoWorkItem workItem) {
        String taskId = workItem.getNodeInstance() != null ? workItem.getNodeInstance().getNodeDefinitionId() : null;

        logger.debug("Resolving bearer token for process '{}', task '{}', taskId '{}'",
                processId, taskName, taskId);

        AuthTokenProvider provider = AuthTokenProviderHolder.getInstance();
        if (provider == null) {
            String errorMessage = String.format(
                    "No AuthTokenProvider available. Cannot resolve bearer token for process '%s', task '%s'. " +
                            "Ensure a framework-specific implementation (Quarkus/Spring Boot) is on the classpath.",
                    processId, taskName);
            logger.error(errorMessage);
            throw new WorkItemExecutionException(errorMessage);
        }

        Optional<String> token = provider.getToken(processId, taskName, taskId);
        if (token.isPresent() && !token.get().isEmpty()) {
            logger.debug("Successfully resolved bearer token for process '{}', task '{}', taskId '{}'",
                    processId, taskName, taskId);
            return removeBearerPrefix(token.get());
        }

        String errorMessage = String.format(
                "Authentication token not found for REST call. " +
                        "Token resolution failed for process '%s', task '%s', taskId '%s'. " +
                        "Checked sources: " +
                        "1. Security Context (AuthTokenProvider) - no authenticated user found, " +
                        "2. Process-specific configuration (kogito.processes.%s.%s.access_token) - not configured, " +
                        "3. Process-specific configuration (kogito.processes.%s.%s.access_token) - not configured. " +
                        "Please ensure the request is authenticated or configure a process-specific token.",
                processId, taskName, taskId, processId, taskName, processId, taskId);
        logger.error(errorMessage);
        throw new WorkItemExecutionException(errorMessage);
    }

    private String removeBearerPrefix(String token) {
        if (token == null || token.isEmpty()) {
            return token;
        }
        return token.startsWith("Bearer ") ? token.substring(7).trim() : token;
    }

    public String getProcessId() {
        return processId;
    }

    public String getTaskName() {
        return taskName;
    }

    @Override
    public <T> Optional<T> getConfigProperty(String name, Class<T> clazz) {
        logger.debug("Getting config property '{}' of type {}", name, clazz.getSimpleName());

        AuthTokenProvider provider = AuthTokenProviderHolder.getInstance();
        if (provider == null) {
            logger.warn("No AuthTokenProvider available for config property '{}'", name);
            return Optional.empty();
        }

        Optional<String> token = provider.getToken(processId, taskName);
        if (token.isPresent() && clazz.isAssignableFrom(String.class)) {
            @SuppressWarnings("unchecked")
            T result = (T) token.get();
            return Optional.of(result);
        }

        return Optional.empty();
    }

    @Override
    public Iterable<String> getPropertyNames() {
        String configKey = String.format("kogito.processes.%s.%s.access_token", processId, taskName);
        return Collections.singletonList(configKey);
    }

    @Override
    public <T> Collection<T> getIndexedConfigProperty(String name, Class<T> clazz) {
        logger.debug("Indexed config property '{}' not supported for bearer tokens", name);
        return Collections.emptyList();
    }
}
