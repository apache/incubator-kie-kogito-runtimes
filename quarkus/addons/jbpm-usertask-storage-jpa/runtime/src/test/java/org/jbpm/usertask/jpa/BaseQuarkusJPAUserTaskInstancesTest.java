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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

import org.assertj.core.api.Assertions;
import org.jbpm.usertask.jpa.model.AttachmentEntity;
import org.jbpm.usertask.jpa.model.CommentEntity;
import org.jbpm.usertask.jpa.model.TaskMetadataEntity;
import org.jbpm.usertask.jpa.model.UserTaskInstanceEntity;
import org.jbpm.usertask.jpa.model.data.AbstractTaskDataEntity;
import org.jbpm.usertask.jpa.models.Person;
import org.jbpm.usertask.jpa.repository.AttachmentRepository;
import org.jbpm.usertask.jpa.repository.CommentRepository;
import org.jbpm.usertask.jpa.repository.QuarkusUserTaskJPAContext;
import org.jbpm.usertask.jpa.repository.UserTaskInstanceRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.auth.IdentityProvider;
import org.kie.kogito.auth.IdentityProviders;
import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.impl.DefaultUserTaskInstance;
import org.kie.kogito.usertask.lifecycle.UserTaskState;
import org.kie.kogito.usertask.model.Attachment;
import org.kie.kogito.usertask.model.Comment;
import org.mockito.Mockito;

