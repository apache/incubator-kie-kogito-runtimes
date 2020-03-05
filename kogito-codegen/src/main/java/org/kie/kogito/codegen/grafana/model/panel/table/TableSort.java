package org.kie.kogito.codegen.grafana.model.panel.table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableSort {
    @JsonProperty("col")
    public int col = 0;

    @JsonProperty("desc")
    public boolean desc;
}