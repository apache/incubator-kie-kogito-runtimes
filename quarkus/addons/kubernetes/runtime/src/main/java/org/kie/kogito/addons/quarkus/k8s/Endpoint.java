/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addons.quarkus.k8s;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.fabric8.kubernetes.api.model.Service;

/**
 * Reference of the discovered endpoint
 */
public class Endpoint {

    private List<String> URLs = new ArrayList<>();

    /**
     * Creates the {@link Endpoint} reference based on a Kubernetes Service instance.
     */
    public static Endpoint fromKubeService(Service service) {
        return new Endpoint(KubernetesServiceUtil.buildURLsForStandardPorts(service));
    }

    public Endpoint() {
    }

    public Endpoint(final List<String> urls) {
        this.URLs = urls;
    }

    public Endpoint(final String url) {
        this.URLs.add(url);
    }

    public List<String> getURLs() {
        return Collections.unmodifiableList(URLs);
    }

    public void setURLs(List<String> URLs) {
        this.URLs = URLs;
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                " URLs='" + URLs + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Endpoint endpoint = (Endpoint) o;
        return Objects.equals(URLs, endpoint.URLs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(URLs);
    }
}
