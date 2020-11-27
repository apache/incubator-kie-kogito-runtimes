package org.kie.kogito.events.rm.document;

import java.util.Date;

public class VariableInstanceEventBodyDocument {

     String variableName;
     Object variableValue;
     Object variablePreviousValue;
     Date changeDate;
     String changedByNodeId;
     String changedByNodeName;
     String changedByNodeType;
     String changedByUser;
     String processInstanceId;
     String rootProcessInstanceId;
     String processId;
     String rootProcessId;

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public Object getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(Object variableValue) {
        this.variableValue = variableValue;
    }

    public Object getVariablePreviousValue() {
        return variablePreviousValue;
    }

    public void setVariablePreviousValue(Object variablePreviousValue) {
        this.variablePreviousValue = variablePreviousValue;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public String getChangedByNodeId() {
        return changedByNodeId;
    }

    public void setChangedByNodeId(String changedByNodeId) {
        this.changedByNodeId = changedByNodeId;
    }

    public String getChangedByNodeName() {
        return changedByNodeName;
    }

    public void setChangedByNodeName(String changedByNodeName) {
        this.changedByNodeName = changedByNodeName;
    }

    public String getChangedByNodeType() {
        return changedByNodeType;
    }

    public void setChangedByNodeType(String changedByNodeType) {
        this.changedByNodeType = changedByNodeType;
    }

    public String getChangedByUser() {
        return changedByUser;
    }

    public void setChangedByUser(String changedByUser) {
        this.changedByUser = changedByUser;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getRootProcessInstanceId() {
        return rootProcessInstanceId;
    }

    public void setRootProcessInstanceId(String rootProcessInstanceId) {
        this.rootProcessInstanceId = rootProcessInstanceId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getRootProcessId() {
        return rootProcessId;
    }

    public void setRootProcessId(String rootProcessId) {
        this.rootProcessId = rootProcessId;
    }
}
