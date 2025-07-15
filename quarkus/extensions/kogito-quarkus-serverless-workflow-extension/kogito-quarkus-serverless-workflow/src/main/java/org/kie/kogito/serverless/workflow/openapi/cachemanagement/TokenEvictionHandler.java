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

import org.kie.kogito.serverless.workflow.openapi.OpenApiCustomCredentialProvider;
import org.kie.kogito.serverless.workflow.openapi.utils.CacheUtils;
import org.kie.kogito.serverless.workflow.openapi.utils.OidcClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.Tokens;

/**
 * Handles token eviction events from the cache and performs background token refresh operations.
 * This class is responsible for managing token lifecycle events including expiration and refresh.
 */
public class TokenEvictionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenEvictionHandler.class);

    private final OpenApiCustomCredentialProvider provider;

    public TokenEvictionHandler(OpenApiCustomCredentialProvider provider) {
        this.provider = provider;
    }

    /**
     * Creates a RemovalListener that can be used with Caffeine cache.
     * 
     * @return A RemovalListener that handles token eviction events
     */
    public RemovalListener<String, CachedTokens> createRemovalListener() {
        return new TokenEvictionListener();
    }

    /**
     * Inner class implementing RemovalListener for token cache eviction events.
     */
    private class TokenEvictionListener implements RemovalListener<String, CachedTokens> {
        @Override
        public void onRemoval(String key, CachedTokens value, RemovalCause cause) {
            LOGGER.info("Token cache eviction for cache key '{}' - Cause: {}", key, cause);
            onTokenExpired(key, value, cause);
        }
    }

    /**
     * Callback method called when tokens are evicted from the cache.
     * 
     * @param cacheKey The cache key of the evicted tokens
     * @param tokens The evicted token data
     * @param cause The reason for eviction
     */
    private void onTokenExpired(String cacheKey, CachedTokens tokens, RemovalCause cause) {
        LOGGER.warn("OAuth2 tokens for cache key '{}' have expired/been evicted: {}", cacheKey, cause);

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

    /**
     * Refreshes tokens using a cached refresh token and updates the cache.
     * 
     * @param cacheKey The cache key for the tokens being refreshed
     * @param refreshToken The refresh token to use for getting new tokens
     */
    private void refreshWithCachedToken(String cacheKey, String refreshToken) {
        LOGGER.info("Attempting background token refresh for cache key '{}'", cacheKey);

        try {
            String authName = CacheUtils.extractAuthNameFromCacheKey(cacheKey);
            OidcClient client = OidcClientUtils.getExchangeTokenClient(authName);

            LOGGER.debug("Refreshing token for cache key '{}' using cached refresh token", cacheKey);

            Tokens refreshedTokens = client.getTokens(Collections.singletonMap("refresh_token", refreshToken))
                    .await().indefinitely();
            CacheUtils.cacheTokens(this.provider.getTokenCache(), cacheKey, refreshedTokens);
        } catch (Exception e) {
            LOGGER.error("Failed to refresh token for cache key '{}': {}", cacheKey, e.getMessage());
        }
    }

}
