package org.kie.kogito.events.rm.document;

import org.bson.Document;
import org.kie.kogito.services.event.ProcessInstanceDataEvent;

import java.util.stream.Collectors;

public class ProcessInstanceDataEventDocument extends AbstractDataEventDocument {

    String kogitoParentProcessinstanceId;
    String kogitoProcessinstanceState;
    String kogitoReferenceId;
    String kogitoStartFromNode;
    ProcessInstanceEventBodyDocument data;

    public ProcessInstanceDataEventDocument(ProcessInstanceDataEvent event) {
        this.setId(event.getId());
        this.setSpecVersion(event.getSpecVersion());
        this.setSource(event.getSource());
        this.setType(event.getType());
        this.setTime(event.getTime());
        this.setSubject(event.getSubject());
        this.setDataContentType(event.getDataContentType());
        this.setDataSchema(event.getDataSchema());
        this.setKogitoProcessinstanceId(event.getKogitoProcessinstanceId());
        this.setKogitoRootProcessinstanceId(event.getKogitoRootProcessinstanceId());
        this.setKogitoProcessId(event.getKogitoProcessId());
        this.setKogitoRootProcessId(event.getKogitoRootProcessId());
        this.setKogitoAddons(event.getKogitoAddons());

        this.setKogitoParentProcessinstanceId(event.getKogitoParentProcessinstanceId());
        this.setKogitoProcessinstanceState(event.getKogitoProcessinstanceState());
        this.setKogitoReferenceId(event.getKogitoReferenceId());
        this.setKogitoStartFromNode(event.getKogitoStartFromNode());

        ProcessInstanceEventBodyDocument bodyDoc = new ProcessInstanceEventBodyDocument();
        bodyDoc.setId(event.getData().getId());
        bodyDoc.setParentInstanceId(event.getData().getParentInstanceId());
        bodyDoc.setRootInstanceId(event.getData().getRootInstanceId());
        bodyDoc.setProcessId(event.getData().getProcessId());
        bodyDoc.setRootProcessId(event.getData().getRootProcessId());
        bodyDoc.setProcessName(event.getData().getProcessName());
        bodyDoc.setStartDate(event.getData().getStartDate());
        bodyDoc.setEndDate(event.getData().getEndDate());
        bodyDoc.setState(event.getData().getState());
        bodyDoc.setBusinessKey(event.getData().getBusinessKey());
        if (event.getData().getNodeInstances() != null) {
            bodyDoc.setNodeInstances(event.getData().getNodeInstances().stream().map(n -> {
                NodeInstanceEventBodyDocument doc = new NodeInstanceEventBodyDocument();
                doc.setId(n.getId());
                doc.setLeaveTime(n.getLeaveTime());
                doc.setNodeDefinitionId(n.getNodeDefinitionId());
                doc.setNodeId(n.getNodeId());
                doc.setNodeName(n.getNodeName());
                doc.setNodeType(n.getNodeType());
                doc.setTriggerTime(n.getTriggerTime());
                return doc;
            }).collect(Collectors.toSet()));
        }
        if (event.getData().getVariables() != null) {
            bodyDoc.setVariables(new Document(event.getData().getVariables()));
        }
        if (event.getData().getError() != null) {
            ProcessErrorEventBodyDocument errorDoc = new ProcessErrorEventBodyDocument();
            errorDoc.setErrorMessage(event.getData().getError().getErrorMessage());
            errorDoc.setNodeDefinitionId(event.getData().getError().getNodeDefinitionId());
            bodyDoc.setError(errorDoc);
        }
        bodyDoc.setRoles(event.getData().getRoles());
        if (event.getData().getMilestones() != null) {
            bodyDoc.setMilestones(event.getData().getMilestones().stream().map(m -> {
                MilestoneEventBodyDocument doc = new MilestoneEventBodyDocument();
                doc.setId(m.getId());
                doc.setName(m.getName());
                doc.setStatus(m.getStatus());
                return doc;
            }).collect(Collectors.toSet()));
        }
        this.setData(bodyDoc);
    }

    public String getKogitoParentProcessinstanceId() {
        return kogitoParentProcessinstanceId;
    }

    public void setKogitoParentProcessinstanceId(String kogitoParentProcessinstanceId) {
        this.kogitoParentProcessinstanceId = kogitoParentProcessinstanceId;
    }

    public String getKogitoProcessinstanceState() {
        return kogitoProcessinstanceState;
    }

    public void setKogitoProcessinstanceState(String kogitoProcessinstanceState) {
        this.kogitoProcessinstanceState = kogitoProcessinstanceState;
    }

    public String getKogitoReferenceId() {
        return kogitoReferenceId;
    }

    public void setKogitoReferenceId(String kogitoReferenceId) {
        this.kogitoReferenceId = kogitoReferenceId;
    }

    public String getKogitoStartFromNode() {
        return kogitoStartFromNode;
    }

    public void setKogitoStartFromNode(String kogitoStartFromNode) {
        this.kogitoStartFromNode = kogitoStartFromNode;
    }

    public ProcessInstanceEventBodyDocument getData() {
        return data;
    }

    public void setData(ProcessInstanceEventBodyDocument data) {
        this.data = data;
    }
}
