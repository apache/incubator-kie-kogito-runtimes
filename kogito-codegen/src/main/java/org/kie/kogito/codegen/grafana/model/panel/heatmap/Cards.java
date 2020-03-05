package org.kie.kogito.codegen.grafana.model.panel.heatmap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Cards {

    @JsonProperty("cardPadding")
    public String cardPadding;

    @JsonProperty("cardRound")
    public String cardRound;

}
