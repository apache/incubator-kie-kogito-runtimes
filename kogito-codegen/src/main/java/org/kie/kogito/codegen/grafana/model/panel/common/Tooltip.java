package org.kie.kogito.codegen.grafana.model.panel.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tooltip {

    @JsonProperty("shared")
    public boolean shared;

    @JsonProperty("sort")
    public int sort;

    @JsonProperty("value_type")
    public String valueType;

    @JsonProperty("show")
    public boolean show;

    @JsonProperty("showHistogram")
    public boolean showHistogram;
}