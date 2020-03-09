package org.kie.kogito.codegen.grafana.model.panel.table;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NumberStyle extends BaseStyle {

    @JsonProperty("colorMode")
    public String colorMode = null;

    @JsonProperty("colors")
    public List<String> colors = generateDefaultColors();

    @JsonProperty("decimals")
    public int decimals = 2;

    @JsonProperty("thresholds")
    public List<String> thresholds = new ArrayList<>();

    @JsonProperty("unit")
    public String unit = "short";

    public NumberStyle() {
        super("", "number", "/.*/", "right");
    }

    private List<String> generateDefaultColors() {
        List<String> col = new ArrayList<>();
        col.add("rgba(245, 54, 54, 0.9)");
        col.add("rgba(237, 129, 40, 0.89)");
        col.add("rgba(50, 172, 45, 0.97)");
        return col;
    }
}
