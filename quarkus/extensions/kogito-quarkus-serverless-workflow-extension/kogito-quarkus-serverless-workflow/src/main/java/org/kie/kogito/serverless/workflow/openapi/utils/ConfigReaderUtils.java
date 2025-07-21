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
package org.kie.kogito.serverless.workflow.openapi.utils;

import java.util.Optional;

import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkiverse.openapi.generator.providers.CredentialsContext;

/**
 * Utility class for reading configuration properties related to OAuth2 token exchange and caching.
 */
public final class ConfigReaderUtils {
    public static final String CANONICAL_EXCHANGE_TOKEN_PROPERTY_NAME = "sonataflow.security.auth.%s.exchange-token";
    private static final String CANONICAL_EXPIRATION_BUFFER_PROPERTY_NAME = "sonataflow.security.auth.%s.exchange-token.expiration-buffer-seconds";
    private static final String CANONICAL_MONITOR_EXPIRING_RATE_PROPERTY_NAME = "sonataflow.security.auth.exchange-token.monitor-expiring-rate-seconds";
    public static final long DEFAULT_EXPIRATION_BUFFER_SECONDS = 300; // 5 minutes
    public static final long DEFAULT_MONITOR_EXPIRING_RATE_SECONDS = 60; // 1 minute

    private ConfigReaderUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets the canonical property name for expiration buffer configuration.
     * 
     * @param authName The authentication name
     * @return The property name in format: sonataflow.security.{authName}.token-exchange.expiration-buffer-seconds
     */
    public static String getCanonicalExpirationBufferConfigPropertyName(String authName) {
        return String.format(CANONICAL_EXPIRATION_BUFFER_PROPERTY_NAME, authName);
    }

    /**
     * Gets the configured expiration buffer seconds for the given auth name.
     * 
     * @param authName The authentication name
     * @return The configured buffer seconds, or default (300) if not configured
     */
    public static long getExpirationBufferSeconds(String authName) {
        return ConfigProvider.getConfig()
                .getOptionalValue(getCanonicalExpirationBufferConfigPropertyName(authName), Long.class)
                .orElse(DEFAULT_EXPIRATION_BUFFER_SECONDS);
    }

    /**
     * Gets the exchange token property value for the given auth name.
     * 
     * @param input The credentials context
     * @return The exchange token property value
     */
    public static Optional<Boolean> getExchangeTokenPropertyValue(CredentialsContext input) {
        return ConfigProvider.getConfig().getOptionalValue(getCanonicalExchangeTokenConfigPropertyName(input.getAuthName()), Boolean.class);
    }

    /**
     * Gets the canonical exchange token property name for the given auth name.
     * 
     * @param authName The authentication name
     * @return The property name in format: sonataflow.security.{authName}.exchange-token
     */
    public static String getCanonicalExchangeTokenConfigPropertyName(String authName) {
        return String.format(CANONICAL_EXCHANGE_TOKEN_PROPERTY_NAME, authName);
    }

    /**
     * Gets the configured monitor expiring rate seconds.
     *
     * @return The configured buffer seconds, or default (60) if not configured
     */
    public static long getMonitorExpiringRateSeconds() {
        return ConfigProvider.getConfig()
                .getOptionalValue(CANONICAL_MONITOR_EXPIRING_RATE_PROPERTY_NAME, Long.class)
                .orElse(DEFAULT_MONITOR_EXPIRING_RATE_SECONDS);
    }
}