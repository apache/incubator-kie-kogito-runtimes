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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

public final class KnativeTypeHandlerOperation {

    private final String knativeServiceName;

    private final String path;

    private final Map<String, Object> parameters;

    KnativeTypeHandlerOperation(String knativeServiceName, String path, Map<String, Object> parameters) {
        this.knativeServiceName = knativeServiceName;
        this.path = path;
        this.parameters = parameters;
    }

    public static KnativeTypeHandlerOperation from(String operation) {
        String[] splitOperation = operation.split("\\?");

        String knativeServiceName = splitOperation[0];

        if (splitOperation.length == 1) {
            return new KnativeTypeHandlerOperation(knativeServiceName, "/", Map.of());
        } else {
            Map<String, String> queryString = Arrays.stream(splitOperation[1].split("&"))
                    .map(param -> param.split("="))
                    .collect(toMap(splitParam -> splitParam[0], splitParam -> splitParam[1]));

            String path = "/";
            Map<String, Object> parameters = new HashMap<>();

            for (Map.Entry<String, String> entry : queryString.entrySet()) {
                if ("path".equals(entry.getKey())) {
                    path = entry.getValue();
                } else {
                    parameters.put(entry.getKey(), entry.getValue());
                }
            }

            return new KnativeTypeHandlerOperation(knativeServiceName, path, parameters);
        }
    }

    public String getKnativeServiceName() {
        return knativeServiceName;
    }

    public String getPath() {
        return path;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KnativeTypeHandlerOperation metadata = (KnativeTypeHandlerOperation) o;
        return knativeServiceName.equals(metadata.knativeServiceName) && path.equals(metadata.path) && parameters.equals(metadata.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(knativeServiceName, path, parameters);
    }

    @Override
    public String toString() {
        return "KnativeTypeHandlerOperation{" +
                "knativeServiceName='" + knativeServiceName + '\'' +
                ", path='" + path + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
