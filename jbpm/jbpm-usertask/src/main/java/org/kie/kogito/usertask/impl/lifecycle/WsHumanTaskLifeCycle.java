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
package org.kie.kogito.usertask.impl.lifecycle;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.kie.kogito.auth.IdentityProvider;
import org.kie.kogito.usertask.UserTaskAssignmentStrategy;
import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.UserTaskInstanceNotAuthorizedException;
import org.kie.kogito.usertask.lifecycle.UserTaskLifeCycle;
import org.kie.kogito.usertask.lifecycle.UserTaskState;
import org.kie.kogito.usertask.lifecycle.UserTaskState.TerminationType;
import org.kie.kogito.usertask.lifecycle.UserTaskTransition;
import org.kie.kogito.usertask.lifecycle.UserTaskTransitionException;
import org.kie.kogito.usertask.lifecycle.UserTaskTransitionToken;

public class WsHumanTaskLifeCycle implements UserTaskLifeCycle {
    public static final String WORKFLOW_ENGINE_USER = "WORKFLOW_ENGINE_USER";

    public static final String PARAMETER_USER = "USER";
    public static final String PARAMETER_NOTIFY = "NOTIFY";
    private static final String PARAMETER_DELEGATED_USER = "DELEGATED_USER";
    private static final String PARAMETER_FORWARDED_USER = "FORWARDED_USER";

    private static final String SKIPPABLE = "Skippable";

    // Actions
    public static final String ACTIVATE = "activate";
    public static final String CLAIM = "claim";
    public static final String DELEGATE = "delegate";
    public static final String RELEASE = "release";
    public static final String FORWARD = "forward";
    public static final String START = "start";
    public static final String STOP = "stop";
    public static final String COMPLETE = "complete";
    public static final String FAIL = "fail";
    public static final String FAULT = "fault";
    public static final String EXIT = "exit";
    public static final String SKIP = "skip";
    public static final String SUSPEND = "suspend";
    public static final String RESUME = "resume";

    public static final UserTaskState CREATED = UserTaskState.initalized();
    public static final UserTaskState READY = UserTaskState.of("Ready");
    public static final UserTaskState RESERVED = UserTaskState.of("Reserved");
    public static final UserTaskState INPROGRESS = UserTaskState.of("InProgress");
    public static final UserTaskState COMPLETED = UserTaskState.of("Completed", TerminationType.COMPLETED);
    public static final UserTaskState FAILED = UserTaskState.of("Failed", TerminationType.FAILED);
    public static final UserTaskState ERROR = UserTaskState.of("Error", TerminationType.ERROR);
    public static final UserTaskState EXITED = UserTaskState.of("Exited", TerminationType.EXITED);
    public static final UserTaskState OBSOLETE = UserTaskState.of("Obsolete", TerminationType.OBSOLETE);
    public static final UserTaskState SUSPENDED = UserTaskState.of("Suspended");

    private final UserTaskTransition T_CREATED_READY = new DefaultUserTransition(ACTIVATE, CREATED, READY, this::activate);
    // Created -> Error, Exited, Obsolete. Is it possible
    // Created -> Reserved also possible
    private final UserTaskTransition T_READY_READY_FORWARD = new DefaultUserTransition(FORWARD, READY, READY, this::forward);
    private final UserTaskTransition T_READY_RESERVED_CLAIM = new DefaultUserTransition(CLAIM, READY, RESERVED, this::claim);
    private final UserTaskTransition T_READY_RESERVED_DELEGATE = new DefaultUserTransition(DELEGATE, READY, RESERVED, this::delegate);
    private final UserTaskTransition T_READY_INPROGRESS = new DefaultUserTransition(START, READY, INPROGRESS, this::start);
    private final UserTaskTransition T_READY_ERROR = new DefaultUserTransition(FAULT, READY, ERROR, this::fault);
    private final UserTaskTransition T_READY_EXITED = new DefaultUserTransition(EXIT, READY, EXITED, this::exit);
    private final UserTaskTransition T_READY_OBSOLETE = new DefaultUserTransition(SKIP, READY, OBSOLETE, this::skip);
    private final UserTaskTransition T_READY_SUSPENDED = new DefaultUserTransition(SUSPEND, READY, SUSPENDED, this::suspend);

