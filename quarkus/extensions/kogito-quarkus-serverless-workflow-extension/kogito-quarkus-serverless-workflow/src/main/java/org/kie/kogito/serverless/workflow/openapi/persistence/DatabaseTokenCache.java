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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.kie.kogito.serverless.workflow.openapi.cachemanagement.CachedTokens;
import org.kie.kogito.serverless.workflow.openapi.persistence.TokenDataStore.TokenEntry;
import org.kie.kogito.serverless.workflow.openapi.utils.CacheUtils;
import org.kie.kogito.serverless.workflow.openapi.utils.ConfigReaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Database-backed cache implementation that uses TokenDataStore for persistence
 * while maintaining in-memory cache for performance and token monitoring.
 */
@ApplicationScoped
public class DatabaseTokenCache implements Cache<String, CachedTokens> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseTokenCache.class);

    @Inject
    TokenDataStore dataStore;

    // In-memory cache for performance
    private static final ConcurrentHashMap<String, CachedTokens> MEMORY_CACHE = new ConcurrentHashMap<>();

    // Removal listener for monitoring token evictions
    private RemovalListener<String, CachedTokens> removalListener;

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
                5, ConfigReaderUtils.getMonitorExpiringRateSeconds(), TimeUnit.SECONDS);

        LOGGER.info("Database token cache initialized with {} backing store",
                dataStore.getClass().getSimpleName());
    }

    @PreDestroy
    public void cleanup() {
        if (refreshMonitoringScheduler != null) {
            refreshMonitoringScheduler.shutdown();
        }
    }

    /**
     * Set removal listener to maintain compatibility with existing TokenEvictionHandler
     */
    public void setRemovalListener(RemovalListener<String, CachedTokens> removalListener) {
        this.removalListener = removalListener;
    }

    @Override
    public CachedTokens getIfPresent(String key) {
        String cacheKey = key;

        // Check memory cache first
        CachedTokens tokens = MEMORY_CACHE.get(cacheKey);
        if (tokens != null) {
            if (!tokens.isExpiredNow()) {
                return tokens;
            } else {
                // Remove expired token from memory and notify removal listener
                MEMORY_CACHE.remove(cacheKey);
                notifyRemoval(cacheKey, tokens, RemovalCause.EXPIRED);
                invalidate(cacheKey);
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

    @Override
    public void put(String key, CachedTokens value) {
        // Store in memory for fast access
        CachedTokens previous = MEMORY_CACHE.put(key, value);

        try {
            dataStore.store(key, value);
            LOGGER.debug("Persisted token cache entry for key: {}", key);

            // Notify removal listener if we replaced an existing value
            if (previous != null) {
                notifyRemoval(key, previous, RemovalCause.REPLACED);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to persist token cache entry for key: {}", key, e);
        }
    }

    @Override
    public void invalidate(String key) {
        String cacheKey = key;
        CachedTokens removed = MEMORY_CACHE.remove(cacheKey);

        try {
            dataStore.remove(cacheKey);
            LOGGER.debug("Invalidated token cache entry for key: {}", cacheKey);

            // Notify removal listener
            if (removed != null) {
                notifyRemoval(cacheKey, removed, RemovalCause.EXPLICIT);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to invalidate token cache entry for key: {}", cacheKey, e);
        }
    }

    @Override
    public void invalidateAll() {
        // Notify removal listener for all entries
        MEMORY_CACHE.forEach((key, value) -> notifyRemoval(key, value, RemovalCause.EXPLICIT));

        MEMORY_CACHE.clear();
        // Note: We don't clear the database to preserve tokens across restarts
        LOGGER.debug("Invalidated all in-memory token cache entries");
    }

    @Override
    public long estimatedSize() {
        return MEMORY_CACHE.size();
    }

    @Override
    public void cleanUp() {
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
                long bufferSeconds = ConfigReaderUtils.getExpirationBufferSeconds(authName);

                if (tokens.isExpiringSoon(bufferSeconds)) {
                    LOGGER.info("Token for cache key '{}' is expiring soon (expiration time: {}, cautionary buffer: {} seconds)", cacheKey, tokens.getExpirationTime(),
                            bufferSeconds);
                    tokensToRemove.add(cacheKey);
                }
            });

            for (String cacheKey : tokensToRemove) {
                LOGGER.info("Triggering proactive refresh for {}", cacheKey);
                notifyRemoval(cacheKey, MEMORY_CACHE.remove(cacheKey), RemovalCause.EXPIRED);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to monitor expiring tokens", e);
        } finally {
            isRefreshMonitoringRunning = false;
        }
    }

    // Notify removal listener if set
    private void notifyRemoval(String key, CachedTokens value, RemovalCause cause) {
        if (removalListener != null) {
            try {
                removalListener.onRemoval(key, value, cause);
            } catch (Exception e) {
                LOGGER.error("Error in removal listener for cache key: {}", key, e);
            }
        } else {
            LOGGER.warn("No removal listener set for cache key: {}", key);
        }
    }

    // Remaining Cache interface implementations...
    @Override
    public void putAll(Map<? extends String, ? extends CachedTokens> map) {
        map.forEach(this::put);
    }

    @Override
    public void invalidateAll(Iterable<? extends String> keys) {
        keys.forEach(this::invalidate);
    }

    @Override
    public CachedTokens get(String key, Function<? super String, ? extends CachedTokens> mappingFunction) {
        CachedTokens value = getIfPresent(key);
        if (value == null) {
            value = mappingFunction.apply(key);
            if (value != null) {
                put(key, value);
            }
        }
        return value;
    }

    @Override
    public Map<String, CachedTokens> getAllPresent(Iterable<? extends String> keys) {
        Map<String, CachedTokens> result = new HashMap<>();
        keys.forEach(key -> {
            CachedTokens value = getIfPresent(key);
            if (value != null) {
                result.put(key, value);
            }
        });
        return result;
    }

    @Override
    public Map<String, CachedTokens> getAll(Iterable<? extends String> keys,
            Function<? super Set<? extends String>, ? extends Map<? extends String, ? extends CachedTokens>> mappingFunction) {
        throw new UnsupportedOperationException("getAll with mappingFunction not supported");
    }

    public CompletableFuture<CachedTokens> get(String key,
            Function<? super String, ? extends CachedTokens> mappingFunction,
            Executor executor) {
        return CompletableFuture.supplyAsync(() -> get(key, mappingFunction), executor);
    }

    @Override
    public CacheStats stats() {
        return CacheStats.empty();
    }

    @Override
    public ConcurrentMap<String, CachedTokens> asMap() {
        return new ConcurrentHashMap<>(MEMORY_CACHE);
    }

    @Override
    public Policy<String, CachedTokens> policy() {
        throw new UnsupportedOperationException("Policy access not supported");
    }
}
