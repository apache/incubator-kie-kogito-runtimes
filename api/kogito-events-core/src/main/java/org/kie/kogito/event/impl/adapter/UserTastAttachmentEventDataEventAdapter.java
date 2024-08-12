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
package org.kie.kogito.event.impl.adapter;

import java.util.Map;

import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.usertask.UserTaskInstanceAttachmentDataEvent;
import org.kie.kogito.event.usertask.UserTaskInstanceAttachmentEventBody;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcessInstance;
import org.kie.kogito.usertask.HumanTaskWorkItem;
import org.kie.kogito.usertask.events.UserTaskAttachmentEvent;

public class UserTastAttachmentEventDataEventAdapter extends AbstractDataEventAdapter {

    public UserTastAttachmentEventDataEventAdapter() {
        super(UserTaskAttachmentEvent.class);
    }

    @Override
    public DataEvent<?> adapt(Object payload) {
        UserTaskAttachmentEvent event = (UserTaskAttachmentEvent) payload;
        Map<String, Object> metadata = AdapterHelper.buildUserTaskMetadata((HumanTaskWorkItem) event.getWorkItem());
        metadata.putAll(AdapterHelper.buildProcessMetadata((KogitoWorkflowProcessInstance) event.getProcessInstance()));
        KogitoWorkflowProcessInstance pi = (KogitoWorkflowProcessInstance) event.getProcessInstance();

        int eventType = UserTaskInstanceAttachmentEventBody.EVENT_TYPE_ADDED;
        if (event.getOldAttachment() != null && event.getNewAttachment() == null) {
            eventType = UserTaskInstanceAttachmentEventBody.EVENT_TYPE_DELETED;
        } else if (event.getOldAttachment() != null && event.getNewAttachment() != null) {
            eventType = UserTaskInstanceAttachmentEventBody.EVENT_TYPE_CHANGE;
        }

        UserTaskInstanceAttachmentEventBody.Builder builder = UserTaskInstanceAttachmentEventBody.create()
                .eventType(eventType)
                .userTaskDefinitionId(event.getUserTaskDefinitionId())
                .userTaskInstanceId(((HumanTaskWorkItem) event.getWorkItem()).getStringId())
                .userTaskName(((HumanTaskWorkItem) event.getWorkItem()).getTaskName());

        String updatedBy = null;
        switch (eventType) {
            case UserTaskInstanceAttachmentEventBody.EVENT_TYPE_ADDED:
            case UserTaskInstanceAttachmentEventBody.EVENT_TYPE_CHANGE:
                builder.attachmentName(event.getNewAttachment().getAttachmentName())
                        .attachmentId(event.getNewAttachment().getAttachmentId())
                        .attachmentURI(event.getNewAttachment().getAttachmentURI())
                        .eventDate(event.getNewAttachment().getUpdatedAt())
                        .eventUser(event.getNewAttachment().getUpdatedBy());
                updatedBy = event.getNewAttachment().getUpdatedBy();

                break;
            case UserTaskInstanceAttachmentEventBody.EVENT_TYPE_DELETED:
                builder.attachmentId(event.getOldAttachment().getAttachmentId())
                        .eventDate(event.getOldAttachment().getUpdatedAt())
                        .eventUser(event.getOldAttachment().getUpdatedBy());
                updatedBy = event.getOldAttachment().getUpdatedBy();
                break;
        }

        UserTaskInstanceAttachmentEventBody body = builder.build();
        UserTaskInstanceAttachmentDataEvent utEvent = new UserTaskInstanceAttachmentDataEvent(AdapterHelper.buildSource(getConfig().service(), event.getProcessInstance().getProcessId()),
                getConfig().addons().toString(), updatedBy, metadata, body);
        utEvent.setKogitoBusinessKey(pi.getBusinessKey());
        return utEvent;
    }

}