    private final UserTaskTransition T_RESERVED_READY_RELEASE = new DefaultUserTransition(RELEASE, RESERVED, READY, this::release);
    private final UserTaskTransition T_RESERVED_READY_FORWARD = new DefaultUserTransition(FORWARD, RESERVED, READY, this::forward);
    private final UserTaskTransition T_RESERVED_INPROGRESS = new DefaultUserTransition(START, RESERVED, INPROGRESS, this::start);
    private final UserTaskTransition T_RESERVED_RESERVED_DELEGATE = new DefaultUserTransition(DELEGATE, RESERVED, RESERVED, this::delegate);
    private final UserTaskTransition T_RESERVED_ERROR = new DefaultUserTransition(FAULT, RESERVED, ERROR, this::fault);
    private final UserTaskTransition T_RESERVED_EXITED = new DefaultUserTransition(EXIT, RESERVED, EXITED, this::exit);
    private final UserTaskTransition T_RESERVED_OBSOLETE = new DefaultUserTransition(SKIP, RESERVED, OBSOLETE, this::skip);
    private final UserTaskTransition T_RESERVED_SUSPENDED = new DefaultUserTransition(SUSPEND, RESERVED, SUSPENDED, this::suspend);

    private final UserTaskTransition T_INPROGRESS_RESERVED_STOP = new DefaultUserTransition(STOP, INPROGRESS, RESERVED, this::stop);
    private final UserTaskTransition T_INPROGRESS_RESERVED_DELEGATE = new DefaultUserTransition(DELEGATE, INPROGRESS, RESERVED, this::delegate);
    private final UserTaskTransition T_INPROGRESS_READY_RELEASE = new DefaultUserTransition(RELEASE, INPROGRESS, READY, this::release);
    private final UserTaskTransition T_INPROGRESS_READY_FORWARD = new DefaultUserTransition(FORWARD, INPROGRESS, READY, this::forward);
    private final UserTaskTransition T_INPROGRESS_COMPLETED = new DefaultUserTransition(COMPLETE, INPROGRESS, COMPLETED, this::complete);
    private final UserTaskTransition T_INPROGRESS_FAILED = new DefaultUserTransition(FAIL, INPROGRESS, FAILED, this::fail);
    private final UserTaskTransition T_INPROGRESS_ERROR = new DefaultUserTransition(FAULT, INPROGRESS, ERROR, this::fault);
    private final UserTaskTransition T_INPROGRESS_EXITED = new DefaultUserTransition(EXIT, INPROGRESS, EXITED, this::exit);
    private final UserTaskTransition T_INPROGRESS_OBSOLETE = new DefaultUserTransition(SKIP, INPROGRESS, OBSOLETE, this::skip);
    private final UserTaskTransition T_INPROGRESS_SUSPENDED = new DefaultUserTransition(SUSPEND, INPROGRESS, SUSPENDED, this::suspend);

    private final UserTaskTransition T_SUSPENDED_READY = new DefaultUserTransition(RESUME, SUSPENDED, READY, this::resume);
    private final UserTaskTransition T_SUSPENDED_RESERVED = new DefaultUserTransition(RESUME, SUSPENDED, RESERVED, this::resume);
    private final UserTaskTransition T_SUSPENDED_INPROGRESS = new DefaultUserTransition(RESUME, SUSPENDED, INPROGRESS, this::resume);

    private List<UserTaskTransition> transitions;

    public WsHumanTaskLifeCycle() {
        transitions = List.of(
                T_CREATED_READY,
                T_READY_READY_FORWARD,
                T_READY_RESERVED_CLAIM,
                T_READY_RESERVED_DELEGATE,
                T_READY_INPROGRESS,
                T_READY_ERROR,
                T_READY_EXITED,
                T_READY_OBSOLETE,
                T_READY_SUSPENDED,
                T_RESERVED_READY_RELEASE,
                T_RESERVED_READY_FORWARD,
                T_RESERVED_INPROGRESS,
                T_RESERVED_RESERVED_DELEGATE,
                T_RESERVED_ERROR,
                T_RESERVED_EXITED,
                T_RESERVED_OBSOLETE,
                T_RESERVED_SUSPENDED,
                T_INPROGRESS_RESERVED_STOP,
                T_INPROGRESS_RESERVED_DELEGATE,
                T_INPROGRESS_READY_RELEASE,
                T_INPROGRESS_READY_FORWARD,
                T_INPROGRESS_COMPLETED,
                T_INPROGRESS_FAILED,
                T_INPROGRESS_ERROR,
                T_INPROGRESS_EXITED,
                T_INPROGRESS_OBSOLETE,
                T_INPROGRESS_SUSPENDED,
                T_SUSPENDED_READY,
                T_SUSPENDED_RESERVED,
                T_SUSPENDED_INPROGRESS);
    }

    @Override
    public List<UserTaskTransition> allowedTransitions(UserTaskInstance userTaskInstance, IdentityProvider identity) {
        checkPermission(userTaskInstance, identity);
        return transitions.stream().filter(t -> t.source().equals(userTaskInstance.getStatus())).toList();
    }

    @Override
    public Optional<UserTaskTransitionToken> transition(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        checkPermission(userTaskInstance, identityProvider);
        UserTaskTransition transition = transitions.stream()
                .filter(t -> t.source().equals(userTaskInstance.getStatus()) && t.id().equals(token.transitionId()))
                .findFirst()
                .orElseThrow(() -> new UserTaskTransitionException("Invalid transition from " + userTaskInstance.getStatus()));
        return transition.executor().execute(userTaskInstance, token, identityProvider);
    }

