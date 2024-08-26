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
package org.kie.kogito.jbpm.usertask.handler;

import java.util.Optional;

import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemManager;
import org.kie.kogito.internal.process.workitem.WorkItemTransition;
import org.kie.kogito.process.workitems.InternalKogitoWorkItem;
import org.kie.kogito.process.workitems.impl.DefaultKogitoWorkItemHandler;
import org.kie.kogito.usertask.UserTask;
import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.UserTasks;
import org.kie.kogito.usertask.model.UserTaskModel;

/**
 * Default Work Item handler based on the standard life cycle
 */
public class UserTaskKogitoWorkItemHandler extends DefaultKogitoWorkItemHandler {

    private static final String DESCRIPTION = "Description";
    private static final String PRIORITY = "Priority";
    private static final String TASK_NAME = "TaskName";

    @Override
    public String getName() {
        return "Human Task";
    }

    @Override
    public Optional<WorkItemTransition> activateWorkItemHandler(KogitoWorkItemManager manager, KogitoWorkItemHandler handler, KogitoWorkItem workItem, WorkItemTransition transition) {
        UserTasks userTasks = handler.getApplication().get(UserTasks.class);

        UserTask userTask = userTasks.userTaskById((String) workItem.getParameter("id"));
        UserTaskModel model = userTask.createModel();
        model.setTaskName((String) workItem.getParameter(TASK_NAME));
        model.setTaskDescription((String) workItem.getParameter(DESCRIPTION));
        model.setTaskPriority((String) workItem.getParameter(PRIORITY));
        model.setExternalReferenceId(workItem.getStringId());
        UserTaskInstance instance = userTask.createInstance(model);

        if (workItem instanceof InternalKogitoWorkItem ikw) {
            ikw.setExternalReferenceId(instance.id());
        }

        return Optional.empty();
    }

    @Override
    public Optional<WorkItemTransition> completeWorkItemHandler(KogitoWorkItemManager manager, KogitoWorkItemHandler handler, KogitoWorkItem workItem, WorkItemTransition transition) {
        UserTasks userTasks = handler.getApplication().get(UserTasks.class);
        UserTask userTask = userTasks.userTaskById((String) workItem.getParameter("id"));
        userTask.instances().findById(workItem.getExternalReferenceId()).ifPresent(UserTaskInstance::complete);

        return Optional.empty();
    }

    @Override
    public Optional<WorkItemTransition> abortWorkItemHandler(KogitoWorkItemManager manager, KogitoWorkItemHandler handler, KogitoWorkItem workItem, WorkItemTransition transition) {
        UserTasks userTasks = handler.getApplication().get(UserTasks.class);
        UserTask userTask = userTasks.userTaskById((String) workItem.getParameter("id"));
        userTask.instances().findById(workItem.getExternalReferenceId()).ifPresent(UserTaskInstance::abort);
        return Optional.empty();
    }
}
