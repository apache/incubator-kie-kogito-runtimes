package org.kie.kogito.codegen.grafana;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.kie.kogito.codegen.grafana.model.GrafanaDashboard;
import org.kie.kogito.codegen.grafana.model.functions.GrafanaFunction;
import org.kie.kogito.codegen.grafana.model.panel.GrafanaPanel;
import org.kie.kogito.codegen.grafana.model.panel.PanelType;

public interface IJGrafana {
    GrafanaDashboard getDashboard();

    GrafanaPanel addPanel(PanelType type, String title, String expr);

    GrafanaPanel addPanel(PanelType type, String title, String expr, HashMap<Integer, GrafanaFunction> functions);

    boolean removePanelByTitle(String title);

    GrafanaPanel getPanelByTitle(String title);

    String serialize() throws IOException;

    void writeToFile(File file) throws IOException;
}