    @Override
    public UserTaskTransitionToken newCompleteTransitionToken(UserTaskInstance userTaskInstance, Map<String, Object> data) {
        return newTransitionToken(COMPLETE, userTaskInstance.getStatus(), null, data);
    }

    @Override
    public UserTaskTransitionToken newAbortTransitionToken(UserTaskInstance userTaskInstance, Map<String, Object> data) {
        return newTransitionToken(FAIL, userTaskInstance.getStatus(), null, data);
    }

    @Override
    public UserTaskTransitionToken newTransitionToken(String transitionId, UserTaskInstance userTaskInstance, Map<String, Object> data) {
        return newTransitionToken(transitionId, userTaskInstance.getStatus(), (String) userTaskInstance.getMetadata().get("PreviousStatus"), data);
    }

    public UserTaskTransitionToken newTransitionToken(String transitionId, UserTaskState state, String previousState, Map<String, Object> data) {
        var transition = transitions.stream()
                .filter(e -> e.source().equals(state) && e.id().equals(transitionId) && (!transitionId.equals(RESUME) || e.target().getName().equals(previousState)))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Invalid transition " + transitionId + " from " + state));

        return new DefaultUserTaskTransitionToken(transition.id(), transition.source(), transition.target(), data);
    }

    public Optional<UserTaskTransitionToken> activate(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        userTaskInstance.getMetadata().put("Lifecycle", "ws-human-task");
        userTaskInstance.startNotCompletedDeadlines();
        userTaskInstance.startNotCompletedReassignments();

        String user = assignStrategy(userTaskInstance, identityProvider);
        if (user != null) {
            return Optional.of(newTransitionToken(CLAIM, READY, null, Map.of(PARAMETER_USER, user)));
        }
        userTaskInstance.startNotStartedDeadlines();
        userTaskInstance.startNotStartedReassignments();
        return Optional.empty();
    }

    public Optional<UserTaskTransitionToken> claim(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        if (token.data().containsKey(PARAMETER_USER)) {
            userTaskInstance.setActualOwner((String) token.data().get(PARAMETER_USER));
        } else {
            userTaskInstance.setActualOwner(identityProvider.getName());
        }
        userTaskInstance.stopNotStartedDeadlines();
        userTaskInstance.stopNotStartedReassignments();
        return Optional.empty();
    }

    private Optional<UserTaskTransitionToken> delegate(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        // Maybe if delegate user is not provided, we need to default to identityprovider user -mandatory
        // Should we check if delegated user is a potential user
        if (token.data().containsKey(PARAMETER_DELEGATED_USER)) {
            userTaskInstance.setActualOwner((String) token.data().get(PARAMETER_DELEGATED_USER));
        } else {
            throw new UserTaskTransitionException("Delegated user not specified");
        }
        return Optional.empty();
    }

    private Optional<UserTaskTransitionToken> forward(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        // Maybe if forwarded user is not provided, we need to default to identityprovider user -mandatory
        // Should we check if forwarded user is a potential user
        if (token.data().containsKey(PARAMETER_FORWARDED_USER)) {
            userTaskInstance.setActualOwner(null);
            userTaskInstance.setPotentialUsers(new HashSet<>(Set.of((String) token.data().get(PARAMETER_FORWARDED_USER))));
        } else {
            throw new UserTaskTransitionException("Forwarded user not specified");
        }
        return Optional.empty();
    }

    private Optional<UserTaskTransitionToken> start(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        if (token.data().containsKey(PARAMETER_USER)) {
            userTaskInstance.setActualOwner((String) token.data().get(PARAMETER_USER));
        } else {
            userTaskInstance.setActualOwner(identityProvider.getName());
        }
        return Optional.empty();
    }

    public Optional<UserTaskTransitionToken> release(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        userTaskInstance.setActualOwner(null);
        return Optional.empty();
    }

    private Optional<UserTaskTransitionToken> stop(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        return Optional.empty();
    }

    public Optional<UserTaskTransitionToken> complete(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        token.data().forEach(userTaskInstance::setOutput);
        userTaskInstance.stopNotStartedDeadlines();
        userTaskInstance.stopNotStartedReassignments();
        userTaskInstance.stopNotCompletedDeadlines();
        userTaskInstance.stopNotCompletedReassignments();
        return Optional.empty();
    }

    private Optional<UserTaskTransitionToken> fault(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        if (token.data().containsKey(PARAMETER_NOTIFY)) {
            userTaskInstance.getMetadata().put(PARAMETER_NOTIFY, token.data().get(PARAMETER_NOTIFY));
        }
        userTaskInstance.stopNotStartedDeadlines();
        userTaskInstance.stopNotStartedReassignments();
        userTaskInstance.stopNotCompletedDeadlines();
        userTaskInstance.stopNotCompletedReassignments();
        return Optional.empty();
    }

