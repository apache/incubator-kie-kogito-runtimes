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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.kie.kogito.Model;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;

public interface DeploymentInstance {

    Process<? extends Model> processById(String processId);

    @SuppressWarnings("unchecked")
    default Optional<ProcessInstance<? extends Model>> findById(String processId, String id) {
        Optional<?> pi = processById(processId).instances().findById(id);
        return (Optional<ProcessInstance<? extends Model>>) pi;
    }

    @SuppressWarnings("unchecked")
    default Optional<ProcessInstance<? extends Model>> findByBusinessKey(String processId, String businessKey) {
        Optional<?> pi = processById(processId).instances().findByBusinessKey(businessKey);
        return (Optional<ProcessInstance<? extends Model>>) pi;
    }

    void destroy();

    Map<String, KogitoWorkItemHandler> getWorkItemHandlers();

    List<Object> getProcessEventListeners();

    void registerWorkItemHandler(String name, KogitoWorkItemHandler handler);

    void register(Object defaultProcessEventListener);

}
