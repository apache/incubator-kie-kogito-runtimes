package org.kie.kogito.codegen.grafana.model.panel.heatmap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HeatMap {
    @JsonProperty("thxforthedocumentation")
    public String thx;
}