    private Optional<UserTaskTransitionToken> exit(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        if (token.data().containsKey(PARAMETER_NOTIFY)) {
            userTaskInstance.getMetadata().put(PARAMETER_NOTIFY, token.data().get(PARAMETER_NOTIFY));
        }
        userTaskInstance.stopNotStartedDeadlines();
        userTaskInstance.stopNotStartedReassignments();
        userTaskInstance.stopNotCompletedDeadlines();
        userTaskInstance.stopNotCompletedReassignments();
        return Optional.empty();
    }

    public Optional<UserTaskTransitionToken> skip(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        if (!Boolean.parseBoolean((String) userTaskInstance.getMetadata().get(SKIPPABLE))) {
            throw new UserTaskTransitionException("Usertask cannot be skipped");
        }
        if (token.data().containsKey(PARAMETER_NOTIFY)) {
            userTaskInstance.getMetadata().put(PARAMETER_NOTIFY, token.data().get(PARAMETER_NOTIFY));
        }
        userTaskInstance.stopNotStartedDeadlines();
        userTaskInstance.stopNotStartedReassignments();
        userTaskInstance.stopNotCompletedDeadlines();
        userTaskInstance.stopNotCompletedReassignments();
        return Optional.empty();
    }

    public Optional<UserTaskTransitionToken> fail(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        if (token.data().containsKey(PARAMETER_NOTIFY)) {
            userTaskInstance.getMetadata().put(PARAMETER_NOTIFY, token.data().get(PARAMETER_NOTIFY));
        }
        userTaskInstance.stopNotStartedDeadlines();
        userTaskInstance.stopNotStartedReassignments();
        userTaskInstance.stopNotCompletedDeadlines();
        userTaskInstance.stopNotCompletedReassignments();
        return Optional.empty();
    }

    private Optional<UserTaskTransitionToken> suspend(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        if (userTaskInstance.getStatus() != null) {
            userTaskInstance.getMetadata().put("PreviousStatus", userTaskInstance.getStatus().getName());
        }
        return Optional.empty();
    }

    private Optional<UserTaskTransitionToken> resume(UserTaskInstance userTaskInstance, UserTaskTransitionToken token, IdentityProvider identityProvider) {
        userTaskInstance.getMetadata().remove("PreviousStatus");
        return Optional.empty();
    }

    private String assignStrategy(UserTaskInstance userTaskInstance, IdentityProvider identityProvider) {
        UserTaskAssignmentStrategy assignmentStrategy = userTaskInstance.getUserTask().getAssignmentStrategy();
        return assignmentStrategy.computeAssignment(userTaskInstance, identityProvider).orElse(null);
    }

    private void checkPermission(UserTaskInstance userTaskInstance, IdentityProvider identityProvider) {
        this.checkPermission(userTaskInstance, identityProvider.getName(), identityProvider.getRoles());
    }

    private void checkPermission(UserTaskInstance userTaskInstance, String user, Collection<String> roles) {

        if (WORKFLOW_ENGINE_USER.equals(user)) {
            return;
        }

        // first we check admins
        Set<String> adminUsers = userTaskInstance.getAdminUsers();
        if (adminUsers.contains(user)) {
            return;
        }

        Set<String> userAdminGroups = new HashSet<>(userTaskInstance.getAdminGroups());
        userAdminGroups.retainAll(roles);
        if (!userAdminGroups.isEmpty()) {
            return;
        }

        if (userTaskInstance.getActualOwner() != null && userTaskInstance.getActualOwner().equals(user)) {
            return;
        }

        Set<String> excludedUsers = userTaskInstance.getExcludedUsers();
        if (excludedUsers != null && excludedUsers.contains(user)) {
            String message = String.format("User '%s' is not authorized to perform an operation on user task '%s'",
                    user, userTaskInstance.getId());
            throw new UserTaskInstanceNotAuthorizedException(message);
        }

        if (List.of(CREATED, READY, SUSPENDED).contains(userTaskInstance.getStatus())) {
            // there is no user
            Set<String> users = new HashSet<>(userTaskInstance.getPotentialUsers());
            users.removeAll(userTaskInstance.getExcludedUsers());
            if (users.contains(user)) {
                return;
            }

            Set<String> userPotGroups = new HashSet<>(userTaskInstance.getPotentialGroups());
            userPotGroups.retainAll(roles);
            if (!userPotGroups.isEmpty()) {
                return;
            }
        }

        throw new UserTaskInstanceNotAuthorizedException("user " + user + " with roles " + roles + " not authorized to perform an operation on user task " + userTaskInstance.getId());
    }

}
