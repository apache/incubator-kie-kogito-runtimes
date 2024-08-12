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
package org.jbpm.workflow.instance.node;

import java.util.HashSet;
import java.util.Set;

import org.jbpm.process.core.context.swimlane.SwimlaneContext;
import org.jbpm.process.instance.context.swimlane.SwimlaneContextInstance;
import org.jbpm.process.instance.impl.humantask.HumanTaskWorkItemImpl;
import org.jbpm.process.instance.impl.humantask.InternalHumanTaskWorkItem;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.kie.kogito.process.workitems.InternalKogitoWorkItem;
import org.kie.kogito.usertask.HumanTaskWorkItem;

public class HumanTaskNodeInstance extends WorkItemNodeInstance {

    private static final long serialVersionUID = 510l;
    private static final String NODE_NAME = "NodeName";
    private static final String DESCRIPTION = "Description";
    private static final String PRIORITY = "Priority";
    private static final String TASK_NAME = "TaskName";
    private String separator = System.getProperty("org.jbpm.ht.user.separator", ",");

    private static final String ACTOR_ID = "ActorId";
    private static final String GROUP_ID = "GroupId";
    private static final String BUSINESSADMINISTRATOR_ID = "BusinessAdministratorId";
    private static final String BUSINESSADMINISTRATOR_GROUP_ID = "BusinessAdministratorGroupId";
    private static final String EXCLUDED_OWNER_ID = "ExcludedOwnerId";
    private static final String WORK_ITEM_TRANSITION = "workItemTransition";

    private transient SwimlaneContextInstance swimlaneContextInstance;

    public HumanTaskNode getHumanTaskNode() {
        return (HumanTaskNode) getNode();
    }

    @Override
    public InternalHumanTaskWorkItem getWorkItem() {
        return (InternalHumanTaskWorkItem) super.getWorkItem();
    }

    @Override
    protected InternalKogitoWorkItem newWorkItem() {
        return new HumanTaskWorkItemImpl();
    }

    @Override
    protected InternalKogitoWorkItem createWorkItem(WorkItemNode workItemNode) {
        InternalHumanTaskWorkItem workItem = (InternalHumanTaskWorkItem) super.createWorkItem(workItemNode);
        String actorId = assignWorkItem(workItem);
        if (actorId != null) {
            workItem.setParameter(ACTOR_ID, actorId);
        }

        workItem.setTaskName((String) workItem.getParameter(TASK_NAME));
        workItem.setTaskDescription((String) workItem.getParameter(DESCRIPTION));
        workItem.setTaskPriority((String) workItem.getParameter(PRIORITY));
        workItem.setReferenceName((String) workItem.getParameter(NODE_NAME));
        return workItem;
    }

    @Override
    protected void addWorkItemListener() {
        super.addWorkItemListener();
        getProcessInstance().addEventListener(WORK_ITEM_TRANSITION, this, false);
    }

    @Override
    protected void removeWorkItemListener() {
        super.removeWorkItemListener();
        getProcessInstance().removeEventListener(WORK_ITEM_TRANSITION, this, false);
    }

    protected String assignWorkItem(InternalKogitoWorkItem workItem) {
        String actorId = null;
        // if this human task node is part of a swimlane, check whether an actor
        // has already been assigned to this swimlane
        String swimlaneName = getHumanTaskNode().getSwimlane();
        SwimlaneContextInstance swimlaneContextInstance = getSwimlaneContextInstance(swimlaneName);
        if (swimlaneContextInstance != null) {
            actorId = swimlaneContextInstance.getActorId(swimlaneName);
            workItem.setParameter("SwimlaneActorId", actorId);
        }
        // if no actor can be assigned based on the swimlane, check whether an
        // actor is specified for this human task
        if (actorId == null) {
            actorId = (String) workItem.getParameter(ACTOR_ID);
            if (actorId != null && swimlaneContextInstance != null && actorId.split(separator).length == 1) {
                swimlaneContextInstance.setActorId(swimlaneName, actorId);
                workItem.setParameter("SwimlaneActorId", actorId);
            }
        }

        InternalHumanTaskWorkItem hunanWorkItem = (InternalHumanTaskWorkItem) workItem;

        Set<String> newPUValue = new HashSet<>(hunanWorkItem.getPotentialUsers());
        processAssigment(ACTOR_ID, workItem, newPUValue);
        hunanWorkItem.setPotentialUsers(newPUValue);

        Set<String> newPGValue = new HashSet<>(hunanWorkItem.getPotentialGroups());
        processAssigment(GROUP_ID, workItem, newPGValue);
        hunanWorkItem.setPotentialGroups(newPGValue);

        Set<String> newEUValue = new HashSet<>(hunanWorkItem.getExcludedUsers());
        processAssigment(EXCLUDED_OWNER_ID, workItem, newEUValue);
        hunanWorkItem.setExcludedUsers(newEUValue);

        Set<String> newAUValue = new HashSet<>(hunanWorkItem.getAdminUsers());
        processAssigment(BUSINESSADMINISTRATOR_ID, workItem, newAUValue);
        hunanWorkItem.setAdminGroups(newAUValue);

        Set<String> newAGValue = new HashSet<>(hunanWorkItem.getAdminGroups());
        processAssigment(BUSINESSADMINISTRATOR_GROUP_ID, workItem, newAGValue);
        hunanWorkItem.setAdminGroups(newAGValue);

        // always return ActorId from workitem as SwimlaneActorId is kept as separate parameter
        return (String) workItem.getParameter(ACTOR_ID);
    }

    private SwimlaneContextInstance getSwimlaneContextInstance(String swimlaneName) {
        if (this.swimlaneContextInstance == null) {
            if (swimlaneName == null) {
                return null;
            }
            SwimlaneContextInstance swimlaneContextInstance =
                    (SwimlaneContextInstance) resolveContextInstance(
                            SwimlaneContext.SWIMLANE_SCOPE, swimlaneName);
            if (swimlaneContextInstance == null) {
                throw new IllegalArgumentException(
                        "Could not find swimlane context instance");
            }
            this.swimlaneContextInstance = swimlaneContextInstance;
        }
        return this.swimlaneContextInstance;
    }

    @Override
    public void triggerCompleted(InternalKogitoWorkItem workItem) {
        String swimlaneName = getHumanTaskNode().getSwimlane();
        SwimlaneContextInstance swimlaneContextInstance = getSwimlaneContextInstance(swimlaneName);
        if (swimlaneContextInstance != null) {
            String newActorId = (workItem instanceof HumanTaskWorkItem) ? ((HumanTaskWorkItem) workItem).getActualOwner() : (String) workItem.getParameter(ACTOR_ID);
            if (newActorId != null) {
                swimlaneContextInstance.setActorId(swimlaneName, newActorId);
            }
        }
        super.triggerCompleted(workItem);
    }

    protected void processAssigment(String type, InternalKogitoWorkItem workItem, Set<String> store) {
        String value = (String) workItem.getParameter(type);

        if (value != null) {
            for (String item : value.split(separator)) {
                store.add(item);
            }
        }
    }
}
