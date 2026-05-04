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

import org.junit.jupiter.api.Test;
import org.kie.kogito.usertask.lifecycle.UserTaskState;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTaskInfoTest {

    @Test
    public void testUserTaskInfoCreation() {
        UserTaskInfo info = new UserTaskInfo();
        info.setId("task-123");
        info.setUserTaskId("ut-456");
        info.setTaskName("hr_interview");
        info.setTaskDescription("Interview with HR");
        info.setTaskPriority("High");
        info.setStatus(UserTaskState.of("Reserved"));
        info.setActualOwner("recruiter");
        info.setProcessId("hiring");
        info.setProcessInstanceId("pi-789");
        info.setProcessVersion("1.0");

        assertThat(info.getId()).isEqualTo("task-123");
        assertThat(info.getUserTaskId()).isEqualTo("ut-456");
        assertThat(info.getTaskName()).isEqualTo("hr_interview");
        assertThat(info.getTaskDescription()).isEqualTo("Interview with HR");
        assertThat(info.getTaskPriority()).isEqualTo("High");
        assertThat(info.getStatus().getName()).isEqualTo("Reserved");
        assertThat(info.getActualOwner()).isEqualTo("recruiter");
        assertThat(info.getProcessId()).isEqualTo("hiring");
        assertThat(info.getProcessInstanceId()).isEqualTo("pi-789");
        assertThat(info.getProcessVersion()).isEqualTo("1.0");
    }

    @Test
    public void testUserTaskInfoWithNullValues() {
        UserTaskInfo info = new UserTaskInfo();
        info.setId("task-123");
        info.setUserTaskId("ut-456");
        info.setTaskName("hr_interview");
        info.setProcessId("hiring");
        info.setProcessInstanceId("pi-789");

        assertThat(info.getId()).isEqualTo("task-123");
        assertThat(info.getUserTaskId()).isEqualTo("ut-456");
        assertThat(info.getTaskName()).isEqualTo("hr_interview");
        assertThat(info.getTaskDescription()).isNull();
        assertThat(info.getTaskPriority()).isNull();
        assertThat(info.getStatus()).isNull();
        assertThat(info.getActualOwner()).isNull();
        assertThat(info.getProcessId()).isEqualTo("hiring");
        assertThat(info.getProcessInstanceId()).isEqualTo("pi-789");
        assertThat(info.getProcessVersion()).isNull();
    }

    @Test
    public void testUserTaskInfoMinimalData() {
        UserTaskInfo info = new UserTaskInfo();
        info.setId("task-123");
        info.setTaskName("task");
        info.setProcessId("process");
        info.setProcessInstanceId("instance");

        assertThat(info.getId()).isEqualTo("task-123");
        assertThat(info.getTaskName()).isEqualTo("task");
        assertThat(info.getProcessId()).isEqualTo("process");
        assertThat(info.getProcessInstanceId()).isEqualTo("instance");
    }

    @Test
    public void testUserTaskInfoEquality() {
        UserTaskInfo info1 = new UserTaskInfo();
        info1.setId("task-123");
        info1.setTaskName("hr_interview");
        info1.setProcessId("hiring");

        UserTaskInfo info2 = new UserTaskInfo();
        info2.setId("task-123");
        info2.setTaskName("hr_interview");
        info2.setProcessId("hiring");

        assertThat(info1.getId()).isEqualTo(info2.getId());
        assertThat(info1.getTaskName()).isEqualTo(info2.getTaskName());
        assertThat(info1.getProcessId()).isEqualTo(info2.getProcessId());
    }

    @Test
    public void testUserTaskInfoDifferentIds() {
        UserTaskInfo info1 = new UserTaskInfo();
        info1.setId("task-123");
        info1.setTaskName("hr_interview");

        UserTaskInfo info2 = new UserTaskInfo();
        info2.setId("task-456");
        info2.setTaskName("hr_interview");

        assertThat(info1.getId()).isNotEqualTo(info2.getId());
        assertThat(info1.getTaskName()).isEqualTo(info2.getTaskName());
    }

    @Test
    public void testUserTaskInfoStatusUpdate() {
        UserTaskInfo info = new UserTaskInfo();
        info.setId("task-123");
        info.setStatus(UserTaskState.of("Ready"));

        assertThat(info.getStatus().getName()).isEqualTo("Ready");

        info.setStatus(UserTaskState.of("Reserved"));
        assertThat(info.getStatus().getName()).isEqualTo("Reserved");

        info.setStatus(UserTaskState.of("InProgress"));
        assertThat(info.getStatus().getName()).isEqualTo("InProgress");
    }

    @Test
    public void testUserTaskInfoOwnerUpdate() {
        UserTaskInfo info = new UserTaskInfo();
        info.setId("task-123");
        info.setActualOwner(null);

        assertThat(info.getActualOwner()).isNull();

        info.setActualOwner("user1");
        assertThat(info.getActualOwner()).isEqualTo("user1");

        info.setActualOwner("user2");
        assertThat(info.getActualOwner()).isEqualTo("user2");
    }
}
