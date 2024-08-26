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
package org.jbpm.usertask.impl;

import org.junit.jupiter.api.Test;
import org.kie.kogito.Addons;
import org.kie.kogito.Application;
import org.kie.kogito.StaticApplication;
import org.kie.kogito.StaticConfig;
import org.kie.kogito.usertask.UserTask;
import org.kie.kogito.usertask.UserTasks;
import org.kie.kogito.usertask.impl.DefaultUserTask;
import org.kie.kogito.usertask.impl.DefaultUserTaskConfig;
import org.kie.kogito.usertask.impl.DefaultUserTasks;
import org.kie.kogito.usertask.model.UserTaskModel;

public class UserTaskTest {

    @Test
    public void testNewUserTask() {
        Application app = buildApplication();
        UserTasks userTasks = app.get(UserTasks.class);

        UserTask userTask = userTasks.userTaskById("userTaskId");

        System.out.println(userTask);

        UserTaskModel model = userTask.createModel();
        userTask.createInstance(model);
    }

    public static Application buildApplication() {
        StaticConfig config = new StaticConfig(Addons.EMTPY, new DefaultUserTaskConfig());
        UserTask userTask = new DefaultUserTask("userTaskId", "userTaskName", "userTaskVersion");

        DefaultUserTasks userTaks = new DefaultUserTasks(userTask);
        Application app = new StaticApplication(config, userTaks);

        return app;
    }

}
