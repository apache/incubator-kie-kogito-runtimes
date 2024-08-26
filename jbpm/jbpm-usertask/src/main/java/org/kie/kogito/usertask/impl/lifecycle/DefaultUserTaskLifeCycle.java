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

import java.util.List;
import java.util.Optional;

import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.lifecycle.UserTaskLifeCycle;
import org.kie.kogito.usertask.lifecycle.UserTaskState;
import org.kie.kogito.usertask.lifecycle.UserTaskState.TerminationType;
import org.kie.kogito.usertask.lifecycle.UserTaskTransition;
import org.kie.kogito.usertask.lifecycle.UserTaskTransitionToken;

public class DefaultUserTaskLifeCycle implements UserTaskLifeCycle {

    public static final UserTaskState INACTIVE = UserTaskState.of(null);
    public static final UserTaskState ACTIVE = UserTaskState.of("Active");
    public static final UserTaskState RESERVED = UserTaskState.of("Reserved");
    public static final UserTaskState COMPLETED = UserTaskState.of("Completed", TerminationType.COMPLETED);
    public static final UserTaskState ERROR = UserTaskState.of("Error", TerminationType.ERROR);
    public static final UserTaskState OBSOLETE = UserTaskState.of("Error", TerminationType.OBSOLETE);

    private static final UserTaskTransition T_NEW_ACTIVE = new DefaultUserTransition("activate", INACTIVE, ACTIVE);
    private static final UserTaskTransition T_ACTIVE_RESERVED = new DefaultUserTransition("claim", ACTIVE, RESERVED);
    private static final UserTaskTransition T_RESERVED_ACTIVE = new DefaultUserTransition("release", RESERVED, ACTIVE);
    private static final UserTaskTransition T_RESERVED_COMPLETED = new DefaultUserTransition("start", RESERVED, COMPLETED);
    private static final UserTaskTransition T_RESERVED_SKIPPED = new DefaultUserTransition("skip", RESERVED, OBSOLETE);
    private static final UserTaskTransition T_RESERVED_ERROR = new DefaultUserTransition("complete", RESERVED, ERROR);

    private List<UserTaskTransition> transitions;

    public DefaultUserTaskLifeCycle() {
        transitions = List.of(
                T_NEW_ACTIVE,
                T_ACTIVE_RESERVED,
                T_RESERVED_ACTIVE,
                T_RESERVED_COMPLETED,
                T_RESERVED_SKIPPED,
                T_RESERVED_ERROR);
    }

    @Override
    public Optional<UserTaskTransition> transition(UserTaskInstance userTaskInstance, UserTaskTransitionToken transition) {
        return Optional.empty();
    }

}
