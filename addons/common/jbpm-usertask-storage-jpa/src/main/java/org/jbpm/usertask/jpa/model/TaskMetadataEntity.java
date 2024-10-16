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

package org.jbpm.usertask.jpa.model;

import java.util.Objects;

import jakarta.persistence.*;

@Entity
@Table(name = "jbpm_user_tasks_metadata")
@SequenceGenerator(name = "jbpm_user_task_metadata_id_seq", sequenceName = "jbpm_user_task_metadata_id_seq")
public class TaskMetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "jbpm_user_task_metadata_id_seq")
    private Long id;

    @Column(name = "metadata_name")
    protected String name;

    @Column(name = "metadata_value")
    protected String value;

    @Column(name = "java_type")
    protected String javaType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", foreignKey = @ForeignKey(name = "jbpm_user_tasks_metadata_tid"))
    private UserTaskInstanceEntity taskInstance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public UserTaskInstanceEntity getTaskInstance() {
        return taskInstance;
    }

    public void setTaskInstance(UserTaskInstanceEntity taskInstance) {
        this.taskInstance = taskInstance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TaskMetadataEntity that = (TaskMetadataEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value) && Objects.equals(javaType, that.javaType) && Objects.equals(taskInstance,
                that.taskInstance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, javaType, taskInstance);
    }
}
