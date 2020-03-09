package org.kie.kogito.codegen.grafana.model.templating;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrafanaTemplating {

    @JsonProperty("enable")
    public boolean enable;

    @JsonProperty("list")
    public List<GrafanaTemplate> list;
}