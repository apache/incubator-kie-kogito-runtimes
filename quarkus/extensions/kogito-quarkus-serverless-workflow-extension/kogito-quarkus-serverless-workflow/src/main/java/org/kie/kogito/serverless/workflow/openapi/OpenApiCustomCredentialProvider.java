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
package org.kie.kogito.serverless.workflow.openapi;

import java.util.Collections;
import java.util.Optional;

import org.eclipse.microprofile.config.ConfigProvider;
import org.kie.kogito.internal.utils.ConversionUtils;
import org.kie.kogito.serverless.workflow.openapi.cachemanagement.CachedTokens;
import org.kie.kogito.serverless.workflow.openapi.cachemanagement.TokenEvictionHandler;
import org.kie.kogito.serverless.workflow.openapi.cachemanagement.TokenExpirationPolicy;
import org.kie.kogito.serverless.workflow.openapi.utils.CacheUtils;
import org.kie.kogito.serverless.workflow.openapi.utils.ConfigReaderUtils;
import org.kie.kogito.serverless.workflow.openapi.utils.OidcClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.quarkiverse.openapi.generator.providers.ConfigCredentialsProvider;
import io.quarkiverse.openapi.generator.providers.CredentialsContext;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.OidcClientConfig;
import io.quarkus.oidc.client.OidcClientException;
import io.quarkus.oidc.client.Tokens;
import io.quarkus.runtime.configuration.ConfigurationException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Specializes;
import jakarta.ws.rs.core.HttpHeaders;

import static io.quarkiverse.openapi.generator.providers.AbstractAuthProvider.getHeaderName;

/**
 * Custom credential provider that supports OAuth2 token exchange and caching.
 *
 * Configuration properties:
 * - sonataflow.security.{authName}.exchange-token: Enable token exchange for the specified auth name
 * - sonataflow.security.{authName}.token-exchange.expiration-buffer-seconds: Number of seconds before token expiration to refresh cache (default: 300)
 */
@ApplicationScoped
@Alternative
@Specializes
@Priority(200)
public class OpenApiCustomCredentialProvider extends ConfigCredentialsProvider {
    private static final String CANONICAL_EXCHANGE_TOKEN_PROPERTY_NAME = "sonataflow.security.auth.%s.exchange-token";

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiCustomCredentialProvider.class);

    // Cache for storing token pairs - key format: processInstanceId:authName
    private Cache<String, CachedTokens> tokenCache;

    @PostConstruct
    public void initCache() {
        this.tokenCache = Caffeine.newBuilder()
                .maximumSize(1000)
                // Use custom expiration policy based on token expiration time
                .expireAfter(new TokenExpirationPolicy())
                // Set up eviction listener for callbacks
                .removalListener(new TokenEvictionHandler(this).createRemovalListener())
                .build();
    }

    @Override
    public Optional<String> getOauth2BearerToken(CredentialsContext input) {
        LOGGER.debug("Calling OpenApiCustomCredentialProvider.getOauth2BearerToken for {}", input.getAuthName());
        String authorizationHeaderName = Optional.ofNullable(getHeaderName(input.getOpenApiSpecId(), input.getAuthName())).orElse(HttpHeaders.AUTHORIZATION);
        boolean exchangeToken = ConfigReaderUtils.getExchangeTokenPropertyeValue(input).orElse(false);
        if (exchangeToken) {
            String accessToken = input.getRequestContext().getHeaderString(authorizationHeaderName);

        if (exchangeToken.isPresent() && exchangeToken.get()) {
            LOGGER.info("Oauth2 token exchange enabled for {}, will generate tokens...", input.getAuthName());

            String cacheKey = CacheUtils.buildCacheKey(input);
            CachedTokens cachedTokens = tokenCache.getIfPresent(cacheKey);

            if (cachedTokens != null) {
                LOGGER.debug("Found cached tokens for cache key '{}'", cacheKey);

                if (!cachedTokens.isExpiredNow()) {
                    LOGGER.debug("Using valid cached access token for cache key '{}'", cacheKey);
                    accessToken = cachedTokens.getAccessToken();
                } else {
                    LOGGER.info("Cached token for cache key '{}' is expired, falling back to new token exchange", cacheKey);
                    tokenCache.invalidate(cacheKey);
                }
            }
            if (accessToken == null) {
                LOGGER.debug("Exchanging tokens and caching in '{}' ...", cacheKey);

                accessToken = input.getRequestContext().getHeaderString(authorizationHeaderName);
                if (accessToken == null || accessToken.isBlank()) {
                    throw new ConfigurationException(
                            "An access token is required in the header %s (default is %s) but none was provided".formatted(authorizationHeaderName, HttpHeaders.AUTHORIZATION));
                }
                accessToken = performTokenExchange(accessToken, OidcClientUtils.getExchangeTokenClient(input.getAuthName()), input, cacheKey);
            }

        }
        return Optional.empty();
    }

    private String performTokenExchange(String token, OidcClient exchangeTokenClient, CredentialsContext input, String cacheKey) {
        OidcClientConfig.Grant.Type exchangeTokenGrantType = ConfigProvider.getConfig()
                .getValue("quarkus.oidc-client.%s.grant.type".formatted(input.getAuthName()), OidcClientConfig.Grant.Type.class);
        try {
            Tokens tokens = exchangeTokenClient.getTokens(Collections.singletonMap(
                    OidcClientUtils.getExchangeTokenProperty(exchangeTokenGrantType), token)).await().indefinitely();

            CacheUtils.cacheTokens(this.tokenCache, cacheKey, tokens);
            return tokens.getAccessToken();

        } catch (OidcClientException e) {
            LOGGER.error("Error while exchanging oauth2 token for cache key '{}'. Using original token.", cacheKey, e);
            return token;
        }
    }

    /**
     * Gets the token cache instance.
     *
     * @return The token cache for storing token pairs
     */
    public Cache<String, CachedTokens> getTokenCache() {
        return tokenCache;
    }

}
