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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OpenShiftResourceUri {

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftResourceUri.class.getName());

    private final VanillaKubernetesResourceUri vanillaKubernetesResourceUri;

    public OpenShiftResourceUri(VanillaKubernetesResourceUri vanillaKubernetesResourceUri) {
        this.vanillaKubernetesResourceUri = vanillaKubernetesResourceUri;
    }

    public VanillaKubernetesResourceUri getVanillaKubernetesResourceUri() {
        return vanillaKubernetesResourceUri;
    }

    @Override
    public String toString() {
        return "OpenShiftResourceUri{" +
                "vanillaKubernetesResourceUri=" + vanillaKubernetesResourceUri +
                '}';
    }

    public static OpenShiftResourceUri parse(String rawUri) {
        OpenShiftResourceUri openShiftResourceUri = new OpenShiftResourceUri(VanillaKubernetesResourceUri.parse(rawUri));
        logger.debug("OpenShiftResourceUri successfully parsed: {}", openShiftResourceUri);
        return openShiftResourceUri;
    }

    public String getResourceName() {
        return vanillaKubernetesResourceUri.getResourceName();
    }

    public String getNamespace() {
        return vanillaKubernetesResourceUri.getNamespace();
    }

    public GVK getGvk() {
        return vanillaKubernetesResourceUri.getGvk();
    }
}
