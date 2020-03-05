package org.kie.kogito.codegen.grafana.model.panel.graph;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.kie.kogito.codegen.grafana.model.panel.GrafanaGridPos;
import org.kie.kogito.codegen.grafana.model.panel.GrafanaPanel;
import org.kie.kogito.codegen.grafana.model.panel.GrafanaTarget;
import org.kie.kogito.codegen.grafana.model.panel.common.Legend;
import org.kie.kogito.codegen.grafana.model.panel.common.Tooltip;
import org.kie.kogito.codegen.grafana.model.panel.common.XAxis;
import org.kie.kogito.codegen.grafana.model.panel.common.YAxis;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GraphPanel extends GrafanaPanel {

    @JsonProperty("bars")
    public boolean bars = false;

    @JsonProperty("dashes")
    public boolean dashes = false;

    @JsonProperty("fill")
    public int fill = 1;

    @JsonProperty("fillGradient")
    public int fillGradient = 0;

    @JsonProperty("hiddenSeries")
    public boolean hiddenSeries = false;

    @JsonProperty("linewidth")
    public int linewidth = 1;

    @JsonProperty("pointradius")
    public int pointRadius = 2;

    @JsonProperty("points")
    public boolean points = false;

    @JsonProperty("percentage")
    public boolean percentage = false;

    @JsonProperty("renderer")
    public String renderer = "flot";

    @JsonProperty("spaceLength")
    public int spaceLength = 10;

    @JsonProperty("stack")
    public boolean stack = false;

    @JsonProperty("steppedLine")
    public boolean steppedLine = false;

    @JsonProperty("lines")
    public boolean lines = true;

    @JsonProperty("aliasColors")
    public AliasColors aliasColors;

    @JsonProperty("dashLength")
    public int dashLength;

    @JsonProperty("legend")
    public Legend legend;

    @JsonProperty("nullPointMode")
    public String nullPointMode;

    @JsonProperty("seriesOverrides")
    public List<String> seriesOverrides;

    @JsonProperty("thresholds")
    public List<String> thresholds;

    @JsonProperty("timeRegions")
    public List<String> timeRegions;

    @JsonProperty("tooltip")
    public Tooltip tooltip;

    @JsonProperty("xaxis")
    public XAxis xaxis;

    @JsonProperty("yaxes")
    public List<YAxis> yaxes;

    @JsonProperty("yaxis")
    public GraphYAxis yaxis;

    public GraphPanel(){
        super();
    }

    public GraphPanel(int id, String title, GrafanaGridPos gridPos, List<GrafanaTarget> targets) {
        super(id, title, "graph", gridPos, targets);
    }
}
