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
package org.kie.kogito.event.usertask;

import java.util.Map;

import org.kie.kogito.event.process.ProcessInstanceEventMetadata;

public class UserTaskInstanceStateDataEvent extends UserTaskInstanceDataEvent<UserTaskInstanceStateEventBody> {

    public UserTaskInstanceStateDataEvent() {
    }

    public UserTaskInstanceStateDataEvent(String source, String addons, String identity, Map<String, Object> metaData, UserTaskInstanceStateEventBody body) {
        super("UserTaskInstanceStateDataEvent",
                source,
                body,
                (String) metaData.get(UserTaskInstanceEventMetadata.USER_TASK_INSTANCE_ID_META_DATA),
                (String) metaData.get(UserTaskInstanceEventMetadata.USER_TASK_INSTANCE_STATE_META_DATA),
                (String) metaData.get(ProcessInstanceEventMetadata.PROCESS_INSTANCE_ID_META_DATA),
                (String) metaData.get(ProcessInstanceEventMetadata.PROCESS_VERSION_META_DATA),
                (String) metaData.get(ProcessInstanceEventMetadata.PARENT_PROCESS_INSTANCE_ID_META_DATA),
                (String) metaData.get(ProcessInstanceEventMetadata.ROOT_PROCESS_INSTANCE_ID_META_DATA),
                (String) metaData.get(ProcessInstanceEventMetadata.PROCESS_ID_META_DATA),
                (String) metaData.get(ProcessInstanceEventMetadata.ROOT_PROCESS_ID_META_DATA),
                (String) metaData.get(ProcessInstanceEventMetadata.PROCESS_INSTANCE_STATE_META_DATA),
                addons,
                (String) metaData.get(ProcessInstanceEventMetadata.PROCESS_TYPE_META_DATA),
                (String) metaData.get(UserTaskInstanceEventMetadata.USER_TASK_INSTANCE_REFERENCE_ID_META_DATA),
                identity);

    }

}
