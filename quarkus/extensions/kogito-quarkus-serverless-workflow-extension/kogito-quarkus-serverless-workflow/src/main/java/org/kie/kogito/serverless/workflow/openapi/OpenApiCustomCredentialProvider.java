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
import org.kie.kogito.serverless.workflow.openapi.persistence.DatabaseTokenCache;
import org.kie.kogito.serverless.workflow.openapi.utils.CacheUtils;
import org.kie.kogito.serverless.workflow.openapi.utils.ConfigReaderUtils;
import org.kie.kogito.serverless.workflow.openapi.utils.OidcClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Specializes;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;

import static io.quarkiverse.openapi.generator.providers.AbstractAuthProvider.getHeaderName;

/**
 * Custom credential provider that supports OAuth2 token exchange and caching.
 *
 * Configuration properties:
 * - sonataflow.security.{authName}.token-exchange.enabled: Enable token exchange for the specified auth name
 * - sonataflow.security.{authName}.token-exchange.proactive-refresh-seconds: Number of seconds before token expiration to refresh cache (default: 300)
 */
@ApplicationScoped
@Alternative
@Specializes
@Priority(200)
public class OpenApiCustomCredentialProvider extends ConfigCredentialsProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiCustomCredentialProvider.class);
    public static final String LOG_PREFIX_STARTING_TOKEN_EXCHANGE = "STARTING TOKEN EXCHANGE";
    public static final String LOG_PREFIX_COMPLETED_TOKEN_EXCHANGE = "COMPLETED TOKEN EXCHANGE";
    public static final String LOG_PREFIX_FAILED_TOKEN_EXCHANGE = "FAILED TOKEN EXCHANGE";

    @Inject
    Instance<DatabaseTokenCache> tokenCacheInstance;

    DatabaseTokenCache tokenCache;

    @PostConstruct
    public void initCache() {
        if (tokenCacheInstance.isResolvable()) {
            tokenCache = tokenCacheInstance.get();
            try {
                tokenCache.setRemovalListener(new TokenEvictionHandler(this).createRemovalListener());
            } catch (IllegalStateException e) {
                LOGGER.error("Error while initializing the database for token cache. Token cache will not be available, make sure the datasource is correctly configured", e);
                tokenCache = null;
            }
            LOGGER.info("Database token cache initialized with eviction handler");
        } else {
            LOGGER.info("No database token cache found, if {} enabled for any spec, you should configure the datasource otherwise the token exchange will not be cached",
                    ConfigReaderUtils.CANONICAL_TOKEN_EXCHANGE_ENABLED_PROPERTY_NAME);
            tokenCache = null;
        }

    }

    @Override
    public Optional<String> getOauth2BearerToken(CredentialsContext input) {
        LOGGER.debug("Calling OpenApiCustomCredentialProvider.getOauth2BearerToken for {}", input.getAuthName());
        String authorizationHeaderName = Optional.ofNullable(getHeaderName(input.getOpenApiSpecId(), input.getAuthName())).orElse(HttpHeaders.AUTHORIZATION);
        boolean exchangeToken = ConfigReaderUtils.getTokenExchangeEnabledPropertyValue(input).orElse(false);
        if (exchangeToken) {
            String accessToken;

            LOGGER.info("Oauth2 token exchange enabled for {}, will generate tokens...", input.getAuthName());

            String cacheKey = CacheUtils.buildCacheKey(input);
            accessToken = getAccessTokenFromCache(cacheKey);
            if (accessToken == null) {
                accessToken = performTokenExchange(input, cacheKey, authorizationHeaderName);
            }
            return Optional.ofNullable(accessToken);
        }
        return Optional.empty();
    }

    private String getAccessTokenFromCache(String cacheKey) {
        String accessToken = null;

        if (tokenCache != null) {
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
        }
        return accessToken;
    }

    private String performTokenExchange(CredentialsContext input, String cacheKey, String authorizationHeaderName) {
        String accessToken;
        LOGGER.info("Performing token exchange for '{}'", cacheKey);

        accessToken = input.getRequestContext().getHeaderString(authorizationHeaderName);
        if (ConversionUtils.isEmpty(accessToken)) {
            throw new ConfigurationException("An access token is required in the header %s (default is %s) but none was provided".formatted(authorizationHeaderName, HttpHeaders.AUTHORIZATION));
        }
        accessToken = exchangeToken(accessToken, OidcClientUtils.getExchangeTokenClient(input.getAuthName()), input, cacheKey);
        return accessToken;
    }

    private String exchangeToken(String token, OidcClient exchangeTokenClient, CredentialsContext input, String cacheKey) {
        OidcClientConfig.Grant.Type exchangeTokenGrantType = ConfigProvider.getConfig()
                .getValue("quarkus.oidc-client.%s.grant.type".formatted(input.getAuthName()), OidcClientConfig.Grant.Type.class);
        try {
            LOGGER.info("{} - Cache key: {}, Thread: {}", LOG_PREFIX_STARTING_TOKEN_EXCHANGE, cacheKey, Thread.currentThread().getName());
            Tokens tokens = exchangeTokenClient.getTokens(Collections.singletonMap(
                    OidcClientUtils.getExchangeTokenProperty(exchangeTokenGrantType), token)).await().indefinitely();

            CacheUtils.cacheTokens(this.tokenCache, cacheKey, tokens);
            LOGGER.info("{} - Cache key: {}, Thread: {}", LOG_PREFIX_COMPLETED_TOKEN_EXCHANGE, cacheKey, Thread.currentThread().getName());

            return tokens.getAccessToken();

        } catch (OidcClientException e) {
            LOGGER.error("{} - Cache key: {}, Thread: {}, Error: {}", LOG_PREFIX_FAILED_TOKEN_EXCHANGE, cacheKey, Thread.currentThread().getName(), e.getMessage());
            return token;
        }
    }

    /**
     * Gets the token cache instance.
     *
     * @return The token cache for storing token pairs
     */
    public DatabaseTokenCache getTokenCache() {
        return tokenCache;
    }

}
