package org.jbpm.bpmn2.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TextAnnotation implements Serializable {

    private String id;
    private String text;
    private final Map<String, Object> metaData = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setMetaData(String name, Object value) {
        this.metaData.put(name, value);
    }

    public Object getMetaData(String name) {
        return this.metaData.get(name);
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }
}
