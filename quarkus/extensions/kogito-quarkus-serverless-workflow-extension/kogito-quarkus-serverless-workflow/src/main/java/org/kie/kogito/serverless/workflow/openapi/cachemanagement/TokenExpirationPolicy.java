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

import org.kie.kogito.serverless.workflow.openapi.utils.CacheUtils;
import org.kie.kogito.serverless.workflow.openapi.utils.ConfigReaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Expiry;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Custom expiry policy for OAuth2 token caching that expires tokens based on their actual expiration time
 * minus a configurable buffer period.
 */
@ApplicationScoped
public class TokenExpirationPolicy implements Expiry<String, CachedTokens> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenExpirationPolicy.class);

    @Override
    public long expireAfterCreate(String key, CachedTokens value, long currentTime) {
        return calculateExpiration(key, value);
    }

    @Override
    public long expireAfterUpdate(String key, CachedTokens value, long currentTime, long currentDuration) {
        return calculateExpiration(key, value);
    }

    @Override
    public long expireAfterRead(String key, CachedTokens value, long currentTime, long currentDuration) {
        return currentDuration; // No change on read
    }

    /**
     * Calculates expiration time based on token's actual expiration minus buffer.
     * 
     * @param key The cache key
     * @param value The cached tokens
     * @return Duration in nanoseconds until expiration
     */
    private long calculateExpiration(String key, CachedTokens value) {
        String authName = CacheUtils.extractAuthNameFromCacheKey(key);
        long bufferSeconds = ConfigReaderUtils.getExpirationBufferSeconds(authName);
        long expirationWithBuffer = value.getExpirationTime() - bufferSeconds;
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        long durationSeconds = Math.max(0, expirationWithBuffer - currentTimeSeconds);
        long durationNanos = durationSeconds * 1_000_000_000L;

        LOGGER.info("Cache key '{}' will really expire in {} seconds (buffer: {}s)", key, durationSeconds, bufferSeconds);
        return durationNanos;
    }
}
