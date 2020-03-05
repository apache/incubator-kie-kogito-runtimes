package org.kie.kogito.codegen.grafana.model.panel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrafanaTarget {
    @JsonProperty("expr")
    public String expr;

    @JsonProperty("format")
    public String format;

    @JsonProperty("intervalFactor")
    public int intervalFactor = 2;

    @JsonProperty("refId")
    public String refId = "A";

    @JsonProperty("instant")
    public boolean instant;

    public GrafanaTarget(){}

    public GrafanaTarget(String expr, String format){
        this.expr = expr;
        this.format = format;
    }
}