import jakarta.inject.Inject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class BaseQuarkusJPAUserTaskInstancesTest {

    @Inject
    QuarkusUserTaskJPAContext context;

    @Inject
    JPAUserTaskInstances userTaskInstances;

    @Inject
    UserTaskInstanceRepository userTaskInstanceRepository;

    @Inject
    AttachmentRepository attachmentRepository;

    @Inject
    CommentRepository commentRepository;

    private Function<UserTaskInstance, UserTaskInstance> connect;
    private Function<UserTaskInstance, UserTaskInstance> disconnect;

    @BeforeEach
    public void init() {
        connect = Mockito.mock(Function.class);
        disconnect = Mockito.mock(Function.class);

        when(connect.apply(any(UserTaskInstance.class))).thenAnswer(i -> i.getArgument(0));
        when(disconnect.apply(any(UserTaskInstance.class))).thenAnswer(i -> i.getArgument(0));

        userTaskInstances.setReconnectUserTaskInstance(connect);
        userTaskInstances.setDisconnectUserTaskInstance(disconnect);
    }

    @Test
    public void testCreateUserTask() {
        UserTaskInstance instance = createUserTaskInstance();

        Assertions.assertThat(userTaskInstances.exists(instance.getId()))
                .isFalse();

        Assertions.assertThat(userTaskInstances.findById(instance.getId()))
                .isNotNull()
                .isEmpty();

        userTaskInstances.create(instance);

        verify(connect, times(1)).apply(any(UserTaskInstance.class));

        Optional<UserTaskInstanceEntity> entityOptional = userTaskInstanceRepository.findById(instance.getId());

        Assertions.assertThat(entityOptional)
                .isNotNull()
                .isPresent();

        UserTaskInstanceEntity entity = entityOptional.get();

        assertEntityAndInstance(entity, instance);

        Optional<UserTaskInstance> persistedInstanceOptional = userTaskInstances.findById(instance.getId());

        Assertions.assertThat(persistedInstanceOptional)
                .isNotNull()
                .isPresent();

        assertEntityAndInstance(entity, persistedInstanceOptional.get());

        userTaskInstances.remove(instance);

        Assertions.assertThat(attachmentRepository.findAll())
                .isEmpty();

        Assertions.assertThat(userTaskInstances.exists(instance.getId()))
                .isFalse();
    }

    @Test
    public void testFindByIdentityByActualOwner() {

        UserTaskInstance instance = createUserTaskInstance();

        userTaskInstances.create(instance);

        IdentityProvider identityProvider = IdentityProviders.of("Homer", "Group");

        List<UserTaskInstance> result = userTaskInstances.findByIdentity(identityProvider);

        Assertions.assertThat(result)
                .hasSize(1);

        verify(connect, times(2)).apply(any(UserTaskInstance.class));

        userTaskInstances.remove(instance);

        Assertions.assertThat(userTaskInstances.exists(instance.getId()))
                .isFalse();
    }

    @Test
    public void testFindByIdentityByPotentialOwners() {
        UserTaskInstance instance = createUserTaskInstance();

        userTaskInstances.create(instance);

        IdentityProvider identityProvider = IdentityProviders.of("Bart", "Group");

        List<UserTaskInstance> result = userTaskInstances.findByIdentity(identityProvider);

        Assertions.assertThat(result)
                .hasSize(1);

        verify(connect, times(2)).apply(any(UserTaskInstance.class));

        userTaskInstances.remove(instance);

        Assertions.assertThat(userTaskInstances.exists(instance.getId()))
                .isFalse();
    }

    @Test
    public void testFindByIdentityByPotentialGroups() {
        UserTaskInstance instance = createUserTaskInstance();

        userTaskInstances.create(instance);

        IdentityProvider identityProvider = IdentityProviders.of("Abraham", "Admin", "Simpson");

        List<UserTaskInstance> result = userTaskInstances.findByIdentity(identityProvider);

        Assertions.assertThat(result)
                .hasSize(1);

        verify(connect, times(2)).apply(any(UserTaskInstance.class));

        userTaskInstances.remove(instance);

        Assertions.assertThat(userTaskInstances.exists(instance.getId()))
                .isFalse();
    }

    @Test
    public void testFindByIdentityByAdminUsers() {

        UserTaskInstance instance = createUserTaskInstance();

        userTaskInstances.create(instance);

        IdentityProvider identityProvider = IdentityProviders.of("Seymour", "Group");

        List<UserTaskInstance> result = userTaskInstances.findByIdentity(identityProvider);

        Assertions.assertThat(result)
                .hasSize(1);

        verify(connect, times(2)).apply(any(UserTaskInstance.class));

        userTaskInstances.remove(instance);

        Assertions.assertThat(userTaskInstances.exists(instance.getId()))
                .isFalse();
    }

    @Test
    public void testFindByIdentityByAdminGroups() {
        UserTaskInstance instance = createUserTaskInstance();

        userTaskInstances.create(instance);

        IdentityProvider identityProvider = IdentityProviders.of("Abraham", "Administrator", "Managers");

        List<UserTaskInstance> result = userTaskInstances.findByIdentity(identityProvider);

        Assertions.assertThat(result)
                .hasSize(1);

        verify(connect, times(2)).apply(any(UserTaskInstance.class));

        userTaskInstances.remove(instance);

        Assertions.assertThat(userTaskInstances.exists(instance.getId()))
                .isFalse();
    }

    @Test
    public void testFindByIdentityByExcludedUser() {
        UserTaskInstance instance = createUserTaskInstance();

        userTaskInstances.create(instance);

        IdentityProvider identityProvider = IdentityProviders.of("Ned", "Simpson", "Family", "Administrators", "Managers");

        List<UserTaskInstance> result = userTaskInstances.findByIdentity(identityProvider);

        Assertions.assertThat(result)
                .hasSize(0);

        verify(connect, times(1)).apply(any(UserTaskInstance.class));

        userTaskInstances.remove(instance);

        Assertions.assertThat(userTaskInstances.exists(instance.getId()))
                .isFalse();
    }

    @Test
    public void testFindByIdentityByUnknownUser() {
        UserTaskInstance instance = createUserTaskInstance();

        userTaskInstances.create(instance);

        IdentityProvider identityProvider = IdentityProviders.of("Someone", "Group");

        List<UserTaskInstance> result = userTaskInstances.findByIdentity(identityProvider);

        Assertions.assertThat(result)
                .hasSize(0);

        verify(connect, times(1)).apply(any(UserTaskInstance.class));

        userTaskInstances.remove(instance);

        Assertions.assertThat(userTaskInstances.exists(instance.getId()))
                .isFalse();
    }

    @Test
    public void testAttachments() throws URISyntaxException {
        UserTaskInstance instance = createUserTaskInstance();

        userTaskInstances.create(instance);

        Optional<UserTaskInstanceEntity> entityOptional = userTaskInstanceRepository.findById(instance.getId());

        Assertions.assertThat(entityOptional)
                .isNotNull()
                .isPresent();

        assertEntityAndInstance(entityOptional.get(), instance);

        Attachment attachment = new Attachment("1", "Admin");
        attachment.setName("attachment 1");
        attachment.setContent(new URI("http://url.com/to/my/attachment"));
        attachment.setUpdatedAt(new Date());

        instance.addAttachment(attachment);

        entityOptional = userTaskInstanceRepository.findById(instance.getId());

        assertTaskAttachments(entityOptional.get().getAttachments(), instance.getAttachments());

        Attachment attachment2 = new Attachment("2", "Admin");
        attachment2.setName("attachment 2");
        attachment2.setContent(new URI("http://url.com/to/my/attachment2"));
        attachment2.setUpdatedAt(new Date());

        instance.addAttachment(attachment2);

        entityOptional = userTaskInstanceRepository.findById(instance.getId());
        assertTaskAttachments(entityOptional.get().getAttachments(), instance.getAttachments());

        instance.removeAttachment(attachment);
        instance.removeAttachment(attachment2);

        userTaskInstances.update(instance);

        entityOptional = userTaskInstanceRepository.findById(instance.getId());

        Assertions.assertThat(entityOptional.get().getAttachments())
                .isEmpty();

        Assertions.assertThat(attachmentRepository.findAll())
                .isEmpty();

        userTaskInstances.remove(instance);

        Assertions.assertThat(userTaskInstances.exists(instance.getId()))
                .isFalse();
    }

    @Test
    public void testComments() throws URISyntaxException {
        UserTaskInstance instance = createUserTaskInstance();

        userTaskInstances.create(instance);

        Optional<UserTaskInstanceEntity> entityOptional = userTaskInstanceRepository.findById(instance.getId());

        Assertions.assertThat(entityOptional)
                .isNotNull()
                .isPresent();

        assertEntityAndInstance(entityOptional.get(), instance);

        Comment comment = new Comment("1", "Admin");
        comment.setContent("This the comment 1");
        comment.setUpdatedAt(new Date());

        instance.addComment(comment);

        entityOptional = userTaskInstanceRepository.findById(instance.getId());

        UserTaskInstanceEntity userTaskInstanceEntity = entityOptional.get();

        Assertions.assertThat(userTaskInstanceEntity.getComments())
                .hasSize(1);

        assertTaskComments(entityOptional.get().getComments(), instance.getComments());

        Comment comment2 = new Comment("2", "Admin");
        comment2.setContent("This the comment 2");
        comment2.setUpdatedAt(new Date());

        instance.addComment(comment2);

        entityOptional = userTaskInstanceRepository.findById(instance.getId());

        userTaskInstanceEntity = entityOptional.get();

        Assertions.assertThat(userTaskInstanceEntity.getComments())
                .hasSize(2);

        assertTaskComments(userTaskInstanceEntity.getComments(), instance.getComments());

        instance.removeComment(comment);
        instance.removeComment(comment2);

        entityOptional = userTaskInstanceRepository.findById(instance.getId());

        Assertions.assertThat(entityOptional.get().getComments())
                .isEmpty();

        Assertions.assertThat(commentRepository.findAll())
                .isEmpty();

        userTaskInstances.remove(instance);

        Assertions.assertThat(userTaskInstances.exists(instance.getId()))
                .isFalse();
    }

    private void assertEntityAndInstance(UserTaskInstanceEntity entity, UserTaskInstance instance) {
        Assertions.assertThat(entity)
                .hasFieldOrPropertyWithValue("id", instance.getId())
                .hasFieldOrPropertyWithValue("userTaskId", instance.getUserTaskId())
                .hasFieldOrPropertyWithValue("taskName", instance.getTaskName())
                .hasFieldOrPropertyWithValue("taskDescription", instance.getTaskDescription())
                .hasFieldOrPropertyWithValue("taskPriority", instance.getTaskPriority())
                .hasFieldOrPropertyWithValue("status", instance.getStatus().getName())
                .hasFieldOrPropertyWithValue("terminationType", instance.getStatus().getTerminate().toString())
                .hasFieldOrPropertyWithValue("externalReferenceId", instance.getExternalReferenceId())
                .hasFieldOrPropertyWithValue("actualOwner", instance.getActualOwner());

        assertTaskUserAssignment(entity.getPotentialUsers(), instance.getPotentialUsers());
        assertTaskUserAssignment(entity.getPotentialGroups(), instance.getPotentialGroups());
        assertTaskUserAssignment(entity.getAdminUsers(), instance.getAdminUsers());
        assertTaskUserAssignment(entity.getAdminGroups(), instance.getAdminGroups());
        assertTaskUserAssignment(entity.getExcludedUsers(), instance.getExcludedUsers());

        assertTaskDataAssignments(entity.getInputs(), instance.getInputs());
        assertTaskDataAssignments(entity.getOutputs(), instance.getOutputs());

        assertTaskAttachments(entity.getAttachments(), instance.getAttachments());
        assertTaskComments(entity.getComments(), instance.getComments());
        assertTaskMetaData(entity.getMetadata(), instance.getMetadata());
    }

    private void assertTaskUserAssignment(Collection<String> entityAssignments, Set<String> instanceAssignments) {
        Assertions.assertThat(entityAssignments)
                .hasSize(instanceAssignments.size())
                .containsExactlyInAnyOrder(instanceAssignments.toArray(new String[0]));
    }

    private void assertTaskDataAssignments(Collection<? extends AbstractTaskDataEntity> entityData, Map<String, Object> instanceData) {
        Assertions.assertThat(entityData)
                .hasSize(instanceData.size())
                .allMatch(data -> instanceData.containsKey(data.getName()))
                .allMatch(data -> Objects.nonNull(data.getValue()));
    }

    private void assertTaskMetaData(Collection<TaskMetadataEntity> metadata, Map<String, Object> instanceMetadata) {
        Assertions.assertThat(metadata)
                .hasSize(instanceMetadata.size())
                .allMatch(data -> instanceMetadata.containsKey(data.getName()))
                .allMatch(data -> Objects.nonNull(data.getValue()))
                .allMatch(data -> data.getJavaType().equals(instanceMetadata.get(data.getName()).getClass().getName()));
    }

    private void assertTaskAttachments(Collection<AttachmentEntity> entityAttachments, Collection<Attachment> instanceAttachments) {
        Assertions.assertThat(entityAttachments)
                .hasSize(instanceAttachments.size());

        entityAttachments.forEach(entityAttachment -> {
            Optional<Attachment> optional = instanceAttachments.stream()
                    .filter(instanceAttachment -> instanceAttachment.getId().equals(entityAttachment.getId()))
                    .findFirst();

            Assertions.assertThat(optional)
                    .isPresent();

            Attachment instanceAttachment = optional.get();

            Assertions.assertThat(entityAttachment)
                    .hasFieldOrPropertyWithValue("id", instanceAttachment.getId())
                    .hasFieldOrPropertyWithValue("name", instanceAttachment.getName())
                    .hasFieldOrPropertyWithValue("updatedBy", instanceAttachment.getUpdatedBy())
                    .matches(entity -> entity.getUpdatedAt().getTime() == instanceAttachment.getUpdatedAt().getTime())
                    .hasFieldOrPropertyWithValue("url", instanceAttachment.getContent().toString());
        });
    }

    private void assertTaskComments(Collection<CommentEntity> entityComments, Collection<Comment> instanceComments) {
        Assertions.assertThat(entityComments)
                .hasSize(instanceComments.size());

        entityComments.forEach(entityComment -> {
            Optional<Comment> optional = instanceComments.stream()
                    .filter(comment -> comment.getId().equals(entityComment.getId()))
                    .findFirst();

            Assertions.assertThat(optional)
                    .isPresent();

            Comment instanceComment = optional.get();

            Assertions.assertThat(instanceComment)
                    .hasFieldOrPropertyWithValue("id", instanceComment.getId())
                    .hasFieldOrPropertyWithValue("updatedBy", instanceComment.getUpdatedBy())
                    .hasFieldOrPropertyWithValue("updatedAt", instanceComment.getUpdatedAt())
                    .hasFieldOrPropertyWithValue("content", instanceComment.getContent());
        });
    }

    private UserTaskInstance createUserTaskInstance() {
        DefaultUserTaskInstance instance = new DefaultUserTaskInstance();
        instance.setId(UUID.randomUUID().toString());
        instance.setUserTaskId("user-task-id");
        instance.setTaskName("test-task");
        instance.setTaskDescription("this is a test task description");
        instance.setTaskPriority(1);
        instance.setStatus(UserTaskState.of("Complete", UserTaskState.TerminationType.COMPLETED));

        instance.setActuaOwner("Homer");
        instance.setPotentialUsers(Set.of("Bart"));
        instance.setPotentialGroups(Set.of("Simpson", "Family"));
        instance.setAdminUsers(Set.of("Seymour"));
        instance.setAdminGroups(Set.of("Administrators", "Managers"));
        instance.setExcludedUsers(Set.of("Ned"));

        instance.setExternalReferenceId("external-reference-id");

        instance.setMetadata("ProcessId", "process-id");
        instance.setMetadata("ProcessType", "BPMN");
        instance.setMetadata("ProcessVersion", "1.0.0");
        instance.setMetadata("boolean", true);
        instance.setMetadata("integer", 0);

        instance.setInput("string", "hello this is a string");
        instance.setInput("integer", 1);
        instance.setInput("long", 1000L);
        instance.setInput("float", 1.02f);
        instance.setInput("boolean", true);
        instance.setInput("date", new Date());
        instance.setInput("person", new Person("Ned", "Stark", 50));

        instance.setOutput("person", new Person("Jon", "Snow", 17));

        instance.setInstances(userTaskInstances);

        return instance;
    }

    @AfterAll
    public static void tearDown() {
        System.out.println("down");
    }
}
