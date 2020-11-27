/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.process;

import java.io.Closeable;

public interface MutableProcessInstances<T> extends ProcessInstances<T> {

    boolean exists(String id, Object... options);

    void create(String id, ProcessInstance<T> instance, Object... options);

    void update(String id, ProcessInstance<T> instance, Object... options);

    void remove(String id, Object... options);

    default Closeable startTransaction() {
        return null;
    }

    default void commitTransaction(Closeable closeable) {

    }

    default boolean isActive(ProcessInstance<T> instance) {
        return instance.status() == ProcessInstance.STATE_ACTIVE || instance.status() == ProcessInstance.STATE_ERROR;
    }

}
