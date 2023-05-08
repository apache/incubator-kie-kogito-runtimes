/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.event.process;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NodeInstanceEventBody {

    private String processInstanceId;
    private String connectionNodeInstanceId;
    private String id;
    private String nodeId;
    private String nodeDefinitionId;
    private String nodeName;
    private String nodeType;
    private Date eventTime;
    private Integer eventType;
    private Integer exitType;
    private Map<String, Object> data;

    private NodeInstanceEventBody() {
        this.data = new HashMap<>();
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getId() {
        return id;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeDefinitionId() {
        return nodeDefinitionId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public Integer getExitType() {
        return exitType;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Integer getEventType() {
        return eventType;
    }

    public String getConnectionNodeInstanceId() {
        return connectionNodeInstanceId;
    }

    @Override
    public String toString() {
        return "NodeInstanceEventBody [processInstanceId=" + processInstanceId + ", connectionNodeInstanceId="
                + connectionNodeInstanceId + ", id=" + id + ", nodeId=" + nodeId + ", nodeDefinitionId="
                + nodeDefinitionId + ", nodeName=" + nodeName + ", nodeType=" + nodeType + ", eventTime=" + eventTime
                + ", eventType=" + eventType + ", exitType=" + exitType + ", data=" + data + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NodeInstanceEventBody other = (NodeInstanceEventBody) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public Builder update() {
        return new Builder(this);
    }

    public static Builder create() {
        return new Builder(new NodeInstanceEventBody());
    }

    public static class Builder {

        private NodeInstanceEventBody instance;

        private Builder(NodeInstanceEventBody instance) {
            this.instance = instance;
        }

        public Builder processInstanceId(String processInstanceId) {
            instance.processInstanceId = processInstanceId;
            return this;
        }

        public Builder id(String id) {
            instance.id = id;
            return this;
        }

        public Builder nodeId(String nodeId) {
            instance.nodeId = nodeId;
            return this;
        }

        public Builder nodeDefinitionId(String nodeDefinitionId) {
            instance.nodeDefinitionId = nodeDefinitionId;
            return this;
        }

        public Builder nodeName(String nodeName) {
            instance.nodeName = nodeName;
            return this;
        }

        public Builder nodeType(String nodeType) {
            instance.nodeType = nodeType;
            return this;
        }

        public Builder data(String name, Object value) {
            instance.data.put(name, value);
            return this;
        }

        public Builder eventType(Integer eventType) {
            instance.eventType = eventType;
            return this;
        }

        public Builder eventTime(Date eventTime) {
            instance.eventTime = eventTime;
            return this;
        }

        public Builder exitType(Integer exitType) {
            instance.exitType = exitType;
            return this;
        }

        public Builder connectionNodeInstanceId(String connectionNodeInstanceId) {
            instance.connectionNodeInstanceId = connectionNodeInstanceId;
            return this;
        }

        public NodeInstanceEventBody build() {
            return instance;
        }
    }
}
