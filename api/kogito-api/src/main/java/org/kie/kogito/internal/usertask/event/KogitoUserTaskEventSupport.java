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
package org.kie.kogito.internal.usertask.event;

import java.util.Map;
import java.util.Set;

import org.kie.api.runtime.KieRuntime;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.usertask.event.KogitoUserTaskEventSupport.AssignmentType;
import org.kie.kogito.usertask.Attachment;
import org.kie.kogito.usertask.Comment;
import org.kie.kogito.usertask.HumanTaskWorkItem;
import org.kie.kogito.usertask.UserTaskEventListener;

public interface KogitoUserTaskEventSupport {
    enum AssignmentType {
        USER_OWNERS,
        USER_GROUPS,
        USERS_EXCLUDED,
        ADMIN_GROUPS,
        ADMIN_USERS
    };

    void fireOneUserTaskStateChange(
            KogitoProcessInstance instance,
            KogitoNodeInstance nodeInstance,
            KieRuntime kruntime,
            String oldPhaseStatus, String newPhaseStatus);

    void fireOnUserTaskNotStartedDeadline(
            KogitoProcessInstance instance,
            KogitoNodeInstance nodeInstance,
            HumanTaskWorkItem workItem,
            Map<String, Object> notification,
            KieRuntime kruntime);

    void fireOnUserTaskNotCompletedDeadline(
            KogitoProcessInstance instance,
            KogitoNodeInstance nodeInstance,
            HumanTaskWorkItem workItem,
            Map<String, Object> notification,
            KieRuntime kruntime);

    void fireOnUserTaskAssignmentChange(
            KogitoProcessInstance instance,
            KogitoNodeInstance nodeInstance,
            KieRuntime kruntime,
            AssignmentType assignmentType,
            Set<String> oldUsersId, Set<String> newUsersId);

    void fireOnUserTaskInputVariableChange(
            KogitoProcessInstance instance,
            KogitoNodeInstance nodeInstance,
            KieRuntime kruntime,
            String variableName,
            Object newValue, Object oldValue);

    void fireOnUserTaskOutputVariableChange(
            KogitoProcessInstance instance,
            KogitoNodeInstance nodeInstance,
            KieRuntime kruntime,
            String variableName,
            Object newValue, Object oldValue);

    void fireOnUserTaskAttachmentAdded(
            KogitoProcessInstance instance,
            KogitoNodeInstance nodeInstance,
            KieRuntime kruntime,
            Attachment addedAttachment);

    void fireOnUserTaskAttachmentDeleted(
            KogitoProcessInstance instance,
            KogitoNodeInstance nodeInstance,
            KieRuntime kruntime,
            Attachment deletedAttachment);

    void fireOnUserTaskAttachmentChange(
            KogitoProcessInstance instance,
            KogitoNodeInstance nodeInstance,
            KieRuntime kruntime,
            Attachment oldAttachment, Attachment newAttachment);

    void fireOnUserTaskCommentChange(
            KogitoProcessInstance instance,
            KogitoNodeInstance nodeInstance,
            KieRuntime kruntime,
            Comment oldComment, Comment newComment);

    void fireOnUserTaskCommentDeleted(
            KogitoProcessInstance instance,
            KogitoNodeInstance nodeInstance,
            KieRuntime kruntime,
            Comment deletedComment);

    void fireOnUserTaskCommentAdded(
            KogitoProcessInstance instance,
            KogitoNodeInstance nodeInstance,
            KieRuntime kruntime,
            Comment addedComment);

    void reset();

    void addEventListener(UserTaskEventListener listener);

    void removeEventListener(UserTaskEventListener listener);

}
