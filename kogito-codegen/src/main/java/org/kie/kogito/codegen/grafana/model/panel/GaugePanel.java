package org.kie.kogito.codegen.grafana.model.panel;

import java.util.List;

public class GaugePanel extends GrafanaPanel {

    public GaugePanel() {
        super();
    }

    public GaugePanel(int id, String title, GrafanaGridPos gridPos, List<GrafanaTarget> targets) {
        super(id, title, "gauge", gridPos, targets);
    }
}
