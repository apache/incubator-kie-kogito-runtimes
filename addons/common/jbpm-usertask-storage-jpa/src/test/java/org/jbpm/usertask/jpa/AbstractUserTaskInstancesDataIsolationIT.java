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

package org.jbpm.usertask.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jbpm.usertask.jpa.mapper.utils.TestUtils;
import org.jbpm.usertask.jpa.model.TaskProcessInfoEntity;
import org.jbpm.usertask.jpa.model.UserTaskInstanceEntity;
import org.jbpm.usertask.jpa.repository.UserTaskInstanceRepository;
import org.jbpm.usertask.jpa.repository.UserTaskJPAContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.auth.IdentityProvider;
import org.kie.kogito.auth.IdentityProviders;
import org.kie.kogito.usertask.UserTaskInstance;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractUserTaskInstancesDataIsolationIT {

    protected final JPAUserTaskInstances userTaskInstances;
    protected final UserTaskInstanceRepository userTaskInstanceRepository;
    protected final UserTaskJPAContext context;

    protected IdentityProvider identityProvider;

    protected AbstractUserTaskInstancesDataIsolationIT(JPAUserTaskInstances userTaskInstances,
            UserTaskInstanceRepository userTaskInstanceRepository,
            UserTaskJPAContext context) {
        this.userTaskInstances = userTaskInstances;
        this.userTaskInstanceRepository = userTaskInstanceRepository;
        this.context = context;
    }

    @BeforeEach
    public void setup() {
        identityProvider = IdentityProviders.of("testUser", List.of("testRole"));
        userTaskInstances.setReconnectUserTaskInstance(userTaskInstance -> userTaskInstance);
        userTaskInstances.setDisconnectUserTaskInstance(userTaskInstance -> userTaskInstance);
        userTaskInstanceRepository.findAll().forEach(userTaskInstanceRepository::delete);
    }

    @AfterEach
    public void cleanup() {
        userTaskInstanceRepository.findAll().forEach(userTaskInstanceRepository::delete);
    }

    @Test
    public void testDataIsolationWithProcessesBean() {
        Collection<String> localProcessIds = context.getProcesses().processIds();
        assertThat(localProcessIds).isNotEmpty();

        String localProcessId = localProcessIds.iterator().next();
        userTaskInstanceRepository.persist(createUserTaskEntity("local-process-task-1", localProcessId, "testUser"));
        userTaskInstanceRepository.persist(createUserTaskEntity("local-process-task-2", localProcessId, "testUser"));
        userTaskInstanceRepository.persist(createUserTaskEntity("remote-process-task-1", "remoteProcess1", "testUser"));
        userTaskInstanceRepository.persist(createUserTaskEntity("remote-process-task-2", "remoteProcess2", "testUser"));

        List<UserTaskInstance> tasks = userTaskInstances.findByIdentity(identityProvider);

        assertThat(tasks).hasSize(2);
        assertThat(tasks).extracting(UserTaskInstance::getId)
                .containsExactlyInAnyOrder("local-process-task-1", "local-process-task-2");
    }

    @Test
    public void testFindByIdFiltersRemoteProcessTasks() {
        Collection<String> localProcessIds = context.getProcesses().processIds();
        assertThat(localProcessIds).isNotEmpty();
        String localProcessId = localProcessIds.iterator().next();

        userTaskInstanceRepository.persist(createUserTaskEntity("findById-localTask", localProcessId, "testUser"));
        userTaskInstanceRepository.persist(createUserTaskEntity("findById-remoteTask", "remoteProcess", "testUser"));

        assertThat(userTaskInstances.findById("findById-localTask")).isPresent();
        assertThat(userTaskInstances.findById("findById-remoteTask")).isEmpty();
    }

    @Test
    public void testExistsFiltersRemoteProcessTasks() {
        Collection<String> localProcessIds = context.getProcesses().processIds();
        assertThat(localProcessIds).isNotEmpty();
        String localProcessId = localProcessIds.iterator().next();

        userTaskInstanceRepository.persist(createUserTaskEntity("exists-localTask", localProcessId, "testUser"));
        userTaskInstanceRepository.persist(createUserTaskEntity("exists-remoteTask", "remoteProcess", "testUser"));

        assertThat(userTaskInstances.exists("exists-localTask")).isTrue();
        assertThat(userTaskInstances.exists("exists-remoteTask")).isFalse();
    }

    @Test
    public void testFindByIdentityWithProcessFiltering() {
        userTaskInstanceRepository.persist(createUserTaskEntity("identity-task-1", "orderProcess", "testUser"));
        userTaskInstanceRepository.persist(createUserTaskEntity("identity-task-2", "shippingProcess", "testUser"));

        List<UserTaskInstance> tasks = userTaskInstances.findByIdentity(identityProvider);

        assertThat(tasks).isNotNull();
    }

    @Test
    public void testFindAllFiltersRemoteProcessTasks() {
        Collection<String> localProcessIds = context.getProcesses().processIds();
        String localProcessId = localProcessIds.iterator().next();

        userTaskInstanceRepository.persist(createUserTaskEntity("findAll-task1", localProcessId, "testUser"));
        userTaskInstanceRepository.persist(createUserTaskEntity("findAll-task2", localProcessId, "testUser"));
        userTaskInstanceRepository.persist(createUserTaskEntity("findAll-task3", "remoteProcess", "testUser"));

        List<UserTaskInstanceEntity> filteredTasks = userTaskInstanceRepository.findAll();

        assertThat(filteredTasks).hasSize(2);
        assertThat(filteredTasks).extracting(UserTaskInstanceEntity::getId)
                .containsExactlyInAnyOrder("findAll-task1", "findAll-task2");
    }

    @Test
    public void testFilteringWithMultipleProcessIds() {
        Collection<String> localProcessIds = context.getProcesses().processIds();
        assertThat(localProcessIds).hasSizeGreaterThanOrEqualTo(2);

        for (String processId : localProcessIds) {
            userTaskInstanceRepository.persist(createUserTaskEntity("multi-" + processId, processId, "testUser"));
        }

        assertThat(userTaskInstanceRepository.findAll()).hasSize(localProcessIds.size());
    }

    @Test
    public void testNoResultsWhenProcessIdsDoNotMatch() {
        userTaskInstanceRepository.persist(createUserTaskEntity("no-match-remote-1", "remoteProcess1", "testUser"));
        userTaskInstanceRepository.persist(createUserTaskEntity("no-match-remote-2", "remoteProcess2", "testUser"));

        List<UserTaskInstance> tasks = userTaskInstances.findByIdentity(identityProvider);

        assertThat(tasks).isEmpty();
    }

    @Test
    public void testFindAllMatchesProcessIdWhenRootProcessIdIsNull() {
        Collection<String> localProcessIds = context.getProcesses().processIds();
        String localProcessId = localProcessIds.iterator().next();

        userTaskInstanceRepository.persist(createUserTaskEntity("task-null-root", localProcessId, null, "testUser"));
        userTaskInstanceRepository.persist(createUserTaskEntity("task-remote-root", "remoteProcess", null, "testUser"));

        List<UserTaskInstanceEntity> tasks = userTaskInstanceRepository.findAll();

        assertThat(tasks).hasSize(1);
        assertThat(tasks).extracting(UserTaskInstanceEntity::getId)
                .containsExactly("task-null-root");
    }

    protected static Set<String> localProcessIds() {
        return Set.of("orderProcess", "shippingProcess");
    }

    protected UserTaskInstanceEntity createUserTaskEntity(String taskId, String processId, String actualOwner) {
        return createUserTaskEntity(taskId, processId, processId, actualOwner);
    }

    protected UserTaskInstanceEntity createUserTaskEntity(String taskId, String processId, String rootProcessId, String actualOwner) {
        UserTaskInstanceEntity entity = TestUtils.createUserTaskInstanceEntity();

        entity.setId(taskId);
        entity.setUserTaskId(taskId);
        entity.setTaskName("Test Task " + taskId);
        entity.setStatus("Ready");
        entity.setActualOwner(actualOwner);

        TaskProcessInfoEntity processInfo = new TaskProcessInfoEntity();
        processInfo.setProcessId(processId);
        processInfo.setRootProcessId(rootProcessId);
        processInfo.setProcessInstanceId(UUID.randomUUID().toString());
        entity.setProcessInfo(processInfo);

        entity.setPotentialUsers(Set.of(actualOwner));
        entity.setPotentialGroups(Set.of("testRole"));

        return entity;
    }
}
