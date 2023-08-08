/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addons.quarkus.microprofile.config.service.catalog;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.kie.kogito.addons.k8s.resource.catalog.KubernetesServiceCatalog;
import org.kie.kogito.addons.k8s.resource.catalog.KubernetesServiceCatalogKey;

@ApplicationScoped
public class MicroProfileConfigServiceCatalog implements KubernetesServiceCatalog {

    private final Map<KubernetesServiceCatalogKey, URI> services = new ConcurrentHashMap<>();

    @Override
    public Optional<URI> getServiceAddress(KubernetesServiceCatalogKey key) {
        URI serviceUrl = services.get(key);

        if (serviceUrl == null) {
            serviceUrl = fetchServiceAddressFromConfig(key);
            services.put(key, serviceUrl);
        }

        return Optional.ofNullable(serviceUrl);
    }

    private URI fetchServiceAddressFromConfig(KubernetesServiceCatalogKey key) {
        try {
            Config config = ConfigProvider.getConfig();
            String configValue = config.getValue(key.getProtocol().getValue() + ":" + key.getCoordinates(), String.class);
            return new URI(configValue);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}
