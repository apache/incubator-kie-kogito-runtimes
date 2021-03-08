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

package org.kogito.junit5.runtime.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.junit.deployment.DeploymentInstance;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.impl.CachedWorkItemHandlerConfig;
import org.kie.kogito.rules.RuleConfig;

public class JavaDeploymentInstance implements DeploymentInstance {

    private JavaFolderClassLoader classLoader;
    private Application application;

    private ProcessConfig processConfig;
    private RuleConfig ruleConfig;
    private List<Object> eventListeners;
    private Map<String, KogitoWorkItemHandler> workItemHandlers;

    public JavaDeploymentInstance(JavaFolderClassLoader classLoader) {
        this.classLoader = classLoader;
        this.eventListeners = new ArrayList<>();
        this.workItemHandlers = new HashMap<>();

    }

    public void setProcessConfig(ProcessConfig processConfig) {
        this.processConfig = processConfig;
    }

    public ProcessConfig getProcessConfig() {
        return processConfig;
    }

    public void setRuleConfig(RuleConfig ruleConfig) {
        this.ruleConfig = ruleConfig;
    }

    public RuleConfig getRuleConfig() {
        return ruleConfig;
    }

    public void registerWorkItemHandler(String name, KogitoWorkItemHandler handler) {
        workItemHandlers.put(name, handler);
        ((CachedWorkItemHandlerConfig) getProcessConfig().workItemHandlers()).register(name, handler);
    }

    public void register(Object processEventListener) {
        if (processEventListener instanceof ProcessEventListener) {
            eventListeners.add(processEventListener);
            getProcessConfig().processEventListeners().listeners().add((ProcessEventListener) processEventListener);
        } else if (processEventListener instanceof AgendaEventListener) {
            eventListeners.add(processEventListener);
            getRuleConfig().ruleEventListeners().agendaListeners().add((AgendaEventListener) processEventListener);
        }
    }

    public Map<String, KogitoWorkItemHandler> getWorkItemHandlers() {
        return Collections.unmodifiableMap(workItemHandlers);
    }

    public List<Object> getProcessEventListeners() {
        return Collections.unmodifiableList(eventListeners);
    }

    public void setApplication(Application application) {
        this.application = application;
        this.setProcessConfig(this.getApplication().config().get(ProcessConfig.class));
        this.setRuleConfig(this.application.config().get(RuleConfig.class));
    }

    public Application getApplication() {
        return application;
    }

    @Override
    public Process<? extends Model> processById(String processId) {
        return application.get(Processes.class).processById(processId);
    }

    @Override
    public void destroy() {
        classLoader.destroy();
    }

}