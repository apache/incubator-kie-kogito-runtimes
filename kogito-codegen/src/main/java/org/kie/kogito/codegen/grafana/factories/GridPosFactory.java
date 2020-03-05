package org.kie.kogito.codegen.grafana.factories;

import org.kie.kogito.codegen.grafana.model.panel.GrafanaGridPos;

public class GridPosFactory {
    public static GrafanaGridPos CalculateGridPosById(int id){
        return new GrafanaGridPos(12 * ( (id - 1) % 2), 8 * ((id - 1) / 2), 12, 8);
    }
}
