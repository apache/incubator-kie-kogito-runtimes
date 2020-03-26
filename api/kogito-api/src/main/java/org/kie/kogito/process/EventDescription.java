package org.kie.kogito.process;

import java.util.HashMap;
import java.util.Map;

public class EventDescription {

    private String event;
    private String nodeId;
    private String nodeName;
    private String eventType;

    private String nodeInstanceId;

    private String processInstanceId;

    private Object dataType;
    
    private Map<String, String> properties = new HashMap<>();
       
    public EventDescription(String event, String nodeId, String nodeName, String eventType, String nodeInstanceId, String processInstanceId, Object dataType) {
        this.event = event;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.eventType = eventType;
        this.nodeInstanceId = nodeInstanceId;
        this.processInstanceId = processInstanceId;
        this.dataType = dataType;
    }
    
    public EventDescription(String event, String nodeId, String nodeName, String eventType, String nodeInstanceId, String processInstanceId, Object dataType, Map<String, String> properties) {
        this.event = event;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.eventType = eventType;
        this.nodeInstanceId = nodeInstanceId;
        this.processInstanceId = processInstanceId;
        this.dataType = dataType;
        this.properties = properties;
    }

    public String getEvent() {
        return event;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }
    
    public String getEventType() {
        return eventType;
    }

    public String getNodeInstanceId() {
        return nodeInstanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public Object getDataType() {
        return dataType;
    }
    
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "EventDesciption [event=" + event + ", nodeId=" + nodeId + ", nodeName=" + nodeName + ", eventType=" + eventType + ", nodeInstanceId=" + nodeInstanceId + ", processInstanceId=" + processInstanceId +
               ", dataType=" + dataType + ", properties=" + properties + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
        result = prime * result + ((event == null) ? 0 : event.hashCode());
        result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
        result = prime * result + ((nodeName == null) ? 0 : nodeName.hashCode());
        result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
        result = prime * result + ((processInstanceId == null) ? 0 : processInstanceId.hashCode());
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
        EventDescription other = (EventDescription) obj;
        if (dataType == null) {
            if (other.dataType != null)
                return false;
        } else if (!dataType.equals(other.dataType))
            return false;
        if (event == null) {
            if (other.event != null)
                return false;
        } else if (!event.equals(other.event))
            return false;
        if (nodeId == null) {
            if (other.nodeId != null)
                return false;
        } else if (!nodeId.equals(other.nodeId))
            return false;
        if (nodeName == null) {
            if (other.nodeName != null)
                return false;
        } else if (!nodeName.equals(other.nodeName))
            return false;
        if (eventType == null) {
            if (other.eventType != null)
                return false;
        } else if (!eventType.equals(other.eventType))
            return false;
        if (processInstanceId == null) {
            if (other.processInstanceId != null)
                return false;
        } else if (!processInstanceId.equals(other.processInstanceId))
            return false;
        return true;
    }
    
    
}
