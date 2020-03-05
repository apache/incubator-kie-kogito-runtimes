package org.kie.kogito.codegen.grafana.model.functions;

public interface GrafanaFunction {
    String getFunction();

    boolean hasTimeParameter();

    String getTimeParameter();
}
