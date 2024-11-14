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
import java.util.List;

import org.jbpm.usertask.jpa.mapper.json.utils.JSONUtils;
import org.jbpm.usertask.jpa.model.TaskDeadlineEntity;
import org.jbpm.usertask.jpa.model.TaskDeadlineType;
import org.jbpm.usertask.jpa.model.UserTaskInstanceEntity;
import org.jbpm.usertask.jpa.repository.TaskDeadlineRepository;
import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.impl.DefaultUserTaskInstance;
import org.kie.kogito.usertask.model.DeadlineInfo;
import org.kie.kogito.usertask.model.Notification;

import com.fasterxml.jackson.databind.JavaType;

public class TaskDeadlineEntityMapper implements EntityMapper {

    private final TaskDeadlineRepository repository;

    public TaskDeadlineEntityMapper(TaskDeadlineRepository repository) {
        this.repository = repository;
    }

    @Override
    public void mapInstanceToEntity(UserTaskInstance userTaskInstance, UserTaskInstanceEntity userTaskInstanceEntity) {
        List<TaskDeadlineEntity> deadlines = new ArrayList<>();
        for (DeadlineInfo<Notification> deadline : userTaskInstance.getNotStartedDeadlines()) {
            TaskDeadlineEntity entity = new TaskDeadlineEntity();
            entity.setTaskInstance(userTaskInstanceEntity);
            entity.setJavaType(DeadlineInfo.class.getName());
            entity.setValue(JSONUtils.valueToString(deadline).getBytes(StandardCharsets.UTF_8));
            entity.setType(TaskDeadlineType.NotStarted);
            deadlines.add(entity);
        }
        for (DeadlineInfo<Notification> deadline : userTaskInstance.getNotCompletedDeadlines()) {
            TaskDeadlineEntity entity = new TaskDeadlineEntity();
            entity.setTaskInstance(userTaskInstanceEntity);
            entity.setJavaType(DeadlineInfo.class.getName());
            entity.setValue(JSONUtils.valueToString(deadline).getBytes(StandardCharsets.UTF_8));
            entity.setType(TaskDeadlineType.NotCompleted);
            deadlines.add(entity);
        }
        userTaskInstanceEntity.setDeadlines(deadlines);
    }

    @Override
    public void mapEntityToInstance(UserTaskInstanceEntity userTaskInstanceEntity, UserTaskInstance userTaskInstance) {
        List<DeadlineInfo<Notification>> notStarted = new ArrayList<>();
        List<DeadlineInfo<Notification>> notCompleted = new ArrayList<>();
        JavaType javaType = JSONUtils.buildJavaType(DeadlineInfo.class, Notification.class);
        for (TaskDeadlineEntity entity : userTaskInstanceEntity.getDeadlines()) {
            DeadlineInfo<Notification> deadline = (DeadlineInfo<Notification>) JSONUtils.stringTreeToValue(new String(entity.getValue()), javaType);
            switch (entity.getType()) {
                case NotCompleted:
                    notCompleted.add(deadline);
                    break;
                case NotStarted:
                    notStarted.add(deadline);
                    break;
            }
        }

        ((DefaultUserTaskInstance) userTaskInstance).setNotStartedDeadlines(notStarted);
        ((DefaultUserTaskInstance) userTaskInstance).setNotCompletedDeadlines(notCompleted);
    }
}
