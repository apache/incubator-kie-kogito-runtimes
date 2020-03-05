package org.kie.kogito.codegen.grafana.model.panel.stat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Step {

    @JsonProperty("color")
    public String color;

    @JsonProperty("value")
    public Double value = null;

    public Step(){}

    public Step(String color, Double value){
        this.color = color;
        this.value = value;
    }
}
