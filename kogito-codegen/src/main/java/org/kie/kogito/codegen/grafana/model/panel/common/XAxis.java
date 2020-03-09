package org.kie.kogito.codegen.grafana.model.panel.common;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class XAxis {

    @JsonProperty("buckets")
    public String buckets;

    @JsonProperty("mode")
    public String mode;

    @JsonProperty("name")
    public String name;

    @JsonProperty("show")
    public boolean show;

    @JsonProperty("values")
    public List<Double> values;
}