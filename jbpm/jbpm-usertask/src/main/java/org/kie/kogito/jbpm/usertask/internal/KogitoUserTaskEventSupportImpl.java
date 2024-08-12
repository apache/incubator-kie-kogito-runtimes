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
package org.kie.kogito.jbpm.usertask.internal;

import java.util.Map;
import java.util.Set;

import org.kie.api.runtime.KieRuntime;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.usertask.event.KogitoUserTaskEventSupport;
import org.kie.kogito.usertask.Attachment;
import org.kie.kogito.usertask.Comment;
import org.kie.kogito.usertask.HumanTaskWorkItem;
import org.kie.kogito.usertask.UserTaskEventListener;

public class KogitoUserTaskEventSupportImpl implements KogitoUserTaskEventSupport {

    @Override
    public void fireOneUserTaskStateChange(KogitoProcessInstance instance, KogitoNodeInstance nodeInstance, KieRuntime kruntime, String oldPhaseStatus, String newPhaseStatus) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fireOnUserTaskNotStartedDeadline(KogitoProcessInstance instance, KogitoNodeInstance nodeInstance, HumanTaskWorkItem workItem, Map<String, Object> notification, KieRuntime kruntime) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fireOnUserTaskNotCompletedDeadline(KogitoProcessInstance instance, KogitoNodeInstance nodeInstance, HumanTaskWorkItem workItem, Map<String, Object> notification, KieRuntime kruntime) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fireOnUserTaskAssignmentChange(KogitoProcessInstance instance, KogitoNodeInstance nodeInstance, KieRuntime kruntime, AssignmentType assignmentType, Set<String> oldUsersId,
            Set<String> newUsersId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fireOnUserTaskInputVariableChange(KogitoProcessInstance instance, KogitoNodeInstance nodeInstance, KieRuntime kruntime, String variableName, Object newValue, Object oldValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fireOnUserTaskOutputVariableChange(KogitoProcessInstance instance, KogitoNodeInstance nodeInstance, KieRuntime kruntime, String variableName, Object newValue, Object oldValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fireOnUserTaskAttachmentAdded(KogitoProcessInstance instance, KogitoNodeInstance nodeInstance, KieRuntime kruntime, Attachment addedAttachment) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fireOnUserTaskAttachmentDeleted(KogitoProcessInstance instance, KogitoNodeInstance nodeInstance, KieRuntime kruntime, Attachment deletedAttachment) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fireOnUserTaskAttachmentChange(KogitoProcessInstance instance, KogitoNodeInstance nodeInstance, KieRuntime kruntime, Attachment oldAttachment, Attachment newAttachment) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fireOnUserTaskCommentChange(KogitoProcessInstance instance, KogitoNodeInstance nodeInstance, KieRuntime kruntime, Comment oldComment, Comment newComment) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fireOnUserTaskCommentDeleted(KogitoProcessInstance instance, KogitoNodeInstance nodeInstance, KieRuntime kruntime, Comment deletedComment) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fireOnUserTaskCommentAdded(KogitoProcessInstance instance, KogitoNodeInstance nodeInstance, KieRuntime kruntime, Comment addedComment) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

    @Override
    public void addEventListener(UserTaskEventListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeEventListener(UserTaskEventListener listener) {
        // TODO Auto-generated method stub

    }
    //
    //    private List<UserTaskEventListener> listeners;
    //
    //    private IdentityProvider identityProvider;
    //
    //    /**
    //     * Do not use this constructor. It should be used just by deserialization.
    //     */
    //    public KogitoUserTaskEventSupportImpl(IdentityProvider identityProvider) {
    //        this.identityProvider = identityProvider;
    //        this.listeners = new CopyOnWriteArrayList<>();
    //    }
    //
    //    private void notifyAllListeners(Consumer<UserTaskEventListener> consumer) {
    //        this.listeners.forEach(consumer::accept);
    //    }
    //
    //    // users tasks events
    //    @Override
    //    public void fireOnUserTaskNotStartedDeadline(KogitoProcessInstance instance,
    //            KogitoNodeInstance nodeInstance,
    //            HumanTaskWorkItem workItem,
    //            Map<String, Object> notification,
    //            KieRuntime kruntime) {
    //        fireUserTaskNotification(instance, nodeInstance, workItem, notification, DeadlineType.Started, kruntime);
    //    }
    //
    //    @Override
    //    public void fireOnUserTaskNotCompletedDeadline(KogitoProcessInstance instance,
    //            KogitoNodeInstance nodeInstance,
    //            HumanTaskWorkItem workItem,
    //            Map<String, Object> notification,
    //            KieRuntime kruntime) {
    //        fireUserTaskNotification(instance, nodeInstance, workItem, notification, DeadlineType.Completed, kruntime);
    //    }
    //
    //    private void fireUserTaskNotification(KogitoProcessInstance instance,
    //            KogitoNodeInstance nodeInstance,
    //            HumanTaskWorkItem workItem,
    //            Map<String, Object> notification,
    //            DeadlineType type,
    //            KieRuntime kruntime) {
    //        UserTaskDeadlineEvent event = new UserTaskDeadlineEventImpl(instance, (HumanTaskNodeInstance) nodeInstance, workItem, notification, type, kruntime, identityProvider.getName());
    //        notifyAllListeners(l -> l.onUserTaskDeadline(event));
    //    }
    //
    //    @Override
    //    public void fireOneUserTaskStateChange(
    //            KogitoProcessInstance instance,
    //            KogitoNodeInstance nodeInstance,
    //            KieRuntime kruntime,
    //            String oldStatus, String newStatus) {
    //        UserTaskStateEventImpl event = new UserTaskStateEventImpl(instance, (HumanTaskNodeInstance) nodeInstance, kruntime, identityProvider.getName());
    //        event.setOldStatus(oldStatus);
    //        event.setNewStatus(newStatus);
    //        notifyAllListeners(l -> l.onUserTaskState(event));
    //    }
    //
    //    @Override
    //    public void fireOnUserTaskAssignmentChange(
    //            KogitoProcessInstance instance,
    //            KogitoNodeInstance nodeInstance,
    //            KieRuntime kruntime,
    //            AssignmentType assignmentType,
    //            Set<String> oldUsersId, Set<String> newUsersId) {
    //        UserTaskAssignmentEventImpl event = new UserTaskAssignmentEventImpl(instance, (HumanTaskNodeInstance) nodeInstance, kruntime, identityProvider.getName());
    //        event.setAssignmentType(assignmentType.name());
    //        event.setOldUsersId(oldUsersId);
    //        event.setNewUsersId(newUsersId);
    //        notifyAllListeners(l -> l.onUserTaskAssignment(event));
    //    }
    //
    //    @Override
    //    public void fireOnUserTaskInputVariableChange(
    //            KogitoProcessInstance instance,
    //            KogitoNodeInstance nodeInstance,
    //            KieRuntime kruntime,
    //            String variableName, Object newValue, Object oldValue) {
    //        UserTaskVariableEventImpl event = new UserTaskVariableEventImpl(instance, (HumanTaskNodeInstance) nodeInstance, kruntime, identityProvider.getName());
    //        event.setVariableName(variableName);
    //        event.setOldValue(oldValue);
    //        event.setNewValue(newValue);
    //        event.setVariableType(VariableEventType.INPUT);
    //        notifyAllListeners(l -> l.onUserTaskInputVariable(event));
    //    }
    //
    //    @Override
    //    public void fireOnUserTaskOutputVariableChange(
    //            KogitoProcessInstance instance,
    //            KogitoNodeInstance nodeInstance,
    //            KieRuntime kruntime,
    //            String variableName, Object newValue, Object oldValue) {
    //        UserTaskVariableEventImpl event = new UserTaskVariableEventImpl(instance, (HumanTaskNodeInstance) nodeInstance, kruntime, identityProvider.getName());
    //        event.setVariableName(variableName);
    //        event.setOldValue(oldValue);
    //        event.setNewValue(newValue);
    //        event.setVariableType(VariableEventType.OUTPUT);
    //        notifyAllListeners(l -> l.onUserTaskOutputVariable(event));
    //    }
    //
    //    @Override
    //    public void fireOnUserTaskAttachmentAdded(
    //            KogitoProcessInstance instance,
    //            KogitoNodeInstance nodeInstance,
    //            KieRuntime kruntime,
    //            Attachment addedAttachment) {
    //
    //        UserTaskAttachmentEventImpl event = new UserTaskAttachmentEventImpl(instance, (HumanTaskNodeInstance) nodeInstance, kruntime, identityProvider.getName());
    //        event.setNewAttachment(addedAttachment);
    //        notifyAllListeners(l -> l.onUserTaskAttachmentAdded(event));
    //    }
    //
    //    @Override
    //    public void fireOnUserTaskAttachmentChange(
    //            KogitoProcessInstance instance,
    //            KogitoNodeInstance nodeInstance,
    //            KieRuntime kruntime,
    //            Attachment oldAttachment, Attachment newAttachment) {
    //        UserTaskAttachmentEventImpl event = new UserTaskAttachmentEventImpl(instance, (HumanTaskNodeInstance) nodeInstance, kruntime, identityProvider.getName());
    //        event.setOldAttachment(oldAttachment);
    //        event.setNewAttachment(newAttachment);
    //        notifyAllListeners(l -> l.onUserTaskAttachmentChange(event));
    //    }
    //
    //    @Override
    //    public void fireOnUserTaskAttachmentDeleted(
    //            KogitoProcessInstance instance,
    //            KogitoNodeInstance nodeInstance,
    //            KieRuntime kruntime,
    //            Attachment deletedAttachment) {
    //        UserTaskAttachmentEventImpl event = new UserTaskAttachmentEventImpl(instance, (HumanTaskNodeInstance) nodeInstance, kruntime, identityProvider.getName());
    //        event.setOldAttachment(deletedAttachment);
    //        notifyAllListeners(l -> l.onUserTaskAttachmentDeleted(event));
    //    }
    //
    //    @Override
    //    public void fireOnUserTaskCommentAdded(
    //            KogitoProcessInstance instance,
    //            KogitoNodeInstance nodeInstance,
    //            KieRuntime kruntime,
    //            Comment addedComment) {
    //        UserTaskCommentEventImpl event = new UserTaskCommentEventImpl(instance, (HumanTaskNodeInstance) nodeInstance, kruntime, identityProvider.getName());
    //        event.setNewComment(addedComment);
    //        notifyAllListeners(l -> l.onUserTaskCommentAdded(event));
    //    }
    //
    //    @Override
    //    public void fireOnUserTaskCommentChange(
    //            KogitoProcessInstance instance,
    //            KogitoNodeInstance nodeInstance,
    //            KieRuntime kruntime,
    //            Comment oldComment, Comment newComment) {
    //        UserTaskCommentEventImpl event = new UserTaskCommentEventImpl(instance, (HumanTaskNodeInstance) nodeInstance, kruntime, identityProvider.getName());
    //        event.setOldComment(oldComment);
    //        event.setNewComment(newComment);
    //        notifyAllListeners(l -> l.onUserTaskCommentChange(event));
    //    }
    //
    //    @Override
    //    public void fireOnUserTaskCommentDeleted(
    //            KogitoProcessInstance instance,
    //            KogitoNodeInstance nodeInstance,
    //            KieRuntime kruntime,
    //            Comment deletedComment) {
    //        UserTaskCommentEventImpl event = new UserTaskCommentEventImpl(instance, (HumanTaskNodeInstance) nodeInstance, kruntime, identityProvider.getName());
    //        event.setOldComment(deletedComment);
    //        notifyAllListeners(l -> l.onUserTaskCommentDeleted(event));
    //    }
    //
    //    @Override
    //    public void reset() {
    //        this.listeners.clear();
    //    }
    //
    //    @Override
    //    public void addEventListener(UserTaskEventListener listener) {
    //        if (!this.listeners.contains(listener)) {
    //            this.listeners.add(listener);
    //        }
    //    }
    //
    //    @Override
    //    public void removeEventListener(UserTaskEventListener listener) {
    //        this.listeners.remove(listener);
    //    }

}
