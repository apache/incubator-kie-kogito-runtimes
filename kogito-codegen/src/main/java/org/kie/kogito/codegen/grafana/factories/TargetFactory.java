package org.kie.kogito.codegen.grafana.factories;

import java.util.ArrayList;
import java.util.List;

import org.kie.kogito.codegen.grafana.model.panel.GrafanaTarget;

public class TargetFactory {
    private TargetFactory(){}

    static List<GrafanaTarget> CreateTargets(String expr){
        List<GrafanaTarget> targets = new ArrayList<>();
        targets.add(new GrafanaTarget(expr, "time_series"));
        return targets;
    }
}
