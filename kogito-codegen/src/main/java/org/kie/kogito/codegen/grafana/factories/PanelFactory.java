package org.kie.kogito.codegen.grafana.factories;

import org.kie.kogito.codegen.grafana.model.panel.GaugePanel;
import org.kie.kogito.codegen.grafana.model.panel.GrafanaPanel;
import org.kie.kogito.codegen.grafana.model.panel.PanelType;
import org.kie.kogito.codegen.grafana.model.panel.graph.GraphPanel;
import org.kie.kogito.codegen.grafana.model.panel.heatmap.HeatMapPanel;
import org.kie.kogito.codegen.grafana.model.panel.stat.SingleStatPanel;
import org.kie.kogito.codegen.grafana.model.panel.stat.StatPanel;
import org.kie.kogito.codegen.grafana.model.panel.table.TablePanel;

public class PanelFactory {
    private PanelFactory(){}

    public static GrafanaPanel CreatePanel(PanelType type, int id, String title, String expr){
        switch(type){
            case GRAPH:
                return createGraph(id, title, expr);
            case STAT:
                return createStat(id, title, expr);
            case HEATMAP:
                return createHeartMap(id, title, expr);
            case SINGLESTAT:
                return createSingleStat(id, title, expr);
            case TABLE:
                return createTable(id, title, expr);
            case GAUGE:
                return createGauge(id, title, expr);
            default:
                throw new UnsupportedOperationException("The panel " + type.toString() + " is not supported.");
        }
    }

    static GrafanaPanel createGraph(int id, String title, String expr){
        return new GraphPanel(id, title,
                              GridPosFactory.calculateGridPosById(id),
                              TargetFactory.CreateTargets(expr));
    }

    static GrafanaPanel createStat(int id, String title, String expr){
        return new StatPanel(id, title,
                             GridPosFactory.calculateGridPosById(id),
                             TargetFactory.CreateTargets(expr));
    }

    static GrafanaPanel createHeartMap(int id, String title, String expr) {
        return new HeatMapPanel(id, title,
                                GridPosFactory.calculateGridPosById(id),
                                TargetFactory.CreateTargets(expr));
    }

    static GrafanaPanel createSingleStat(int id, String title, String expr){
        return new SingleStatPanel(id, title,
                                   GridPosFactory.calculateGridPosById(id),
                                   TargetFactory.CreateTargets(expr));
    }

    static GrafanaPanel createGauge(int id, String title, String expr){
        return new GaugePanel(id, title,
                              GridPosFactory.calculateGridPosById(id),
                              TargetFactory.CreateTargets(expr));
    }

    static GrafanaPanel createTable(int id, String title, String expr){
        return new TablePanel(id, title,
                              GridPosFactory.calculateGridPosById(id),
                              TargetFactory.CreateTargets(expr));
    }
}
