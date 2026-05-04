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
package org.kie.kogito.usertask;

import java.util.List;

import org.kie.kogito.usertask.lifecycle.UserTaskState;

/**
 * Filter criteria for querying user tasks.
 * All filters are combined using AND logic.
 * Null filters are ignored (no filtering applied for that criterion).
 */
public class UserTaskFilter {

    private String processId;
    private String processInstanceId;
    private List<UserTaskState> statuses;
    private String taskName;

    public UserTaskFilter() {
    }

    private UserTaskFilter(Builder builder) {
        this.processId = builder.processId;
        this.processInstanceId = builder.processInstanceId;
        this.statuses = builder.statuses;
        this.taskName = builder.taskName;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public List<UserTaskState> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<UserTaskState> statuses) {
        this.statuses = statuses;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String processId;
        private String processInstanceId;
        private List<UserTaskState> statuses;
        private String taskName;
        private String actualOwner;

        public Builder processId(String processId) {
            this.processId = processId;
            return this;
        }

        public Builder processInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        public Builder statuses(List<UserTaskState> statuses) {
            this.statuses = statuses;
            return this;
        }

        public Builder status(UserTaskState status) {
            this.statuses = status != null ? List.of(status) : null;
            return this;
        }

        public Builder taskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder actualOwner(String actualOwner) {
            this.actualOwner = actualOwner;
            return this;
        }

        public UserTaskFilter build() {
            return new UserTaskFilter(this);
        }
    }
}
