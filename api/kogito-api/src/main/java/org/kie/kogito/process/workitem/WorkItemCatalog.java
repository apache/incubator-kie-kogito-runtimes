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
package org.kie.kogito.process.workitem;

import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.process.WorkItem;

import java.util.Collection;
import java.util.function.Function;

public interface WorkItemCatalog {

    /**
     * Completes work item belonging to this process instance with given variables
     *
     * @param id id of the work item to complete
     * @param variables optional variables
     * @param policies optional list of policies to be enforced
     */
    void complete(String id, Object variables, Policy<?>... policies);

    /**
     * Updates work item according to provided consumer
     *
     * @param id the id of the work item that has been completed
     * @param updater consumer implementation that contains the logic to update workitem
     * @param policies optional security information
     * @return result of the operation performed by the updater
     */
    <R> R update(String id, Function<KogitoWorkItem, R> updater, Policy<?>... policies);

    /**
     * Aborts work item belonging to this process instance
     *
     * @param id id of the work item to complete
     * @param policies optional list of policies to be enforced
     */
    void abort(String id, Policy<?>... policies);

    /**
     * Transition work item belonging to this process instance not another life cycle phase
     *
     * @param id id of the work item to complete
     * @param transition target transition including phase, identity and data
     */
    void transition(String id, Transition<?> transition);

    /**
     * Returns work item identified by given id if found
     *
     * @param workItemId id of the work item
     * @param policies optional list of policies to be enforced
     * @return work item with its parameters if found
     */
    WorkItem get(String workItemId, Policy<?>... policies);

    /**
     * Returns list of currently active work items.
     *
     * @param policies optional list of policies to be enforced
     * @return non empty list of identifiers of currently active tasks.
     */
    Collection<WorkItem> get(Policy<?>... policies);
}
