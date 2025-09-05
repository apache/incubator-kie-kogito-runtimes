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

import java.util.*;

import org.kie.kogito.Application;
import org.kie.kogito.auth.IdentityProvider;
import org.kie.kogito.services.uow.UnitOfWorkExecutor;
import org.kie.kogito.usertask.UserTaskConfig;
import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.UserTaskService;
import org.kie.kogito.usertask.UserTasks;
import org.kie.kogito.usertask.lifecycle.UserTaskLifeCycle;
import org.kie.kogito.usertask.lifecycle.UserTaskTransition;
import org.kie.kogito.usertask.model.Attachment;
import org.kie.kogito.usertask.model.Comment;
import org.kie.kogito.usertask.view.UserTaskTransitionView;
import org.kie.kogito.usertask.view.UserTaskView;

public class UserTaskServiceImpl implements UserTaskService {

    private Application application;

    public UserTaskServiceImpl(Application application) {
        this.application = application;
    }

    @Override
    public Optional<UserTaskView> getUserTaskInstance(String taskId, IdentityProvider identity) {
        return application.get(UserTasks.class).instances().findById(taskId).map(this::toUserTaskView);
    }

    @Override
    public List<UserTaskView> list(IdentityProvider identity) {
        return application.get(UserTasks.class).instances().findByIdentity(identity).stream().map(this::toUserTaskView).toList();
    }

    private UserTaskView toUserTaskView(UserTaskInstance instance) {
        UserTaskView view = new UserTaskView();
        view.setId(instance.getId());
        view.setUserTaskId(instance.getUserTaskId());
        view.setStatus(instance.getStatus());
        view.setTaskName(instance.getTaskName());
        view.setTaskDescription(instance.getTaskDescription());
        view.setTaskPriority(instance.getTaskPriority());
        view.setPotentialUsers(instance.getPotentialUsers());
        view.setPotentialGroups(instance.getPotentialGroups());
        view.setExcludedUsers(instance.getExcludedUsers());
        view.setAdminUsers(instance.getAdminUsers());
        view.setAdminGroups(instance.getAdminGroups());
        view.setActualOwner(instance.getActualOwner());
        view.setInputs(instance.getInputs());
        view.setOutputs(instance.getOutputs());
        view.setMetadata(instance.getMetadata());
        view.setExternalReferenceId(instance.getExternalReferenceId());
        return view;
    }

