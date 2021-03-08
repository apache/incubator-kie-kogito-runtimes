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

package org.kie.kogito.junit.api;

import java.util.Collection;
import java.util.Optional;

import org.kie.api.event.process.ProcessEventListener;
import org.kie.kogito.Model;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceReadMode;
import org.kie.kogito.process.ProcessInstances;

import static java.util.Collections.emptyList;

public interface KogitoUnitTestContext {

    default boolean isSupported(Class<?> clazz) {
        return find(clazz) != null;
    }

    <T> T find(Class<T> class1);

    Process<? extends Model> processById(String processId);

    Optional<ProcessInstance<? extends Model>> processInstanceById(String processId, String id);

    Optional<ProcessInstance<? extends Model>> findByBusinessKey(String processId, String businessKey);

    default ProcessInstances<? extends Model> instances(String processId) {
        return new ProcessInstances<Model>() {

            @Override
            public Optional<ProcessInstance<Model>> findById(String id, ProcessInstanceReadMode mode) {
                return Optional.empty();
            }

            @Override
            public Collection<ProcessInstance<Model>> values(ProcessInstanceReadMode mode) {
                return emptyList();
            }

            @Override
            public Integer size() {
                return 0;
            }
        };
    }

    default void registerWorkItemHandler(String string, KogitoWorkItemHandler signallingTaskHandlerDecorator) {
    }

    default void registerEventListener(ProcessEventListener defaultProcessEventListener) {
    }

    void destroy();

}
