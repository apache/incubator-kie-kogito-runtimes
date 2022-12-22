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
package org.kie.kogito.addons.quarkus.knative.serving.customfunctions;

import java.util.Optional;

final class KnativeService {

    private final String namespace;

    private final String name;

    KnativeService(String url) {
        String[] splitUrl = url.split("/");

        if (splitUrl.length == 1) {
            namespace = null;
            name = splitUrl[0];
        } else {
            namespace = splitUrl[0];
            name = splitUrl[1];
        }
    }

    String getName() {
        return name;
    }

    Optional<String> getNamespace() {
        return Optional.ofNullable(namespace);
    }
}
