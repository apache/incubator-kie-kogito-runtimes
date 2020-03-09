package org.kie.kogito.codegen.grafana.model.panel.stat;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldOptions {

    @JsonProperty("calcs")
    public List<String> calcs = generateDefault();

    @JsonProperty("defaults")
    public Defaults defaults = new Defaults();

    @JsonProperty("overrides")
    public List<String> overrides = new ArrayList<>();

    @JsonProperty("values")
    public boolean values = false;

    private List<String> generateDefault() {
        List<String> s = new ArrayList<>();
        s.add("last");
        return s;
    }
}
