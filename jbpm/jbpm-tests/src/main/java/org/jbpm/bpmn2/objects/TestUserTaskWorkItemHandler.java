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
package org.jbpm.bpmn2.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jbpm.process.instance.impl.humantask.HumanTaskWorkItemImpl;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemManager;
import org.kie.kogito.internal.process.workitem.WorkItemLifeCycle;
import org.kie.kogito.internal.process.workitem.WorkItemPhaseState;
import org.kie.kogito.internal.process.workitem.WorkItemTerminationType;
import org.kie.kogito.internal.process.workitem.WorkItemTransition;
import org.kie.kogito.process.workitems.impl.DefaultKogitoWorkItemHandler;
import org.kie.kogito.process.workitems.impl.DefaultWorkItemLifeCycle;
import org.kie.kogito.process.workitems.impl.DefaultWorkItemLifeCyclePhase;

public class TestUserTaskWorkItemHandler extends DefaultKogitoWorkItemHandler {
    public static final WorkItemPhaseState initialized = WorkItemPhaseState.initialized();
    public static final WorkItemPhaseState completed = WorkItemPhaseState.of("Completed", WorkItemTerminationType.COMPLETE);
    public static final WorkItemPhaseState aborted = WorkItemPhaseState.of("Aborted", WorkItemTerminationType.ABORT);
    public static final WorkItemPhaseState activated = WorkItemPhaseState.of("Activated");
    public static final WorkItemPhaseState reserved = WorkItemPhaseState.of("Reserved");

    private List<KogitoWorkItem> workItems = new ArrayList<>();

    @Override
    public WorkItemLifeCycle init() {
        DefaultWorkItemLifeCyclePhase complete = new DefaultWorkItemLifeCyclePhase("complete", reserved, completed, this::completeWorkItemHandler);
        DefaultWorkItemLifeCyclePhase abort = new DefaultWorkItemLifeCyclePhase("abort", reserved, aborted, this::abortWorkItemHandler);
        DefaultWorkItemLifeCyclePhase claim = new DefaultWorkItemLifeCyclePhase("claim", activated, reserved, this::claimWorkItemHandler);
        DefaultWorkItemLifeCyclePhase active = new DefaultWorkItemLifeCyclePhase("activate", initialized, activated, this::activateWorkItemHandler);

        return new DefaultWorkItemLifeCycle(active, claim, abort, complete);
    }

    public Optional<WorkItemTransition> claimWorkItemHandler(KogitoWorkItemManager manager, KogitoWorkItemHandler handler, KogitoWorkItem workItem, WorkItemTransition transition) {
        return Optional.empty();
    }

    @Override
    public Optional<WorkItemTransition> activateWorkItemHandler(KogitoWorkItemManager manager, KogitoWorkItemHandler handler, KogitoWorkItem workItem, WorkItemTransition transition) {
        workItems.add(workItem);

        if (workItem instanceof HumanTaskWorkItemImpl humanTaskWorkItem) {
            Optional<String> user = humanTaskWorkItem.getPotentialUsers().stream().findFirst();
            if (user.isPresent()) {
                humanTaskWorkItem.setActualOwner(user.get());
                return Optional.of(workItemLifeCycle.newTransition("claim", workItem.getPhaseStatus(), workItem.getResults()));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<WorkItemTransition> abortWorkItemHandler(KogitoWorkItemManager manager, KogitoWorkItemHandler handler, KogitoWorkItem workItem, WorkItemTransition transition) {
        workItems.remove(workItem);
        return Optional.empty();
    }

    @Override
    public Optional<WorkItemTransition> completeWorkItemHandler(KogitoWorkItemManager manager, KogitoWorkItemHandler handler, KogitoWorkItem workItem, WorkItemTransition transition) {
        workItems.remove(workItem);
        return Optional.empty();
    }

    public KogitoWorkItem getWorkItem() {
        switch (workItems.size()) {
            case 0:
                return null;
            case 1:
                KogitoWorkItem result = workItems.get(0);
                this.workItems.clear();
                return result;
            default:
                throw new IllegalArgumentException("More than one work item active");
        }
    }

    public List<KogitoWorkItem> getWorkItems() {
        List<KogitoWorkItem> result = new ArrayList<>(workItems);
        workItems.clear();
        return result;
    }

}
