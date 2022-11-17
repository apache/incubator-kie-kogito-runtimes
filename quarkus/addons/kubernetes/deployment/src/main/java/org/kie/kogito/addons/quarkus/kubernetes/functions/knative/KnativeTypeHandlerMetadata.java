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
package org.kie.kogito.addons.quarkus.kubernetes.functions.knative;

final class KnativeTypeHandlerMetadata {

    private final String knativeServiceName;

    private final String path;

    private KnativeTypeHandlerMetadata(String knativeServiceName, String path) {
        this.knativeServiceName = knativeServiceName;
        this.path = path;
    }

    public static KnativeTypeHandlerMetadata from(String operation) {
        String[] splitOperation = operation.split("\\?");

        String path;
        if (splitOperation.length > 1) {
            path = splitOperation[1].substring("path=".length());
        } else {
            path = "/";
        }

        return new KnativeTypeHandlerMetadata(splitOperation[0], path);
    }

    String getKnativeServiceName() {
        return knativeServiceName;
    }

    String getPath() {
        return path;
    }
}
