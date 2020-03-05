package org.kie.kogito.codegen.grafana.model.panel.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Legend {
    @JsonProperty("avg")
    public boolean avg;

    @JsonProperty("alignAsTable")
    public boolean alignAsTable;

    @JsonProperty("rightSide")
    public boolean rightSide;

    @JsonProperty("legend")
    public boolean legend;

    @JsonProperty("current")
    public boolean current;

    @JsonProperty("max")
    public boolean max;

    @JsonProperty("min")
    public boolean min;

    @JsonProperty("show")
    public boolean show;

    @JsonProperty("total")
    public boolean total;

    @JsonProperty("values")
    public boolean values;
}
