/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.jobs.service.repository.infinispan;

import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.interceptor.Interceptor;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Readiness;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.kie.kogito.infinispan.health.InfinispanHealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Priority(Interceptor.Priority.PLATFORM_BEFORE)
@Singleton
public class InfinispanConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfinispanConfiguration.class);
    public static final String PERSISTENCE_CONFIG_KEY = "kogito.jobs-service.persistence";

    /**
     * Constants for Caches
     */
    public static class Caches {

        private Caches() {

        }

        public static final String SCHEDULED_JOBS = "SCHEDULED_JOBS";

        public static String[] all() {
            return new String[]{SCHEDULED_JOBS};
        }
    }

    private Optional<RemoteCacheManager> cacheManager;

    @Inject
    public InfinispanConfiguration(Instance<RemoteCacheManager> cacheManagerInstance,
                                   @ConfigProperty(name = PERSISTENCE_CONFIG_KEY)
                                           Optional<String> persistence) {

        LOGGER.info("Persistence config {}", persistence);
        this.cacheManager = persistence
                .filter("infinispan"::equals)
                .map(p -> cacheManagerInstance.get());
    }

    @Produces
    @Readiness
    public InfinispanHealthCheck infinispanHealthCheck(@ConfigProperty(name = PERSISTENCE_CONFIG_KEY)
                                                               Optional<String> persistence,
                                                       Instance<RemoteCacheManager> cacheManagerInstance) {
        return persistence
                .filter("infinispan"::equals)
                .map(p -> new InfinispanHealthCheck(cacheManagerInstance))
                .orElse(null);
    }
}