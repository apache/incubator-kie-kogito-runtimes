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
import java.util.Optional;

import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.impl.CachedWorkItemHandlerConfig;
import org.kie.kogito.rules.RuleConfig;

public class DeploymentInstance {

    private Application application;
    private List<Object> eventListeners;
    private Map<String, KogitoWorkItemHandler> workItemHandlers;

    public DeploymentInstance() {
        this.eventListeners = new ArrayList<>();
        this.workItemHandlers = new HashMap<>();
    }

    public static DeploymentInstance newInstance(Deployment deployment) {
        try {
            DeploymentInstance instance = new DeploymentInstance();
            instance.application = deployment.getApplication().newInstance();

            for (Class<?> listener : deployment.getEventListeners()) {
                instance.register(listener.newInstance());
            }
            for (Map.Entry<String, Class<? extends KogitoWorkItemHandler>> entry : deployment.getWorkItemHandlers().entrySet()) {
                KogitoWorkItemHandler handler = entry.getValue().newInstance();
                String name = entry.getKey();
                instance.registerWorkItemHandler(name, handler);
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException
                | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public void registerWorkItemHandler(String name, KogitoWorkItemHandler handler) {
        workItemHandlers.put(name, handler);
        ((CachedWorkItemHandlerConfig) application.config().get(ProcessConfig.class).workItemHandlers()).register(name,
                                                                                                                  handler);
    }

    public void register(Object processEventListener) {
        if(processEventListener instanceof ProcessEventListener) {
            eventListeners.add(processEventListener);
            application.config().get(ProcessConfig.class).processEventListeners().listeners().add((ProcessEventListener) processEventListener);
        } else if (processEventListener instanceof AgendaEventListener) {
            eventListeners.add(processEventListener);
            application.config().get(RuleConfig.class).ruleEventListeners().agendaListeners().add((AgendaEventListener) processEventListener);
        }
    }

    public Application getApplication() {
        return application;
    }

    public Map<String, KogitoWorkItemHandler> getWorkItemHandlers() {
        return Collections.unmodifiableMap(workItemHandlers);
    }

    public List<Object> getProcessEventListeners() {
        return Collections.unmodifiableList(eventListeners);
    }

    @SuppressWarnings("unchecked")
    public Optional<ProcessInstance<? extends Model>> findById(String processId, String id) {
        Optional<?> pi = application.get(Processes.class).processById(processId).instances().findById(id);
        return (Optional<ProcessInstance<? extends Model>>) pi;
    }

    @SuppressWarnings("unchecked")
    public Optional<ProcessInstance<? extends Model>> findByBusinessKey(String processId, String businessKey) {
        Optional<?> pi = application.get(Processes.class).processById(processId).instances().findByBusinessKey(
                                                                                                               businessKey);
        return (Optional<ProcessInstance<? extends Model>>) pi;
    }

}
