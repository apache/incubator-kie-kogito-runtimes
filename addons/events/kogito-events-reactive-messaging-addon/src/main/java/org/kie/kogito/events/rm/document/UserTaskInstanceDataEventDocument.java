package org.kie.kogito.events.rm.document;

import org.bson.Document;
import org.kie.kogito.services.event.UserTaskInstanceDataEvent;


public class UserTaskInstanceDataEventDocument extends AbstractDataEventDocument {

    String kogitoUserTaskinstanceId;
    String kogitoUserTaskinstanceState;
    UserTaskInstanceEventBodyDocument data;

    public UserTaskInstanceDataEventDocument(UserTaskInstanceDataEvent event) {
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

        this.setKogitoUserTaskinstanceId(event.getKogitoUserTaskinstanceId());
        this.setKogitoUserTaskinstanceState(event.getKogitoUserTaskinstanceState());

        UserTaskInstanceEventBodyDocument bodyDoc = new UserTaskInstanceEventBodyDocument();
        bodyDoc.setId(event.getData().getId());
        bodyDoc.setTaskName(event.getData().getTaskName());
        bodyDoc.setTaskDescription(event.getData().getTaskDescription());
        bodyDoc.setTaskPriority(event.getData().getTaskPriority());
        bodyDoc.setReferenceName(event.getData().getReferenceName());
        bodyDoc.setStartDate(event.getData().getStartDate());
        bodyDoc.setCompleteDate(event.getData().getCompleteDate());
        bodyDoc.setState(event.getData().getState());
        bodyDoc.setActualOwner(event.getData().getActualOwner());
        bodyDoc.setPotentialUsers(event.getData().getPotentialUsers());
        bodyDoc.setPotentialGroups(event.getData().getPotentialGroups());
        bodyDoc.setExcludedUsers(event.getData().getExcludedUsers());
        bodyDoc.setAdminUsers(event.getData().getAdminUsers());
        bodyDoc.setAdminGroups(event.getData().getAdminGroups());
        bodyDoc.setInputs(new Document(event.getData().getInputs()));
        bodyDoc.setOutputs(new Document(event.getData().getOutputs()));
        bodyDoc.setProcessInstanceId(event.getData().getProcessInstanceId());
        bodyDoc.setRootProcessInstanceId(event.getData().getRootProcessInstanceId());
        bodyDoc.setProcessId(event.getData().getProcessId());
        bodyDoc.setRootProcessId(event.getData().getRootProcessId());
        this.setData(bodyDoc);
    }

    public String getKogitoUserTaskinstanceId() {
        return kogitoUserTaskinstanceId;
    }

    public void setKogitoUserTaskinstanceId(String kogitoUserTaskinstanceId) {
        this.kogitoUserTaskinstanceId = kogitoUserTaskinstanceId;
    }

    public String getKogitoUserTaskinstanceState() {
        return kogitoUserTaskinstanceState;
    }

    public void setKogitoUserTaskinstanceState(String kogitoUserTaskinstanceState) {
        this.kogitoUserTaskinstanceState = kogitoUserTaskinstanceState;
    }

    public UserTaskInstanceEventBodyDocument getData() {
        return data;
    }

    public void setData(UserTaskInstanceEventBodyDocument data) {
        this.data = data;
    }
}
