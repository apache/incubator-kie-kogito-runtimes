package org.kie.kogito.codegen.grafana.model.panel.common;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Options {

    @JsonProperty("dataLinks")
    public List<String> dataLinks;
}
