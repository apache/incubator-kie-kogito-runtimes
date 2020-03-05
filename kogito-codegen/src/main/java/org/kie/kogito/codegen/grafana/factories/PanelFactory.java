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

    public static GrafanaPanel CreatePanel(PanelType type, int id, String title, String expr){
        switch(type){
            case GRAPH:
                return CreateGraph(id, title, expr);
            case STAT:
                return CreateStat(id, title, expr);
            case HEATMAP:
                return CreateHeartMap(id, title, expr);
            case SINGLESTAT:
                return CreateSingleStat(id, title, expr);
            case TABLE:
                return CreateTable(id, title, expr);
            case GAUGE:
                return CreateGauge(id, title, expr);
            default:
                throw new UnsupportedOperationException("The panel " + type.toString() + " is not supported.");
        }
    }

    static GrafanaPanel CreateGraph(int id, String title, String expr){
        return new GraphPanel(id, title,
                              GridPosFactory.CalculateGridPosById(id),
                              TargetFactory.CreateTargets(expr));
    }

    static GrafanaPanel CreateStat(int id, String title, String expr){
        return new StatPanel(id, title,
                             GridPosFactory.CalculateGridPosById(id),
                             TargetFactory.CreateTargets(expr));
    }

    static GrafanaPanel CreateHeartMap(int id, String title, String expr) {
        return new HeatMapPanel(id, title,
                                GridPosFactory.CalculateGridPosById(id),
                                TargetFactory.CreateTargets(expr));
    }

    static GrafanaPanel CreateSingleStat(int id, String title, String expr){
        return new SingleStatPanel(id, title,
                                   GridPosFactory.CalculateGridPosById(id),
                                   TargetFactory.CreateTargets(expr));
    }

    static GrafanaPanel CreateGauge(int id, String title, String expr){
        return new GaugePanel(id, title,
                              GridPosFactory.CalculateGridPosById(id),
                              TargetFactory.CreateTargets(expr));
    }

    static GrafanaPanel CreateTable(int id, String title, String expr){
        return new TablePanel(id, title,
                              GridPosFactory.CalculateGridPosById(id),
                              TargetFactory.CreateTargets(expr));
    }

}
