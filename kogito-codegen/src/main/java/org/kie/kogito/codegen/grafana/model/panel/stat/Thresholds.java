package org.kie.kogito.codegen.grafana.model.panel.stat;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Thresholds {

    @JsonProperty("mode")
    public String mode = "absolute";

    @JsonProperty("steps")
    public List<Step> steps = generateDefault();

    private List<Step> generateDefault() {
        List<Step> s = new ArrayList<>();
        s.add(new Step("green", null));
        return s;
    }
}