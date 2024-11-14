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
import org.jbpm.usertask.jpa.model.TaskReassignmentEntity;
import org.jbpm.usertask.jpa.model.TaskReassignmentType;
import org.jbpm.usertask.jpa.model.UserTaskInstanceEntity;
import org.jbpm.usertask.jpa.repository.TaskReassignmentRepository;
import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.impl.DefaultUserTaskInstance;
import org.kie.kogito.usertask.model.DeadlineInfo;
import org.kie.kogito.usertask.model.Reassignment;

import com.fasterxml.jackson.databind.JavaType;

public class TaskReassignmentEntityMapper implements EntityMapper {

    private final TaskReassignmentRepository repository;

    public TaskReassignmentEntityMapper(TaskReassignmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public void mapInstanceToEntity(UserTaskInstance userTaskInstance, UserTaskInstanceEntity userTaskInstanceEntity) {
        List<TaskReassignmentEntity> reassignments = new ArrayList<>();
        for (DeadlineInfo<Reassignment> reassignment : userTaskInstance.getNotStartedReassignments()) {
            TaskReassignmentEntity entity = new TaskReassignmentEntity();
            entity.setTaskInstance(userTaskInstanceEntity);
            entity.setJavaType(DeadlineInfo.class.getName());
            entity.setValue(JSONUtils.valueToString(reassignment).getBytes(StandardCharsets.UTF_8));
            entity.setType(TaskReassignmentType.NotStarted);
            reassignments.add(entity);
        }
        for (DeadlineInfo<Reassignment> reassignment : userTaskInstance.getNotCompletedReassignments()) {
            TaskReassignmentEntity entity = new TaskReassignmentEntity();
            entity.setTaskInstance(userTaskInstanceEntity);
            entity.setJavaType(DeadlineInfo.class.getName());
            entity.setValue(JSONUtils.valueToString(reassignment).getBytes(StandardCharsets.UTF_8));
            entity.setType(TaskReassignmentType.NotCompleted);
            reassignments.add(entity);
        }
        userTaskInstanceEntity.setReassignments(reassignments);
    }

    @Override
    public void mapEntityToInstance(UserTaskInstanceEntity userTaskInstanceEntity, UserTaskInstance userTaskInstance) {
        List<DeadlineInfo<Reassignment>> notStarted = new ArrayList<>();
        List<DeadlineInfo<Reassignment>> notCompleted = new ArrayList<>();
        JavaType javaType = JSONUtils.buildJavaType(DeadlineInfo.class, Reassignment.class);
        for (TaskReassignmentEntity entity : userTaskInstanceEntity.getReassignments()) {
            DeadlineInfo<Reassignment> deadline = (DeadlineInfo<Reassignment>) JSONUtils.stringTreeToValue(new String(entity.getValue()), javaType);
            switch (entity.getType()) {
                case NotCompleted:
                    notCompleted.add(deadline);
                    break;
                case NotStarted:
                    notStarted.add(deadline);
                    break;
            }
        }

        ((DefaultUserTaskInstance) userTaskInstance).setNotStartedReassignments(notStarted);
        ((DefaultUserTaskInstance) userTaskInstance).setNotCompletedReassignments(notCompleted);
    }
}
