package org.kie.kogito.codegen.grafana.model.panel.graph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AliasColors {

    @JsonProperty("colors")
    public String colors;
}
