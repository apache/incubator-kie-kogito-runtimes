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

package org.kie.kogito.junit.extension;

import java.util.Optional;

import org.kie.api.event.process.ProcessEventListener;
import org.kie.kogito.Model;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.junit.api.KogitoUnitTestContext;
import org.kie.kogito.junit.deployment.DeploymentContext;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstances;

public class KogitoUnitTestContextImpl implements KogitoUnitTestContext {

    private DeploymentContext deploymentContext;

    public KogitoUnitTestContextImpl(DeploymentContext deploymentContext) {
        this.deploymentContext = deploymentContext;
    }

    @Override
    public <T> T find(Class<T> clazz) {
        if (KogitoWorkItemHandler.class.isAssignableFrom(clazz)) {
            for (KogitoWorkItemHandler wih : deploymentContext.get().getWorkItemHandlers().values()) {
                if (wih.getClass().equals(clazz)) {
                    return clazz.cast(wih);
                }
            }
        } else {
            for (Object listener : deploymentContext.get().getProcessEventListeners()) {
                if (listener.getClass().equals(clazz)) {
                    return clazz.cast(listener);
                }
            }
        }

        return null;
    }

    @Override
    public Process<? extends Model> processById(String processId) {
        return deploymentContext.get().processById(processId);
    }

    @Override
    public ProcessInstances<? extends Model> instances(String processId) {
        return deploymentContext.get().processById(processId).instances();
    }

    @Override
    public Optional<ProcessInstance<? extends Model>> processInstanceById(String processId, String id) {
        return deploymentContext.get().findById(processId, id);
    }

    @Override
    public Optional<ProcessInstance<? extends Model>> findByBusinessKey(String processId, String businessKey) {
        return deploymentContext.get().findByBusinessKey(processId, businessKey);
    }

    @Override
    public void registerEventListener(ProcessEventListener defaultProcessEventListener) {
        deploymentContext.get().register(defaultProcessEventListener);
    }

    @Override
    public void registerWorkItemHandler(String name, KogitoWorkItemHandler handler) {
        deploymentContext.get().registerWorkItemHandler(name, handler);
    }

    @Override
    public void destroy() {
        deploymentContext.destroy();
    }

}
