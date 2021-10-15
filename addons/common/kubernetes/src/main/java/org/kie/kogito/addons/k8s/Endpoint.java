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
package org.kie.kogito.addons.k8s;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Reference of the discovered endpoint
 */
public class Endpoint implements Serializable {

    private String url;
    private Map<String, String> secondaryURLs = new HashMap<>();
    private Map<String, String> labels = new HashMap<>();

    public Endpoint() {
    }

    public Endpoint(final String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Labels given to the Service
     */
    public Map<String, String> getLabels() {
        return Collections.unmodifiableMap(labels);
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    /**
     * Map to secondary URLs found in the target service.
     * For example, a service might expose monitoring ports targeting the very same service.
     * Key is the name of the port, and the value the resolved URL via Cluster IP.
     */
    public Map<String, String> getSecondaryURLs() {
        return Collections.unmodifiableMap(secondaryURLs);
    }

    /**
     * From the ServicePort spec:
     * <p/>
     * <quote>
     * The name of this port within the service. This must be a DNS_LABEL.
     * All ports within a ServiceSpec must have unique names.
     * When considering the endpoints for a Service, this must match the 'name' field in the EndpointPort.
     * Optional if only one ServicePort is defined on this service.
     * </quote>
     *
     * @see <a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.22/#serviceport-v1-core">ServicePort v1 core</a>
     * @param portName non-null, non-empty port name
     * @param url non-null, non-empty URL
     * @throws NullPointerException in case either parameters are null or empty
     */
    public void addSecondaryUrl(final String portName, final String url) {
        if (portName == null || portName.isEmpty()) {
            throw new NullPointerException("Service port name can't be null or empty");
        }
        if (url == null || url.isEmpty()) {
            throw new NullPointerException("Endpoint URL can't be null or empty");
        }
        if (this.secondaryURLs == null) {
            this.secondaryURLs = new HashMap<>();
        }
        this.secondaryURLs.put(portName, url);
    }

    public String getSecondaryUrl(final String portName) {
        return this.secondaryURLs.get(portName);
    }

    public void removeSecondaryUrl(final String portName) {
        this.secondaryURLs.remove(portName);
    }

    /**
     * Set the given URL as primary if none defined.
     *
     * @param url the given URL
     */
    public void setUrlIfEmpty(final String url) {
        if (url != null && !url.isEmpty() && this.urlIsEmpty()) {
            this.setUrl(url);
        }
    }

    public boolean urlIsEmpty() {
        return this.getUrl() == null || this.getUrl().isEmpty();
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
        return Objects.equals(url, endpoint.url) && Objects.equals(secondaryURLs, endpoint.secondaryURLs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, secondaryURLs);
    }
}
