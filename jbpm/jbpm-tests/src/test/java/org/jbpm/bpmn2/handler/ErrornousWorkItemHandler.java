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
package org.jbpm.bpmn2.handler;

import java.util.Optional;

import org.kie.api.runtime.process.ProcessWorkItemHandlerException;
import org.kie.api.runtime.process.ProcessWorkItemHandlerException.HandlingStrategy;
import org.kie.api.runtime.process.WorkItem;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemManager;
import org.kie.kogito.internal.process.workitem.WorkItemTransition;
import org.kie.kogito.process.workitems.impl.DefaultKogitoWorkItemHandler;

public class ErrornousWorkItemHandler extends DefaultKogitoWorkItemHandler {

    private String processId;
    private HandlingStrategy strategy;

    private WorkItem workItem;

    public ErrornousWorkItemHandler(String processId, HandlingStrategy strategy) {
        super();
        this.processId = processId;
        this.strategy = strategy;
    }

    @Override
    public Optional<WorkItemTransition> activateWorkItemHandler(KogitoWorkItemManager manager, KogitoWorkItemHandler handler, KogitoWorkItem workItem, WorkItemTransition transition) {
        this.workItem = workItem;
        if (processId != null && strategy != null) {

            if (workItem.getParameter("isCheckedCheckbox") != null) {
                return Optional.of(this.workItemLifeCycle.newTransition("complete", workItem.getPhaseStatus(), workItem.getParameters()));
            } else {
                throw new ProcessWorkItemHandlerException(processId, strategy, new RuntimeException("On purpose"));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<WorkItemTransition> abortWorkItemHandler(KogitoWorkItemManager manager, KogitoWorkItemHandler handler, KogitoWorkItem workItem, WorkItemTransition transition) {
        this.workItem = workItem;
        return Optional.empty();
    }

    public WorkItem getWorkItem() {
        return workItem;
    }
}
