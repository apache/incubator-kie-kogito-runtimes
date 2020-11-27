package org.kie.kogito.events.rm.document;

public class AbstractDataEventDocument {

    String id;
    String specVersion;
    String source;
    String type;
    String time;
    String subject;
    String dataContentType;
    String dataSchema;
    String kogitoProcessinstanceId;
    String kogitoRootProcessinstanceId;
    String kogitoProcessId;
    String kogitoRootProcessId;
    String kogitoAddons;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDataContentType() {
        return dataContentType;
    }

    public void setDataContentType(String dataContentType) {
        this.dataContentType = dataContentType;
    }

    public String getDataSchema() {
        return dataSchema;
    }

    public void setDataSchema(String dataSchema) {
        this.dataSchema = dataSchema;
    }

    public String getKogitoProcessinstanceId() {
        return kogitoProcessinstanceId;
    }

    public void setKogitoProcessinstanceId(String kogitoProcessinstanceId) {
        this.kogitoProcessinstanceId = kogitoProcessinstanceId;
    }

    public String getKogitoRootProcessinstanceId() {
        return kogitoRootProcessinstanceId;
    }

    public void setKogitoRootProcessinstanceId(String kogitoRootProcessinstanceId) {
        this.kogitoRootProcessinstanceId = kogitoRootProcessinstanceId;
    }

    public String getKogitoProcessId() {
        return kogitoProcessId;
    }

    public void setKogitoProcessId(String kogitoProcessId) {
        this.kogitoProcessId = kogitoProcessId;
    }

    public String getKogitoRootProcessId() {
        return kogitoRootProcessId;
    }

    public void setKogitoRootProcessId(String kogitoRootProcessId) {
        this.kogitoRootProcessId = kogitoRootProcessId;
    }

    public String getKogitoAddons() {
        return kogitoAddons;
    }

    public void setKogitoAddons(String kogitoAddons) {
        this.kogitoAddons = kogitoAddons;
    }
}
