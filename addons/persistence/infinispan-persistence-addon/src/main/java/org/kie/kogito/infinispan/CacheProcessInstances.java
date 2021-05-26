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
package org.kie.kogito.infinispan;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.kie.kogito.process.MutableProcessInstances;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceDuplicatedException;
import org.kie.kogito.process.ProcessInstanceReadMode;
import org.kie.kogito.process.impl.AbstractProcessInstance;
import org.kie.kogito.serialization.process.ProcessInstanceMarshallerService;

import static org.kie.kogito.process.ProcessInstanceReadMode.MUTABLE;

@SuppressWarnings({ "rawtypes" })
public class CacheProcessInstances implements MutableProcessInstances {

    private final RemoteCache<String, byte[]> cache;
    private ProcessInstanceMarshallerService marshaller;
    private org.kie.kogito.process.Process process;

    public CacheProcessInstances(Process process, RemoteCacheManager cacheManager, String templateName) {
        this.process = process;
        this.cache = cacheManager.administration().getOrCreateCache(process.id() + "_store", ignoreNullOrEmpty(templateName));
        this.marshaller = ProcessInstanceMarshallerService.newBuilder().withDefaultObjectMarshallerStrategies().build();
    }

    @Override
    public Integer size() {
        return cache.size();
    }

    @Override
    public Optional<ProcessInstance> findById(String id, ProcessInstanceReadMode mode) {
        byte[] data = cache.get(id);
        if (data == null) {
            return Optional.empty();
        }

        return Optional.of(mode == MUTABLE ? marshaller.unmarshallProcessInstance(data, process) : marshaller.unmarshallReadOnlyProcessInstance(data, process));
    }

    @Override
    public Collection<ProcessInstance> values(ProcessInstanceReadMode mode) {
        return cache.values()
                .parallelStream()
                .map(data -> mode == MUTABLE ? marshaller.unmarshallProcessInstance(data, process) : marshaller.unmarshallReadOnlyProcessInstance(data, process))
                .collect(Collectors.toList());
    }

    @Override
    public void update(String id, ProcessInstance instance) {
        updateStorage(id, instance, false);
    }

    @Override
    public void remove(String id) {
        cache.remove(id);
    }

    protected String ignoreNullOrEmpty(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value;
    }

    @Override
    public void create(String id, ProcessInstance instance) {
        updateStorage(id, instance, true);
    }

    @SuppressWarnings("unchecked")
    protected void updateStorage(String id, ProcessInstance instance, boolean checkDuplicates) {
        if (isActive(instance)) {
            byte[] data = marshaller.marshallProcessInstance(instance);

            if (checkDuplicates) {
                byte[] existing = cache.putIfAbsent(id, data);
                if (existing != null) {
                    throw new ProcessInstanceDuplicatedException(id);
                }
            } else {
                cache.put(id, data);
            }
            Supplier<byte[]> supplier = () -> cache.get(id);
            ((AbstractProcessInstance) instance).internalRemoveProcessInstance(marshaller.createdReloadFunction(supplier));
        }
    }

    @Override
    public boolean exists(String id) {
        return cache.containsKey(id);
    }
}
