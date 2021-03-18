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

package org.kie.kogito.tck.junit.extension;

import java.util.Optional;

import org.kie.api.event.process.ProcessEventListener;
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstances;
import org.kie.kogito.process.Processes;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.extension.model.DeploymentInstance;

public class KogitoUnitTestContextImpl implements KogitoUnitTestContext {

    private DeploymentInstance deploymentInstance;


    public KogitoUnitTestContextImpl(DeploymentInstance deploymentInstance) {
        this.deploymentInstance = deploymentInstance;
    }

    @Override
    public <T> T find(Class<T> clazz) {
        if(Application.class.isAssignableFrom(clazz)) {
            return clazz.cast(deploymentInstance.getApplication());
        } else if (KogitoWorkItemHandler.class.isAssignableFrom(clazz)) {
            for(KogitoWorkItemHandler wih : deploymentInstance.getWorkItemHandlers().values()) {
                if(wih.getClass().equals(clazz)) {
                    return clazz.cast(wih);
                }
            }
        } else {
            for(Object listener : deploymentInstance.getProcessEventListeners()) {
                if(listener.getClass().equals(clazz)) {
                    return clazz.cast(listener);
                }
            }
        }
        

        return null;
    }

    @Override
    public ProcessInstances<? extends Model> instances(String processId) {
        return deploymentInstance.getApplication().get(Processes.class).processById(processId).instances();
    }
    @Override
    public void registerEventListener(ProcessEventListener defaultProcessEventListener) {
        deploymentInstance.register(defaultProcessEventListener);
    }

    @Override
    public void registerWorkItemHandler(String name, KogitoWorkItemHandler handler) {
        deploymentInstance.registerWorkItemHandler(name, handler);
    }
    @Override
    public Optional<ProcessInstance<? extends Model>> findById(String processId, String id) {
        return deploymentInstance.findById(processId, id);
    }

    @Override
    public Optional<ProcessInstance<? extends Model>> findByBusinessKey(String processId, String businessKey) {
        return deploymentInstance.findByBusinessKey(processId, businessKey);
    }

}
