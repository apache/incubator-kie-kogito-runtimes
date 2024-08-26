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

import java.util.Map;
import java.util.UUID;

import org.kie.kogito.internal.usertask.event.KogitoUserTaskEventSupport;
import org.kie.kogito.usertask.UserTask;
import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.UserTaskInstances;
import org.kie.kogito.usertask.lifecycle.UserTaskTransitionToken;
import org.kie.kogito.usertask.model.UserTaskModel;

public class DefaultUserTaskInstance implements UserTaskInstance {

    private String id;
    private UserTask userTask;
    private UserTaskModel userTaskModel;
    private String status;
    private String actualOwner;
    private String externalReferenceId;
    private UserTaskInstances instances;

    private KogitoUserTaskEventSupport userTaskEventSupport;

    public DefaultUserTaskInstance(UserTaskInstances instances, UserTaskModel userTaskModel) {
        this.id = UUID.randomUUID().toString();
        this.userTaskModel = userTaskModel;
    }

    public void setUserTaskEventSupport(KogitoUserTaskEventSupport userTaskEventSupport) {
        this.userTaskEventSupport = userTaskEventSupport;
    }

    @Override
    public void complete() {
        instances.remove(id);
    }

    @Override
    public void abort() {
        instances.remove(id);
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public UserTaskModel getUserTaskModel() {
        return userTaskModel;
    }

    @Override
    public String status() {
        return status;
    }

    @Override
    public String getActualOwner() {
        return actualOwner;
    }

    @Override
    public UserTaskTransitionToken createTransitionToken(String transitionId, Map<String, Object> data) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void transition(UserTaskTransitionToken token) {
        // TODO Auto-generated method stub

    }

    @Override
    public UserTask getUserTask() {
        return userTask;
    }

}
