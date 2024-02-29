package org.jbpm.flow.migration.model;

import java.util.Objects;

public class ProcessDefinitionMigrationPlan {

    private String processId;
    private String processVersion;

    public ProcessDefinitionMigrationPlan() {
        // do nothing
    }

    public ProcessDefinitionMigrationPlan(String processId, String processVersion) {
        this.processId = processId;
        this.processVersion = processVersion;
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

    @Override
    public String toString() {
        return "Process [processId=" + processId + ", processVersion=" + processVersion + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(processId, processVersion);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProcessDefinitionMigrationPlan other = (ProcessDefinitionMigrationPlan) obj;
        return Objects.equals(processId, other.processId) && Objects.equals(processVersion, other.processVersion);
    }

}