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

package org.kie.kogito.junit.deployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;

public class Deployment implements Cloneable {

    private String namespace;
    private List<Class<?>> eventListeners;
    private Map<String, Class<? extends KogitoWorkItemHandler>> workItemHandlers;
    private List<DeploymentResource> resources = new ArrayList<>();

    public Deployment(String namespace) {
        this.namespace = namespace;
        this.eventListeners = new ArrayList<>();
        this.workItemHandlers = new HashMap<>();
    }

    @Override
    public Deployment clone() throws CloneNotSupportedException {
        Deployment newDeployment = new Deployment(namespace);
        eventListeners.stream().forEach(e -> newDeployment.addEventListener(e));
        workItemHandlers.entrySet().forEach((k) -> newDeployment.addWorkItemHandler(k.getKey(), k.getValue()));
        resources.stream().forEach(e -> newDeployment.addResource(e));
        return newDeployment;
    }

    public void addResource(DeploymentResource resource) {
        resources.add(resource);
    }

    public String namespace() {
        return namespace;
    }

    public void addEventListener(Class<?> listener) {
        eventListeners.add(listener);
    }

    public void addWorkItemHandler(String name, Class<? extends KogitoWorkItemHandler> handler) {
        workItemHandlers.put(name, handler);
    }

    public List<Class<?>> getEventListeners() {
        return Collections.unmodifiableList(eventListeners);
    }

    public Map<String, Class<? extends KogitoWorkItemHandler>> getWorkItemHandlers() {
        return Collections.unmodifiableMap(workItemHandlers);
    }

    public List<DeploymentResource> getResources() {
        return Collections.unmodifiableList(resources);
    }

}
