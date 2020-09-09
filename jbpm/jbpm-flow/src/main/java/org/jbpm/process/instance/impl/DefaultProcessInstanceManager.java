/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.process.instance.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jbpm.process.instance.ProcessInstanceManager;
import org.kie.api.runtime.process.ProcessInstance;

public class DefaultProcessInstanceManager implements ProcessInstanceManager {

    private Map<String, ProcessInstance> processInstances = new ConcurrentHashMap<>();

    public void addProcessInstance(ProcessInstance processInstance) {
        String id = UUID.randomUUID().toString();
        ((org.jbpm.process.instance.ProcessInstance) processInstance).setId(id);
        internalAddProcessInstance(processInstance);
    }

    public void internalAddProcessInstance(ProcessInstance processInstance) {
        processInstances.put(processInstance.getId(), processInstance);
    }

    public Collection<ProcessInstance> getProcessInstances() {
        return Collections.unmodifiableCollection(processInstances.values());
    }

    public ProcessInstance getProcessInstance(String id) {
        return processInstances.get(id);
    }

    public ProcessInstance getProcessInstance(String id, boolean readOnly) {
        return processInstances.get(id);
    }

    public void removeProcessInstance(ProcessInstance processInstance) {
        internalRemoveProcessInstance(processInstance);
    }

    public void internalRemoveProcessInstance(ProcessInstance processInstance) {
        processInstances.remove(processInstance.getId());
    }

    public void clearProcessInstances() {
    	processInstances.clear();
    }

    public void clearProcessInstancesState() {

    }
}
