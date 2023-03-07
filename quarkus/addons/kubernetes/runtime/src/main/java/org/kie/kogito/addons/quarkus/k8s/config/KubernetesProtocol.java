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
package org.kie.kogito.addons.quarkus.k8s.config;

import org.kie.kogito.addons.quarkus.k8s.KubeConstants;

enum KubernetesProtocol {

    VANILLA_KUBERNETES,
    OPENSHIFT,
    KNATIVE;

    static KubernetesProtocol parse(String uri) {
        if (uri.contains(":")) {
            String protocol = uri.substring(0, uri.indexOf(':'));

            switch (protocol) {
                case "kubernetes":
                    return VANILLA_KUBERNETES;
                case "openshift":
                    return OPENSHIFT;
                case "knative":
                    return KNATIVE;
                default:
                    throw new IllegalArgumentException("The provided protocol [" + protocol + "] is not " +
                            "supported, supported values are " +
                            KubeConstants.SUPPORTED_PROTOCOLS);
            }
        } else {
            throw new IllegalArgumentException("The provided uri [" + uri + "] doesn't have a defined protocol");
        }
    }
}
