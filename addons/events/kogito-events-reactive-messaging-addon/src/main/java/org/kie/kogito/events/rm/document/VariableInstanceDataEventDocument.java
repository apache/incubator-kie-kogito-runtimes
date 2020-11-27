package org.kie.kogito.events.rm.document;

import org.kie.kogito.services.event.VariableInstanceDataEvent;

public class VariableInstanceDataEventDocument extends AbstractDataEventDocument {

    String kogitoVariableName;
    VariableInstanceEventBodyDocument data;

    public VariableInstanceDataEventDocument(VariableInstanceDataEvent event) {
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

        this.setKogitoVariableName(event.getKogitoVariableName());

        VariableInstanceEventBodyDocument bodyDoc = new VariableInstanceEventBodyDocument();
        bodyDoc.setVariableName(event.getData().getVariableName());
        bodyDoc.setVariableValue(event.getData().getVariableValue());
        bodyDoc.setVariablePreviousValue(event.getData().getVariablePreviousValue());
        bodyDoc.setChangeDate(event.getData().getChangeDate());
        bodyDoc.setChangedByNodeId(event.getData().getChangedByNodeId());
        bodyDoc.setChangedByNodeName(event.getData().getChangedByNodeName());
        bodyDoc.setChangedByNodeType(event.getData().getChangedByNodeType());
        bodyDoc.setChangedByUser(event.getData().getChangedByUser());
        bodyDoc.setProcessInstanceId(event.getData().getProcessInstanceId());
        bodyDoc.setRootProcessInstanceId(event.getData().getRootProcessInstanceId());
        bodyDoc.setProcessId(event.getData().getProcessId());
        bodyDoc.setRootProcessId(event.getData().getRootProcessId());
        this.setData(bodyDoc);
    }

    public String getKogitoVariableName() {
        return kogitoVariableName;
    }

    public void setKogitoVariableName(String kogitoVariableName) {
        this.kogitoVariableName = kogitoVariableName;
    }

    public VariableInstanceEventBodyDocument getData() {
        return data;
    }

    public void setData(VariableInstanceEventBodyDocument data) {
        this.data = data;
    }
}
