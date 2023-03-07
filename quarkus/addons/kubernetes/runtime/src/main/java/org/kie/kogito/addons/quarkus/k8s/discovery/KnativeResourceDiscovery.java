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
package org.kie.kogito.addons.quarkus.k8s.discovery;

import java.net.URI;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class KnativeResourceDiscovery extends AbstractResourceDiscovery {

    private final VanillaKubernetesResourceDiscovery vanillaKubernetesResourceDiscovery;

    @Inject
    public KnativeResourceDiscovery(VanillaKubernetesResourceDiscovery vanillaKubernetesResourceDiscovery) {
        this.vanillaKubernetesResourceDiscovery = vanillaKubernetesResourceDiscovery;
    }

    public Optional<URI> query(KnativeResourceUri knativeResourceUri) {
        return vanillaKubernetesResourceDiscovery.query(knativeResourceUri.getVanillaKubernetesResourceUri());
    }
}