    @Override
    public Optional<UserTaskView> transition(String taskId, String transitionId, Map<String, Object> data, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            ut.transition(transitionId, data, identity);
            return Optional.of(toUserTaskView(ut));
        });
    }

    @Override
    public List<UserTaskTransitionView> allowedTransitions(String taskId, IdentityProvider identity) {
        Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
        if (userTaskInstance.isEmpty()) {
            return Collections.emptyList();
        }
        UserTaskInstance ut = userTaskInstance.get();
        UserTaskLifeCycle userTaskLifeCycle = application.config().get(UserTaskConfig.class).userTaskLifeCycle();
        List<UserTaskTransition> transitions = userTaskLifeCycle.allowedTransitions(ut, identity);
        return toUserTaskTransitionView(transitions);
    }

    private List<UserTaskTransitionView> toUserTaskTransitionView(List<UserTaskTransition> transitions) {
        List<UserTaskTransitionView> views = new ArrayList<>();
        for (UserTaskTransition transition : transitions) {
            UserTaskTransitionView view = new UserTaskTransitionView();
            view.setTransitionId(transition.id());
            view.setSource(transition.source());
            view.setTarget(transition.target());
            views.add(view);
        }

        return views;
    }

    @Override
    public Optional<UserTaskView> setOutputs(String taskId, Map<String, Object> data, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            data.forEach(ut::setOutput);
            return Optional.of(toUserTaskView(ut));
        });
    }

    @Override
    public Optional<UserTaskView> setInputs(String taskId, Map<String, Object> data, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            data.forEach(ut::setInput);
            return Optional.of(toUserTaskView(ut));
        });
    }

    @Override
    public List<Comment> getComments(String taskId, IdentityProvider identity) {
        Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
        if (userTaskInstance.isEmpty()) {
            return Collections.emptyList();
        }
        UserTaskInstance ut = userTaskInstance.get();
        return new ArrayList<>(ut.getComments());
    }

    @Override
    public Optional<Comment> getComment(String taskId, String commentId, IdentityProvider identity) {
        Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
        if (userTaskInstance.isEmpty()) {
            return Optional.empty();
        }
        UserTaskInstance ut = userTaskInstance.get();
        return Optional.ofNullable(ut.findCommentById(commentId));
    }

    @Override
    public Optional<Comment> addComment(String taskId, Comment comment, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            Comment wrap = new Comment(null, identity.getName());
            wrap.setContent(comment.getContent());
            return Optional.ofNullable(ut.addComment(wrap));
        });
    }

    @Override
    public Optional<Comment> updateComment(String taskId, Comment comment, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            Comment wrap = new Comment(comment.getId(), identity.getName());
            wrap.setContent(comment.getContent());
            return Optional.ofNullable(ut.updateComment(wrap));
        });
    }

    @Override
    public Optional<Comment> removeComment(String taskId, String commentId, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            return Optional.ofNullable(ut.removeComment(new Comment(commentId, identity.getName())));
        });
    }

    @Override
    public List<Attachment> getAttachments(String taskId, IdentityProvider identity) {
        Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
        if (userTaskInstance.isEmpty()) {
            return Collections.emptyList();
        }
        UserTaskInstance ut = userTaskInstance.get();
        return new ArrayList<>(ut.getAttachments());
    }

    @Override
    public Optional<Attachment> getAttachment(String taskId, String attachmentId, IdentityProvider identity) {
        Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
        if (userTaskInstance.isEmpty()) {
            return Optional.empty();
        }
        UserTaskInstance ut = userTaskInstance.get();
        return Optional.ofNullable(ut.findAttachmentById(attachmentId));
    }

    @Override
    public Optional<Attachment> addAttachment(String taskId, Attachment attachment, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            Attachment wrap = new Attachment(null, identity.getName());
            wrap.setContent(attachment.getContent());
            wrap.setName(attachment.getName());
            return Optional.ofNullable(ut.addAttachment(wrap));
        });
    }

    @Override
    public Optional<Attachment> updateAttachment(String taskId, Attachment attachment, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            Attachment wrap = new Attachment(attachment.getId(), identity.getName());
            wrap.setContent(attachment.getContent());
            wrap.setName(attachment.getName());
            return Optional.ofNullable(ut.updateAttachment(attachment));
        });
    }

    @Override
    public Optional<Attachment> removeAttachment(String taskId, String attachmentId, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            return Optional.ofNullable(ut.removeAttachment(new Attachment(attachmentId, identity.getName())));
        });
    }

    @Override
    public Optional<UserTaskView> addAdminUsers(String taskId, Set<String> adminUsers, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            ut.addAdminUsers(adminUsers);
            return Optional.of(toUserTaskView(ut));
        });
    }

    @Override
    public Optional<UserTaskView> setAdminUsers(String taskId, Set<String> adminUsers, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            ut.setAdminUsers(adminUsers);
            return Optional.of(toUserTaskView(ut));
        });
    }

    @Override
    public Optional<UserTaskView> removeAdminUsers(String taskId, Set<String> adminUsersToRemove, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            ut.removeAdminUsers(adminUsersToRemove);
            return Optional.of(toUserTaskView(ut));
        });
    }

    @Override
    public Optional<UserTaskView> addPotentialUsers(String taskId, Set<String> potentialUsers, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            ut.addPotentialUsers(potentialUsers);
            return Optional.of(toUserTaskView(ut));
        });
    }

    @Override
    public Optional<UserTaskView> setPotentialUsers(String taskId, Set<String> potentialUsers, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            ut.setPotentialUsers(potentialUsers);
            return Optional.of(toUserTaskView(ut));
        });
    }

    @Override
    public Optional<UserTaskView> removePotentialUsers(String taskId, Set<String> potentialUsersToRemove, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            ut.removePotentialUsers(potentialUsersToRemove);
            return Optional.of(toUserTaskView(ut));
        });
    }

    @Override
    public Optional<UserTaskView> addExcludedUsers(String taskId, Set<String> excludedUsers, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            ut.addExcludedUsers(excludedUsers);
            return Optional.of(toUserTaskView(ut));
        });
    }

    @Override
    public Optional<UserTaskView> setExcludedUsers(String taskId, Set<String> excludedUsers, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            ut.setExcludedUsers(excludedUsers);
            return Optional.of(toUserTaskView(ut));
        });
    }

    @Override
    public Optional<UserTaskView> removeExcludedUsers(String taskId, Set<String> excludedUsersToRemove, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            ut.removeExcludedUsers(excludedUsersToRemove);
            return Optional.of(toUserTaskView(ut));
        });
    }

    @Override
    public Optional<UserTaskView> addAdminGroups(String taskId, Set<String> adminGroups, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            ut.addAdminGroups(adminGroups);
            return Optional.of(toUserTaskView(ut));
        });
    }

    @Override
    public Optional<UserTaskView> setAdminGroups(String taskId, Set<String> adminGroups, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            ut.setAdminGroups(adminGroups);
            return Optional.of(toUserTaskView(ut));
        });
    }

    @Override
    public Optional<UserTaskView> removeAdminGroups(String taskId, Set<String> adminGroupsToRemove, IdentityProvider identity) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            Optional<UserTaskInstance> userTaskInstance = application.get(UserTasks.class).instances().findById(taskId);
            if (userTaskInstance.isEmpty()) {
                return Optional.empty();
            }
            UserTaskInstance ut = userTaskInstance.get();
            ut.removeAdminGroups(adminGroupsToRemove);
            return Optional.of(toUserTaskView(ut));
        });
    }
}
