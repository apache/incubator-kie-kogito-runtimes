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

import java.util.Date;
import java.util.Map;

import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.usertask.UserTaskInstanceDeadlineDataEvent;
import org.kie.kogito.event.usertask.UserTaskInstanceDeadlineEventBody;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcessInstance;
import org.kie.kogito.usertask.HumanTaskWorkItem;
import org.kie.kogito.usertask.events.UserTaskDeadlineEvent;

public class UserTaskDeadlineEventDataEventAdapter extends AbstractDataEventAdapter {

    public UserTaskDeadlineEventDataEventAdapter() {
        super(UserTaskDeadlineEvent.class);
    }

    @Override
    public DataEvent<?> adapt(Object payload) {
        UserTaskDeadlineEvent event = (UserTaskDeadlineEvent) payload;
        Map<String, Object> metadata = AdapterHelper.buildUserTaskMetadata((HumanTaskWorkItem) event.getWorkItem());
        metadata.putAll(AdapterHelper.buildProcessMetadata((KogitoWorkflowProcessInstance) event.getProcessInstance()));
        KogitoWorkflowProcessInstance pi = (KogitoWorkflowProcessInstance) event.getProcessInstance();
        UserTaskInstanceDeadlineEventBody.Builder builder = UserTaskInstanceDeadlineEventBody.create()
                .eventDate(new Date())
                .eventUser(event.getEventUser())
                .userTaskDefinitionId(event.getUserTaskDefinitionId())
                .userTaskInstanceId(((HumanTaskWorkItem) event.getWorkItem()).getStringId())
                .userTaskName(((HumanTaskWorkItem) event.getWorkItem()).getTaskName())
                .notification(event.getNotification());

        UserTaskInstanceDeadlineEventBody body = builder.build();
        UserTaskInstanceDeadlineDataEvent utEvent =
                new UserTaskInstanceDeadlineDataEvent(AdapterHelper.buildSource(getConfig().service(), event.getProcessInstance().getProcessId()), getConfig().addons().toString(),
                        event.getEventUser(), metadata, body);
        utEvent.setKogitoBusinessKey(pi.getBusinessKey());
        return utEvent;
    }

}
