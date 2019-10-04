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

public abstract class AbstractCacheObjectListener<K, T> {

    protected RemoteCache<K, T> cache;
    protected Consumer<T> consumer;

    public AbstractCacheObjectListener(RemoteCache<K, T> cache, Consumer<T> consumer) {
        this.cache = cache;
        this.consumer = consumer;
    }

    protected void handleEvent(K key, long version) {
        cache.getWithMetadataAsync(key).thenAccept(meta -> {
            if (meta.getVersion() == version) {
                consumer.accept(meta.getValue());
            }
        });
    }
}
