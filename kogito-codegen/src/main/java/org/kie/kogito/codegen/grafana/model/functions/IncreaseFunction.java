package org.kie.kogito.codegen.grafana.model.functions;

public class IncreaseFunction implements GrafanaFunction {

    private static final String FUNCTION = "increase";

    private String timeParameter;

    public IncreaseFunction(String timeParameter) {
        if (timeParameter == null) {
            throw new IllegalArgumentException("timeParameter for Increase function can't be null");
        }
        this.timeParameter = timeParameter;
    }

    @Override
    public String getFunction() {
        return FUNCTION;
    }

    @Override
    public boolean hasTimeParameter() {
        return true;
    }

    @Override
    public String getTimeParameter() {
        return timeParameter;
    }
}
