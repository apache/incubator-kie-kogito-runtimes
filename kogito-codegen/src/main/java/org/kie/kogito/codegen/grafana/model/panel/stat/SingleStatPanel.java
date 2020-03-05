package org.kie.kogito.codegen.grafana.model.panel.stat;

import java.util.List;

import org.kie.kogito.codegen.grafana.model.panel.GrafanaGridPos;
import org.kie.kogito.codegen.grafana.model.panel.GrafanaPanel;
import org.kie.kogito.codegen.grafana.model.panel.GrafanaTarget;

public class SingleStatPanel extends GrafanaPanel {

    public SingleStatPanel(){
        super();
    }

    public SingleStatPanel(int id, String title, GrafanaGridPos gridPos, List<GrafanaTarget> targets) {
        super(id, title, "singleStat", gridPos, targets);
    }
}
