/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.index.model;

import java.time.ZonedDateTime;
import java.util.List;

public class KogitoMetadata {

    private ZonedDateTime lastUpdate;
    private List<ProcessInstanceMeta> processInstances;
    private List<UserTaskInstanceMeta> userTasks;

    public ZonedDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(ZonedDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<ProcessInstanceMeta> getProcessInstances() {
        return processInstances;
    }

    public void setProcessInstances(List<ProcessInstanceMeta> processInstances) {
        this.processInstances = processInstances;
    }

    public List<UserTaskInstanceMeta> getUserTasks() {
        return userTasks;
    }

    public void setUserTasks(List<UserTaskInstanceMeta> userTasks) {
        this.userTasks = userTasks;
    }

    @Override
    public String toString() {
        return "KogitoMeta{" +
                "lastUpdate=" + lastUpdate +
                ", processInstances=" + processInstances +
                ", userTasks=" + userTasks +
                '}';
    }
}
