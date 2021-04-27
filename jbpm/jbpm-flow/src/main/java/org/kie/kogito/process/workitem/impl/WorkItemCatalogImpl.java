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
package org.kie.kogito.process.workitem.impl;

import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;
import org.kie.kogito.internal.process.runtime.WorkItemNotFoundException;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.impl.BaseWorkItem;
import org.kie.kogito.process.workitem.Policy;
import org.kie.kogito.process.workitem.Transition;
import org.kie.kogito.process.workitem.WorkItemCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class WorkItemCatalogImpl<T> implements WorkItemCatalog {

    private final KogitoWorkItemManager workItemManager;
    private final Supplier<WorkflowProcessInstance> processInstanceSupplier;
    private final Runnable removeOnFinish;
    private final Runnable updateNotifier;

    public WorkItemCatalogImpl(
            KogitoWorkItemManager workItemManager,
            Supplier<WorkflowProcessInstance> processInstanceSupplier,
            Runnable removeOnFinish,
            Runnable updateNotifier) {
        this.workItemManager = workItemManager;
        this.processInstanceSupplier = processInstanceSupplier;
        this.removeOnFinish = removeOnFinish;
        this.updateNotifier = updateNotifier;
    }


    @Override
    public void complete(String id, Object variables, Policy<?>... policies) {
        workItemManager.completeWorkItem(id, (Map<String, Object>) variables, policies);
        removeOnFinish.run();
    }

    @Override
    public <R> R update(String id, Function<KogitoWorkItem, R> updater, Policy<?>... policies) {
        R result = workItemManager.updateWorkItem(id, updater,
                policies);
        updateNotifier.run();
        return result;
    }

    @Override
    public void abort(String id, Policy<?>... policies) {
        workItemManager.abortWorkItem(id, policies);
        removeOnFinish.run();
    }

    @Override
    public void transition(String id, Transition<?> transition) {
        workItemManager.transitionWorkItem(id, transition);
        removeOnFinish.run();
    }

    @Override
    public WorkItem get(String workItemId, Policy<?>... policies) {
        WorkItemNodeInstance workItemInstance = (WorkItemNodeInstance) processInstanceSupplier.get().getNodeInstances(true)
                .stream()
                .filter(ni -> ni instanceof WorkItemNodeInstance && ((WorkItemNodeInstance) ni).getWorkItemId().equals(workItemId) && ((WorkItemNodeInstance) ni).getWorkItem().enforce(policies))
                .findFirst()
                .orElseThrow(() -> new WorkItemNotFoundException("Work item with id " + workItemId + " was not found in current process instance", workItemId));
        return new BaseWorkItem(workItemInstance.getStringId(),
                workItemInstance.getWorkItem().getStringId(),
                Long.toString(workItemInstance.getNode().getId()),
                (String) workItemInstance.getWorkItem().getParameters().getOrDefault("TaskName", workItemInstance.getNodeName()),
                workItemInstance.getWorkItem().getState(),
                workItemInstance.getWorkItem().getPhaseId(),
                workItemInstance.getWorkItem().getPhaseStatus(),
                workItemInstance.getWorkItem().getParameters(),
                workItemInstance.getWorkItem().getResults());
    }

    @Override
    public Collection<WorkItem> get(Policy<?>... policies) {
        return processInstanceSupplier.get().getNodeInstances(true)
                .stream()
                .filter(ni -> ni instanceof WorkItemNodeInstance && ((WorkItemNodeInstance) ni).getWorkItem().enforce(policies))
                .map(ni -> new BaseWorkItem(ni.getStringId(),
                        ((WorkItemNodeInstance) ni).getWorkItemId(),
                        Long.toString(((WorkItemNodeInstance) ni).getNode().getId()),
                        (String) ((WorkItemNodeInstance) ni).getWorkItem().getParameters().getOrDefault("TaskName", ni.getNodeName()),
                        ((WorkItemNodeInstance) ni).getWorkItem().getState(),
                        ((WorkItemNodeInstance) ni).getWorkItem().getPhaseId(),
                        ((WorkItemNodeInstance) ni).getWorkItem().getPhaseStatus(),
                        ((WorkItemNodeInstance) ni).getWorkItem().getParameters(),
                        ((WorkItemNodeInstance) ni).getWorkItem().getResults()))
                .collect(Collectors.toList());
    }

}
