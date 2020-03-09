package org.kie.kogito.codegen.grafana.model.functions;

public class SumFunction implements GrafanaFunction {

    private final static String FUNCTION = "sum";

    public SumFunction() {
        // intentionally left blank
    }

    @Override
    public String getFunction() {
        return FUNCTION;
    }

    @Override
    public boolean hasTimeParameter() {
        return false;
    }

    @Override
    public String getTimeParameter() {
        return null;
    }
}
