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
package org.kie.kogito.event.process;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ProcessInstanceStateEventBody {

    public static final int EVENT_TYPE_STARTED = 1;
    public static final int EVENT_TYPE_ENDED = 2;

    // common fields for events
    private Date eventDate;

    private String eventUser;

    private Integer eventType; // start, complete, error....

    // data fields for process instance event

    private String processId;

    private String processVersion;

    private String processType;

    private String processInstanceId;

    // custom data fields

    private String businessKey;

    private String processName;

    private String parentInstanceId;

    private String rootProcessId;

    private String rootProcessInstanceId;

    private Integer state;

    private Set<String> roles;

    public Date getEventDate() {
        return eventDate;
    }

    public String getEventUser() {
        return eventUser;
    }

    public Integer getEventType() {
        return eventType;
    }

    public String getProcessId() {
        return processId;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public String getProcessType() {
        return processType;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getProcessName() {
        return processName;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public String getParentInstanceId() {
        return parentInstanceId;
    }

    public String getRootProcessInstanceId() {
        return rootProcessInstanceId;
    }

    public String getRootProcessId() {
        return rootProcessId;
    }

    public Integer getState() {
        return state;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Builder update() {
        return new Builder(this);
    }

    public Map<String, Object> metaData() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(ProcessInstanceEventMetadata.PROCESS_INSTANCE_ID_META_DATA, processInstanceId);
        metadata.put(ProcessInstanceEventMetadata.PROCESS_VERSION_META_DATA, processVersion);
        metadata.put(ProcessInstanceEventMetadata.PARENT_PROCESS_INSTANCE_ID_META_DATA, parentInstanceId);
        metadata.put(ProcessInstanceEventMetadata.ROOT_PROCESS_INSTANCE_ID_META_DATA, rootProcessInstanceId);
        metadata.put(ProcessInstanceEventMetadata.PROCESS_ID_META_DATA, processId);
        metadata.put(ProcessInstanceEventMetadata.PROCESS_TYPE_META_DATA, processType);
        metadata.put(ProcessInstanceEventMetadata.ROOT_PROCESS_ID_META_DATA, rootProcessId);
        metadata.put(ProcessInstanceEventMetadata.PROCESS_INSTANCE_STATE_META_DATA, String.valueOf(state));
        return metadata;
    }

    @Override
    public String toString() {
        return "ProcessInstanceStateEventBody [eventDate=" + eventDate + ", eventUser=" + eventUser + ", eventType=" + eventType + ", processId=" + processId + ", processVersion=" + processVersion
                + ", processType=" + processType + ", processInstanceId=" + processInstanceId + ", businessKey=" + businessKey + ", processName=" + processName + ", parentInstanceId="
                + parentInstanceId + ", rootProcessId=" + rootProcessId + ", rootProcessInstanceId=" + rootProcessInstanceId + ", state=" + state + ", roles=" + roles + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(processInstanceId);
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProcessInstanceStateEventBody other = (ProcessInstanceStateEventBody) obj;
        return Objects.equals(processInstanceId, other.processInstanceId);
    }

    public static Builder create() {
        return new Builder(new ProcessInstanceStateEventBody());
    }

    public static class Builder {

        private ProcessInstanceStateEventBody instance;

        public Builder(ProcessInstanceStateEventBody instance) {
            this.instance = instance;
        }

        public Builder eventDate(Date eventDate) {
            this.instance.eventDate = eventDate;
            return this;
        }

        public Builder eventUser(String userId) {
            this.instance.eventUser = userId;
            return this;
        }

        public Builder eventType(Integer eventType) {
            this.instance.eventType = eventType;
            return this;
        }

        public Builder processId(String processId) {
            this.instance.processId = processId;
            return this;
        }

        public Builder processVersion(String processVersion) {
            this.instance.processVersion = processVersion;
            return this;
        }

        public Builder processType(String processType) {
            this.instance.processType = processType;
            return this;
        }

        public Builder processInstanceId(String processInstanceId) {
            this.instance.processInstanceId = processInstanceId;
            return this;
        }

        public Builder businessKey(String businessKey) {
            this.instance.businessKey = businessKey;
            return this;
        }

        public Builder processName(String processName) {
            this.instance.processName = processName;
            return this;
        }

        public Builder parentInstanceId(String parentInstanceId) {
            this.instance.parentInstanceId = parentInstanceId;
            return this;
        }

        public Builder rootProcessId(String rootProcessId) {
            this.instance.rootProcessId = rootProcessId;
            return this;
        }

        public Builder rootProcessInstanceId(String rootProcessInstanceId) {
            this.instance.rootProcessInstanceId = rootProcessInstanceId;
            return this;
        }

        public Builder state(Integer state) {
            this.instance.state = state;
            return this;
        }

        public Builder roles(String... roles) {
            instance.roles = Set.of(roles);
            return this;
        }

        public ProcessInstanceStateEventBody build() {
            return instance;
        }
    }
}
