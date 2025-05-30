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
package org.kie.kogito.jobs.descriptors;

import java.util.Map;
import java.util.UUID;

import org.kie.kogito.jobs.ExpirationTime;

public class UserTaskInstanceJobDescriptionBuilder {

    private String id;
    private ExpirationTime expirationTime;
    private Integer priority = ProcessInstanceJobDescription.DEFAULT_PRIORITY;
    private String userTaskInstanceId;
    private Map<String, Object> metadata;

    public UserTaskInstanceJobDescriptionBuilder id(String id) {
        this.id = id;
        return this;
    }

    public UserTaskInstanceJobDescriptionBuilder generateId() {
        return id(UUID.randomUUID().toString());
    }

    public UserTaskInstanceJobDescriptionBuilder expirationTime(ExpirationTime expirationTime) {
        this.expirationTime = expirationTime;
        return this;
    }

    public UserTaskInstanceJobDescriptionBuilder priority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public UserTaskInstanceJobDescriptionBuilder userTaskInstanceId(String userTaskInstanceId) {
        this.userTaskInstanceId = userTaskInstanceId;
        return this;
    }

    public UserTaskInstanceJobDescriptionBuilder metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public UserTaskInstanceJobDescription build() {
        return new UserTaskInstanceJobDescription(
                id,
                expirationTime,
                priority,
                userTaskInstanceId,
                (String) this.metadata.get("ProcessId"),
                (String) this.metadata.get("ProcessInstanceId"),
                (String) this.metadata.get("NodeInstanceId"),
                (String) this.metadata.get("RootProcessInstanceId"),
                (String) this.metadata.get("RootProcessId"));
    }
}
