package org.kie.kogito.codegen.grafana.model.templating;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GrafanaTemplateCurrent {
    @JsonProperty("tags")
    public List<String> tags;

    @JsonProperty("text")
    public String text;

    @JsonProperty("value")
    public String value;
}