/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.kie.api.internal.utils;

import java.util.Map;
import java.util.function.Supplier;

import static org.kie.api.internal.utils.ServiceUtil.instanceFromNames;

/**
 * This is an internal class, not for public consumption.
 */
public class ServiceRegistryImpl
        implements
        ServiceRegistry {

    private static final String DYNAMIC_IMPL = "org.drools.dynamic.common.DynamicServiceRegistrySupplier";
    private static final String STATIC_IMPL = "org.drools.statics.common.StaticServiceRegistrySupplier";

    private static Supplier<ServiceRegistry> supplier = instanceFromNames(DYNAMIC_IMPL, STATIC_IMPL);

    private Map<String, Object> registry;

    public ServiceRegistryImpl() {
        registry = ServiceDiscoveryImpl.getInstance().getServices();
    }

    public synchronized void reset() {
        ServiceDiscoveryImpl.getInstance().reset();
    }

    public synchronized void reload() {
        registry = ServiceDiscoveryImpl.getInstance().getServices();
    }

    public synchronized <T> T get(Class<T> cls) {
        Object service = this.registry.get( cls.getName() );
        return cls.isInstance( service ) ? (T) service : null;
    }

    public static ServiceRegistry getServiceRegistry() {
        return supplier.get();
    }
}
