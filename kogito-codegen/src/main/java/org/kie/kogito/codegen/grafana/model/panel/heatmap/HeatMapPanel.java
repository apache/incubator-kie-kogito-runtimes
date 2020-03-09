package org.kie.kogito.codegen.grafana.model.panel.heatmap;

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
public class HeatMapPanel extends GrafanaPanel {

    @JsonProperty("color")
    public PanelColor color = new PanelColor();

    @JsonProperty("dataFormat")
    public String dataFormat = "tsbuckets";

    @JsonProperty("reverseYBuckets")
    public boolean reverseYBuckets = false;

    @JsonProperty("hideZeroBuckets")
    public boolean hideZeroBuckets = false;

    @JsonProperty("highlightCards")
    public boolean highlighCards = true;

    @JsonProperty("yAxis")
    public YAxis yAxis = new YAxis();

    @JsonProperty("yBucketBound")
    public String yBucketBound = "auto";

    @JsonProperty("yBucketNumber")
    public String yBucketNumber = null;

    @JsonProperty("yBucketSize")
    public String yBucketSize = null;

    @JsonProperty("cards")
    public Cards cards;

    @JsonProperty("heatmap")
    public HeatMap heatMap;

    @JsonProperty("legend")
    public Legend legend;

    @JsonProperty("tooltip")
    public Tooltip tooltip;

    @JsonProperty("xAxis")
    public XAxis xAxis;

    @JsonProperty("xBucketNumber")
    public String xBucketNumber;

    @JsonProperty("xBucketSize")
    public String xBucketSize;

    public HeatMapPanel() {
    }

    public HeatMapPanel(int id, String title, GrafanaGridPos gridPos, List<GrafanaTarget> targets) {
        super(id, title, "heatmap", gridPos, targets);
    }
}
