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

package org.jbpm.usertask.jpa.mapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.usertask.jpa.mapper.json.utils.JSONUtils;
import org.jbpm.usertask.jpa.model.TaskDeadlineTimerEntity;
import org.jbpm.usertask.jpa.model.TaskDeadlineType;
import org.jbpm.usertask.jpa.model.UserTaskInstanceEntity;
import org.jbpm.usertask.jpa.repository.TaskDeadlineTimerRepository;
import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.impl.DefaultUserTaskInstance;
import org.kie.kogito.usertask.model.Notification;

public class TaskDeadlineTimerEntityMapper implements EntityMapper {

    private final TaskDeadlineTimerRepository repository;

    public TaskDeadlineTimerEntityMapper(TaskDeadlineTimerRepository repository) {
        this.repository = repository;
    }

    @Override
    public void mapInstanceToEntity(UserTaskInstance instance, UserTaskInstanceEntity userTaskInstanceEntity) {
        DefaultUserTaskInstance userTaskInstance = (DefaultUserTaskInstance) instance;
        List<TaskDeadlineTimerEntity> timers = new ArrayList<>();
        for (Map.Entry<String, Notification> timer : userTaskInstance.getNotStartedDeadlinesTimers().entrySet()) {
            TaskDeadlineTimerEntity entity = new TaskDeadlineTimerEntity();
            entity.setTaskInstance(userTaskInstanceEntity);
            entity.setJobId(timer.getKey());
            entity.setJavaType(Notification.class.getName());
            entity.setValue(JSONUtils.valueToString(timer.getValue()).getBytes(StandardCharsets.UTF_8));
            entity.setType(TaskDeadlineType.NotStarted);
            timers.add(entity);
        }
        for (Map.Entry<String, Notification> timer : userTaskInstance.getNotCompletedDeadlinesTimers().entrySet()) {
            TaskDeadlineTimerEntity entity = new TaskDeadlineTimerEntity();
            entity.setJobId(timer.getKey());
            entity.setTaskInstance(userTaskInstanceEntity);
            entity.setJavaType(Notification.class.getName());
            entity.setValue(JSONUtils.valueToString(timer.getValue()).getBytes(StandardCharsets.UTF_8));
            entity.setType(TaskDeadlineType.NotCompleted);
            timers.add(entity);
        }
        userTaskInstanceEntity.setDeadlineTimers(timers);
    }

    @Override
    public void mapEntityToInstance(UserTaskInstanceEntity userTaskInstanceEntity, UserTaskInstance userTaskInstance) {
        Map<String, Notification> notStarted = new HashMap<>();
        Map<String, Notification> notCompleted = new HashMap<>();
        for (TaskDeadlineTimerEntity entity : userTaskInstanceEntity.getDeadlineTimers()) {
            Notification notification = (Notification) JSONUtils.stringTreeToValue(new String(entity.getValue()), Notification.class.getName());
            switch (entity.getType()) {
                case NotCompleted:
                    notCompleted.put(entity.getJobId(), notification);
                    break;
                case NotStarted:
                    notStarted.put(entity.getJobId(), notification);
                    break;
            }
        }

        ((DefaultUserTaskInstance) userTaskInstance).setNotStartedDeadlinesTimers(notStarted);
        ((DefaultUserTaskInstance) userTaskInstance).setNotCompletedDeadlinesTimers(notCompleted);
    }
}
