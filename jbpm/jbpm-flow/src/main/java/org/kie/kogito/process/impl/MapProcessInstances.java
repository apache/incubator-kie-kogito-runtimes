/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.process.impl;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.kie.kogito.Model;
import org.kie.kogito.process.MutableProcessInstances;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceDuplicatedException;
import org.kie.kogito.process.ProcessInstanceNotFoundException;
import org.kie.kogito.process.ProcessInstanceReadMode;

class MapProcessInstances<T extends Model> implements MutableProcessInstances<T> {

    private final ConcurrentHashMap<String, WorkflowProcessInstance> instances = new ConcurrentHashMap<>();
    private AbstractProcess<T> process;

    public MapProcessInstances(AbstractProcess<T> process) {
        this.process = process;
    }

    @Override
    public Optional<ProcessInstance<T>> findById(String id, ProcessInstanceReadMode mode) {
        WorkflowProcessInstance instance = instances.get(id);
        if (instance == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(toProcessInstance(instance, mode));
    }

    @Override
    public void create(String id, ProcessInstance<T> instance) {
        WorkflowProcessInstance existing = instances.putIfAbsent(id, ((AbstractProcessInstance<T>) instance).internalGetProcessInstance());
        if (existing != null) {
            throw new ProcessInstanceDuplicatedException(id);
        }
        connectProcessInstance(instance);
    }

    @Override
    public void update(String id, ProcessInstance<T> instance) {
        if (isActive(instance)) {
            instances.put(id, ((AbstractProcessInstance<T>) instance).internalGetProcessInstance());
            connectProcessInstance(instance);
        }
    }

    @Override
    public void remove(String id) {
        instances.remove(id);
    }

    @Override
    public boolean exists(String id) {
        return instances.containsKey(id);
    }

    @Override
    public Stream<ProcessInstance<T>> stream(ProcessInstanceReadMode mode) {
        return instances.values().stream().map(e -> toProcessInstance(e, mode));
    }

    private ProcessInstance<T> toProcessInstance(WorkflowProcessInstance instance, ProcessInstanceReadMode mode) {
        if (mode.equals(ProcessInstanceReadMode.READ_ONLY)) {
            return process.createReadOnlyInstance(instance);
        }

        ProcessInstance<T> processInstance = process.createInstance(instance);
        connectProcessInstance(processInstance);
        return processInstance;
    }

    protected void connectProcessInstance(ProcessInstance<T> instance) {
        ((AbstractProcessInstance<?>) instance).internalSetReloadSupplier(pi -> {
            WorkflowProcessInstance workflowProcessInstance = instances.get(instance.id());
            if (workflowProcessInstance == null) {
                throw new ProcessInstanceNotFoundException(instance.id());
            }
            ((AbstractProcessInstance<?>) instance).internalSetProcessInstance(workflowProcessInstance);
        });
    }
}
