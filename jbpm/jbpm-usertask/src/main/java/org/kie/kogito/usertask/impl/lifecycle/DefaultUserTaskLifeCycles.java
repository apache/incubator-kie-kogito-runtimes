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

import java.util.HashMap;
import java.util.Map;

import org.kie.kogito.usertask.lifecycle.UserTaskLifeCycle;
import org.kie.kogito.usertask.lifecycle.UserTaskLifeCycleException;
import org.kie.kogito.usertask.lifecycle.UserTaskLifeCycles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultUserTaskLifeCycles implements UserTaskLifeCycles {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultUserTaskLifeCycles.class);

    private final Map<String, UserTaskLifeCycle> userTaskLifeCycleRegistry = new HashMap<>();
    private String defaultUserTaskLifeCycleId;

    public DefaultUserTaskLifeCycles() {
        this.defaultUserTaskLifeCycleId = "kogito";
        registerUserTaskLifeCycles();
        LOG.info("Registered UserTaskLifeCycles {} with default lifecycle: {}", userTaskLifeCycleRegistry, this.defaultUserTaskLifeCycleId);
    }

    public DefaultUserTaskLifeCycles(String defaultUserTaskLifeCycleId, Iterable<UserTaskLifeCycle> userTaskLifeCycle) {
        this.defaultUserTaskLifeCycleId = defaultUserTaskLifeCycleId;
        registerCustomUserTaskLifeCycleIfAny(userTaskLifeCycle);
        registerUserTaskLifeCycles();
        LOG.info("Registered UserTaskLifeCycles {} with default lifecycle: {}", userTaskLifeCycleRegistry, this.defaultUserTaskLifeCycleId);
    }

    private void registerUserTaskLifeCycles() {
        registerUserTaskLifeCycle("kogito", new DefaultUserTaskLifeCycle());
        registerUserTaskLifeCycle("ws-human-task", new WsHumanTaskLifeCycle());
    }

    private void registerCustomUserTaskLifeCycleIfAny(Iterable<UserTaskLifeCycle> userTaskLifeCycle) {
        var iterator = userTaskLifeCycle.iterator();
        if (iterator.hasNext()) {
            defaultUserTaskLifeCycleId = "custom";
            registerUserTaskLifeCycle("custom", iterator.next());

            if (iterator.hasNext()) {
                var message = "Multiple custom usertask lifecycle implementations found";
                LOG.error(message);
                throw new UserTaskLifeCycleException(message);
            }
        }
    }

    private void registerUserTaskLifeCycle(String userTaskLifeCycleId, UserTaskLifeCycle userTaskLifeCycle) {
        userTaskLifeCycleRegistry.put(userTaskLifeCycleId, userTaskLifeCycle);
    }

    @Override
    public String getDefaultUserTaskLifeCycleId() {
        return defaultUserTaskLifeCycleId;
    }

    @Override
    public UserTaskLifeCycle getUserTaskLifeCycleById(String userTaskLifeCycleId) {
        return userTaskLifeCycleRegistry.getOrDefault(userTaskLifeCycleId, userTaskLifeCycleRegistry.get(defaultUserTaskLifeCycleId));
    }

}
