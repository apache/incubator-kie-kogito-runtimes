package org.jbpm.usertask.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class TaskProcessInfoEntity {

    @Column(name = "process_instance_id")
    private String processInstanceId;

    @Column(name = "process_id")
    private String processId;

    @Column(name = "process_version")
    private String processVersion;

    @Column(name = "parent_process_instance_id")
    private String parentProcessInstanceId;

    @Column(name = "root_process_instance_id")
    private String rootProcessInstanceId;

    @Column(name = "root_process_id")
    private String rootProcessId;

    public TaskProcessInfoEntity() {
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
    }

    public String getParentProcessInstanceId() {
        return parentProcessInstanceId;
    }

    public void setParentProcessInstanceId(String parentProcessInstanceId) {
        this.parentProcessInstanceId = parentProcessInstanceId;
    }

    public String getRootProcessInstanceId() {
        return rootProcessInstanceId;
    }

    public void setRootProcessInstanceId(String rootProcessInstanceId) {
        this.rootProcessInstanceId = rootProcessInstanceId;
    }

    public String getRootProcessId() {
        return rootProcessId;
    }

    public void setRootProcessId(String rootProcessId) {
        this.rootProcessId = rootProcessId;
    }
}
