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
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcessInstanceMeta {

    private String id;
    private String processId;
    private String processName;
    private Integer state;
    private String businessKey;
    private String endpoint;
    private Set<String> roles;
    @JsonProperty("startDate")
    private ZonedDateTime start;
    @JsonProperty("endDate")
    private ZonedDateTime end;
    @JsonProperty("rootInstanceId")
    private String rootProcessInstanceId;
    private String rootProcessId;
    @JsonProperty("parentInstanceId")
    private String parentProcessInstanceId;
    private ZonedDateTime lastUpdate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id != null && !id.trim().isEmpty()) {
            this.id = id;
        }
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
    
    public String getBusinessKey() {
        return businessKey;
    }
    
    public void setBusinessKey(String businessKey) {
        if (businessKey != null && !businessKey.trim().isEmpty()) {
            this.businessKey = businessKey;
        }
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

    public void setEnd(ZonedDateTime end) {
        this.end = end;
    }

    public String getRootProcessInstanceId() {
        return rootProcessInstanceId;
    }

    public void setRootProcessInstanceId(String id) {
        if (id != null && !id.trim().isEmpty()) {
            this.rootProcessInstanceId = id;
        }
    }

    public String getRootProcessId() {
        return rootProcessId;
    }

    public void setRootProcessId(String rootProcessId) {
        this.rootProcessId = rootProcessId;
    }

    public String getParentProcessInstanceId() {
        return parentProcessInstanceId;
    }

    public void setParentProcessInstanceId(String id) {
        if (id != null && !id.trim().isEmpty()) {
            this.parentProcessInstanceId = id;
        }
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public ZonedDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(ZonedDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return "ProcessInstanceMeta{" +
                "id='" + id + '\'' +
                ", processId='" + processId + '\'' +
                ", processName='" + processName + '\'' +
                ", state=" + state +
                ", businessKey=" + businessKey +
                ", endpoint='" + endpoint + '\'' +
                ", roles=" + roles +
                ", start=" + start +
                ", end=" + end +
                ", rootProcessInstanceId='" + rootProcessInstanceId + '\'' +
                ", rootProcessId='" + rootProcessId + '\'' +
                ", parentProcessInstanceId='" + parentProcessInstanceId + '\'' +
                ", lastUpdated=" + lastUpdate +
                '}';
    }
}
