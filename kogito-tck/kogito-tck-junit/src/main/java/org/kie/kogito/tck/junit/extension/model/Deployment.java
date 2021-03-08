/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.tck.junit.extension.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.kogito.Application;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.tck.junit.extension.TestClassLoader;

public class Deployment {

    private String namespace;
    private TestClassLoader classloader;
    private List<Class<?>> eventListeners;
    private Map<String, Class<? extends KogitoWorkItemHandler>> workItemHandlers;

    public Deployment(String namespace) {
        this.namespace = namespace;
        this.eventListeners = new ArrayList<>();
        this.workItemHandlers = new HashMap<>();
        this.classloader = new TestClassLoader(this.getClass().getClassLoader());
    }


    public void addExtraClasses(Map<String, byte[]> compiledCode) {
        classloader.addExtraClasses(compiledCode);
    }

    public void addEventListener(Class<?> listener) {
        eventListeners.add(listener);
    }

    public void addWorkItemHandler(String name, Class<? extends KogitoWorkItemHandler> handler) {
        workItemHandlers.put(name, handler);
    }


    @SuppressWarnings("unchecked")
    public Class<? extends Application> getApplication() throws ClassNotFoundException {
        return (Class<? extends Application>) Class.forName(namespace + ".Application", true, classloader);
    }


    public List<Class<?>> getEventListeners() {
        return Collections.unmodifiableList(eventListeners);
    }


    public Map<String, Class<? extends KogitoWorkItemHandler>> getWorkItemHandlers() {
        return Collections.unmodifiableMap(workItemHandlers);
    }

}
