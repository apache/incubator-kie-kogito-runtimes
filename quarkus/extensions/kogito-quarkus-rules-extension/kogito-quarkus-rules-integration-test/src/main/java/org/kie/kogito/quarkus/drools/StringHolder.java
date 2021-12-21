package org.kie.kogito.quarkus.drools;

public class StringHolder {
    private String value;

    public StringHolder(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
