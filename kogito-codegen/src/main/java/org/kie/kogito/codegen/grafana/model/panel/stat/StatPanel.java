package org.kie.kogito.codegen.grafana.model.panel.stat;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.kie.kogito.codegen.grafana.model.panel.GrafanaGridPos;
import org.kie.kogito.codegen.grafana.model.panel.GrafanaPanel;
import org.kie.kogito.codegen.grafana.model.panel.GrafanaTarget;

public class StatPanel extends GrafanaPanel {

    @JsonProperty("options")
    public StatOptions options = new StatOptions();

    public StatPanel() {
        super();
    }

    public StatPanel(int id, String title, GrafanaGridPos gridPos, List<GrafanaTarget> targets) {
        super(id, title, "stat", gridPos, targets);
    }
}
