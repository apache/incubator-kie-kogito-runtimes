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
package org.kie.kogito.usertask.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.auth.IdentityProvider;
import org.kie.kogito.usertask.UserTaskFilter;
import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.lifecycle.UserTaskState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InMemoryUserTaskInstancesFilterTest {

    private InMemoryUserTaskInstances instances;
    private IdentityProvider identity;

    @BeforeEach
    public void setup() {
        instances = new InMemoryUserTaskInstances();
        instances.setReconnectUserTaskInstance(task -> task);
        instances.setDisconnectUserTaskInstance(task -> task);
        identity = mock(IdentityProvider.class);
    }

    @Test
    public void testFindByIdentityAndFilterWithEmptyFilter() {
        when(identity.getName()).thenReturn("recruiter");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        // Create test tasks
        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "recruiter", "hiring", "pi1", "Reserved");
        DefaultUserTaskInstance task2 = createTask("task2", "it_interview", "developer", "hiring", "pi2", "Reserved");

        instances.create(task1);
        instances.create(task2);

        UserTaskFilter filter = UserTaskFilter.builder().build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        // Should return only recruiter's tasks
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("task1");
    }

    @Test
    public void testFindByIdentityAndFilterWithProcessId() {
        when(identity.getName()).thenReturn("recruiter");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "recruiter", "hiring", "pi1", "Reserved");
        DefaultUserTaskInstance task2 = createTask("task2", "hr_review", "recruiter", "onboarding", "pi2", "Reserved");

        instances.create(task1);
        instances.create(task2);

        UserTaskFilter filter = UserTaskFilter.builder()
                .processId("hiring")
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("task1");
        assertThat(result.get(0).getProcessInfo().getProcessId()).isEqualTo("hiring");
    }

    @Test
    public void testFindByIdentityAndFilterWithProcessInstanceId() {
        when(identity.getName()).thenReturn("recruiter");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "recruiter", "hiring", "pi1", "Reserved");
        DefaultUserTaskInstance task2 = createTask("task2", "hr_review", "recruiter", "hiring", "pi2", "Reserved");

        instances.create(task1);
        instances.create(task2);

        UserTaskFilter filter = UserTaskFilter.builder()
                .processInstanceId("pi1")
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("task1");
        assertThat(result.get(0).getProcessInfo().getProcessInstanceId()).isEqualTo("pi1");
    }

    @Test
    public void testFindByIdentityAndFilterWithStatus() {
        when(identity.getName()).thenReturn("recruiter");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "recruiter", "hiring", "pi1", "Reserved");
        DefaultUserTaskInstance task2 = createTask("task2", "hr_review", "recruiter", "hiring", "pi2", "InProgress");

        instances.create(task1);
        instances.create(task2);

        UserTaskFilter filter = UserTaskFilter.builder()
                .status(UserTaskState.of("Reserved"))
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("task1");
        assertThat(result.get(0).getStatus().getName()).isEqualTo("Reserved");
    }

    @Test
    public void testFindByIdentityAndFilterWithTaskName() {
        when(identity.getName()).thenReturn("recruiter");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "recruiter", "hiring", "pi1", "Reserved");
        DefaultUserTaskInstance task2 = createTask("task2", "hr_review", "recruiter", "hiring", "pi2", "Reserved");

        instances.create(task1);
        instances.create(task2);

        UserTaskFilter filter = UserTaskFilter.builder()
                .taskName("hr_interview")
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("task1");
        assertThat(result.get(0).getTaskName()).isEqualTo("hr_interview");
    }

    @Test
    public void testFindByIdentityAndFilterWithMultipleFilters() {
        when(identity.getName()).thenReturn("recruiter");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "recruiter", "hiring", "pi1", "Reserved");
        DefaultUserTaskInstance task2 = createTask("task2", "hr_interview", "recruiter", "hiring", "pi2", "InProgress");
        DefaultUserTaskInstance task3 = createTask("task3", "hr_review", "recruiter", "hiring", "pi3", "Reserved");

        instances.create(task1);
        instances.create(task2);
        instances.create(task3);

        UserTaskFilter filter = UserTaskFilter.builder()
                .processId("hiring")
                .taskName("hr_interview")
                .status(UserTaskState.of("Reserved"))
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("task1");
    }

    @Test
    public void testFindByIdentityAndFilterWithNoMatches() {
        when(identity.getName()).thenReturn("recruiter");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "recruiter", "hiring", "pi1", "Reserved");
        instances.create(task1);

        UserTaskFilter filter = UserTaskFilter.builder()
                .taskName("it_interview")
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByIdentityAndFilterWithGroupAccess() {
        when(identity.getName()).thenReturn("user1");
        when(identity.getRoles()).thenReturn(Arrays.asList("IT"));

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "recruiter", "hiring", "pi1", "Reserved");
        task1.setPotentialGroups(Collections.singleton("IT"));
        DefaultUserTaskInstance task2 = createTask("task2", "it_interview", "developer", "hiring", "pi2", "Reserved");
        task2.setPotentialGroups(Collections.singleton("IT"));

        instances.create(task1);
        instances.create(task2);

        UserTaskFilter filter = UserTaskFilter.builder()
                .taskName("hr_interview")
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("task1");
    }

    @Test
    public void testFindByIdentityAndFilterAllFilters() {
        when(identity.getName()).thenReturn("recruiter");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "recruiter", "hiring", "pi1", "Reserved");
        DefaultUserTaskInstance task2 = createTask("task2", "hr_interview", "recruiter", "hiring", "pi2", "Reserved");

        instances.create(task1);
        instances.create(task2);

        UserTaskFilter filter = UserTaskFilter.builder()
                .processId("hiring")
                .processInstanceId("pi1")
                .status(UserTaskState.of("Reserved"))
                .taskName("hr_interview")
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("task1");
    }

    @Test
    public void testFindByIdentityAndFilterWithMultipleStatuses() {
        when(identity.getName()).thenReturn("recruiter");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "recruiter", "hiring", "pi1", "Reserved");
        DefaultUserTaskInstance task2 = createTask("task2", "hr_review", "recruiter", "hiring", "pi2", "InProgress");
        DefaultUserTaskInstance task3 = createTask("task3", "hr_final", "recruiter", "hiring", "pi3", "Completed");
        DefaultUserTaskInstance task4 = createTask("task4", "hr_other", "recruiter", "hiring", "pi4", "Suspended");

        instances.create(task1);
        instances.create(task2);
        instances.create(task3);
        instances.create(task4);

        UserTaskFilter filter = UserTaskFilter.builder()
                .statuses(Arrays.asList(
                        UserTaskState.of("Reserved"),
                        UserTaskState.of("InProgress"),
                        UserTaskState.of("Completed")))
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(UserTaskInstance::getId)
                .containsExactlyInAnyOrder("task1", "task2", "task3");
    }

    @Test
    public void testFindByIdentityAndFilterWithSingleStatusUsingStatusesMethod() {
        when(identity.getName()).thenReturn("recruiter");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "recruiter", "hiring", "pi1", "Reserved");
        DefaultUserTaskInstance task2 = createTask("task2", "hr_review", "recruiter", "hiring", "pi2", "InProgress");

        instances.create(task1);
        instances.create(task2);

        // Test backward compatibility: single status via statuses() method
        UserTaskFilter filter = UserTaskFilter.builder()
                .statuses(Collections.singletonList(UserTaskState.of("Reserved")))
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("task1");
        assertThat(result.get(0).getStatus().getName()).isEqualTo("Reserved");
    }

    @Test
    public void testFilterByStatusCaseInsensitive() {
        // Given - Create tasks with different statuses
        when(identity.getName()).thenReturn("john");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "john", "hiring", "proc1", "Reserved");
        DefaultUserTaskInstance task2 = createTask("task2", "it_interview", "john", "hiring", "proc2", "Ready");
        instances.create(task1);
        instances.create(task2);

        // When - Filter with lowercase status
        UserTaskFilter filter = UserTaskFilter.builder()
                .status(UserTaskState.of("reserved"))
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        // Then - Should match case-insensitively
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("task1");
        assertThat(result.get(0).getStatus().getName()).isEqualTo("Reserved");
    }

    @Test
    public void testFilterByStatusUpperCase() {
        // Given - Create tasks with different statuses
        when(identity.getName()).thenReturn("john");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "john", "hiring", "proc1", "Reserved");
        DefaultUserTaskInstance task2 = createTask("task2", "it_interview", "john", "hiring", "proc2", "Ready");
        instances.create(task1);
        instances.create(task2);

        // When - Filter with uppercase status
        UserTaskFilter filter = UserTaskFilter.builder()
                .status(UserTaskState.of("RESERVED"))
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        // Then - Should match case-insensitively
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("task1");
        assertThat(result.get(0).getStatus().getName()).isEqualTo("Reserved");
    }

    @Test
    public void testFilterByStatusMixedCase() {
        // Given - Create tasks with different statuses
        when(identity.getName()).thenReturn("john");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "john", "hiring", "proc1", "Reserved");
        DefaultUserTaskInstance task2 = createTask("task2", "it_interview", "john", "hiring", "proc2", "Ready");
        instances.create(task1);
        instances.create(task2);

        // When - Filter with mixed case status
        UserTaskFilter filter = UserTaskFilter.builder()
                .status(UserTaskState.of("ReSeRvEd"))
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        // Then - Should match case-insensitively
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("task1");
        assertThat(result.get(0).getStatus().getName()).isEqualTo("Reserved");
    }

    @Test
    public void testFilterByMultipleStatusesCaseInsensitive() {
        // Given - Create tasks with different statuses
        when(identity.getName()).thenReturn("john");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "john", "hiring", "proc1", "Reserved");
        DefaultUserTaskInstance task2 = createTask("task2", "it_interview", "john", "hiring", "proc2", "Ready");
        DefaultUserTaskInstance task3 = createTask("task3", "tech_interview", "john", "hiring", "proc3", "InProgress");
        instances.create(task1);
        instances.create(task2);
        instances.create(task3);

        // When - Filter with multiple statuses in different cases
        UserTaskFilter filter = UserTaskFilter.builder()
                .statuses(Arrays.asList(
                        UserTaskState.of("reserved"),
                        UserTaskState.of("READY")))
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        // Then - Should match both statuses case-insensitively
        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserTaskInstance::getId).containsExactlyInAnyOrder("task1", "task2");
    }

    @Test
    public void testFindByIdentityAndFilterWithTaskNameDoesNotMatchPartialValue() {
        when(identity.getName()).thenReturn("recruiter");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "recruiter", "hiring", "pi1", "Reserved");
        instances.create(task1);

        UserTaskFilter filter = UserTaskFilter.builder()
                .taskName("interview")
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByIdentityAndFilterWithTaskNameIsCaseSensitive() {
        when(identity.getName()).thenReturn("recruiter");
        when(identity.getRoles()).thenReturn(Collections.emptyList());

        DefaultUserTaskInstance task1 = createTask("task1", "hr_interview", "recruiter", "hiring", "pi1", "Reserved");
        instances.create(task1);

        UserTaskFilter filter = UserTaskFilter.builder()
                .taskName("HR_INTERVIEW")
                .build();
        List<UserTaskInstance> result = instances.findByIdentity(identity, filter);

        assertThat(result).isEmpty();
    }

    private DefaultUserTaskInstance createTask(String id, String taskName, String owner,
            String processId, String processInstanceId, String status) {
        DefaultUserTaskInstance task = new DefaultUserTaskInstance();
        task.setId(id);
        task.setTaskName(taskName);
        task.setActualOwner(owner);
        task.setProcessInfo(org.kie.kogito.usertask.model.ProcessInfo.builder()
                .withProcessId(processId)
                .withProcessInstanceId(processInstanceId)
                .build());
        task.setStatus(UserTaskState.of(status));
        task.setPotentialUsers(Collections.singleton(owner));
        return task;
    }
}
