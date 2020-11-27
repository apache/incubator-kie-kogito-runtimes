package org.kie.kogito.events.rm.document;

import org.bson.Document;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class ProcessInstanceEventBodyDocument {

    String id;
    String parentInstanceId;
    String rootInstanceId;
    String processId;
    String rootProcessId;
    String processName;
    Date startDate;
    Date endDate;
    Integer state;
    String businessKey;
    Set<NodeInstanceEventBodyDocument> nodeInstances;
    Document variables;
    ProcessErrorEventBodyDocument error;
    List<String> roles;
    Set<MilestoneEventBodyDocument> milestones;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentInstanceId() {
        return parentInstanceId;
    }

    public void setParentInstanceId(String parentInstanceId) {
        this.parentInstanceId = parentInstanceId;
    }

    public String getRootInstanceId() {
        return rootInstanceId;
    }

    public void setRootInstanceId(String rootInstanceId) {
        this.rootInstanceId = rootInstanceId;
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

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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
        this.businessKey = businessKey;
    }

    public Set<NodeInstanceEventBodyDocument> getNodeInstances() {
        return nodeInstances;
    }

    public void setNodeInstances(Set<NodeInstanceEventBodyDocument> nodeInstances) {
        this.nodeInstances = nodeInstances;
    }

    public Document getVariables() {
        return variables;
    }

    public void setVariables(Document variables) {
        this.variables = variables;
    }

    public ProcessErrorEventBodyDocument getError() {
        return error;
    }

    public void setError(ProcessErrorEventBodyDocument error) {
        this.error = error;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Set<MilestoneEventBodyDocument> getMilestones() {
        return milestones;
    }

    public void setMilestones(Set<MilestoneEventBodyDocument> milestones) {
        this.milestones = milestones;
    }
}
