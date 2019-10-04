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

package org.kie.kogito.index.infinispan.listener;

import java.util.function.Consumer;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientListener
public class CacheObjectCreatedListener<K, V> extends AbstractCacheObjectListener<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheObjectCreatedListener.class);

    public CacheObjectCreatedListener(RemoteCache<K, V> cache, Consumer<V> consumer) {
        super(cache, consumer);
    }

    @ClientCacheEntryCreated
    public void handleCreatedEvent(ClientCacheEntryCreatedEvent<K> e) {
        LOGGER.debug("Handle create event for entry with id: {} on cache: {}", e.getKey(), cache.getName());
        handleEvent(e.getKey(), e.getVersion());
    }
}
