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
package org.kie.kogito.usertask.view;

import java.util.Objects;

import org.kie.kogito.usertask.lifecycle.UserTaskState;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Lightweight data transfer object for user task list operations.
 * Contains only essential task information without inputs, outputs, or metadata.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserTaskInfoView {

    private String id;
    private String userTaskId;
    private String taskName;
    private String taskDescription;
    private String taskPriority;
    private UserTaskState status;
    private String actualOwner;

    private String processId;
    private String processInstanceId;
    private String processVersion;

    public UserTaskInfoView() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserTaskId() {
        return userTaskId;
    }

    public void setUserTaskId(String userTaskId) {
        this.userTaskId = userTaskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskPriority() {
        return taskPriority;
    }

    public void setTaskPriority(String taskPriority) {
        this.taskPriority = taskPriority;
    }

    public UserTaskState getStatus() {
        return status;
    }

    public void setStatus(UserTaskState status) {
        this.status = status;
    }

    public String getActualOwner() {
        return actualOwner;
    }

    public void setActualOwner(String actualOwner) {
        this.actualOwner = actualOwner;
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

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserTaskInfoView that = (UserTaskInfoView) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userTaskId, that.userTaskId) &&
                Objects.equals(taskName, that.taskName) &&
                Objects.equals(taskDescription, that.taskDescription) &&
                Objects.equals(taskPriority, that.taskPriority) &&
                Objects.equals(status, that.status) &&
                Objects.equals(actualOwner, that.actualOwner) &&
                Objects.equals(processId, that.processId) &&
                Objects.equals(processInstanceId, that.processInstanceId) &&
                Objects.equals(processVersion, that.processVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userTaskId, taskName, taskDescription, taskPriority, status, actualOwner, processId, processInstanceId, processVersion);
    }
}
