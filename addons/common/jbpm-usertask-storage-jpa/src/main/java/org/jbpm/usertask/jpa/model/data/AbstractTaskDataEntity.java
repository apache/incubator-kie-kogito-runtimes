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

package org.jbpm.usertask.jpa.model.data;

import org.jbpm.usertask.jpa.model.UserTaskInstanceEntity;

import jakarta.persistence.*;

@MappedSuperclass
@SequenceGenerator(name = "jbpm_user_task_data_mapping_id_seq", sequenceName = "jbpm_user_task_data_mapping_id_seq")
public abstract class AbstractTaskDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "jbpm_user_task_data_mapping_id_seq")
    protected Long id;

    protected String name;

    protected byte[] value;

    @Column(name = "java_type")
    protected String javaType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    abstract UserTaskInstanceEntity getTaskInstance();

    abstract void setTaskInstance(UserTaskInstanceEntity taskInstance);
}
