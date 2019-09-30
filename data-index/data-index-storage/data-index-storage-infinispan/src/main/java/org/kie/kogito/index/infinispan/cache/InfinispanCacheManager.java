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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.kie.kogito.index.cache.CacheService;
import org.kie.kogito.index.model.ProcessInstance;
import org.kie.kogito.index.model.UserTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class InfinispanCacheManager implements CacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfinispanCacheManager.class);
    private static final String PROCESS_INSTANCES_CACHE = "processinstances";
    private static final String USER_TASK_INSTANCES_CACHE = "usertaskinstances";
    private static final String PROCESS_ID_MODEL_CACHE = "processidmodel";
    private static final String HOTROD_CLIENT_PROPS_FILE = "hotrod-client.properties";

    private DataFormat jsonDataFormat = DataFormat.builder().valueType(MediaType.APPLICATION_JSON).valueMarshaller(new JsonDataFormatMarshaller()).build();

    @Inject
    @ConfigProperty(name = "kogito.cache.domain.template", defaultValue = "kogito-template")
    String cacheTemplateName;
    
    @Inject
    @ConfigProperty(name = "kogito.cache.hotrod.property.path", defaultValue = "")
    String hotRodPropertiesFilePath;

    @Inject
    RemoteCacheManager manager;

    @PostConstruct
    public void init() throws IOException {
        this.configure();
        manager.start();
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
     * Configures the {@link RemoteCacheManager} with a custom {@value #HOTROD_CLIENT_PROPS_FILE} file that will be read 
     * locally from the value of <code>kogito.cache.hotrod.property.path</code> property.
     * 
     * @throws IOException
     */
    protected void configure() throws IOException {
        if (hotRodPropertiesFilePath == null || hotRodPropertiesFilePath.isEmpty()) {
            LOGGER.info("External hotrod-client.properties file not set, skipping custom configuration");
            return;
        }
        ConfigurationBuilder b = new ConfigurationBuilder();
        Properties p = new Properties();
        try (Reader r = new FileReader(String.format("%s/%s", hotRodPropertiesFilePath, HOTROD_CLIENT_PROPS_FILE))) {
            p.load(r);
            if (manager == null) {
                LOGGER.debug("Creating new configuration using hotrod-client properties: {}", p);
                b.withProperties(p);
            } else {
                LOGGER.debug("Merging already existed configuration from {} using hotrod-client properties: {}", manager.getConfiguration(), p);
                b.read(manager.getConfiguration()).withProperties(p);            
            }
        } catch (FileNotFoundException e) {
            LOGGER.info("No {} found in the path {}, using default values", HOTROD_CLIENT_PROPS_FILE, hotRodPropertiesFilePath);
            return;
        }
        LOGGER.info("Building new RemoteCacheManager with configuration from custom hotrod-client properties file in the path {}", hotRodPropertiesFilePath);
        if (manager != null && manager.isStarted()) {
            manager.stop();
        }
        manager = new RemoteCacheManager(b.build());
    }

    /**
     * Gets the cache if exists, otherwise tries to create one with the given template.
     * If the template does not exist on the server, creates the cache based on a default configuration.
     * 
     * @param name the cache manager name
     * @param template the template that must exists on the server
     * @see KogitoCacheDefaultConfiguration
     */
    protected <K, V> RemoteCache<K, V> getOrCreateCache(final String name, final String template) {
        try {
            LOGGER.debug("Trying to get cache {} from the server", name);
            RemoteCache<K, V> remoteCache = manager.getCache(name);
            if (remoteCache == null) {
                LOGGER.debug("Cache {} not found, trying to create a new one based on template {}", name, template);
                return manager.administration().createCache(name, template);
            }
            return remoteCache;
        } catch (HotRodClientException e) {
            if (e.isServerError()) {
                LOGGER.info("Creating a cache for '{}' based on the default configuration", name);
                RemoteCache<K, V> cache = manager.administration().createCache(name, new KogitoCacheDefaultConfiguration(name));
                LOGGER.debug("Default cache created {}", cache.getName());
                return cache;
            }
            throw e;
        }
    }

    @Override
    public Map<String, ProcessInstance> getProcessInstancesCache() {
        return getOrCreateCache(PROCESS_INSTANCES_CACHE, cacheTemplateName);
    }

    @Override
    public Map<String, UserTaskInstance> getUserTaskInstancesCache() {
        return getOrCreateCache(USER_TASK_INSTANCES_CACHE, cacheTemplateName);
    }

    public Map<String, String> getProtobufCache() {
        return manager.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
    }

    @Override
    public Map<String, String> getProcessIdModelCache() {
        return manager.administration().getOrCreateCache(PROCESS_ID_MODEL_CACHE, (String) null);
    }

    @Override
    public Map<String, JsonObject> getDomainModelCache(String processId) {
        return getOrCreateCache(processId + "_domain", cacheTemplateName).withDataFormat(jsonDataFormat);
    }
}
