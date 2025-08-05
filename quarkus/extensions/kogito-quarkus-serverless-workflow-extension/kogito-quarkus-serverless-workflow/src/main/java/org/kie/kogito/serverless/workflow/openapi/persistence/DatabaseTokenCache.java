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
package org.kie.kogito.serverless.workflow.openapi.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.kie.kogito.serverless.workflow.openapi.cachemanagement.CachedTokens;
import org.kie.kogito.serverless.workflow.openapi.cachemanagement.TokenRemovalCause;
import org.kie.kogito.serverless.workflow.openapi.persistence.TokenDataStore.TokenEntry;
import org.kie.kogito.serverless.workflow.openapi.utils.CacheUtils;
import org.kie.kogito.serverless.workflow.openapi.utils.ConfigReaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Database-backed cache implementation that uses TokenDataStore for persistence
 * while maintaining in-memory cache for performance and token monitoring.
 */
@ApplicationScoped
public class DatabaseTokenCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseTokenCache.class);

    @Inject
    TokenDataStore dataStore;

    // In-memory cache for performance
    private static final ConcurrentHashMap<String, CachedTokens> MEMORY_CACHE = new ConcurrentHashMap<>();

    // Removal listener for monitoring token evictions
    private Consumer<TokenRemovalEvent> removalListener;

    // Schedulers
    private ScheduledExecutorService refreshMonitoringScheduler;

    // Flag to ensure only one refresh monitoring execution at a time
    private volatile boolean isRefreshMonitoringRunning = false;

    @PostConstruct
    public void init() {
        loadFromDataStore();

        refreshMonitoringScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "token-cache-refresh-monitor");
            t.setDaemon(true);
            return t;
        });
        // Use scheduleWithFixedDelay to ensure previous execution completes before next one starts
        refreshMonitoringScheduler.scheduleWithFixedDelay(this::monitorExpiringTokens,
                5, ConfigReaderUtils.getMonitorRateSeconds(), TimeUnit.SECONDS);

        LOGGER.info("Database token cache initialized with {} backing store",
                dataStore.getClass().getSimpleName());
    }

    @PreDestroy
    public void cleanup() {
        if (refreshMonitoringScheduler != null) {
            refreshMonitoringScheduler.shutdown();
        }
    }

    public void setRemovalListener(Consumer<TokenRemovalEvent> removalListener) {
        this.removalListener = removalListener;
    }

    public CachedTokens getIfPresent(String key) {
        // Check memory cache first
        CachedTokens tokens = MEMORY_CACHE.get(key);
        if (tokens != null) {
            if (!tokens.isExpiredNow()) {
                return tokens;
            } else {
                // Remove expired token from memory and notify removal listener
                MEMORY_CACHE.remove(key);
                notifyRemoval(key, tokens, TokenRemovalCause.EXPIRED);
                invalidate(key);
            }
        }

        return dataStore.retrieve(key)
                .map(t -> {
                    // Load back into memory cache
                    MEMORY_CACHE.put(key, t);
                    return t;
                })
                .orElse(null);
    }

    public void put(String key, CachedTokens value) {
        // Store in memory for fast access
        CachedTokens previous = MEMORY_CACHE.put(key, value);

        try {
            dataStore.store(key, value);
            LOGGER.debug("Persisted token cache entry for key: {}", key);

            // Notify removal listener if we replaced an existing value
            if (previous != null) {
                notifyRemoval(key, previous, TokenRemovalCause.REPLACED);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to persist token cache entry for key: {}", key, e);
        }
    }

    public void invalidate(String key) {
        CachedTokens removed = MEMORY_CACHE.remove(key);

        try {
            dataStore.remove(key);
            LOGGER.debug("Invalidated token cache entry for key: {}", key);

            // Notify removal listener
            if (removed != null) {
                notifyRemoval(key, removed, TokenRemovalCause.EXPLICIT);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to invalidate token cache entry for key: {}", key, e);
        }
    }

    private void loadFromDataStore() {
        try {
            List<TokenEntry> entries = dataStore.loadAll();
            entries.forEach(entry -> MEMORY_CACHE.put(entry.cacheKey(), entry.tokens()));

            LOGGER.info("Loaded {} token cache entries from data store", entries.size());
        } catch (Exception e) {
            LOGGER.error("Failed to load token cache from data store", e);
        }
    }

    /**
     * Monitor tokens that are expiring soon and trigger proactive refresh
     * Ensures only one execution runs at a time
     */
    private void monitorExpiringTokens() {
        // Check to prevent overlapping executions
        if (isRefreshMonitoringRunning) {
            LOGGER.debug("Token monitoring already in progress, skipping this execution");
            return;
        }

        isRefreshMonitoringRunning = true;

        try {
            LOGGER.info("Monitoring tokens for proactive refresh...");
            List<String> tokensToRemove = new ArrayList<>();

            MEMORY_CACHE.forEach((cacheKey, tokens) -> {
                String authName = CacheUtils.extractAuthNameFromCacheKey(cacheKey);
                long bufferSeconds = ConfigReaderUtils.getProactiveRefreshSeconds(authName);

                if (tokens.isExpiringSoon(bufferSeconds)) {
                    LOGGER.info("Token for cache key '{}' is expiring soon (expiration time: {}, cautionary buffer: {} seconds)", cacheKey, tokens.getExpirationTime(),
                            bufferSeconds);
                    tokensToRemove.add(cacheKey);
                }
            });

            for (String cacheKey : tokensToRemove) {
                LOGGER.info("Triggering proactive refresh for {}", cacheKey);
                notifyRemoval(cacheKey, MEMORY_CACHE.remove(cacheKey), TokenRemovalCause.EXPIRED);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to monitor expiring tokens", e);
        } finally {
            isRefreshMonitoringRunning = false;
        }
    }

    // Notify removal listener if set
    private void notifyRemoval(String key, CachedTokens value, TokenRemovalCause cause) {
        if (removalListener != null) {
            try {
                removalListener.accept(new TokenRemovalEvent(key, value, cause));
            } catch (Exception e) {
                LOGGER.error("Error in removal listener for cache key: {}", key, e);
            }
        } else {
            LOGGER.warn("No removal listener set for cache key: {}", key);
        }
    }

    /**
     * Simple record to represent token removal events
     */
    public static record TokenRemovalEvent(String key, CachedTokens value, TokenRemovalCause cause) {
    }
}
