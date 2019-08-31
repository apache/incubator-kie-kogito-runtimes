/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.index.infinispan.cache;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.kie.kogito.index.cache.CacheService;
import org.kie.kogito.index.model.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class InfinispanCacheManager implements CacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfinispanCacheManager.class);
    private static final String PROCESS_INSTANCES_CACHE = "processinstances";

    private DataFormat jsonDataFormat = DataFormat.builder().valueType(MediaType.APPLICATION_JSON).valueMarshaller(new JsonDataFormatMarshaller()).build();

    @Inject
    @ConfigProperty(name = "kogito.cache.domain.template", defaultValue = "kogito-template")
    String domainCacheTemplate;

    @Inject
    RemoteCacheManager manager;

    @PostConstruct
    public void init() {
        manager.start();
        this.initializeCacheManager(PROCESS_INSTANCES_CACHE, domainCacheTemplate);
    }

    @PreDestroy
    public void destroy() {
        manager.stop();
        try {
            manager.close();
        } catch (IOException ex) {
            LOGGER.warn("Error trying to close Infinispan remote cache manager", ex);
        }
    }

    /**
     * Initializes the CacheManager if not exists based first on the given template. 
     * If the template does not exist on the server, uses the default cache configuration.
     * 
     * @param name the cache manager name
     * @param template the template that must exists on the server
     */
    private void initializeCacheManager(String name, String template) {
        try {
            // verifies if the cache exists
            LOGGER.debug("Trying to get cache {} from the server", name);
            RemoteCache<Object, Object> remoteCache = manager.getCache(name);
            if (remoteCache == null) {
                // create a cache using the given template
                LOGGER.debug("Cache {} not found, trying to create a new one based on template {}", name, template);
                manager.administration().createCache(name, template);
            }
        } catch (HotRodClientException e) {
            //template doesn't exist, create the cache with default configurations
            LOGGER.debug("Creating a default cache '{}' configuration", name);
            RemoteCache<Object, Object> cache = manager.administration().createCache(name, new KogitoCacheDefaultConfiguration());
            LOGGER.debug("Cache created {}", cache.getName());
        }
    }

    @Override
    public Map<String, ProcessInstance> getProcessInstancesCache() {
        return manager.getCache(PROCESS_INSTANCES_CACHE);
    }

    @Override
    public Map<String, JsonObject> getProcessInstancesCacheAsJson() {
        return manager.getCache(PROCESS_INSTANCES_CACHE).withDataFormat(jsonDataFormat);
    }

    @Override
    public Map<String, String> getProtobufCache() {
        return manager.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
    }

    @Override
    public Map<String, String> getProcessIdModelCache() {
        return manager.administration().getOrCreateCache("processidmodel", (String) null);
    }

    @Override
    public Map<String, JsonObject> getDomainModelCache(String processId) {
        initializeCacheManager(processId + "_domain", domainCacheTemplate);
        return manager.getCache(processId + "_domain").withDataFormat(jsonDataFormat);
    }

}
