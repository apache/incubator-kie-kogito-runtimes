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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.UserTaskInstances;

public class InMemoryUserTaskInstances implements UserTaskInstances {

    Map<String, UserTaskInstance> userTaskInstances;

    public InMemoryUserTaskInstances() {
        this.userTaskInstances = new HashMap<>();
    }

    @Override
    public Optional<UserTaskInstance> findById(String userTaskInstanceId) {
        return Optional.ofNullable(userTaskInstances.get(userTaskInstanceId));
    }

    @Override
    public boolean exists(String userTaskInstanceId) {
        return userTaskInstances.containsKey(userTaskInstanceId);
    }

    @Override
    public void create(UserTaskInstance userTaskInstance) {
        userTaskInstances.put(userTaskInstance.id(), userTaskInstance);

    }

    @Override
    public void update(UserTaskInstance userTaskInstance) {
        userTaskInstances.put(userTaskInstance.id(), userTaskInstance);

    }

    @Override
    public void remove(String userTaskInstanceId) {
        userTaskInstances.remove(userTaskInstanceId);
    }

}
