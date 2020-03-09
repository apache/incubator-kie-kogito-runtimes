package org.kie.kogito.codegen.grafana.model.panel.stat;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Defaults {

    @JsonProperty("mappings")
    public List<String> mappings = new ArrayList<>();

    @JsonProperty("thresholds")
    public Thresholds thresholds = new Thresholds();
}