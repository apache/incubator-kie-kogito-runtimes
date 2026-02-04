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
package org.kie.kogito.auth;

import java.util.Optional;

/**
 * Provider for authentication tokens used in REST work items.
 * Implementations should read tokens from the application security context
 * and provide fallback to configured default tokens.
 */
public interface AuthTokenProvider {

    /**
     * Configuration property for default bearer token.
     * Can be configured in application.properties as:
     * kogito.security.auth.rest.bearer-token=your-token-here
     */
    String BEARER_TOKEN_CONFIG_PROPERTY = "kogito.security.auth.rest.bearer-token";

    /**
     * Get the actual authentication token from the current security context.
     * This reads the token from the authenticated user's credentials.
     *
     * @return Optional containing the token if available in security context, empty otherwise
     */
    Optional<String> getActualToken();

    /**
     * Get the actual authentication token from security context, or fall back to
     * the default token configured in application properties.
     * 
     * Priority:
     * 1. Token from current security context (authenticated user)
     * 2. Default token from configuration (kogito.security.auth.rest.bearer_token)
     *
     * @return Optional containing the token if available from either source, empty otherwise
     */
    Optional<String> getActualTokenOrDefault();
}
