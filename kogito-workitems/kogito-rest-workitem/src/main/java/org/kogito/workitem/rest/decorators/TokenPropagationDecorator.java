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
package org.kogito.workitem.rest.decorators;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kogito.workitem.rest.auth.AuthDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.mutiny.ext.web.client.HttpRequest;

enum AccessTokenAcquisitionStrategy {
    NONE("none"),
    CONFIGURED("configured"),
    PROPAGATE("propagated");

    private String name;

    AccessTokenAcquisitionStrategy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static AccessTokenAcquisitionStrategy fromName(String strategyName) {
        return Arrays.stream(AccessTokenAcquisitionStrategy.values())
                .filter(strategy -> strategy.getName().equals(strategyName))
                .findFirst()
                .orElse(NONE);
    }
}

public class TokenPropagationDecorator implements AuthDecorator {

    private static final Logger logger = LoggerFactory.getLogger(TokenPropagationDecorator.class);

    public static final String ACCESS_TOKEN_ACQUISITION_STRATEGY = "AccessTokenAcquisitionStrategy";

    private static final String PROPAGATE_TOKEN_PARAM = "propagateToken";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public void decorate(KogitoWorkItem item, Map<String, Object> parameters, HttpRequest<?> request) {
        Optional<String> bearerToken = getBearerToken(item, parameters);

        bearerToken.ifPresentOrElse(
                token -> {
                    logger.debug("Rest workItem `{}`: Bearer token available, request will be sent with authentication", item.getNodeInstance().getId());
                    request.bearerTokenAuthentication(token);
                },
                () -> logger.debug("Rest workItem `{}`: No Bearer Token available, request will be sent without authentication", item.getNodeInstance().getId()));
    }

    Optional<String> getBearerToken(KogitoWorkItem item, Map<String, Object> parameters) {
        AccessTokenAcquisitionStrategy strategy = AccessTokenAcquisitionStrategy.fromName(
                (String) parameters.get(ACCESS_TOKEN_ACQUISITION_STRATEGY));

        return switch (strategy) {
            case PROPAGATE -> getPropagatedTokenFromHeaders(item)
                    .or(() -> getConfiguredToken(parameters));
            case CONFIGURED -> getConfiguredToken(parameters);
            default -> {
                logger.debug("No token acquisition strategy specified or strategy is NONE, skipping authentication");
                yield Optional.empty();
            }
        };
    }

    private Optional<String> getPropagatedTokenFromHeaders(KogitoWorkItem item) {
        // Get token from process instance headers
        KogitoProcessInstance processInstance = item.getProcessInstance();
        if (processInstance == null) {
            logger.debug("No process instance available, cannot propagate token from headers");
            return Optional.empty();
        }

        Map<String, List<String>> headers = processInstance.getHeaders();
        if (headers == null || headers.isEmpty()) {
            logger.debug("No headers available in process instance, cannot propagate token");
            return Optional.empty();
        }

        List<String> authHeaders = headers.get(AUTHORIZATION_HEADER);
        if (authHeaders == null || authHeaders.isEmpty()) {
            logger.debug("No Authorization header found in process instance headers");
            return Optional.empty();
        }

        // Get the first Authorization header value
        String authHeader = authHeaders.get(0);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            logger.debug("Using propagated token from process instance headers");
            return Optional.of(token);
        }

        logger.debug("Authorization header found but does not contain Bearer token");
        return Optional.empty();
    }

    private Optional<String> getConfiguredToken(Map<String, Object> parameters) {
        Optional<String> token = Optional.ofNullable((String) parameters.get(PROPAGATE_TOKEN_PARAM));
        token.ifPresent(t -> logger.debug("Using configured token from parameters"));
        return token;
    }
}
