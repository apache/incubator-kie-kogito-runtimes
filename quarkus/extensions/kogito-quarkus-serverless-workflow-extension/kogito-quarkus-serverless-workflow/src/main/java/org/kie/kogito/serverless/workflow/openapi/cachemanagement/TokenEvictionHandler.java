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
package org.kie.kogito.serverless.workflow.openapi.cachemanagement;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.kie.kogito.serverless.workflow.openapi.OpenApiCustomCredentialProvider;
import org.kie.kogito.serverless.workflow.openapi.persistence.DatabaseTokenCache;
import org.kie.kogito.serverless.workflow.openapi.utils.CacheUtils;
import org.kie.kogito.serverless.workflow.openapi.utils.OidcClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.Tokens;

/**
 * Handles token eviction events from the cache and performs background token refresh operations.
 * This class is responsible for managing token lifecycle events including expiration and refresh.
 */
public class TokenEvictionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenEvictionHandler.class);
    public static final String LOG_PREFIX_TOKEN_REFRESH = "Attempting background token refresh";
    public static final String LOG_PREFIX_REFRESH_COMPLETED = "Background refresh completed";
    public static final String LOG_PREFIX_FAILED_TO_REFRESH_TOKEN = "Failed to refresh token";

    private final OpenApiCustomCredentialProvider provider;

    public TokenEvictionHandler(OpenApiCustomCredentialProvider provider) {
        this.provider = provider;
    }

    /**
     * Creates a removal listener that can be used with token cache.
     * 
     * @return A removal listener that handles token eviction events
     */
    public Consumer<DatabaseTokenCache.TokenRemovalEvent> createRemovalListener() {
        return event -> {
            LOGGER.info("Token cache eviction for cache key '{}' - Cause: {}", event.key(), event.cause());
            onTokenExpired(event.key(), event.value(), event.cause());
        };
    }

    /**
     * Callback method called when tokens are evicted from the cache.
     * 
     * @param cacheKey The cache key of the evicted tokens
     * @param tokens The evicted token data
     * @param cause The reason for eviction
     */
    private void onTokenExpired(String cacheKey, CachedTokens tokens, TokenRemovalCause cause) {
        LOGGER.warn("OAuth2 tokens for cache key '{}' have expired/been evicted: {}", cacheKey, cause);
        if (cause == TokenRemovalCause.EXPIRED) {
            if (tokens.getRefreshToken() != null) {
                CompletableFuture.runAsync(() -> {
                    try {
                        refreshWithCachedToken(cacheKey, tokens.getRefreshToken());
                    } catch (Exception e) {
                        LOGGER.error("Background token refresh failed for cache key '{}': {}", cacheKey, e.getMessage());
                    }
                });
            }
        }
    }

    /**
     * Refreshes tokens using a cached refresh token and updates the cache.
     * 
     * @param cacheKey The cache key for the tokens being refreshed
     * @param refreshToken The refresh token to use for getting new tokens
     */
    private void refreshWithCachedToken(String cacheKey, String refreshToken) {
        LOGGER.info("{} - cache key '{}'", LOG_PREFIX_TOKEN_REFRESH, cacheKey);

        try {
            String authName = CacheUtils.extractAuthNameFromCacheKey(cacheKey);
            OidcClient client = OidcClientUtils.getExchangeTokenClient(authName);

            LOGGER.debug("Refreshing token for cache key '{}' using cached refresh token", cacheKey);

            Tokens refreshedTokens = client.getTokens(Collections.singletonMap("refresh_token", refreshToken))
                    .await().indefinitely();
            CacheUtils.cacheTokens(this.provider.getTokenCache(), cacheKey, refreshedTokens);
            LOGGER.info("{} - cache key '{}'", LOG_PREFIX_REFRESH_COMPLETED, cacheKey);
        } catch (Exception e) {
            LOGGER.error("{} - cache key '{}'", LOG_PREFIX_FAILED_TO_REFRESH_TOKEN, cacheKey, e);
        }
    }

}
