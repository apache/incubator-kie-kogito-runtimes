/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addons.quarkus.k8s.functions.knative;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.kie.kogito.addons.quarkus.k8s.ServiceDiscoveryException;

public final class KnativeServiceRegistry {

    private final Map<String, Server> services = new HashMap<>();

    private final KnativeServiceDiscovery knativeServiceDiscovery;

    public KnativeServiceRegistry(KnativeServiceDiscovery knativeServiceDiscovery) {
        this.knativeServiceDiscovery = knativeServiceDiscovery;
    }

    void addService(String serviceName) {
        services.put(serviceName,
                knativeServiceDiscovery.discover(serviceName).orElseThrow(() -> new ServiceDiscoveryException("A Knative service with name '" + serviceName + "' could not be found.")));
    }

    public Optional<Server> getServer(String serviceName) {
        return Optional.ofNullable(services.get(serviceName));
    }
}
